package com.smt.refactor.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 工单列表项（含客户名/手机号/耗时）
 */
@Data
public class WorkTaskVO {

    private String workId;
    private String customId;
    private String customName;
    private String customMobile;
    private String workType;
    private String bussinessType;
    private String belongAgent;
    private String workAgent;
    private String createTime;
    private String workTime;
    private String workStatus;
    private String bussinessDesc;
    private String solution;
    private String mark;
    private String closeTime;
    /** 处理耗时展示（sec_to_time 效果） */
    private String tl;
    /** 字典翻译：状态/类型/业务类型 */
    private Map<String, String> labels = new HashMap<>();
}
