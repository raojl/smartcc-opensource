package com.smt.refactor.dto;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginResponse {

    private String token;
    private UserInfoVO user;

    public LoginResponse(String token, UserInfoVO user) {
        this.token = token;
        this.user = user;
    }
}
