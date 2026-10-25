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
 * PBX 出局中继（pbx_trunk）
 * </p>
 *
 * @author smartms-refactor
 * @since 2026-08-02
 */
@Getter
@Setter
@TableName("pbx_trunk")
public class PbxTrunk implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("type")
    private String type;

    @TableField("cid")
    private String cid;

    @TableField("prefix")
    private String prefix;

    @TableField("keepcid")
    private String keepcid;

    @TableField("maxchans")
    private String maxchans;

    @TableField("fail")
    private String fail;

    @TableField("disable")
    private String disable;

    @TableField("sip_account")
    private String sipAccount;

    @TableField("sip_host")
    private String sipHost;

    @TableField("sip_port")
    private String sipPort;

    @TableField("sip_username")
    private String sipUsername;

    @TableField("sip_secret")
    private String sipSecret;

    @TableField("sip_type")
    private String sipType;

    @TableField("sip_context")
    private String sipContext;

    @TableField("sip_nat")
    private String sipNat;

    @TableField("callerchange")
    private String callerchange;
}
