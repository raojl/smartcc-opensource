package com.smt.refactor.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 用户新增/编辑请求
 */
@Data
public class UserSaveRequest {

    private Integer userId;

    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    private String userName;

    /** 登录名（新增必填） */
    @NotBlank(message = "登录名不能为空")
    private String loginName;

    /** 明文密码；为空时：新增=登录名 MD5，编辑=保持不变 */
    private String password;

    private String telephone;
    private String email;
    private String qualityAgCode;
    private String qualityAgDN;
    private String skillist;
    private String tech;
    private Integer roleType;
    /** 角色 ID 列表（覆盖式绑定，空则清空） */
    private List<Integer> roleIds;
}
