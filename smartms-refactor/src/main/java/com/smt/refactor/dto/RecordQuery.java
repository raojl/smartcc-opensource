package com.smt.refactor.dto;

import lombok.Data;

/**
 * 通话详单查询参数（老系统 RecordController index/myCall）
 */
@Data
public class RecordQuery {

    private long page = 1;
    private long size = 15;
    /** 坐席工号（"my"=当前登录坐席，对应 my_record） */
    private String agentId;
    /** 设备分机 */
    private String deviceid;
    /** 主叫号码 */
    private String oriAni;
    /** 被叫号码 */
    private String oriDnis;
    /** 开始日期 yyyy-MM-dd（默认昨天） */
    private String startTime;
    /** 结束日期 yyyy-MM-dd（默认今天+1） */
    private String endTime;
    /** 时长下限（秒） */
    private Integer lenStart;
    /** 时长上限（秒） */
    private Integer lenEnd;
}
