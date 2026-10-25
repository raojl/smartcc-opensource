package com.smt.refactor.entity;

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
@TableName("crm_custominfo")
public class Custominfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("custom_id")
    private String customId;

    @TableField("custom_name")
    private String customName;

    @TableField("custom_sex")
    private String customSex;

    @TableField("company_name")
    private String companyName;

    @TableField("qq_number")
    private String qqNumber;

    @TableField("email_addr")
    private String emailAddr;

    @TableField("update_time")
    private String updateTime;

    @TableField("custom_city")
    private String customCity;

    @TableField("custom_addr")
    private String customAddr;

    @TableField("custom_desc")
    private String customDesc;

    @TableField("custom_desc1")
    private String customDesc1;

    @TableField("custom_level")
    private Short customLevel;

    @TableField("custom_mobile")
    private String customMobile;

    @TableField("custom_phone")
    private String customPhone;

    @TableField("custom_phone2")
    private String customPhone2;

    @TableField("is_delete")
    private Short isDelete;

    @TableField("cust_type")
    private String custType;

    @TableField("agentid")
    private String agentid;
}
