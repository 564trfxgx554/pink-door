package com.pinkdoor.security;

/**
 * 通过 JWT 校验后得到的当前登录用户。
 */
public record LoginUser(Long id, String nickname, Long coupleId) {
}
