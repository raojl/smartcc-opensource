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
 * 阶段 2 客户管理测试：分页/新增/详情/软删/恢复/回收站/弹屏/导出
 * 测试数据 customName 用 t_ 前缀，测试后清理
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String testCustomId = null;
    private static final String TEST_NAME = "t客户_" + System.currentTimeMillis() % 100000;

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
        String body = "{\"customName\":\"" + TEST_NAME + "\",\"customSex\":\"1\",\"companyName\":\"测试公司\","
                + "\"customMobile\":\"13900001111\",\"customPhone\":\"0571-88888888\",\"qqNumber\":\"12345678\","
                + "\"emailAddr\":\"t@test.com\",\"customCity\":\"杭州\",\"customAddr\":\"测试路1号\","
                + "\"customLevel\":1,\"custType\":\"VIP\",\"agentid\":\"3007\"}";
        MvcResult result = mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json;charset=UTF-8")
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customId").isNotEmpty())
                .andReturn();
        testCustomId = objectMapper.readTree(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("customId").asText();
        System.out.println(">>> [customer] 新增客户 " + TEST_NAME + " -> " + testCustomId);
    }

    @Test
    @Order(2)
    void detail_shouldReturn() throws Exception {
        String token = adminToken();
        mockMvc.perform(get("/api/customers/{id}", testCustomId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customName").value(TEST_NAME))
                .andExpect(jsonPath("$.customMobile").value("13900001111"));
        System.out.println(">>> [customer] 详情查询正常");
    }

    @Test
    @Order(3)
    void page_shouldFilter() throws Exception {
        String token = adminToken();
        // 按客户名搜索
        MvcResult result = mockMvc.perform(get("/api/customers")
                        .param("customName", "t客户").param("size", "50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        long total = objectMapper.readTree(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("total").asLong();
        System.out.println(">>> [customer] 搜\"t客户\"命中 " + total + " 条");
        // 全部客户
        MvcResult all = mockMvc.perform(get("/api/customers").param("size", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(">>> [customer] 全部客户 total=" + objectMapper.readTree(
                all.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("total").asLong());
    }

    @Test
    @Order(4)
    void deleteAndRestore_shouldWork() throws Exception {
        String token = adminToken();
        // 软删除
        mockMvc.perform(delete("/api/customers/{id}", testCustomId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        // 正常列表查不到
        MvcResult result = mockMvc.perform(get("/api/customers")
                        .param("customName", "t客户").param("size", "50")
                        .header("Authorization", "Bearer " + token))
                .andReturn();
        boolean found = false;
        com.fasterxml.jackson.databind.JsonNode list = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("list");
        for (com.fasterxml.jackson.databind.JsonNode item : list) {
            if (testCustomId.equals(item.get("customId").asText())) {
                found = true;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertFalse(found, "软删后正常列表不应出现");
        // 回收站能查到
        MvcResult trash = mockMvc.perform(get("/api/customers").param("deleted", "true")
                        .param("size", "100").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        boolean inTrash = false;
        com.fasterxml.jackson.databind.JsonNode trashList = objectMapper.readTree(trash.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("list");
        for (com.fasterxml.jackson.databind.JsonNode item : trashList) {
            if (testCustomId.equals(item.get("customId").asText())) {
                inTrash = true;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(inTrash, "回收站应包含被删客户");
        // 恢复
        mockMvc.perform(post("/api/customers/{id}/restore", testCustomId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/customers/{id}", testCustomId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [customer] 软删/回收站/恢复全链路通过");
    }

    @Test
    @Order(5)
    void dialSearch_shouldFindByMobile() throws Exception {
        String token = adminToken();
        mockMvc.perform(get("/api/customers/dial-search")
                        .param("callNumber", "13900001111")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customName").value(TEST_NAME));
        System.out.println(">>> [customer] 弹屏按手机号查客户通过");
    }

    @Test
    @Order(6)
    void export_shouldProduceExcel() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/customers/export")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andReturn();
        byte[] body = result.getResponse().getContentAsByteArray();
        org.junit.jupiter.api.Assertions.assertTrue(body.length > 100, "Excel 文件应有内容");
        System.out.println(">>> [customer] Excel 导出 " + body.length + " 字节");
    }

    @Test
    @Order(7)
    void cleanup_shouldDeleteTestData() throws Exception {
        // 测试收尾：直删测试客户
        if (testCustomId != null) {
            java.sql.DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/smartcc?characterEncoding=utf8&useSSL=false",
                            "root", "")
                    .createStatement()
                    .executeUpdate("DELETE FROM crm_custominfo WHERE custom_id = '" + testCustomId + "'");
            System.out.println(">>> [customer] 测试客户已清理");
        }
    }

    @Test
    @Order(8)
    void customerApi_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/customers")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/customers/export")).andExpect(status().isUnauthorized());
        System.out.println(">>> [customer] 未认证访问客户接口全部 401");
    }
}
