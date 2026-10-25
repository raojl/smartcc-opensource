package com.smt.refactor.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户列表项（分页返回）
 */
@Data
public class UserVO {

    private Integer userId;
    private String userName;
    private String loginName;
    private String telephone;
    private String email;
    private String qualityAgCode;
    private String qualityAgDN;
    private String skillist;
    private String tech;
    private Integer roleType;
    /** 角色名列表 */
    private List<String> roles;
}
