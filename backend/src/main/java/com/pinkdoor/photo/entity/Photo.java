package com.pinkdoor.photo.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 相册照片。文件本身存在阿里云 OSS，这里只存档案：
 * OSS 上的路径、访问地址、标题和时间。
 * 照片属于整个情侣空间，两个人都能看到。
 */
@Data
@TableName("pd_photo")
public class Photo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属情侣空间，租户外层。
     */
    private Long coupleId;

    /**
     * 上传人。
     */
    private Long userId;

    /**
     * OSS 上的对象键，例如 pink-door/1/2026-11/uuid.jpg。
     */
    private String objectKey;

    /**
     * 浏览器直接访问的公开地址，图片不经过 Java。
     */
    private String url;

    private String title;

    /**
     * 拍摄时间；现在先填上传时间，以后从照片 EXIF 里读。
     */
    private LocalDateTime takenAt;

    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;
}
