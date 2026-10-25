package com.smt.refactor.controller;

import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.entity.OutboundTaskInfo;
import com.smt.refactor.service.OutboundService;
import com.smt.refactor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 外呼任务接口（翻译自老系统 outbound 包 2 个 Controller）
 */
@RestController
@RequestMapping("/api/outbound")
@RequiredArgsConstructor
public class OutboundController {

    private final OutboundService outboundService;
    private final UserService userService;

    private Integer myUserId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Integer)) {
            throw new BusinessException("未登录");
        }
        return (Integer) auth.getPrincipal();
    }

    private String myAgentCode(Authentication auth) {
        return userService.agentCodeOf(myUserId(auth));
    }

    /** 任务列表（按 task_id 聚合：完成率/接通率；myCustom=true=我的任务） */
    @GetMapping("/tasks")
    public PageResult<Map<String, Object>> tasks(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "15") long size,
                                                 @RequestParam(required = false) String taskId,
                                                 @RequestParam(required = false) String taskDesc,
                                                 @RequestParam(required = false) String agentId,
                                                 @RequestParam(required = false) String startTime,
                                                 @RequestParam(required = false) String endTime,
                                                 @RequestParam(defaultValue = "false") boolean myCustom,
                                                 Authentication auth) {
        return outboundService.taskPage(page, size, taskId, taskDesc, agentId, startTime, endTime,
                myUserId(auth), myCustom);
    }

    /** 任务详单（agentId="my"=我的详单） */
    @GetMapping("/details")
    public PageResult<OutboundTaskInfo> details(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "15") long size,
                                                @RequestParam(required = false) String taskId,
                                                @RequestParam(required = false) String phoneNumber,
                                                @RequestParam(required = false) String agentId,
                                                @RequestParam(required = false) String startTime,
                                                @RequestParam(required = false) String endTime,
                                                Authentication auth) {
        String myAgent = "my".equals(agentId) ? myAgentCode(auth) : null;
        return outboundService.detailPage(page, size, taskId, phoneNumber, agentId, startTime, endTime, myAgent);
    }

    /** 启动任务 */
    @PostMapping("/tasks/{taskId}/start")
    public void start(@PathVariable String taskId) {
        outboundService.start(taskId);
    }

    /** 停止任务 */
    @PostMapping("/tasks/{taskId}/stop")
    public void stop(@PathVariable String taskId) {
        outboundService.stop(taskId);
    }

    /** 删除任务 */
    @DeleteMapping("/tasks/{taskId}")
    public void delete(@PathVariable String taskId) {
        outboundService.deleteTask(taskId);
    }

    /** 号码导入（每行一个号码，可 "calling,phone" 格式） */
    @PostMapping("/tasks/import")
    public Map<String, Integer> importNumbers(@RequestBody Map<String, Object> body, Authentication auth) {
        @SuppressWarnings("unchecked")
        List<String> numbers = (List<String>) body.get("numbers");
        String taskDesc = (String) body.get("taskDesc");
        @SuppressWarnings("unchecked")
        List<String> agentIds = (List<String>) body.get("agentIds");
        int count = outboundService.importNumbers(numbers, taskDesc, agentIds,
                myUserId(auth), myAgentCode(auth));
        return java.util.Collections.singletonMap("imported", count);
    }
}
