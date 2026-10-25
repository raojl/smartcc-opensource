package com.smt.refactor.controller;

import com.smt.refactor.common.BusinessException;
import com.smt.refactor.dto.MenuVO;
import com.smt.refactor.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单接口
 */
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /** 当前用户的权限菜单树 */
    @GetMapping("/tree")
    public List<MenuVO> tree(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Integer)) {
            throw new BusinessException("未登录");
        }
        return menuService.treeForUser((Integer) authentication.getPrincipal());
    }
}
