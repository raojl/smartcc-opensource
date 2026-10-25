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
@TableName("cc_core_numanalysis")
public class Numanalysis implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "rid", type = IdType.AUTO)
    private Integer rid;

    @TableField("dnistitle")
    private String dnistitle;

    @TableField("trunkgroupid")
    private String trunkgroupid;

    @TableField("dnisminlen")
    private String dnisminlen;

    @TableField("dnismaxlen")
    private String dnismaxlen;

    @TableField("numconvid")
    private String numconvid;
}
