package com.smt.refactor.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 工单保存请求（新增/编辑/办结/转办共用）
 */
@Data
public class WorkTaskSaveRequest {

    @NotBlank(message = "工单号不能为空")
    private String workId;
    private String customId;
    private String customName;
    private String workType;
    private String bussinessType;
    private String belongAgent;
    private String workAgent;
    private String workStatus;
    private String bussinessDesc;
    private String solution;
    private String mark;
    /** 转办目标坐席（zhuanban 用） */
    private String transferAgent;
}
