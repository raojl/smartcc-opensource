package com.smt.refactor.dto;

import lombok.Data;

/**
 * PBX 出局中继（pbx_trunk）新增/编辑请求
 */
@Data
public class PbxTrunkSaveRequest {

    private Integer id;
    private String name;
    private String type;
    private String cid;
    private String prefix;
    private String keepcid;
    private String maxchans;
    private String fail;
    private String disable;
    private String sipAccount;
    private String sipHost;
    private String sipPort;
    private String sipUsername;
    private String sipSecret;
    private String sipType;
    private String sipContext;
    private String sipNat;
    private String callerchange;
}
