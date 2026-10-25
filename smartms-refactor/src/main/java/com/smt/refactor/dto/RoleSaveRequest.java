package com.smt.refactor.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 角色新增/编辑请求
 */
@Data
public class RoleSaveRequest {

    private Integer roleId;

    @NotBlank(message = "角色名不能为空")
    private String roleName;

    private String description;

    /** 菜单 ID 列表（覆盖式授权，空则清空） */
    private List<Integer> menuIds;
}
