package com.pinkdoor.photo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pinkdoor.photo.entity.Photo;
import com.pinkdoor.photo.vo.PhotoVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 相册照片服务。照片属于整个情侣空间，两个人都能看到；
 * 删除只校验空间归属，不校验是谁上传的。
 */
public interface PhotoService extends IService<Photo> {

    /**
     * 上传一张照片：先传到 OSS，再把档案写进数据库。
     *
     * @param userId   上传人
     * @param coupleId 所属情侣空间
     * @param file     图片文件
     * @param title    标题，可空，空时用文件名
     * @return 新照片
     */
    PhotoVO uploadPhoto(Long userId, Long coupleId, MultipartFile file, String title);

    /**
     * 按时间倒序分页列出本空间的照片。
     */
    Page<PhotoVO> listPhotos(Long userId, Long coupleId, long page, long size);

    /**
     * 删除一张照片：先删 OSS 上的文件，再删数据库记录。
     */
    void deletePhoto(Long userId, Long coupleId, Long photoId);

    /**
     * 给一张照片改名。标题不能为空，最长 100 字；只校验空间归属。
     *
     * @param userId   操作人
     * @param coupleId 所属情侣空间
     * @param photoId  照片 id
     * @param title    新标题
     * @return 更新后的照片
     */
    PhotoVO renameTitle(Long userId, Long coupleId, Long photoId, String title);
}
