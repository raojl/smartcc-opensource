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
@TableName("cc_core_trunk")
public class Trunk implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "rid", type = IdType.AUTO)
    private Integer rid;

    @TableField("trunk_id")
    private String trunkId;

    @TableField("trunk_name")
    private String trunkName;

    @TableField("trunk_type")
    private String trunkType;

    @TableField("tac_code")
    private String tacCode;

    @TableField("circuit_code")
    private String circuitCode;

    @TableField("extn_dn")
    private String extnDn;

    /**
     * 中继方向
     */
    @TableField("trunk_direction")
    private String trunkDirection;
}
