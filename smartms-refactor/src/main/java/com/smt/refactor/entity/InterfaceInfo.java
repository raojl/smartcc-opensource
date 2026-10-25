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
@TableName("im_interface_info")
public class InterfaceInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("appid")
    private String appid;

    @TableField("appsecret")
    private String appsecret;

    @TableField("token")
    private String token;

    @TableField("encodingaeskey")
    private String encodingaeskey;

    @TableField("encryptmessage")
    private String encryptmessage;

    @TableField("alipay_rsa_pub")
    private String alipayRsaPub;

    @TableField("rsa_pub")
    private String rsaPub;

    @TableField("rsa_pri")
    private String rsaPri;

    @TableField("upload_url")
    private String uploadUrl;

    @TableField("download_url")
    private String downloadUrl;

    @TableField("send_url")
    private String sendUrl;

    @TableField("type")
    private Short type;

    @TableField("procedures")
    private String procedures;

    @TableField("knowlagereplys")
    private String knowlagereplys;

    @TableField("menu_json")
    private String menuJson;
}
