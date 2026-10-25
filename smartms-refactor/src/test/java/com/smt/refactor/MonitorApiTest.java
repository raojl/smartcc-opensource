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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 阶段 2 实时监控测试：技能组领域列表（真实 DB）+ CTI 代理接口（服务不可达降级）
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MonitorApiTest {

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
                        .content(objectMapper.writeValueAsString(req).getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).get("token").asText();
    }

    @Test
    @Order(1)
    void skillDomains_shouldReturnRootPlusSkills() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/monitor/skill-domains")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].domain_name").value("root"))
                .andReturn();
        int size = objectMapper.readTree(result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).size();
        System.out.println(">>> [monitor] 技能组领域列表: " + size + " 个节点（root + 技能组）");
    }

    @Test
    @Order(2)
    void ctiProxy_shouldDegradeGracefully_whenServiceDown() throws Exception {
        String token = adminToken();
        // 本地无 8091 CTI 服务，代理接口应返回业务错误（400）而非 500
        mockMvc.perform(get("/api/monitor/agents").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CTI 实时服务不可达"));
        mockMvc.perform(get("/api/monitor/rtdata-query").param("queryId", "305")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CTI 实时服务不可达"));
        System.out.println(">>> [monitor] CTI 代理接口服务不可达时优雅降级（400 而非 500）✓");
    }

    @Test
    @Order(3)
    void monitorApi_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/monitor/agents")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/monitor/skill-domains")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/monitor/rtdata-query").param("queryId", "1"))
                .andExpect(status().isUnauthorized());
        System.out.println(">>> [monitor] 未认证访问监控接口全部 401");
    }
}
