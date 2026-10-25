package com.smt.refactor.dto;

import lombok.Data;

/**
 * 客户查询参数（老系统 CustomInfoController index/myCustom/viewIsDeleteCustom）
 */
@Data
public class CustomQuery {

    private long page = 1;
    private long size = 15;
    private String customName;
    private String phoneNumber;
    private String companyName;
    private String qqNumber;
    private String peopleSex;
    /** 坐席工号（"my"=当前登录坐席，对应 my_custom） */
    private String agentId;
    /** 排序字段（白名单） */
    private String orderBy = "update_time";
    private String orderDir = "desc";
    /** true=回收站（is_delete=1），false=正常（is_delete=0） */
    private boolean deleted;
}
