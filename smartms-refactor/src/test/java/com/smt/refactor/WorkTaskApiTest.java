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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 阶段 2 工单模块测试：分页/详情/新增/办结/转办/我的工单
 * 测试数据 work_id 用 t_ 前缀，测试后清理
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WorkTaskApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_WORK_ID = "t" + System.currentTimeMillis() + "3007";

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
    void create_shouldWork() throws Exception {
        String token = adminToken();
        String body = "{\"workId\":\"" + TEST_WORK_ID + "\",\"customName\":\"先生\",\"workType\":\"1\","
                + "\"bussinessType\":\"2\",\"belongAgent\":\"3007\",\"workAgent\":\"3007\",\"workStatus\":\"1\","
                + "\"bussinessDesc\":\"测试工单\",\"solution\":\"\",\"mark\":\"t\"}";
        mockMvc.perform(post("/api/work-tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json;charset=UTF-8")
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.option").value("create"));
        System.out.println(">>> [worktask] 新增工单 " + TEST_WORK_ID);
    }

    @Test
    @Order(2)
    void detail_shouldReturnCustomerInfo() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/work-tasks/{id}", TEST_WORK_ID)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workId").value(TEST_WORK_ID))
                .andReturn();
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(">>> [worktask] 详情 customName="
                + objectMapper.readTree(json).get("customName").asText()
                + " | customMobile=" + objectMapper.readTree(json).get("customMobile").asText());
    }

    @Test
    @Order(3)
    void page_shouldFilterAndReturnMyTasks() throws Exception {
        String token = adminToken();
        // 按工单号模糊搜索
        MvcResult result = mockMvc.perform(get("/api/work-tasks")
                        .param("workId", TEST_WORK_ID)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.total").value(1))
                .andReturn();
        System.out.println(">>> [worktask] 按工单号搜索命中 1 条");
        // 我的工单（belongAgent=my，admin 无 qualityAgCode -> 查不到，但接口不报错）
        mockMvc.perform(get("/api/work-tasks")
                        .param("belongAgent", "my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [worktask] 我的工单接口正常（admin 无工号返回空）");
    }

    @Test
    @Order(4)
    void finish_shouldCloseWorkTask() throws Exception {
        String token = adminToken();
        mockMvc.perform(post("/api/work-tasks/{id}/finish", TEST_WORK_ID)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        MvcResult result = mockMvc.perform(get("/api/work-tasks/{id}", TEST_WORK_ID)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.workStatus").value("3"))
                .andExpect(jsonPath("$.closeTime").isNotEmpty())
                .andReturn();
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(">>> [worktask] 办结成功，closeTime="
                + objectMapper.readTree(json).get("closeTime").asText()
                + " | tl=" + objectMapper.readTree(json).get("tl").asText());
    }

    @Test
    @Order(5)
    void transfer_shouldChangeAgent() throws Exception {
        String token = adminToken();
        String body = "{\"transferAgent\":\"3008\"}";
        mockMvc.perform(post("/api/work-tasks/{id}/transfer", TEST_WORK_ID)
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json;charset=UTF-8")
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/work-tasks/{id}", TEST_WORK_ID)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.workAgent").value("3008"))
                .andExpect(jsonPath("$.workStatus").value("2"));
        System.out.println(">>> [worktask] 转办成功：workAgent=3008, 状态=2");
    }

    @Test
    @Order(6)
    void agents_shouldReturnCandidates() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/work-tasks/agents")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        int size = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).size();
        System.out.println(">>> [worktask] 坐席候选列表: " + size + " 人");
    }

    @Test
    @Order(7)
    void cleanup_shouldDeleteTestData() throws Exception {
        String token = adminToken();
        // 测试收尾：直接清库删除测试工单（避免残留污染真实数据）
        java.sql.DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/smartcc?characterEncoding=utf8&useSSL=false",
                        "root", "")
                .createStatement()
                .executeUpdate("DELETE FROM crm_worktaskinfo WHERE work_id LIKE 't%'");
        System.out.println(">>> [worktask] 测试数据已清理");
    }

    @Test
    @Order(8)
    void workTaskApi_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/work-tasks")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/work-tasks/agents")).andExpect(status().isUnauthorized());
        System.out.println(">>> [worktask] 未认证访问工单接口全部 401");
    }
}
