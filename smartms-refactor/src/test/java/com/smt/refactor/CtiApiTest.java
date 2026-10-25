package com.smt.refactor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smt.refactor.dto.LoginRequest;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 阶段 2 CTI 管理测试：链路/号码/CTI参数/录音/技能组 5 子模块
 * 测试数据用 t_ 前缀，测试后清理
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CtiApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("token").asText();
    }

    @Test
    @Order(1)
    void links_shouldCRUD() throws Exception {
        String token = adminToken();
        // 新增
        String body = "{\"linkServername\":\"tlink_srv\",\"linkUsername\":\"cti\",\"linkPassword\":\"pw\",\"linkVersion\":\"v1\",\"linkApitype\":\"1\",\"pbxType\":\"asterisk\"}";
        MvcResult result = mockMvc.perform(post("/api/cti/links")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        // 详情（取 rid）
        MvcResult pageResult = mockMvc.perform(get("/api/cti/links").param("page", "1").param("size", "100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        // 通过分页找到刚建的
        String listJson = pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        int idx = listJson.indexOf("tlink_srv");
        // 回查最近一条
        String lastId = objectMapper.readTree(listJson).get("list").get(0).get("rid").asText();
        mockMvc.perform(get("/api/cti/links/{id}", lastId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        // 删除
        mockMvc.perform(delete("/api/cti/links/{id}", lastId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [cti] 链路配置 CRUD 成功 (idx=" + idx + ")");
    }

    @Test
    @Order(2)
    void extensions_shouldPage() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/cti/extensions").param("page", "1").param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andReturn();
        System.out.println(">>> [cti] 号码配置 total="
                + objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("total").asLong());
    }

    @Test
    @Order(3)
    void profiles_shouldCRUD() throws Exception {
        String token = adminToken();
        String body = "{\"conSection\":\"tsection\",\"conKey\":\"tkey\",\"conValue\":\"tvalue\",\"conDesc\":\"测试参数\"}";
        mockMvc.perform(post("/api/cti/profiles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        MvcResult pageResult = mockMvc.perform(get("/api/cti/profiles").param("page", "1").param("size", "100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String listJson = pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        int idx = listJson.indexOf("tkey");
        String lastId = objectMapper.readTree(listJson).get("list").get(0).get("rid").asText();
        mockMvc.perform(delete("/api/cti/profiles/{id}", lastId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [cti] CTI 参数 CRUD 成功 (idx=" + idx + ")");
    }

    @Test
    @Order(4)
    void records_shouldPage() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/cti/records").param("page", "1").param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andReturn();
        System.out.println(">>> [cti] 录音配置 total="
                + objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("total").asLong());
    }

    @Test
    @Order(5)
    void skills_shouldCRUD() throws Exception {
        String token = adminToken();
        // 新增技能组
        String body = "{\"skillId\":\"t9\",\"skillName\":\"tskill\",\"skillGroupType\":\"1\",\"channelType\":\"1\",\"isValid\":\"1\",\"skillDesc\":\"测试技能组\"}";
        mockMvc.perform(post("/api/cti/skills")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        // 搜索
        MvcResult result = mockMvc.perform(get("/api/cti/skills").param("keyword", "tskill")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andReturn();
        String listJson = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        String rid = objectMapper.readTree(listJson).get("list").get(0).get("rid").asText();
        // 详情
        mockMvc.perform(get("/api/cti/skills/{id}", rid).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.skillName").value("tskill"));
        // 删除
        mockMvc.perform(delete("/api/cti/skills/{id}", rid).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [cti] 技能组 CRUD 成功");
    }

    @Test
    @Order(6)
    void ctiApi_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/cti/links")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/cti/skills")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/cti/profiles")).andExpect(status().isUnauthorized());
        System.out.println(">>> [cti] 未认证访问 CTI 接口全部 401");
    }
}
