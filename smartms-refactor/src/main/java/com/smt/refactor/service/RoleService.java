package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.RoleSaveRequest;
import com.smt.refactor.dto.RoleVO;
import com.smt.refactor.entity.SysRole;
import com.smt.refactor.entity.SysRolemenu;
import com.smt.refactor.entity.SysUserrole;
import com.smt.refactor.mapper.SysRoleMapper;
import com.smt.refactor.mapper.SysRolemenuMapper;
import com.smt.refactor.mapper.SysUserroleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理服务：CRUD + 菜单授权（rolemenu 覆盖式绑定）+ 删除保护
 * 翻译自老系统 UserRoleController
 */
@Service
@RequiredArgsConstructor
@DS("conf")
public class RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRolemenuMapper sysRolemenuMapper;
    private final SysUserroleMapper sysUserroleMapper;

    /**
     * 分页查询角色列表（role_name 模糊，与老系统 index 一致）
     */
    public PageResult<RoleVO> page(long pageNo, long pageSize, String roleName) {
        QueryWrapper<SysRole> qw = new QueryWrapper<>();
        if (StringUtils.hasText(roleName)) {
            qw.like("role_name", roleName.trim());
        }
        qw.orderByAsc("role_id");
        Page<SysRole> p = sysRoleMapper.selectPage(new Page<>(pageNo, pageSize), qw);
        List<RoleVO> list = p.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(list, p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    /**
     * 角色详情（含已授权菜单 ID 列表）
     */
    public RoleVO detail(Integer roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return toVO(role);
    }

    /**
     * 新增角色 + 菜单授权
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer create(RoleSaveRequest req) {
        SysRole role = new SysRole();
        role.setRoleName(req.getRoleName());
        role.setDescription(req.getDescription());
        sysRoleMapper.insert(role);
        bindMenus(role.getRoleId(), req.getMenuIds());
        return role.getRoleId();
    }

    /**
     * 更新角色 + 菜单覆盖式授权
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Integer roleId, RoleSaveRequest req) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        role.setRoleName(req.getRoleName());
        role.setDescription(req.getDescription());
        sysRoleMapper.updateById(role);
        bindMenus(roleId, req.getMenuIds());
    }

    /**
     * 删除角色：被用户引用时拒绝（老系统 checkdel 逻辑内聚到删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        Long refCount = sysUserroleMapper.selectCount(
                new QueryWrapper<SysUserrole>().eq("role_id", roleId));
        if (refCount > 0) {
            throw new BusinessException("该角色已被用户引用，无法删除");
        }
        sysRolemenuMapper.delete(new QueryWrapper<SysRolemenu>().eq("role_id", roleId));
        sysRoleMapper.deleteById(roleId);
    }

    /**
     * 角色是否可删除（0=被引用/不存在, 1=可删）
     */
    public boolean isDeletable(Integer roleId) {
        return sysUserroleMapper.selectCount(
                new QueryWrapper<SysUserrole>().eq("role_id", roleId)) == 0;
    }

    /**
     * 菜单覆盖式绑定（与老系统 Db.tx 先删后插一致）
     */
    private void bindMenus(Integer roleId, List<Integer> menuIds) {
        sysRolemenuMapper.delete(new QueryWrapper<SysRolemenu>().eq("role_id", roleId));
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Integer menuId : menuIds.stream().distinct().collect(Collectors.toList())) {
                if (menuId == null) {
                    continue;
                }
                SysRolemenu rm = new SysRolemenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                sysRolemenuMapper.insert(rm);
            }
        }
    }

    private RoleVO toVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setRoleId(role.getRoleId());
        vo.setRoleName(role.getRoleName());
        vo.setDescription(role.getDescription());
        List<Integer> menuIds = sysRolemenuMapper.selectList(
                        new QueryWrapper<SysRolemenu>().eq("role_id", role.getRoleId()))
                .stream().map(SysRolemenu::getMenuId).collect(Collectors.toList());
        vo.setMenuIds(menuIds != null ? menuIds : Collections.emptyList());
        return vo;
    }
}
