package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.RouteSaveRequest;
import com.smt.refactor.dto.RouteVO;
import com.smt.refactor.entity.Route;
import com.smt.refactor.entity.RouteTrunk;
import com.smt.refactor.mapper.RouteMapper;
import com.smt.refactor.mapper.RouteTrunkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PBX 路由服务（pbx_route + pbx_route_trunk）
 * 翻译自老系统 TrunkRouteController
 */
@Service
@RequiredArgsConstructor
@DS("conf")
public class RouteService {

    private final RouteMapper routeMapper;
    private final RouteTrunkMapper routeTrunkMapper;

    public PageResult<RouteVO> page(long pageNo, long pageSize) {
        Page<Route> p = routeMapper.selectPage(new Page<>(pageNo, pageSize),
                new QueryWrapper<Route>().orderByAsc("route_id"));
        List<RouteVO> list = p.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(list, p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    public RouteVO detail(Integer routeId) {
        Route route = routeMapper.selectById(routeId);
        if (route == null) {
            throw new BusinessException("路由不存在");
        }
        return toVO(route);
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer create(RouteSaveRequest req) {
        Route route = new Route();
        route.setRouteDial(req.getRouteDial());
        route.setRouteName(req.getRouteName());
        route.setRouteDemo(req.getRouteDemo() == null ? "" : req.getRouteDemo());
        routeMapper.insert(route);
        bindTrunks(route.getRouteId(), req.getTrunkIds());
        return route.getRouteId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Integer routeId, RouteSaveRequest req) {
        Route route = routeMapper.selectById(routeId);
        if (route == null) {
            throw new BusinessException("路由不存在");
        }
        route.setRouteDial(req.getRouteDial());
        route.setRouteName(req.getRouteName());
        route.setRouteDemo(req.getRouteDemo() == null ? "" : req.getRouteDemo());
        routeMapper.updateById(route);
        bindTrunks(routeId, req.getTrunkIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer routeId) {
        if (routeMapper.selectById(routeId) == null) {
            throw new BusinessException("路由不存在");
        }
        routeTrunkMapper.delete(new QueryWrapper<RouteTrunk>().eq("route_id", routeId));
        routeMapper.deleteById(routeId);
    }

    /** 中继按顺序覆盖式绑定（orderno = 顺序号，与老系统一致） */
    private void bindTrunks(Integer routeId, List<Integer> trunkIds) {
        routeTrunkMapper.delete(new QueryWrapper<RouteTrunk>().eq("route_id", routeId));
        if (trunkIds != null && !trunkIds.isEmpty()) {
            int order = 1;
            for (Integer trunkId : trunkIds.stream().distinct().collect(Collectors.toList())) {
                if (trunkId == null) {
                    continue;
                }
                RouteTrunk rt = new RouteTrunk();
                rt.setRouteId(routeId);
                rt.setTrunkId(trunkId);
                rt.setOrderno(order++);
                routeTrunkMapper.insert(rt);
            }
        }
    }

    private RouteVO toVO(Route route) {
        RouteVO vo = new RouteVO();
        vo.setRouteId(route.getRouteId());
        vo.setRouteDial(route.getRouteDial());
        vo.setRouteName(route.getRouteName());
        vo.setRouteDemo(route.getRouteDemo());
        List<Integer> trunkIds = routeTrunkMapper.selectList(
                        new QueryWrapper<RouteTrunk>().eq("route_id", route.getRouteId()).orderByAsc("orderno"))
                .stream().map(RouteTrunk::getTrunkId).collect(Collectors.toList());
        vo.setTrunkIds(trunkIds);
        return vo;
    }
}
