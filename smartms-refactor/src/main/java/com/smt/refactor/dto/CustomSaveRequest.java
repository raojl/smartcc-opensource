package com.smt.refactor.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 客户新增/编辑请求（老系统 saveOrupdate）
 */
@Data
public class CustomSaveRequest {

    private String customId;
    @NotBlank(message = "客户姓名不能为空")
    private String customName;
    private String customSex;
    private String companyName;
    private String custType;
    private Integer customLevel;
    private String customMobile;
    private String customPhone;
    private String customPhone2;
    private String qqNumber;
    private String emailAddr;
    private String customCity;
    private String customAddr;
    private String customDesc;
    private String customDesc1;
    private String agentid;
}
