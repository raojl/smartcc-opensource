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
@TableName("crm_worktaskinfo")
public class Worktaskinfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("work_id")
    private String workId;

    @TableField("custom_id")
    private String customId;

    @TableField("work_type")
    private String workType;

    @TableField("bussiness_type")
    private String bussinessType;

    @TableField("belong_agent")
    private String belongAgent;

    @TableField("work_agent")
    private String workAgent;

    @TableField("create_time")
    private String createTime;

    @TableField("work_time")
    private String workTime;

    @TableField("work_status")
    private String workStatus;

    @TableField("bussiness_desc")
    private String bussinessDesc;

    @TableField("solution")
    private String solution;

    @TableField("mark")
    private String mark;

    /**
     * 办结时间
     */
    @TableField("close_time")
    private String closeTime;
}
