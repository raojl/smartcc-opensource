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
@TableName("base_talkinfo")
public class Talkinfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("ContactID")
    private String contactID;

    @TableField("BizANI")
    private String bizANI;

    @TableField("BizDNIS")
    private String bizDNIS;

    @TableField("ChannelType")
    private Short channelType;

    @TableField("CallDirection")
    private Short callDirection;

    @TableField("CallType")
    private Short callType;

    @TableField("StartTime")
    private String startTime;

    @TableField("EndTime")
    private String endTime;

    @TableField("IVRChannelDN")
    private String iVRChannelDN;

    @TableField("AgentLoginID")
    private String agentLoginID;

    @TableField("SkillID")
    private BigDecimal skillID;

    @TableField("SkillDesc")
    private String skillDesc;

    @TableField("AgentID")
    private String agentID;

    @TableField("AgentName")
    private String agentName;

    @TableField("DeviceDN")
    private String deviceDN;

    @TableField("DeviceType")
    private Short deviceType;

    @TableField("TalkStartTime")
    private String talkStartTime;

    @TableField("TimeLength")
    private BigDecimal timeLength;

    @TableField("FileName")
    private String fileName;

    @TableField("PlatformID")
    private String platformID;

    @TableField("TrunkDN")
    private String trunkDN;

    @TableField("C2T")
    private String c2t;

    @TableField("BizType")
    private Short bizType;

    @TableField("satisfaction")
    private String satisfaction;

    @TableField("Inspected")
    private Short inspected;

    @TableField("firstEnterSkillgroupAt")
    private String firstEnterSkillgroupAt;

    @TableField("OptionData")
    private String optionData;

    @TableField("CallID")
    private BigDecimal callID;

    @TableField("TalkEndTime")
    private String talkEndTime;

    /**
     * 是否已出账 0 未出账 1  已出账
     */
    @TableField("is_account")
    private Integer isAccount;
}
