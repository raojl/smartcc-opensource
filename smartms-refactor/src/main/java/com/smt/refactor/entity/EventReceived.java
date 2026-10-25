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
@TableName("im_event_received")
public class EventReceived implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tousername")
    private String tousername;

    @TableField("fromusername")
    private String fromusername;

    @TableField("createtime")
    private Long createtime;

    @TableField("msgtype")
    private String msgtype;

    @TableField("event")
    private String event;

    @TableField("eventkey")
    private String eventkey;

    @TableField("ticket")
    private String ticket;

    @TableField("latitude")
    private String latitude;

    @TableField("longitude")
    private String longitude;

    @TableField("precision")
    private String precision;

    @TableField("actionparam")
    private String actionparam;

    @TableField("agreementid")
    private String agreementid;

    @TableField("accountno")
    private String accountno;

    @TableField("msgid")
    private String msgid;

    @TableField("handled")
    private Integer handled;

    @TableField("agent_id")
    private Long agentId;

    @TableField("type")
    private Short type;

    @TableField("logon_id")
    private String logonId;

    @TableField("user_name")
    private String userName;
}
