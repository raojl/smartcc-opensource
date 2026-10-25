package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smt.refactor.dto.MenuVO;
import com.smt.refactor.entity.SysMenu;
import com.smt.refactor.entity.SysRolemenu;
import com.smt.refactor.entity.SysUserrole;
import com.smt.refactor.mapper.SysMenuMapper;
import com.smt.refactor.mapper.SysRolemenuMapper;
import com.smt.refactor.mapper.SysUserroleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务：按用户角色权限返回菜单树
 */
@Service
@RequiredArgsConstructor
@DS("conf")
public class MenuService {

    private final SysUserroleMapper sysUserroleMapper;
    private final SysRolemenuMapper sysRolemenuMapper;
    private final SysMenuMapper sysMenuMapper;

    /**
     * 全部菜单树（角色授权勾选用，不做权限过滤）
     */
    public List<MenuVO> allTree() {
        List<SysMenu> menus = sysMenuMapper.selectList(null);
        Map<Integer, MenuVO> nodeMap = new HashMap<>();
        List<MenuVO> roots = new ArrayList<>();
        for (SysMenu m : menus) {
            if (m.getMenuName() == null || m.getMenuName().trim().isEmpty()) {
                continue;
            }
            MenuVO vo = new MenuVO(m.getMenuId(), m.getMenuName(), m.getMenuUrl(),
                    m.getMenuOrder() != null ? m.getMenuOrder() : 0);
            nodeMap.put(m.getMenuId(), vo);
        }
        for (SysMenu m : menus) {
            if (m.getMenuName() == null || m.getMenuName().trim().isEmpty()) {
                continue;
            }
            MenuVO vo = nodeMap.get(m.getMenuId());
            if (vo == null) {
                continue;
            }
            Integer pid = m.getParentId();
            if (pid != null && pid != 0 && nodeMap.containsKey(pid)) {
                nodeMap.get(pid).getChildren().add(vo);
            } else {
                roots.add(vo);
            }
        }
        Comparator<MenuVO> byOrder = Comparator.comparing(MenuVO::getMenuOrder);
        roots.sort(byOrder);
        nodeMap.values().forEach(n -> n.getChildren().sort(byOrder));
        return roots;
    }

    /**
     * 获取用户有权限的菜单树（rolemenu 过滤）
     */
    public List<MenuVO> treeForUser(Integer userId) {
        // 用户角色
        List<Integer> roleIds = sysUserroleMapper.selectList(
                        new QueryWrapper<SysUserrole>().eq("user_id", userId))
                .stream().map(SysUserrole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 角色菜单
        Set<Integer> allowedMenuIds = sysRolemenuMapper.selectList(
                        new QueryWrapper<SysRolemenu>().in("role_id", roleIds))
                .stream().map(SysRolemenu::getMenuId).collect(Collectors.toSet());
        if (allowedMenuIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<SysMenu> menus = sysMenuMapper.selectList(null);
        // 过滤 + 组织树（parent_id 为空/0 为顶级；menu_level 兜底）
        Map<Integer, MenuVO> nodeMap = new HashMap<>();
        List<MenuVO> roots = new ArrayList<>();
        for (SysMenu m : menus) {
            // 过滤空名脏数据（如 menu_id=706 的空记录）
            if (m.getMenuName() == null || m.getMenuName().trim().isEmpty()) {
                continue;
            }
            if (!allowedMenuIds.contains(m.getMenuId())) {
                continue;
            }
            MenuVO vo = new MenuVO(m.getMenuId(), m.getMenuName(), m.getMenuUrl(),
                    m.getMenuOrder() != null ? m.getMenuOrder() : 0);
            nodeMap.put(m.getMenuId(), vo);
        }
        for (SysMenu m : menus) {
            if (m.getMenuName() == null || m.getMenuName().trim().isEmpty()) {
                continue;
            }
            MenuVO vo = nodeMap.get(m.getMenuId());
            if (vo == null) {
                continue;
            }
            Integer pid = m.getParentId();
            if (pid != null && pid != 0 && nodeMap.containsKey(pid)) {
                nodeMap.get(pid).getChildren().add(vo);
            } else {
                roots.add(vo);
            }
        }
        // 排序
        Comparator<MenuVO> byOrder = Comparator.comparing(MenuVO::getMenuOrder);
        roots.sort(byOrder);
        nodeMap.values().forEach(n -> n.getChildren().sort(byOrder));
        return roots;
    }
}
