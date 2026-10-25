package com.smt.refactor.dto;

import lombok.Data;

/**
 * 工单查询参数
 */
@Data
public class WorkTaskQuery {

    private long page = 1;
    private long size = 15;
    /** 客户名模糊 */
    private String customName;
    /** 工单号模糊 */
    private String workId;
    /** 工单状态（0 或空=全部） */
    private String workStatus;
    /** 归属坐席（"my"=当前登录坐席，对应老系统 my_belongtask） */
    private String belongAgent;
    /** 受理坐席（"my"=当前登录坐席，对应老系统 my_worktask） */
    private String workAgent;
    /** 开始日期 yyyy-MM-dd（默认昨天） */
    private String startTime;
    /** 结束日期 yyyy-MM-dd（默认今天+2天） */
    private String endTime;
}
