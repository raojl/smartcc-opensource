package com.smt.refactor.dto;

import lombok.Data;

/**
 * SIP 分机（pbx_device）新增/编辑请求
 */
@Data
public class DeviceSaveRequest {

    /** 分机号（主键） */
    private String id;
    private String cid;
    private String secret;
    private String type;
    private String port;
    private String dtmfmode;
    private String disallow;
    private String allow;
    private String nat;
    private String canreinvite;
    private String context;
    private String bindIpaddr;
    private String callerid;
    private String username;
    private String host;
}
