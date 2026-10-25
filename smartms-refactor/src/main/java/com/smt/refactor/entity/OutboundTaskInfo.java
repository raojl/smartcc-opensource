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
@TableName("crm_outbound_task_info")
public class OutboundTaskInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("calling_number")
    private String callingNumber;

    @TableField("phone_number")
    private String phoneNumber;

    @TableField("task_id")
    private String taskId;

    @TableField("task_status")
    private String taskStatus;

    @TableField("outbound_status")
    private String outboundStatus;

    @TableField("timestamp")
    private String timestamp;

    @TableField("`desc`")
    private String desc;

    @TableField("user_id")
    private String userId;

    @TableField("agentid")
    private String agentid;
}
