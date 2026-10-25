package com.smt.refactor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smt.refactor.dto.LoginRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 阶段 2 统计分析测试：呼入/外呼/坐席KPI/满意度/工单量/呼入统计
 * 数据为 2023 年历史数据，需显式传时间
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReportApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json;charset=UTF-8")
                        .content(objectMapper.writeValueAsString(req).getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("token").asText();
    }

    @Test
    @Order(1)
    void inboundTalk_shouldReturnRows() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/report/talk/inbound")
                        .param("timeType", "day").param("timeVal", "2023-12-13")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(">>> [report] 呼入话务统计: " + objectMapper.readTree(json).size() + " 行（含合计）");
    }

    @Test
    @Order(2)
    void outboundTalk_shouldReturnRows() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/report/talk/outbound")
                        .param("timeType", "day").param("timeVal", "2023-12-13")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        System.out.println(">>> [report] 外呼话务统计: " + objectMapper.readTree(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8)).size() + " 行");
    }

    @Test
    @Order(3)
    void agentKpi_shouldReturnRows() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/report/talk/agent-kpi")
                        .param("timeType", "day").param("timeVal", "2023-12-13")
                        .param("agentId", "3004")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(">>> [report] 坐席KPI统计: " + objectMapper.readTree(json).size() + " 行");
    }

    @Test
    @Order(4)
    void satisfaction_shouldReturnRows() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/report/satisfaction")
                        .param("startTime", "2023-12-01 00:00:00").param("endTime", "2023-12-31 23:59:59")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        System.out.println(">>> [report] 满意度统计: " + objectMapper.readTree(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8)).size() + " 行");
    }

    @Test
    @Order(5)
    void workTaskStat_shouldReturnRows() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/report/work-task")
                        .param("timeType", "month").param("timeVal", "2023-12")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        System.out.println(">>> [report] 工单量统计: " + objectMapper.readTree(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8)).size() + " 行");
    }

    @Test
    @Order(6)
    void inboundStat_shouldReturnRows() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/report/inbound")
                        .param("timeType", "day").param("timeVal", "2023-12-13")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        System.out.println(">>> [report] 呼入统计: " + objectMapper.readTree(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8)).size() + " 行");
    }

    @Test
    @Order(7)
    void reportApi_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/report/talk/inbound")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/report/satisfaction")).andExpect(status().isUnauthorized());
        System.out.println(">>> [report] 未认证访问统计接口全部 401");
    }
}
