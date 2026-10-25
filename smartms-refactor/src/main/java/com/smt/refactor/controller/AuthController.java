package com.smt.refactor.controller;

import com.smt.refactor.common.BusinessException;
import com.smt.refactor.dto.LoginRequest;
import com.smt.refactor.dto.LoginResponse;
import com.smt.refactor.dto.UserInfoVO;
import com.smt.refactor.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 登录：返回 JWT + 用户信息 */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req.getUsername(), req.getPassword());
    }

    /** 当前用户信息 */
    @GetMapping("/me")
    public UserInfoVO me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Integer)) {
            throw new BusinessException("未登录");
        }
        return authService.me((Integer) authentication.getPrincipal());
    }
}
