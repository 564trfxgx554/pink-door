package com.pinkdoor.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 前端提交的登录请求参数。
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Please tell the door who you are first.")
    private String nickname;

    @NotBlank(message = "Please tell the door who you are first.")
    private String password;
}
