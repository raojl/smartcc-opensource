package com.smt.refactor.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点
 */
@Data
public class MenuVO {

    private Integer menuId;
    private String menuName;
    private String menuUrl;
    private Integer menuOrder;
    private List<MenuVO> children = new ArrayList<>();

    public MenuVO(Integer menuId, String menuName, String menuUrl, Integer menuOrder) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.menuUrl = menuUrl;
        this.menuOrder = menuOrder;
    }
}
