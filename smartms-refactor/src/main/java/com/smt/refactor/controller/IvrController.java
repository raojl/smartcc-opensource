package com.smt.refactor.controller;

import com.smt.refactor.common.PageResult;
import com.smt.refactor.entity.*;
import com.smt.refactor.service.IvrService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IVR 管理接口（翻译自老系统 ivrmanager 包 + IvrSystemParamsController）
 */
@RestController
@RequestMapping("/api/ivr")
@RequiredArgsConstructor
public class IvrController {

    private final IvrService ivrService;

    /* ==================== 语音服务（cc_core_serviceinfo）==================== */

    @GetMapping("/services")
    public PageResult<Serviceinfo> servicePage(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "15") long size) {
        return ivrService.servicePage(page, size);
    }

    @GetMapping("/services/{id}")
    public Serviceinfo serviceDetail(@PathVariable Integer id) {
        return ivrService.serviceDetail(id);
    }

    @PostMapping("/services")
    public void serviceSave(@RequestBody Serviceinfo entity) {
        ivrService.serviceSave(entity);
    }

    @DeleteMapping("/services/{id}")
    public void serviceDelete(@PathVariable Integer id) {
        ivrService.serviceDelete(id);
    }

    /* ==================== 中继群（cc_core_trunkgroup）==================== */

    @GetMapping("/trunk-groups")
    public PageResult<Trunkgroup> trunkgroupPage(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "15") long size) {
        return ivrService.trunkgroupPage(page, size);
    }

    @GetMapping("/trunk-groups/all")
    public List<Trunkgroup> trunkgroupAll() {
        return ivrService.trunkgroupAll();
    }

    @GetMapping("/trunk-groups/{id}")
    public Trunkgroup trunkgroupDetail(@PathVariable Integer id) {
        return ivrService.trunkgroupDetail(id);
    }

    @PostMapping("/trunk-groups")
    public void trunkgroupSave(@RequestBody Trunkgroup entity) {
        ivrService.trunkgroupSave(entity);
    }

    @DeleteMapping("/trunk-groups/{id}")
    public void trunkgroupDelete(@PathVariable Integer id) {
        ivrService.trunkgroupDelete(id);
    }

    /* ==================== 语音文件管理（cc_core_fileinfo）==================== */

    @GetMapping("/files")
    public PageResult<Fileinfo> filePage(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "15") long size,
                                         @RequestParam(required = false) String fileType,
                                         @RequestParam(required = false) String fileName,
                                         @RequestParam(required = false) String fileDesc,
                                         @RequestParam(defaultValue = "file_name") String orderBy,
                                         @RequestParam(defaultValue = "asc") String orderDir) {
        return ivrService.filePage(page, size, fileType, fileName, fileDesc, orderBy, orderDir);
    }

    @GetMapping("/files/{id}")
    public Fileinfo fileDetail(@PathVariable Integer id) {
        return ivrService.fileDetail(id);
    }

    @PostMapping("/files")
    public void fileSave(@RequestBody Fileinfo entity) {
        ivrService.fileSave(entity);
    }

    @DeleteMapping("/files/{id}")
    public void fileDelete(@PathVariable Integer id) {
        ivrService.fileDelete(id);
    }

    @DeleteMapping("/files")
    public void fileBatchDelete(@RequestParam List<Integer> ids) {
        ivrService.fileBatchDelete(ids);
    }

    /* ==================== IVR 参数（cc_core_systemparam）==================== */

    @GetMapping("/params")
    public PageResult<Systemparam> paramPage(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "15") long size) {
        return ivrService.paramPage(page, size);
    }

    @GetMapping("/params/{id}")
    public Systemparam paramDetail(@PathVariable Integer id) {
        return ivrService.paramDetail(id);
    }

    @PostMapping("/params")
    public void paramSave(@RequestBody Systemparam entity) {
        ivrService.paramSave(entity);
    }

    @DeleteMapping("/params/{id}")
    public void paramDelete(@PathVariable Integer id) {
        ivrService.paramDelete(id);
    }

    /* ==================== IVR 呼入路由（cc_core_ivrroute）==================== */

    @GetMapping("/routes")
    public PageResult<Ivrroute> routePage(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "15") long size) {
        return ivrService.routePage(page, size);
    }

    @GetMapping("/routes/{id}")
    public Ivrroute routeDetail(@PathVariable Integer id) {
        return ivrService.routeDetail(id);
    }

    @PostMapping("/routes")
    public void routeSave(@RequestBody Ivrroute entity) {
        ivrService.routeSave(entity);
    }

    @DeleteMapping("/routes/{id}")
    public void routeDelete(@PathVariable Integer id) {
        ivrService.routeDelete(id);
    }
}
