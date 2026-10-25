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
@TableName("cc_core_numconvert")
public class Numconvert implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "rid", type = IdType.AUTO)
    private Integer rid;

    @TableField("numconvid")
    private String numconvid;

    @TableField("aniconvtype")
    private String aniconvtype;

    @TableField("aniconvbegin")
    private String aniconvbegin;

    @TableField("aniconvlen")
    private String aniconvlen;

    @TableField("aniconvdest")
    private String aniconvdest;

    @TableField("dnisconvtype")
    private String dnisconvtype;

    @TableField("dnisconvbegin")
    private String dnisconvbegin;

    @TableField("dnisconvlen")
    private String dnisconvlen;

    @TableField("dnisconvdest")
    private String dnisconvdest;
}
