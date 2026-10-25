package com.smt.refactor.controller;

import com.smt.refactor.common.PageResult;
import com.smt.refactor.entity.*;
import com.smt.refactor.service.CtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * CTI 管理接口（翻译自老系统 ctimanager 包 5 个 Controller + CtiSystemParamsController）
 */
@RestController
@RequestMapping("/api/cti")
@RequiredArgsConstructor
public class CtController {

    private final CtService ctService;

    /* ==================== 链路配置（cc_core_linkinfo）==================== */

    @GetMapping("/links")
    public PageResult<Linkinfo> linkPage(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "15") long size) {
        return ctService.linkPage(page, size);
    }

    @GetMapping("/links/{id}")
    public Linkinfo linkDetail(@PathVariable Integer id) {
        return ctService.linkDetail(id);
    }

    @PostMapping("/links")
    public void linkSave(@RequestBody Linkinfo entity) {
        ctService.linkSave(entity);
    }

    @DeleteMapping("/links/{id}")
    public void linkDelete(@PathVariable Integer id) {
        ctService.linkDelete(id);
    }

    /* ==================== 号码配置（cc_core_extension）==================== */

    @GetMapping("/extensions")
    public PageResult<Extension> extenPage(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "15") long size) {
        return ctService.extenPage(page, size);
    }

    @GetMapping("/extensions/{id}")
    public Extension extenDetail(@PathVariable Integer id) {
        return ctService.extenDetail(id);
    }

    @PostMapping("/extensions")
    public void extenSave(@RequestBody Extension entity) {
        ctService.extenSave(entity);
    }

    @DeleteMapping("/extensions/{id}")
    public void extenDelete(@PathVariable Integer id) {
        ctService.extenDelete(id);
    }

    /* ==================== CTI 参数（cc_core_profile）==================== */

    @GetMapping("/profiles")
    public PageResult<Profile> profilePage(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "15") long size) {
        return ctService.profilePage(page, size);
    }

    @GetMapping("/profiles/{id}")
    public Profile profileDetail(@PathVariable Integer id) {
        return ctService.profileDetail(id);
    }

    @PostMapping("/profiles")
    public void profileSave(@RequestBody Profile entity) {
        ctService.profileSave(entity);
    }

    @DeleteMapping("/profiles/{id}")
    public void profileDelete(@PathVariable Integer id) {
        ctService.profileDelete(id);
    }

    /* ==================== 录音配置（cc_core_recordmember）==================== */

    @GetMapping("/records")
    public PageResult<Recordmember> recordPage(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "15") long size) {
        return ctService.recordPage(page, size);
    }

    @GetMapping("/records/{id}")
    public Recordmember recordDetail(@PathVariable Integer id) {
        return ctService.recordDetail(id);
    }

    @PostMapping("/records")
    public void recordSave(@RequestBody Recordmember entity) {
        ctService.recordSave(entity);
    }

    @DeleteMapping("/records/{id}")
    public void recordDelete(@PathVariable Integer id) {
        ctService.recordDelete(id);
    }

    /* ==================== 技能组配置（cc_core_skill）==================== */

    @GetMapping("/skills")
    public PageResult<Skill> skillPage(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "15") long size,
                                       @RequestParam(required = false) String keyword) {
        return ctService.skillPage(page, size, keyword);
    }

    @GetMapping("/skills/{id}")
    public Skill skillDetail(@PathVariable Integer id) {
        return ctService.skillDetail(id);
    }

    @PostMapping("/skills")
    public void skillSave(@RequestBody Skill entity) {
        ctService.skillSave(entity);
    }

    @DeleteMapping("/skills/{id}")
    public void skillDelete(@PathVariable Integer id) {
        ctService.skillDelete(id);
    }
}
