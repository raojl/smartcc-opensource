package com.smt.refactor.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户信息（登录后 /me）
 */
@Data
public class UserInfoVO {

    private Integer userId;
    private String userName;
    private String loginName;
    private String telephone;
    private String email;
    /** 0=管理员 1=坐席 */
    private Integer roleType;
    /** 坐席工号（roleType=1 时用于软电话/弹屏） */
    private String qualityAgCode;
    /** 坐席分机号 DN（roleType=1 时用于话机/WebRTC 语音注册） */
    private String qualityAgDN;
    private List<String> roles;
}
