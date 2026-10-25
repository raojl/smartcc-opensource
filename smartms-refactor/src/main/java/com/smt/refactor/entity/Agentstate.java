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
@TableName("base_agentstate")
public class Agentstate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("AgentLoginID")
    private String agentLoginID;

    @TableField("ContactID")
    private String contactID;

    @TableField("EventID")
    private BigDecimal eventID;

    @TableField("SkillID")
    private BigDecimal skillID;

    @TableField("AgentStatus")
    private Short agentStatus;

    @TableField("StartTime")
    private String startTime;

    @TableField("LastStatus")
    private Short lastStatus;

    @TableField("LastStatusTimeLen")
    private BigDecimal lastStatusTimeLen;

    @TableField("Reason")
    private BigDecimal reason;

    @TableField("LastReason")
    private BigDecimal lastReason;
}
