package com.smt.refactor.controller;

import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.*;
import com.smt.refactor.entity.Device;
import com.smt.refactor.entity.PbxTrunk;
import com.smt.refactor.entity.Route;
import com.smt.refactor.entity.Trunk;
import com.smt.refactor.service.DeviceService;
import com.smt.refactor.service.PbxTrunkService;
import com.smt.refactor.service.RouteService;
import com.smt.refactor.service.TrunkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PBX 管理接口（翻译自老系统 pbxmanager 包 4 个 Controller）
 */
@RestController
@RequestMapping("/api/pbx")
@RequiredArgsConstructor
public class PbxController {

    private final DeviceService deviceService;
    private final PbxTrunkService pbxTrunkService;
    private final RouteService routeService;
    private final TrunkService trunkService;

    /* ==================== SIP 分机（pbx_device）==================== */

    @GetMapping("/devices")
    public PageResult<Device> devicePage(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "15") long size,
                                         @RequestParam(required = false) String id,
                                         @RequestParam(required = false) String cid,
                                         @RequestParam(defaultValue = "id") String orderBy,
                                         @RequestParam(defaultValue = "asc") String orderDir) {
        return deviceService.page(page, size, id, cid, orderBy, orderDir);
    }

    @GetMapping("/devices/{id}")
    public Device deviceDetail(@PathVariable String id) {
        return deviceService.detail(id);
    }

    @PostMapping("/devices")
    public void deviceCreate(@Valid @RequestBody DeviceSaveRequest req) {
        deviceService.create(req);
    }

    @PutMapping("/devices/{id}")
    public void deviceUpdate(@PathVariable String id, @Valid @RequestBody DeviceSaveRequest req) {
        deviceService.update(id, req);
    }

    @DeleteMapping("/devices/{id}")
    public void deviceDelete(@PathVariable String id) {
        deviceService.delete(id);
    }

    @DeleteMapping("/devices")
    public void deviceBatchDelete(@RequestParam List<String> ids) {
        deviceService.batchDelete(ids);
    }

    @GetMapping("/devices/check-id")
    public Map<String, Boolean> deviceCheckId(@RequestParam String id) {
        Map<String, Boolean> r = new HashMap<>();
        r.put("available", deviceService.isIdAvailable(id));
        return r;
    }

    /* ==================== PBX 出局中继（pbx_trunk）==================== */

    @GetMapping("/trunks")
    public PageResult<PbxTrunk> trunkPage(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "15") long size) {
        return pbxTrunkService.page(page, size);
    }

    @GetMapping("/trunks/all")
    public List<PbxTrunk> trunkAll() {
        return pbxTrunkService.all();
    }

    @GetMapping("/trunks/{id}")
    public PbxTrunk trunkDetail(@PathVariable Integer id) {
        return pbxTrunkService.detail(id);
    }

    @PostMapping("/trunks")
    public Map<String, Object> trunkCreate(@RequestBody PbxTrunkSaveRequest req) {
        Integer id = pbxTrunkService.create(req);
        Map<String, Object> r = new HashMap<>();
        r.put("id", id);
        return r;
    }

    @PutMapping("/trunks/{id}")
    public void trunkUpdate(@PathVariable Integer id, @RequestBody PbxTrunkSaveRequest req) {
        pbxTrunkService.update(id, req);
    }

    @DeleteMapping("/trunks/{id}")
    public void trunkDelete(@PathVariable Integer id) {
        pbxTrunkService.delete(id);
    }

    /* ==================== PBX 路由（pbx_route）==================== */

    @GetMapping("/routes")
    public PageResult<RouteVO> routePage(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "15") long size) {
        return routeService.page(page, size);
    }

    @GetMapping("/routes/{id}")
    public RouteVO routeDetail(@PathVariable Integer id) {
        return routeService.detail(id);
    }

    @PostMapping("/routes")
    public Map<String, Object> routeCreate(@RequestBody RouteSaveRequest req) {
        Integer id = routeService.create(req);
        Map<String, Object> r = new HashMap<>();
        r.put("routeId", id);
        return r;
    }

    @PutMapping("/routes/{id}")
    public void routeUpdate(@PathVariable Integer id, @RequestBody RouteSaveRequest req) {
        routeService.update(id, req);
    }

    @DeleteMapping("/routes/{id}")
    public void routeDelete(@PathVariable Integer id) {
        routeService.delete(id);
    }

    /* ==================== 局向分配（cc_core_trunk）==================== */

    @GetMapping("/trunk-distributes")
    public PageResult<Trunk> trunkDistributePage(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "15") long size) {
        return trunkService.page(page, size);
    }

    @GetMapping("/trunk-distributes/{id}")
    public Trunk trunkDistributeDetail(@PathVariable Integer id) {
        return trunkService.detail(id);
    }

    @PostMapping("/trunk-distributes")
    public Map<String, Object> trunkDistributeCreate(@RequestBody TrunkSaveRequest req) {
        Integer rid = trunkService.create(req);
        Map<String, Object> r = new HashMap<>();
        r.put("rid", rid);
        return r;
    }

    @PutMapping("/trunk-distributes/{id}")
    public void trunkDistributeUpdate(@PathVariable Integer id, @RequestBody TrunkSaveRequest req) {
        trunkService.update(id, req);
    }

    @DeleteMapping("/trunk-distributes/{id}")
    public void trunkDistributeDelete(@PathVariable Integer id) {
        trunkService.delete(id);
    }
}
