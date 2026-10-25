package com.smt.refactor.entity;

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
@TableName("im_service_record")
public class ServiceRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("ServiceRecordID")
    private String serviceRecordID;

    @TableField("CustOpenID")
    private String custOpenID;

    @TableField("CustType")
    private Short custType;

    @TableField("AppID")
    private String appID;

    @TableField("StartTime")
    private Long startTime;

    @TableField("EndTime")
    private Long endTime;

    @TableField("LastMsgTime")
    private Long lastMsgTime;

    @TableField("SkillID")
    private String skillID;

    @TableField("AgentID")
    private String agentID;

    @TableField("LastAgentID")
    private String lastAgentID;

    @TableField("PreciseReply")
    private Short preciseReply;

    @TableField("VagueReply")
    private Short vagueReply;

    @TableField("AgentReply")
    private Short agentReply;

    @TableField("MechineReply")
    private Short mechineReply;

    @TableField("AutoReply")
    private Short autoReply;

    @TableField("ServiceDirection")
    private Short serviceDirection;
}
