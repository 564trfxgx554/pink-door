package com.pinkdoor.security;

/**
 * 当前请求的登录用户上下文，请求结束后必须清理，避免线程复用导致数据串号。
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser loginUser) {
        CURRENT_USER.set(loginUser);
    }

    public static LoginUser get() {
        return CURRENT_USER.get();
    }

    public static Long getUserId() {
        LoginUser loginUser = CURRENT_USER.get();
        return loginUser == null ? null : loginUser.id();
    }

    public static String getNickname() {
        LoginUser loginUser = CURRENT_USER.get();
        return loginUser == null ? null : loginUser.nickname();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
