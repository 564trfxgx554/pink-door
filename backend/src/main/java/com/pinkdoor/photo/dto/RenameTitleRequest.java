package com.pinkdoor.photo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 前端提交的照片改名请求。
 */
@Data
public class RenameTitleRequest {

    @NotBlank(message = "照片名字不能为空。")
    @Size(max = 100, message = "照片名字不能超过 100 个字符。")
    private String title;
}
