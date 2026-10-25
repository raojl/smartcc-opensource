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
@TableName("crm_customopenid")
public class Customopenid implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("custom_id")
    private String customId;

    @TableField("custom_openid")
    private String customOpenid;

    @TableField("custom_type")
    private Short customType;

    @TableField("agent_id")
    private String agentId;

    @TableField("tech_id")
    private String techId;

    @TableField("recv_time")
    private Long recvTime;

    @TableField("send_time")
    private Long sendTime;

    @TableField("user_name")
    private String userName;

    @TableField("appid")
    private String appid;

    @TableField("latitude")
    private String latitude;

    @TableField("longitude")
    private String longitude;

    @TableField("precision")
    private String precision;

    @TableField("headurl")
    private String headurl;
}
