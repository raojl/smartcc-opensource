package com.smt.refactor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 系统菜单
 * </p>
 *
 * @author smartms-refactor
 * @since 2026-08-02
 */
@Getter
@Setter
@TableName("cc_core_sys_menu")
public class SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("menu_id")
    private Integer menuId;

    /**
     * 菜单名称
     */
    @TableField("menu_name")
    private String menuName;

    /**
     * 菜单URL
     */
    @TableField("menu_url")
    private String menuUrl;

    /**
     * 菜单级别
     */
    @TableField("menu_level")
    private Integer menuLevel;

    /**
     * 顺序
     */
    @TableField("menu_order")
    private Integer menuOrder;

    /**
     * 上级菜单
     */
    @TableField("parent_id")
    private Integer parentId;
}
