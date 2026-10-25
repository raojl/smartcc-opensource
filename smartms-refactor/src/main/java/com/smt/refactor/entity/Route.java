package com.smt.refactor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * PBX中继路由表
 * </p>
 *
 * @author smartms-refactor
 * @since 2026-08-02
 */
@Getter
@Setter
@TableName("pbx_route")
public class Route implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 路由Id
     */
    @TableId(value = "route_id", type = IdType.AUTO)
    private Integer routeId;

    /**
     * 路由字冠
     */
    @TableField("route_dial")
    private String routeDial;

    /**
     * 路由名字
     */
    @TableField("route_name")
    private String routeName;

    /**
     * 路由备注
     */
    @TableField("route_demo")
    private String routeDemo;
}
