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
@TableName("cc_core_linkinfo")
public class Linkinfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "rid", type = IdType.AUTO)
    private Integer rid;

    @TableField("link_servername")
    private String linkServername;

    @TableField("link_username")
    private String linkUsername;

    @TableField("link_password")
    private String linkPassword;

    @TableField("link_version")
    private String linkVersion;

    @TableField("link_apitype")
    private String linkApitype;

    @TableField("pbx_type")
    private String pbxType;
}
