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
 * 
 * </p>
 *
 * @author smartms-refactor
 * @since 2026-08-02
 */
@Getter
@Setter
@TableName("cc_core_ivrroute")
public class Ivrroute implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "trunksn", type = IdType.AUTO)
    private Integer trunksn;

    @TableField("trunk_servicename")
    private String trunkServicename;

    @TableField("flow_servicename")
    private String flowServicename;

    @TableField("trunkgroupid")
    private String trunkgroupid;

    @TableField("calldirection")
    private String calldirection;

    @TableField("channelCount")
    private Integer channelCount;

    @TableField("proxyname")
    private String proxyname;

    @TableField("routenum")
    private String routenum;

    @TableField("flowname")
    private String flowname;

    @TableField("displayname")
    private String displayname;
}
