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
@TableName("cc_core_systemparam")
public class Systemparam implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "rid", type = IdType.AUTO)
    private Integer rid;

    @TableField("servicename")
    private String servicename;

    @TableField("paramname")
    private String paramname;

    @TableField("paramvalue")
    private String paramvalue;

    @TableField("paramnote")
    private String paramnote;
}
