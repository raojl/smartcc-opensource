package com.smt.refactor.controller;

import com.smt.refactor.service.AgentPhoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 软电话代理 API（移植老系统 js/softphoneBar 的 AMI 协议）
 *
 * 前端坐席工作台通过本接口与 CTI 服务交互：
 *  - 登入/登出/示闲/示忙/拨号/挂断/保持/恢复/咨询/转接/会议/DTMF
 *  - GetState 长轮询接收实时事件（来电/状态变化/强拆）
 *
 * 老系统参考：webapp/js/softphoneBar/js/phoneBar/phone.js（2094 行）
 */
@RestController
@RequestMapping("/api/agent-phone")
@RequiredArgsConstructor
public class AgentPhoneController {

    private final AgentPhoneService agentPhoneService;

    /**
     * 通用软电话指令（老系统各 phone.js 方法等价）
     * body: { action, exten, workNo, sessionId, agentName, skills, destNum, ... }
     */
    @PostMapping("/action")
    public Map<String, Object> action(@RequestBody Map<String, Object> req) {
        String action = String.valueOf(req.getOrDefault("action", ""));
        if (action.isEmpty()) {
            throw new com.smt.refactor.common.BusinessException("缺少 action");
        }
        Map<String, Object> params = new LinkedHashMap<>();
        putIfPresent(params, req, "Exten", "exten");
        putIfPresent(params, req, "WorkNo", "workNo");
        putIfPresent(params, req, "SessionID", "sessionId");
        putIfPresent(params, req, "agentname", "agentName");
        putIfPresent(params, req, "AgentType", "agentType");
        putIfPresent(params, req, "SkillDesc", "skills");
        putIfPresent(params, req, "DestNum", "destNum");
        putIfPresent(params, req, "DestType", "destType");
        putIfPresent(params, req, "DeviceDN", "exten");
        putIfPresent(params, req, "phoneNum", "phoneNum");
        return agentPhoneService.action(action, params);
    }

    /**
     * 事件长轮询（老系统 _waitEvent GetState 递归轮询等价）
     * 阻塞至多有事件返回（CTI 侧长轮询 30s 上限）
     */
    @GetMapping("/events")
    public Map<String, Object> events(@RequestParam String exten,
                                      @RequestParam String workNo,
                                      @RequestParam(required = false) String sessionId) {
        return agentPhoneService.pollEvents(exten, workNo, sessionId);
    }

    private void putIfPresent(Map<String, Object> target, Map<String, Object> src,
                              String amiKey, String reqKey) {
        Object v = src.get(reqKey);
        if (v != null && !String.valueOf(v).isEmpty()) {
            target.put(amiKey, v);
        }
    }
}
