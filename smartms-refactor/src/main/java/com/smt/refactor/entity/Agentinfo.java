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
@TableName("base_agentinfo")
public class Agentinfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("AgentLoginID")
    private String agentLoginID;

    @TableField("AgentID")
    private String agentID;

    @TableField("AgentName")
    private String agentName;

    @TableField("AgentDN")
    private String agentDN;

    @TableField("AgentIP")
    private String agentIP;

    @TableField("SkillDesc")
    private String skillDesc;

    @TableField("LoginTime")
    private String loginTime;

    @TableField("AgentType")
    private Short agentType;

    @TableField("PlatformID")
    private String platformID;
}
