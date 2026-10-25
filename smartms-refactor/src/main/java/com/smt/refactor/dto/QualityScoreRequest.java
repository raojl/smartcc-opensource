package com.smt.refactor.dto;

import lombok.Data;

import java.util.List;

/**
 * 质检评分生成请求（老系统 generateResult）
 */
@Data
public class QualityScoreRequest {

    private Long templateId;
    private String contactId;
    private String agentid;
    private String talkstarttime;
    /** 质检项名列表（与 scores/weights 对齐） */
    private List<String> columnNames;
    /** 各项得分（0-100） */
    private List<Integer> columnScores;
    /** 各项权重 */
    private List<Integer> columnWeights;
}
