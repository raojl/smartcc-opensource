package com.smt.refactor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("crm_orderinfo")
public class Orderinfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("status")
    private String status;

    @TableField("no")
    private String no;

    @TableField("custom_id")
    private String customId;

    @TableField("name")
    private String name;

    @TableField("mobile")
    private String mobile;

    @TableField("contact")
    private String contact;

    @TableField("phone")
    private String phone;

    @TableField("type")
    private String type;

    @TableField("media")
    private String media;

    @TableField("send_type")
    private String sendType;

    @TableField("pay_type")
    private String payType;

    @TableField("bank")
    private String bank;

    @TableField("account")
    private String account;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("product")
    private String product;

    @TableField("quantity")
    private BigDecimal quantity;

    @TableField("agentid")
    private String agentid;

    @TableField("agent")
    private String agent;

    @TableField("createtime")
    private String createtime;

    @TableField("`desc`")
    private String desc;
}
