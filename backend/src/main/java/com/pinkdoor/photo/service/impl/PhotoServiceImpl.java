package com.pinkdoor.photo.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pinkdoor.common.ResultCode;
import com.pinkdoor.common.exception.BusinessException;
import com.pinkdoor.config.properties.OssProperties;
import com.pinkdoor.photo.entity.Photo;
import com.pinkdoor.photo.mapper.PhotoMapper;
import com.pinkdoor.photo.service.PhotoService;
import com.pinkdoor.photo.vo.PhotoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 相册照片实现：Java 负责校验、转存和归属，文件存在 OSS，浏览器直接读 OSS。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoServiceImpl
        extends ServiceImpl<PhotoMapper, Photo>
        implements PhotoService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final int TITLE_MAX_LENGTH = 30;
    // 改名标题上限：与 pd_photo.title VARCHAR(100) 和前端 100 字限制对齐。
    // 上传标题上限 TITLE_MAX_LENGTH = 30 是另一回事，这里不要动它。
    private static final int RENAME_TITLE_MAX_LENGTH = 100;
    private static final int PAGE_MAX_SIZE = 50;
    private static final String DEFAULT_TITLE = "我们的照片";
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * 只收这三种图片类型。以后想收更多格式，在这里加一行。
     */
    private static final Map<String, String> CONTENT_TYPE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private static final Set<String> ALLOWED_CONTENT_TYPES = CONTENT_TYPE_EXTENSIONS.keySet();

    private final OSS ossClient;
    private final OssProperties ossProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PhotoVO uploadPhoto(Long userId, Long coupleId, MultipartFile file, String title) {
        validateFile(file);
        String contentType = file.getContentType();
        String extension = CONTENT_TYPE_EXTENSIONS.get(contentType);
        String objectKey = buildObjectKey(coupleId, extension);

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(file.getSize());
            ossClient.putObject(ossProperties.getBucket(), objectKey,
                    file.getInputStream(), metadata);
        } catch (OSSException | IOException ex) {
            log.error("上传照片到 OSS 失败", ex);
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "照片上传失败，请稍后再试。");
        }

        Photo photo = new Photo();
        photo.setCoupleId(coupleId);
        photo.setUserId(userId);
        photo.setObjectKey(objectKey);
        photo.setUrl(buildPublicUrl(objectKey));
        photo.setTitle(buildTitle(title, file.getOriginalFilename()));
        photo.setTakenAt(LocalDateTime.now());
        if (!save(photo)) {
            // OSS 上已经有了文件，数据库写失败时把文件删掉，不留孤儿文件。
            deleteObjectQuietly(objectKey);
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "照片保存失败，请稍后再试。");
        }
        return toVO(photo);
    }

    @Override
    public Page<PhotoVO> listPhotos(Long userId, Long coupleId, long page, long size) {
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), PAGE_MAX_SIZE);
        Page<Photo> photoPage = new Page<>(safePage, safeSize);
        // 租户插件会自动追加 couple_id，这里显式写出，读代码时能直接看到边界。
        lambdaQuery()
                .eq(Photo::getCoupleId, coupleId)
                .orderByDesc(Photo::getCreatedAt)
                .page(photoPage);
        Page<PhotoVO> result = new Page<>(photoPage.getCurrent(), photoPage.getSize(), photoPage.getTotal());
        result.setRecords(photoPage.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePhoto(Long userId, Long coupleId, Long photoId) {
        Photo photo = requireOwnedPhoto(coupleId, photoId);
        try {
            ossClient.deleteObject(ossProperties.getBucket(), photo.getObjectKey());
        } catch (OSSException ex) {
            log.error("删除 OSS 照片失败", ex);
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "删除照片失败，请稍后再试。");
        }
        if (!removeById(photoId)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "删除照片失败，请稍后再试。");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PhotoVO renameTitle(Long userId, Long coupleId, Long photoId, String title) {
        String newTitle = normalizeTitle(title);
        Photo photo = requireOwnedPhoto(coupleId, photoId);
        // 只 set id 和 title：默认更新策略是不更新 null 字段，所以只会改 title。
        Photo update = new Photo();
        update.setId(photoId);
        update.setTitle(newTitle);
        if (!updateById(update)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "照片改名失败，请稍后再试。");
        }
        photo.setTitle(newTitle);
        return toVO(photo);
    }

    /**
     * 改名标题校验：不能为空，最长 100 字。
     */
    private String normalizeTitle(String title) {
        String trimmed = title == null ? "" : title.trim();
        if (!StringUtils.hasText(trimmed)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "照片名字不能为空。");
        }
        if (trimmed.length() > RENAME_TITLE_MAX_LENGTH) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "照片名字不能超过 100 个字符。");
        }
        return trimmed;
    }

    /**
     * 校验照片归属：只确认在同一个情侣空间。
     * 跨空间和不存在都返回 404，不泄露其他空间是否存在该照片。
     * 相册是两个人共有的，删除不校验是谁上传的。
     */
    private Photo requireOwnedPhoto(Long coupleId, Long photoId) {
        Photo photo = getById(photoId);
        if (photo == null || !coupleId.equals(photo.getCoupleId())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "照片不存在。");
        }
        return photo;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "请先选一张照片。");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "照片不能超过 5MB。");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "只支持 jpg、png、webp 格式的照片。");
        }
    }

    /**
     * 对象键：pink-door/{coupleId}/2026-11/{uuid}.jpg。
     * 按空间和月份分目录，文件名用 UUID，不会重名。
     */
    private String buildObjectKey(Long coupleId, String extension) {
        String month = LocalDate.now().format(MONTH_FORMAT);
        return "pink-door/" + coupleId + "/" + month + "/"
                + UUID.randomUUID() + "." + extension;
    }

    /**
     * OSS 公开地址：https://java-scx.oss-cn-beijing.aliyuncs.com/{key}。
     * endpoint 配的是 https://oss-cn-beijing.aliyuncs.com，这里只取 host 部分拼地址。
     */
    private String buildPublicUrl(String objectKey) {
        String host = ossProperties.getEndpoint().replaceFirst("^https?://", "");
        return "https://" + ossProperties.getBucket() + "." + host + "/" + objectKey;
    }

    private String buildTitle(String title, String originalFilename) {
        if (StringUtils.hasText(title)) {
            String trimmed = title.trim();
            return trimmed.length() <= TITLE_MAX_LENGTH
                    ? trimmed
                    : trimmed.substring(0, TITLE_MAX_LENGTH) + "…";
        }
        if (StringUtils.hasText(originalFilename)) {
            String name = originalFilename;
            int dot = name.lastIndexOf('.');
            if (dot > 0) {
                name = name.substring(0, dot);
            }
            if (StringUtils.hasText(name)) {
                return name.length() <= TITLE_MAX_LENGTH ? name : name.substring(0, TITLE_MAX_LENGTH) + "…";
            }
        }
        return DEFAULT_TITLE;
    }

    private void deleteObjectQuietly(String objectKey) {
        try {
            ossClient.deleteObject(ossProperties.getBucket(), objectKey);
        } catch (Exception ex) {
            log.warn("清理 OSS 孤儿文件失败: {}", objectKey, ex);
        }
    }

    private PhotoVO toVO(Photo photo) {
        return PhotoVO.builder()
                .id(photo.getId())
                .url(photo.getUrl())
                .title(photo.getTitle())
                .takenAt(photo.getTakenAt())
                .createdAt(photo.getCreatedAt())
                .build();
    }
}
