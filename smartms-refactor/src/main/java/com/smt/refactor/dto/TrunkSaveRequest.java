package com.smt.refactor.dto;

import lombok.Data;

/**
 * 局向分配（cc_core_trunk）新增/编辑请求
 */
@Data
public class TrunkSaveRequest {

    private Integer rid;
    private String trunkId;
    private String trunkName;
    private String trunkType;
    private String tacCode;
    private String circuitCode;
    private String extnDn;
    private String trunkDirection;
}
