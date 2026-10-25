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
@TableName("cc_core_serviceinfo")
public class Serviceinfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "rid", type = IdType.AUTO)
    private Integer rid;

    @TableField("servicename")
    private String servicename;

    @TableField("servicetype")
    private String servicetype;

    @TableField("serviceip")
    private String serviceip;

    @TableField("serviceport")
    private String serviceport;

    @TableField("mcsdevice")
    private String mcsdevice;

    @TableField("iscluster")
    private String iscluster;

    @TableField("loadcfgtype")
    private String loadcfgtype;
}
