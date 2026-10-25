package com.smt.refactor.controller;

import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.QualityScoreRequest;
import com.smt.refactor.dto.QualityTemplateRequest;
import com.smt.refactor.entity.QualityResult;
import com.smt.refactor.entity.QualityTemplate;
import com.smt.refactor.entity.Talkinfo;
import com.smt.refactor.service.QualityService;
import com.smt.refactor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 质检管理接口（翻译自老系统 qualityinspect 包 3 个 Controller）
 */
@RestController
@RequestMapping("/api/quality")
@RequiredArgsConstructor
public class QualityController {

    private final QualityService qualityService;
    private final UserService userService;

    private String myAgentCode(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Integer)) {
            throw new BusinessException("未登录");
        }
        return userService.agentCodeOf((Integer) auth.getPrincipal());
    }

    /* ==================== 质检模板 ==================== */

    @GetMapping("/templates")
    public PageResult<QualityTemplate> templatePage(@RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "15") long size,
                                                    @RequestParam(required = false) String name,
                                                    @RequestParam(required = false) String status,
                                                    @RequestParam(required = false) String creater) {
        return qualityService.templatePage(page, size, name, status, creater);
    }

    @GetMapping("/templates/active")
    public List<QualityTemplate> activeTemplates() {
        return qualityService.activeTemplates();
    }

    @GetMapping("/templates/{id}")
    public QualityTemplate templateDetail(@PathVariable Long id) {
        return qualityService.templateDetail(id);
    }

    @PostMapping("/templates")
    public Map<String, Long> templateSave(@Valid @RequestBody QualityTemplateRequest req) {
        Long id = qualityService.templateSave(req);
        return Collections.singletonMap("id", id);
    }

    @PutMapping("/templates/{id}/status")
    public void templateChangeStatus(@PathVariable Long id, @RequestParam String status) {
        qualityService.templateChangeStatus(id, status);
    }

    /* ==================== 待质检通话 ==================== */

    @GetMapping("/calls")
    public PageResult<Talkinfo> inspectCalls(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "15") long size,
                                             @RequestParam(required = false) String ani,
                                             @RequestParam(required = false) String dnis,
                                             @RequestParam(required = false) Integer callDirection,
                                             @RequestParam(required = false) String agentId,
                                             @RequestParam(required = false) String startTime,
                                             @RequestParam(required = false) String endTime,
                                             @RequestParam(required = false) Long minTime,
                                             @RequestParam(required = false) Long maxTime) {
        return qualityService.inspectCalls(page, size, ani, dnis, callDirection, agentId,
                startTime, endTime, minTime, maxTime);
    }

    /* ==================== 质检成绩 ==================== */

    /** 质检成绩（agentId="my"=我的成绩） */
    @GetMapping("/results")
    public PageResult<QualityResult> results(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "15") long size,
                                             @RequestParam(required = false) String agentId,
                                             @RequestParam(required = false) String inspector,
                                             @RequestParam(required = false) String grade,
                                             @RequestParam(required = false) Long templateId,
                                             @RequestParam(required = false) String startTime,
                                             @RequestParam(required = false) String endTime,
                                             Authentication auth) {
        String agent = agentId;
        if ("my".equals(agentId)) {
            agent = myAgentCode(auth);
        }
        return qualityService.resultPage(page, size, agent, inspector, grade, templateId, startTime, endTime);
    }

    @GetMapping("/results/{id}")
    public QualityResult resultDetail(@PathVariable Long id) {
        return qualityService.resultDetail(id);
    }

    /** 生成质检成绩 */
    @PostMapping("/results")
    public void generateResult(@Valid @RequestBody QualityScoreRequest req) {
        qualityService.generateResult(req);
    }

    /** 撤销质检 */
    @DeleteMapping("/results")
    public void reInspect(@RequestParam String contactId,
                          @RequestParam String agentId,
                          @RequestParam String talkStartTime) {
        qualityService.reInspect(contactId, agentId, talkStartTime);
    }
}
