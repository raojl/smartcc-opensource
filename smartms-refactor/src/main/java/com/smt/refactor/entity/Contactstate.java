package com.smt.refactor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
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
@TableName("base_contactstate")
public class Contactstate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("ContactID")
    private String contactID;

    @TableField("EventID")
    private BigDecimal eventID;

    @TableField("ContactStatus")
    private Short contactStatus;

    @TableField("StartTime")
    private String startTime;

    @TableField("LastStatus")
    private Short lastStatus;

    @TableField("LastStatusTimeLen")
    private BigDecimal lastStatusTimeLen;

    @TableField("SkillID")
    private BigDecimal skillID;

    @TableField("DeviceDN")
    private String deviceDN;

    @TableField("DeviceType")
    private Short deviceType;

    @TableField("Reason")
    private BigDecimal reason;

    @TableField("RouteID")
    private BigDecimal routeID;

    @TableField("RouteMaxTime")
    private BigDecimal routeMaxTime;

    @TableField("WorkUnitID")
    private BigDecimal workUnitID;
}
