package com.smt.refactor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * PBX路由的中继顺序表
 * </p>
 *
 * @author smartms-refactor
 * @since 2026-08-02
 */
@Getter
@Setter
@TableName("pbx_route_trunk")
public class RouteTrunk implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 路由Id
     */
    @TableId("route_id")
    private Integer routeId;

    /**
     * 中继Id
     */
    @TableField("trunk_id")
    private Integer trunkId;

    @TableField("orderno")
    private Integer orderno;
}
