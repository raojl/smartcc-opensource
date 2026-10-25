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
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 阶段 2 外呼任务测试：号码导入/任务列表聚合/启停/删除/详单
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OutboundApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String testTaskId = null;

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
    void importNumbers_shouldCreateTask() throws Exception {
        String token = adminToken();
        String body = "{\"numbers\":[\"13900001111\",\"13900002222\",\"10086,13900003333\"],\"taskDesc\":\"t外呼任务\",\"agentIds\":[\"3007\",\"3008\"]}";
        MvcResult result = mockMvc.perform(post("/api/outbound/tasks/import")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json;charset=UTF-8")
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(3))
                .andReturn();
        System.out.println(">>> [outbound] 号码导入 3 条");
    }

    @Test
    @Order(2)
    void tasks_shouldAggregate() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/outbound/tasks")
                        .param("taskDesc", "t外呼任务")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        com.fasterxml.jackson.databind.JsonNode list = objectMapper.readTree(json).get("list");
        org.junit.jupiter.api.Assertions.assertTrue(list.size() >= 1, "应有导入的任务");
        testTaskId = list.get(0).get("task_id").asText();
        System.out.println(">>> [outbound] 任务聚合: task_id=" + testTaskId
                + " 完成率=" + list.get(0).get("wcl").asText());
    }

    @Test
    @Order(3)
    void details_shouldReturnRows() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/outbound/details")
                        .param("taskId", testTaskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(">>> [outbound] 任务详单 total=" + objectMapper.readTree(json).get("total").asLong());
    }

    @Test
    @Order(4)
    void startStop_shouldToggleStatus() throws Exception {
        String token = adminToken();
        // 启动
        mockMvc.perform(post("/api/outbound/tasks/{taskId}/start", testTaskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        // 详单应显示状态 1
        MvcResult result = mockMvc.perform(get("/api/outbound/details")
                        .param("taskId", testTaskId).param("size", "10")
                        .header("Authorization", "Bearer " + token))
                .andReturn();
        com.fasterxml.jackson.databind.JsonNode list = objectMapper.readTree(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("list");
        boolean allOne = true;
        for (com.fasterxml.jackson.databind.JsonNode item : list) {
            if (!"1".equals(item.get("taskStatus").asText())) {
                allOne = false;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(allOne, "启动后 task_status 应为 1");
        // 停止
        mockMvc.perform(post("/api/outbound/tasks/{taskId}/stop", testTaskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [outbound] 启动/停止状态切换成功");
    }

    @Test
    @Order(5)
    void delete_shouldRemoveTask() throws Exception {
        String token = adminToken();
        mockMvc.perform(delete("/api/outbound/tasks/{taskId}", testTaskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        MvcResult result = mockMvc.perform(get("/api/outbound/details")
                        .param("taskId", testTaskId)
                        .header("Authorization", "Bearer " + token))
                .andReturn();
        long total = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("total").asLong();
        org.junit.jupiter.api.Assertions.assertEquals(0, total, "删除后详单应为空");
        System.out.println(">>> [outbound] 任务删除成功");
    }

    @Test
    @Order(6)
    void myFilter_shouldNotBreak() throws Exception {
        String token = adminToken();
        mockMvc.perform(get("/api/outbound/tasks").param("myCustom", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/outbound/details").param("agentId", "my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [outbound] 我的任务/我的详单接口正常");
    }

    @Test
    @Order(7)
    void outboundApi_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/outbound/tasks")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/outbound/details")).andExpect(status().isUnauthorized());
        System.out.println(">>> [outbound] 未认证访问外呼接口全部 401");
    }
}
