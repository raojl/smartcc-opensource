package com.smt.refactor.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * WebRTC 分机创建请求
 */
@Data
public class WebRtcExtensionRequest {

    /** 分机号（2-8 位数字） */
    @NotBlank(message = "分机号不能为空")
    @Pattern(regexp = "\\d{2,8}", message = "分机号须为 2-8 位数字")
    private String exten;

    /** 分机密码（至少 6 位，用于 WSS Digest 认证） */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 显示名称（可选，如坐席姓名） */
    private String displayName;
}
