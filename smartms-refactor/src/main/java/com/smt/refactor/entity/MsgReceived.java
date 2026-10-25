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
@TableName("im_msg_received")
public class MsgReceived implements Serializable {

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

    @TableField("content")
    private String content;

    @TableField("msgid")
    private String msgid;

    @TableField("picurl")
    private String picurl;

    @TableField("mediaid")
    private String mediaid;

    @TableField("format")
    private String format;

    @TableField("thumbmediaid")
    private String thumbmediaid;

    @TableField("location_x")
    private String locationX;

    @TableField("location_y")
    private String locationY;

    @TableField("scale")
    private String scale;

    @TableField("title")
    private String title;

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
