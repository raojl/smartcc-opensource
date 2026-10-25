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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 阶段 2 质检管理测试：模板 CRUD + 待质检通话 + 评分生成/撤销 + 成绩查询
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QualityApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static Long templateId = null;

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
    void template_shouldCreate() throws Exception {
        String token = adminToken();
        String body = "{\"name\":\"t质检模板\",\"desc\":\"测试\",\"excellent\":\"90\",\"fine\":\"75\",\"pass\":\"60\","
                + "\"qualityColumns\":\"服务态度=40;业务准确=30;用语规范=30\",\"status\":\"2\",\"creater\":\"3007\"}";
        MvcResult result = mockMvc.perform(post("/api/quality/templates")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json;charset=UTF-8")
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();
        templateId = objectMapper.readTree(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("id").asLong();
        System.out.println(">>> [quality] 新增质检模板 -> id=" + templateId);
        // 详情
        mockMvc.perform(get("/api/quality/templates/{id}", templateId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.name").value("t质检模板"))
                .andExpect(jsonPath("$.qualityColumns").value("服务态度=40;业务准确=30;用语规范=30"));
        // 启用模板列表
        mockMvc.perform(get("/api/quality/templates/active").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        System.out.println(">>> [quality] 模板详情 + 启用列表正常");
    }

    @Test
    @Order(2)
    void template_shouldRejectInvalidScoreRange() throws Exception {
        String token = adminToken();
        // excellent(70) <= fine(75) 非法
        String body = "{\"name\":\"t非法模板\",\"excellent\":\"70\",\"fine\":\"75\",\"pass\":\"60\",\"qualityColumns\":\"项=100\"}";
        mockMvc.perform(post("/api/quality/templates")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json;charset=UTF-8")
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("高评价分数必须大于低评价分数"));
        // 权重超 100
        String body2 = "{\"name\":\"t非法模板2\",\"excellent\":\"90\",\"fine\":\"75\",\"pass\":\"60\",\"qualityColumns\":\"项1=60;项2=50\"}";
        mockMvc.perform(post("/api/quality/templates")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json;charset=UTF-8")
                        .content(body2.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("质检项权重总和不得超过100"));
        System.out.println(">>> [quality] 非法分数区间/权重超限被拒绝 ✓");
    }

    @Test
    @Order(3)
    void inspectCalls_shouldReturnTalkCalls() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/quality/calls")
                        .param("page", "1").param("size", "5")
                        .param("startTime", "2023-12-10").param("endTime", "2023-12-15")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andReturn();
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(">>> [quality] 待质检通话 total=" + objectMapper.readTree(json).get("total").asLong());
    }

    @Test
    @Order(4)
    void generateResult_shouldScoreAndGrade() throws Exception {
        String token = adminToken();
        // 取一条真实通话作为质检对象
        MvcResult call = mockMvc.perform(get("/api/quality/calls")
                        .param("page", "1").param("size", "1")
                        .param("startTime", "2023-12-10").param("endTime", "2023-12-15")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        com.fasterxml.jackson.databind.JsonNode callList = objectMapper.readTree(call.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("list");
        if (callList.size() == 0) {
            System.out.println(">>> [quality] 无通话数据，跳过评分测试");
            return;
        }
        String contactId = callList.get(0).get("contactID").asText();
        String agentId = callList.get(0).get("agentID").asText();
        String talkStart = callList.get(0).get("talkStartTime").asText();
        // 各项满分 -> 总分 100 -> 优秀(4)
        String body = "{\"templateId\":" + templateId + ",\"contactId\":\"" + contactId + "\",\"agentid\":\"" + agentId + "\","
                + "\"talkstarttime\":\"" + talkStart + "\",\"columnNames\":[\"服务态度\",\"业务准确\",\"用语规范\"],"
                + "\"columnScores\":[100,100,100],\"columnWeights\":[40,30,30]}";
        mockMvc.perform(post("/api/quality/results")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json;charset=UTF-8")
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk());
        // 成绩列表应包含该条（按 agentid 过滤）
        MvcResult result = mockMvc.perform(get("/api/quality/results")
                        .param("agentId", agentId).param("size", "50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        com.fasterxml.jackson.databind.JsonNode list = objectMapper.readTree(json).get("list");
        boolean found = false;
        for (com.fasterxml.jackson.databind.JsonNode item : list) {
            if (contactId.equals(item.get("contactId").asText()) && "4".equals(item.get("resultGrade").asText())) {
                found = true;
                System.out.println(">>> [quality] 评分生成：总分=" + item.get("resultScore").asText() + " 等级=" + item.get("resultGrade").asText() + "(优秀)");
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(found, "应找到生成的优秀成绩");
    }

    @Test
    @Order(5)
    void reInspect_shouldReset() throws Exception {
        String token = adminToken();
        // 撤销最近一条成绩
        MvcResult result = mockMvc.perform(get("/api/quality/results").param("size", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        com.fasterxml.jackson.databind.JsonNode list = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("list");
        if (list.size() > 0) {
            String contactId = list.get(0).get("contactId").asText();
            String agentId = list.get(0).get("agentid").asText();
            String talkStart = list.get(0).get("talkstarttime").asText();
            mockMvc.perform(delete("/api/quality/results")
                            .param("contactId", contactId).param("agentId", agentId).param("talkStartTime", talkStart)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
            System.out.println(">>> [quality] 撤销质检成功");
        } else {
            System.out.println(">>> [quality] 无成绩可撤销");
        }
    }

    @Test
    @Order(6)
    void templateStatus_shouldToggle() throws Exception {
        String token = adminToken();
        mockMvc.perform(put("/api/quality/templates/{id}/status", templateId)
                        .param("status", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/quality/templates/{id}", templateId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.status").value("1"));
        System.out.println(">>> [quality] 模板停用成功");
    }

    @Test
    @Order(7)
    void cleanup_shouldDeleteTestData() throws Exception {
        if (templateId != null) {
            java.sql.DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/smartcc?characterEncoding=utf8&useSSL=false",
                            "root", "")
                    .createStatement()
                    .executeUpdate("DELETE FROM crm_quality_template WHERE id = " + templateId);
            System.out.println(">>> [quality] 测试模板已清理");
        }
    }

    @Test
    @Order(8)
    void qualityApi_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/quality/templates")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/quality/results")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/quality/calls")).andExpect(status().isUnauthorized());
        System.out.println(">>> [quality] 未认证访问质检接口全部 401");
    }
}
