package com.pinkdoor.photo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 照片展示对象。url 是 OSS 公开地址，前端直接用它当 img 的 src。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoVO {

    private Long id;

    private String url;

    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime takenAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
