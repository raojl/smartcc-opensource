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
 * ÏµÍ³ÓÃ»§
 * </p>
 *
 * @author smartms-refactor
 * @since 2026-08-02
 */
@Getter
@Setter
@TableName("cc_core_sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    /**
     * ÓÃ»§Ãû
     */
    @TableField("user_name")
    private String userName;

    /**
     * µÇÂ¼Ãû
     */
    @TableField("login_name")
    private String loginName;

    /**
     * ÃÜÂë
     */
    @TableField("password")
    private String password;

    /**
     * µç»°ºÅÂë
     */
    @TableField("telephone")
    private String telephone;

    /**
     * µç×ÓÓÊÏä
     */
    @TableField("email")
    private String email;

    @TableField("qualityAgCode")
    private String qualityAgCode;

    @TableField("qualityAgDN")
    private String qualityAgDN;

    @TableField("skillist")
    private String skillist;

    @TableField("tech")
    private String tech;

    @TableField("role_type")
    private Byte roleType;
}
