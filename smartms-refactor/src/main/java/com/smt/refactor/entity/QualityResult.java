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
@TableName("crm_quality_result")
public class QualityResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("template_id")
    private Long templateId;

    @TableField("contact_id")
    private String contactId;

    @TableField("agentid")
    private String agentid;

    @TableField("talkstarttime")
    private String talkstarttime;

    @TableField("inspector")
    private String inspector;

    @TableField("create_time")
    private String createTime;

    @TableField("result_grade")
    private String resultGrade;

    @TableField("result_score")
    private String resultScore;

    @TableField("quality_columns_score")
    private String qualityColumnsScore;
}
