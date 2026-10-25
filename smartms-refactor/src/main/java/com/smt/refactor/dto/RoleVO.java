package com.smt.refactor.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色列表项 / 详情
 */
@Data
public class RoleVO {

    private Integer roleId;
    private String roleName;
    private String description;
    /** 角色绑定的菜单 ID 列表（编辑回显用） */
    private List<Integer> menuIds = new ArrayList<>();
}
