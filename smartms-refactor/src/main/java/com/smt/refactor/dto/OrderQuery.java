package com.smt.refactor.dto;

import lombok.Data;

/**
 * 订单查询参数（老系统 OrderInfoController index/myOrder）
 */
@Data
public class OrderQuery {

    private long page = 1;
    private long size = 15;
    private String no;
    private String name;
    private String product;
    /** 状态（0 或空=全部） */
    private String status;
    /** 开始时间 yyyyMMddHHmmss（默认7天前） */
    private String startTime;
    /** 结束时间 yyyyMMddHHmmss（默认今天+7天） */
    private String endTime;
    /** 坐席工号（"my"=我的订单） */
    private String agentId;
}
