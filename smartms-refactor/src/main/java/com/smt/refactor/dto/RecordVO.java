package com.smt.refactor.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 通话记录列表项
 */
@Data
public class RecordVO {

    private String contactID;
    private String agentId;
    private String agentName;
    private String deviceDN;
    private String callDirection;
    private String fileName;
    private String bizANI;
    private String bizDNIS;
    private String talkStartTime;
    /** 通话时长（秒） */
    private Long timeLength;
    private String skillDesc;
    private String startTime;
    private String endTime;
    /** 字典翻译 */
    private Map<String, String> labels = new HashMap<>();
}
