package com.smt.refactor.controller;

import com.smt.refactor.service.MonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实时监控接口（翻译自老系统 businessmonitor 包 5 个 Controller）
 */
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    /** 实时数据查询代理（老系统 rtdataquery，前端定时轮询） */
    @GetMapping("/rtdata-query")
    public String rtdataQuery(@RequestParam String queryId) {
        return monitorService.rtdataQuery(queryId);
    }

    /** 全部坐席 ID（老系统 getAgents） */
    @GetMapping("/agents")
    public String agents() {
        return monitorService.getAgents();
    }

    /** 空闲坐席（老系统 getAllIdleAgent） */
    @GetMapping("/agents/idle")
    public String idleAgents() {
        return monitorService.getAllIdleAgent();
    }

    /** 坐席实时统计（老系统 getAgentResults） */
    @GetMapping("/agents/results")
    public String agentResults(@RequestParam(required = false) String agentId) {
        return monitorService.getAgentResults(agentId);
    }

    /** 坐席 DN（老系统 getAllAgentDn） */
    @GetMapping("/agents/dns")
    public String agentDns(@RequestParam(required = false) String agentDN) {
        return monitorService.getAllAgentDn(agentDN);
    }

    /** 技能组领域列表（老系统 SkillMonitorController index） */
    @GetMapping("/skill-domains")
    public List<Map<String, Object>> skillDomains() {
        return monitorService.skillDomains();
    }
}
