package com.smt.refactor.controller;

import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.MenuVO;
import com.smt.refactor.dto.RoleSaveRequest;
import com.smt.refactor.dto.RoleVO;
import com.smt.refactor.service.MenuService;
import com.smt.refactor.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色管理接口（翻译自老系统 /userrole）
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final MenuService menuService;

    /** 分页列表（role_name 模糊） */
    @GetMapping
    public PageResult<RoleVO> page(@RequestParam(defaultValue = "1") long page,
                                   @RequestParam(defaultValue = "15") long size,
                                   @RequestParam(required = false) String roleName) {
        return roleService.page(page, size, roleName);
    }

    /** 全部角色（下拉框用） */
    @GetMapping("/all")
    public List<RoleVO> all() {
        return roleService.page(1, 1000, null).getList();
    }

    /** 详情（含已授权菜单） */
    @GetMapping("/{id}")
    public RoleVO detail(@PathVariable Integer id) {
        return roleService.detail(id);
    }

    /** 新增 */
    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody RoleSaveRequest req) {
        Integer id = roleService.create(req);
        Map<String, Object> r = new HashMap<>();
        r.put("roleId", id);
        return r;
    }

    /** 编辑 */
    @PutMapping("/{id}")
    public void update(@PathVariable Integer id, @Valid @RequestBody RoleSaveRequest req) {
        roleService.update(id, req);
    }

    /** 删除（被用户引用时拒绝） */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        roleService.delete(id);
    }

    /** 角色是否可删除（老系统 checkdel） */
    @GetMapping("/{id}/deletable")
    public Map<String, Boolean> deletable(@PathVariable Integer id) {
        Map<String, Boolean> r = new HashMap<>();
        r.put("deletable", roleService.isDeletable(id));
        return r;
    }

    /** 全部菜单树（角色授权勾选用） */
    @GetMapping("/menu-tree")
    public List<MenuVO> menuTree() {
        return menuService.allTree();
    }
}
