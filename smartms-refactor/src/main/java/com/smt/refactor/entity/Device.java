package com.smt.refactor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * PBX分机表
 * </p>
 *
 * @author smartms-refactor
 * @since 2026-08-02
 */
@Getter
@Setter
@TableName("pbx_device")
public class Device implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分机号码
     */
    @TableId("id")
    private String id;

    /**
     * CID/DID号码
     */
    @TableField("cid")
    private String cid;

    /**
     * 分机密码
     */
    @TableField("secret")
    private String secret;

    @TableField("type")
    private String type;

    /**
     * 分机端口
     */
    @TableField("port")
    private String port;

    /**
     * DTMF模式
     */
    @TableField("dtmfmode")
    private String dtmfmode;

    /**
     * 禁用编解码
     */
    @TableField("disallow")
    private String disallow;

    /**
     * 允许编解码
     */
    @TableField("allow")
    private String allow;

    /**
     * NAT穿透
     */
    @TableField("nat")
    private String nat;

    /**
     * 媒体流透传
     */
    @TableField("canreinvite")
    private String canreinvite;

    /**
     * 拨号方案
     */
    @TableField("context")
    private String context;

    @TableField("bind_ipaddr")
    private String bindIpaddr;

    @TableField("callerid")
    private String callerid;

    @TableField("username")
    private String username;

    @TableField("host")
    private String host;
}
