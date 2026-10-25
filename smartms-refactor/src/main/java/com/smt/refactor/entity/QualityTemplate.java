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
@TableName("crm_quality_template")
public class QualityTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("`desc`")
    private String desc;

    @TableField("excellent")
    private String excellent;

    @TableField("fine")
    private String fine;

    @TableField("pass")
    private String pass;

    @TableField("quality_columns")
    private String qualityColumns;

    @TableField("status")
    private String status;

    @TableField("creater")
    private String creater;

    @TableField("create_time")
    private String createTime;
}
