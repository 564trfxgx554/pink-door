package com.pinkdoor.auth.controller;

import com.pinkdoor.auth.dto.LoginRequest;
import com.pinkdoor.auth.dto.LoginResponse;
import com.pinkdoor.auth.service.AuthService;
import com.pinkdoor.auth.vo.UserVO;
import com.pinkdoor.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开的登录接口，以及登录用户查询自身信息的接口。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 公开接口。返回扁平结构，严格匹配前端契约。
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * 需要携带有效 JWT。
     */
    @GetMapping("/me")
    public Result<UserVO> currentUser() {
        return Result.success(authService.getCurrentUser());
    }
}
