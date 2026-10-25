package com.smt.refactor.dto;

import lombok.Data;

/**
 * 质检模板新增/编辑请求
 */
@Data
public class QualityTemplateRequest {

    private Long id;
    private String name;
    private String desc;
    /** 优秀分（满分上限） */
    private String excellent;
    /** 良好分 */
    private String fine;
    /** 及格分 */
    private String pass;
    /** 质检项，格式 "项名=权重;项名=权重;" */
    private String qualityColumns;
    private String status;
    private String creater;
}
