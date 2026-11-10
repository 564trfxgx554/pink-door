package com.pinkdoor.auth.service.impl;

import com.pinkdoor.auth.dto.LoginRequest;
import com.pinkdoor.auth.dto.LoginResponse;
import com.pinkdoor.auth.service.AuthService;
import com.pinkdoor.auth.vo.UserVO;
import com.pinkdoor.common.ResultCode;
import com.pinkdoor.common.exception.BusinessException;
import com.pinkdoor.security.JwtTokenProvider;
import com.pinkdoor.security.UserContext;
import com.pinkdoor.user.entity.User;
import com.pinkdoor.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 登录时使用 BCrypt 校验预置账号并签发 JWT。
 * 没有注册入口：这个小站只属于我们两个人。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(LoginRequest request) {
        String nickname = request.getNickname().trim();
        User user = userService.getByNickname(nickname);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for nickname '{}'", nickname);
            throw new BusinessException(ResultCode.INVALID_CREDENTIALS);
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getNickname());
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .nickname(user.getNickname())
                .expiresIn(jwtTokenProvider.getExpireSeconds())
                .build();
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "Your account no longer exists.");
        }
        return UserVO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
