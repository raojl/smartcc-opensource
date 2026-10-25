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
@TableName("cc_core_skill")
public class Skill implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "rid", type = IdType.AUTO)
    private Integer rid;

    @TableField("skill_id")
    private String skillId;

    @TableField("skill_name")
    private String skillName;

    @TableField("skill_group_type")
    private String skillGroupType;

    @TableField("domain_id")
    private String domainId;

    @TableField("skill_weight")
    private String skillWeight;

    @TableField("channel_type")
    private String channelType;

    @TableField("queue_dn")
    private String queueDn;

    @TableField("agent_route_tactic")
    private String agentRouteTactic;

    @TableField("call_route_tactic")
    private String callRouteTactic;

    @TableField("queue_over_timer")
    private String queueOverTimer;

    @TableField("queue_max_count")
    private String queueMaxCount;

    @TableField("overflow_type1")
    private String overflowType1;

    @TableField("overflow_desc1")
    private String overflowDesc1;

    @TableField("overflow_type2")
    private String overflowType2;

    @TableField("overflow_desc2")
    private String overflowDesc2;

    @TableField("overflow_type3")
    private String overflowType3;

    @TableField("overflow_desc3")
    private String overflowDesc3;

    @TableField("pop3_user")
    private String pop3User;

    @TableField("ftp_user")
    private String ftpUser;

    @TableField("is_valid")
    private String isValid;

    @TableField("skill_desc")
    private String skillDesc;
}
