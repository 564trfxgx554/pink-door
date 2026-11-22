package com.pinkdoor.photo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pinkdoor.common.Result;
import com.pinkdoor.photo.dto.RenameTitleRequest;
import com.pinkdoor.photo.service.PhotoService;
import com.pinkdoor.photo.vo.PhotoVO;
import com.pinkdoor.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 相册接口。文件存在 OSS，数据库只存档案；浏览器直接读 OSS 的图片地址。
 */
@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    /**
     * 上传一张照片。title 可空，空时用文件名。
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<PhotoVO> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "title", required = false) String title) {
        return Result.success(photoService.uploadPhoto(
                UserContext.getUserId(), UserContext.getCoupleId(), file, title));
    }

    /**
     * 按时间倒序分页列出本空间的照片。
     */
    @GetMapping
    public Result<Page<PhotoVO>> list(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "20") long size) {
        return Result.success(photoService.listPhotos(
                UserContext.getUserId(), UserContext.getCoupleId(), page, size));
    }

    /**
     * 给一张照片改名。标题不能为空，最长 100 字。
     */
    @PatchMapping("/{photoId}")
    public Result<PhotoVO> renameTitle(@PathVariable Long photoId,
                                       @Valid @RequestBody RenameTitleRequest request) {
        return Result.success(photoService.renameTitle(
                UserContext.getUserId(), UserContext.getCoupleId(), photoId, request.getTitle()));
    }

    /**
     * 删除一张照片及其 OSS 上的文件，不可恢复。
     */
    @DeleteMapping("/{photoId}")
    public Result<Void> delete(@PathVariable Long photoId) {
        photoService.deletePhoto(UserContext.getUserId(), UserContext.getCoupleId(), photoId);
        return Result.success();
    }
}
