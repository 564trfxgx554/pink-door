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

    /**
     * 所属情侣空间。前端暂时不用，方便调试和以后切换空间。
     */
    private Long coupleId;

    private long expiresIn;
}
