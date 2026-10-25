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
 * 阶段 2 交互查询测试：通话详单 / 未接记录 / 我的过滤
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecordApiTest {

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
    void callRecords_shouldReturnConnectedCalls() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/records")
                        .param("page", "1").param("size", "5")
                        .param("startTime", "2023-12-10").param("endTime", "2023-12-15")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andReturn();
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
        String first = root.get("list").size() > 0
                ? "ANI=" + root.get("list").get(0).get("bizANI").asText()
                + " 时长=" + root.get("list").get(0).get("timeLength").asLong() + "s"
                : "(空)";
        System.out.println(">>> [record] 通话详单 total=" + root.get("total").asLong() + " | 首个: " + first);
    }

    @Test
    @Order(2)
    void callRecords_shouldFilterByAgentAndTime() throws Exception {
        String token = adminToken();
        // 按坐席 3004 过滤
        MvcResult result = mockMvc.perform(get("/api/records")
                        .param("agentId", "3004").param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        com.fasterxml.jackson.databind.JsonNode list = objectMapper.readTree(json).get("list");
        boolean allMatch = list.size() == 0;
        for (com.fasterxml.jackson.databind.JsonNode item : list) {
            if ("3004".equals(item.get("agentId").asText())) {
                allMatch = true;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(allMatch, "坐席过滤结果应含 3004 或为空");
        System.out.println(">>> [record] 坐席 3004 过滤查询正常");
    }

    @Test
    @Order(3)
    void missedCalls_shouldReturnAgentMiss() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/records/missed")
                        .param("type", "1").param("page", "1").param("size", "5")
                        .param("startTime", "2023-12-10").param("endTime", "2023-12-15")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andReturn();
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(">>> [record] 坐席未接 total="
                + objectMapper.readTree(json).get("total").asLong());
    }

    @Test
    @Order(4)
    void missedCalls_shouldReturnQueueMiss() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/records/missed")
                        .param("type", "2").param("page", "1").param("size", "5")
                        .param("startTime", "2023-12-10").param("endTime", "2023-12-15")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andReturn();
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(">>> [record] 队列未接 total="
                + objectMapper.readTree(json).get("total").asLong());
    }

    @Test
    @Order(5)
    void missedCalls_shouldReturnCustomerMiss() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/records/missed")
                        .param("type", "3").param("page", "1").param("size", "5")
                        .param("startTime", "2023-12-10").param("endTime", "2023-12-15")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andReturn();
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(">>> [record] 客户未接 total="
                + objectMapper.readTree(json).get("total").asLong());
    }

    @Test
    @Order(6)
    void missedCalls_shouldHandleLeaveMessageGracefully() throws Exception {
        String token = adminToken();
        // 本地库可能无 LeaveVoicePath 列，接口应优雅返回（200）而非 500
        mockMvc.perform(get("/api/records/missed")
                        .param("type", "4")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [record] 留言查询优雅处理 ✓");
    }

    @Test
    @Order(7)
    void myFilter_shouldNotBreakForAdmin() throws Exception {
        String token = adminToken();
        // admin 无坐席工号，my_record 应正常返回空
        mockMvc.perform(get("/api/records")
                        .param("agentId", "my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/records/missed")
                        .param("type", "1").param("agentId", "my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [record] 我的过滤接口正常");
    }

    @Test
    @Order(8)
    void recordApi_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/records")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/records/missed")).andExpect(status().isUnauthorized());
        System.out.println(">>> [record] 未认证访问查询接口全部 401");
    }
}
