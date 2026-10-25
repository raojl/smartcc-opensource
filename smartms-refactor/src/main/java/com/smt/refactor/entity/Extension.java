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
@TableName("cc_core_extension")
public class Extension implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "rid", type = IdType.AUTO)
    private Integer rid;

    @TableField("extn_dn")
    private String extnDn;

    @TableField("extn_type")
    private String extnType;
}
