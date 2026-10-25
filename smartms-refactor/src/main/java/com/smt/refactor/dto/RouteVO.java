package com.smt.refactor.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * PBX 路由（含绑定中继）
 */
@Data
public class RouteVO {

    private Integer routeId;
    private String routeDial;
    private String routeName;
    private String routeDemo;
    /** 绑定的中继 ID（按 orderno 排序） */
    private List<Integer> trunkIds = new ArrayList<>();
}
