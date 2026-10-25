package com.smt.refactor.controller;

import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.WorkTaskQuery;
import com.smt.refactor.dto.WorkTaskSaveRequest;
import com.smt.refactor.dto.WorkTaskVO;
import com.smt.refactor.service.WorkTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 工单接口（翻译自老系统 /worktaskinfo）
 */
@RestController
@RequestMapping("/api/work-tasks")
@RequiredArgsConstructor
public class WorkTaskController {

    private final WorkTaskService workTaskService;

    /** 当前登录用户 ID（principal 为 userId） */
    private Integer myUserId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Integer)) {
            throw new BusinessException("未登录");
        }
        return (Integer) auth.getPrincipal();
    }

    /** 工单分页（工单信息/我的工单/我的受理 通过 belongAgent/workAgent="my" 区分） */
    @GetMapping
    public PageResult<WorkTaskVO> page(WorkTaskQuery query, Authentication auth) {
        return workTaskService.page(query, myUserId(auth));
    }

    /** 工单详情 */
    @GetMapping("/{workId}")
    public WorkTaskVO detail(@PathVariable String workId) {
        return workTaskService.detail(workId);
    }

    /** 新增（option=create） */
    @PostMapping
    public Map<String, String> create(@Valid @RequestBody WorkTaskSaveRequest req) {
        String option = workTaskService.save(req, "create");
        return java.util.Collections.singletonMap("option", option);
    }

    /** 编辑 */
    @PutMapping("/{workId}")
    public Map<String, String> update(@PathVariable String workId,
                                      @Valid @RequestBody WorkTaskSaveRequest req) {
        req.setWorkId(workId);
        String option = workTaskService.save(req, "update");
        return java.util.Collections.singletonMap("option", option);
    }

    /** 办结（状态置 3） */
    @PostMapping("/{workId}/finish")
    public void finish(@PathVariable String workId) {
        workTaskService.finish(workId);
    }

    /** 转办 */
    @PostMapping("/{workId}/transfer")
    public void transfer(@PathVariable String workId, @RequestBody Map<String, String> body) {
        workTaskService.transfer(workId, body.get("transferAgent"));
    }

    /** 坐席列表（转办选人） */
    @GetMapping("/agents")
    public List<com.smt.refactor.entity.SysUser> agents(@RequestParam(required = false) String searchKey) {
        return workTaskService.allAgents(searchKey);
    }
}
