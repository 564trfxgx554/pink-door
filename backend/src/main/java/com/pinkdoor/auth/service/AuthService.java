package com.pinkdoor.auth.service;

import com.pinkdoor.auth.dto.LoginRequest;
import com.pinkdoor.auth.dto.LoginResponse;
import com.pinkdoor.auth.vo.UserVO;

/**
 * 登录与当前用户查询。
 */
public interface AuthService {

    /**
     * 校验预置账号并签发 JWT。
     *
     * @param request 登录请求参数
     * @return token 与昵称
     */
    LoginResponse login(LoginRequest request);

    /**
     * @return 当前请求上下文中的登录用户
     */
    UserVO getCurrentUser();
}
