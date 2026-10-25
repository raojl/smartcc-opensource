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
@TableName("im_msg_sent")
public class MsgSent implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("type")
    private Short type;

    @TableField("agenttype")
    private Short agenttype;

    @TableField("createtime")
    private Long createtime;

    @TableField("fromusername")
    private String fromusername;

    @TableField("agent_id")
    private Long agentId;

    @TableField("tousername")
    private String tousername;

    @TableField("msgtype")
    private String msgtype;

    @TableField("content")
    private String content;

    @TableField("media_id")
    private String mediaId;

    @TableField("thumb_media_id")
    private String thumbMediaId;

    @TableField("title")
    private String title;

    @TableField("url")
    private String url;

    @TableField("picurl")
    private String picurl;

    @TableField("handled")
    private Byte handled;
}
