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
@TableName("base_ivrdevicestate")
public class Ivrdevicestate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("ContactID")
    private String contactID;

    @TableField("EventID")
    private BigDecimal eventID;

    @TableField("DeviceDN")
    private String deviceDN;

    @TableField("DeviceType")
    private Short deviceType;

    @TableField("StartTime")
    private String startTime;

    @TableField("CurrStatus")
    private Short currStatus;

    @TableField("LastStatus")
    private Short lastStatus;

    @TableField("LastStatusTimeLen")
    private BigDecimal lastStatusTimeLen;

    @TableField("RouteID")
    private BigDecimal routeID;

    @TableField("Reason")
    private BigDecimal reason;

    @TableField("CallID")
    private BigDecimal callID;

    @TableField("OldCallID")
    private BigDecimal oldCallID;

    @TableField("SecOldCallID")
    private BigDecimal secOldCallID;

    @TableField("OtherParty")
    private String otherParty;

    @TableField("ThirdParty")
    private String thirdParty;

    @TableField("CallingParty")
    private String callingParty;

    @TableField("CalledParty")
    private String calledParty;
}
