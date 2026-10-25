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
@TableName("base_recordfile")
public class Recordfile implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("ContactID")
    private String contactID;

    @TableField("AgentLoginID")
    private String agentLoginID;

    @TableField("DeviceDN")
    private String deviceDN;

    @TableField("DeviceType")
    private Short deviceType;

    @TableField("CallDirection")
    private Short callDirection;

    @TableField("CallType")
    private Short callType;

    @TableField("OriANI")
    private String oriANI;

    @TableField("OriDNIS")
    private String oriDNIS;

    @TableField("StartTime")
    private String startTime;

    @TableField("TimeLength")
    private BigDecimal timeLength;

    @TableField("FileName")
    private String fileName;

    @TableField("CallID")
    private BigDecimal callID;

    @TableField("OldCallID")
    private BigDecimal oldCallID;

    @TableField("SecOldCallID")
    private BigDecimal secOldCallID;

    @TableField("CallingParty")
    private String callingParty;

    @TableField("CalledParty")
    private String calledParty;

    @TableField("OtherParty")
    private String otherParty;

    @TableField("ThirdParty")
    private String thirdParty;

    @TableField("UserField")
    private String userField;
}
