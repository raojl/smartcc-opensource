package com.smt.refactor.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * PBX 路由（pbx_route + pbx_route_trunk）新增/编辑请求
 */
@Data
public class RouteSaveRequest {

    private Integer routeId;
    private String routeDial;
    private String routeName;
    private String routeDemo;
    /** 中继 ID 列表（按顺序绑定 orderno） */
    private List<Integer> trunkIds = new ArrayList<>();
}
