package com.pinkdoor.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前端要求的扁平登录响应：{@code { token, nickname }}。
 * Vue 应用会忽略多余字段。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private String tokenType;

    private String nickname;

    private long expiresIn;
}
