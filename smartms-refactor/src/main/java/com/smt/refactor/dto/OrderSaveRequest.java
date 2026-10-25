package com.smt.refactor.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单新增/编辑请求（老系统 saveOrupdate）
 */
@Data
public class OrderSaveRequest {

    private Long id;
    private String status;
    private String no;
    private String customId;
    private String name;
    private String mobile;
    private String contact;
    private String phone;
    private String type;
    private String media;
    private String sendType;
    private String payType;
    private String bank;
    private String account;
    private BigDecimal amount;
    private String product;
    private BigDecimal quantity;
    private String agentid;
    private String agent;
    private String desc;
}
