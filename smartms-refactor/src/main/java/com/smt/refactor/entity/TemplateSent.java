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
@TableName("im_template_sent")
public class TemplateSent implements Serializable {

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

    @TableField("template_id")
    private String templateId;

    @TableField("first")
    private String first;

    @TableField("url")
    private String url;

    @TableField("picurl")
    private String picurl;

    @TableField("keynote1")
    private String keynote1;

    @TableField("keynote2")
    private String keynote2;

    @TableField("keynote3")
    private String keynote3;

    @TableField("keynote4")
    private String keynote4;

    @TableField("keynote5")
    private String keynote5;

    @TableField("remark")
    private String remark;

    @TableField("handled")
    private Byte handled;
}
