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
 * 常用代码表
 * </p>
 *
 * @author smartms-refactor
 * @since 2026-08-02
 */
@Getter
@Setter
@TableName("cc_core_generalcode")
public class Generalcode implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "rid", type = IdType.AUTO)
    private Integer rid;

    @TableField("gesection")
    private String gesection;

    @TableField("gekey")
    private String gekey;

    @TableField("gevalue")
    private String gevalue;

    @TableField("genote")
    private String genote;
}
