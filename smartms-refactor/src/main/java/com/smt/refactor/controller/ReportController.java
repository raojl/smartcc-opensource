package com.smt.refactor.controller;

import com.smt.refactor.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统计分析接口（翻译自老系统 report 包 2 个 Controller）
 */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /** 呼入话务统计（type=hrhwltj） */
    @GetMapping("/talk/inbound")
    public List<Map<String, Object>> inboundTalk(@RequestParam(defaultValue = "day") String timeType,
                                                 @RequestParam(required = false) String timeVal,
                                                 @RequestParam(required = false) String timeStartVal,
                                                 @RequestParam(required = false) String timeEndVal,
                                                 @RequestParam(required = false) String bizDnis) {
        return reportService.inboundTalk(timeType, timeVal, timeStartVal, timeEndVal, bizDnis);
    }

    /** 外呼话务统计（type=whhwltj） */
    @GetMapping("/talk/outbound")
    public List<Map<String, Object>> outboundTalk(@RequestParam(defaultValue = "day") String timeType,
                                                  @RequestParam(required = false) String timeVal,
                                                  @RequestParam(required = false) String timeStartVal,
                                                  @RequestParam(required = false) String timeEndVal) {
        return reportService.outboundTalk(timeType, timeVal, timeStartVal, timeEndVal);
    }

    /** 坐席 KPI 统计（type=zxkpitj） */
    @GetMapping("/talk/agent-kpi")
    public List<Map<String, Object>> agentKpi(@RequestParam(defaultValue = "day") String timeType,
                                              @RequestParam(required = false) String timeVal,
                                              @RequestParam(required = false) String timeStartVal,
                                              @RequestParam(required = false) String timeEndVal,
                                              @RequestParam(required = false) String agentId,
                                              @RequestParam(required = false) String skillId) {
        return reportService.agentKpi(timeType, timeVal, timeStartVal, timeEndVal, agentId, skillId);
    }

    /** 满意度统计（type=mydtj） */
    @GetMapping("/satisfaction")
    public List<Map<String, Object>> satisfaction(@RequestParam(required = false) String startTime,
                                                  @RequestParam(required = false) String endTime,
                                                  @RequestParam(required = false) String agentId,
                                                  @RequestParam(required = false) String skillId) {
        return reportService.satisfaction(startTime, endTime, agentId, skillId);
    }

    /** 工单量统计（type=gzltj） */
    @GetMapping("/work-task")
    public List<Map<String, Object>> workTaskStat(@RequestParam(defaultValue = "day") String timeType,
                                                  @RequestParam(required = false) String timeVal,
                                                  @RequestParam(required = false) String timeStartVal,
                                                  @RequestParam(required = false) String timeEndVal,
                                                  @RequestParam(required = false) String agentId) {
        return reportService.workTaskStat(timeType, timeVal, timeStartVal, timeEndVal, agentId);
    }

    /** 呼入统计（type=hrtj） */
    @GetMapping("/inbound")
    public List<Map<String, Object>> inboundStat(@RequestParam(defaultValue = "day") String timeType,
                                                 @RequestParam(required = false) String timeVal,
                                                 @RequestParam(required = false) String timeStartVal,
                                                 @RequestParam(required = false) String timeEndVal) {
        return reportService.inboundStat(timeType, timeVal, timeStartVal, timeEndVal);
    }
}
