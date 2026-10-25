package com.smt.refactor.dto;

import lombok.Data;

/**
 * 未接记录查询参数（老系统 MissedCallController）
 */
@Data
public class MissCallQuery {

    private long page = 1;
    private long size = 15;
    /** 类型：1=坐席未接(agentmiss) 2=队列未接(queuemiss) 3=客户未接(custmiss) 4=留言(custmLeaveMessage) */
    private Integer type = 1;
    /** 客户手机号 */
    private String customMobile;
    /** 分机 */
    private String agentDn;
    /** 坐席工号（"my"=当前登录坐席） */
    private String agentId;
    /** 开始日期 yyyy-MM-dd（默认7天前） */
    private String startTime;
    /** 结束日期 yyyy-MM-dd（默认今天+8天） */
    private String endTime;
}
