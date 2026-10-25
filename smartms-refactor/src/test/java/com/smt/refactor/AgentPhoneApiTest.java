package com.smt.refactor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 软电话代理 API 测试（移植老系统 softphoneBar AMI 协议）
 * 本地无 CTI 8091，验证：参数校验 + 不可达容错（明确 400 而非 500）
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AgentPhoneApiTest {

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper om = new ObjectMapper();

    private String adminToken() throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return om.readTree(body).get("token").asText();
    }

    @Test
    @Order(1)
    void action_shouldRejectMissingAction() throws Exception {
        mockMvc.perform(post("/api/agent-phone/action")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
        System.out.println(">>> [agent-phone] 缺 action 返回 400 ✅");
    }

    @Test
    @Order(2)
    void login_shouldReturnClearError_whenCtiDown() throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("action", "Login");
        req.put("exten", "8001");
        req.put("workNo", "3004");
        req.put("agentName", "测试坐席");
        req.put("skills", "sales;");
        String body = mockMvc.perform(post("/api/agent-phone/action")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(">>> [agent-phone] Login(CTI 不可达) → 400: "
                + om.readTree(body).get("message").asText());
    }

    @Test
    @Order(3)
    void events_shouldReturnClearError_whenCtiDown() throws Exception {
        mockMvc.perform(get("/api/agent-phone/events")
                        .param("exten", "8001").param("workNo", "3004").param("sessionId", "abc")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
        System.out.println(">>> [agent-phone] 事件长轮询(CTI 不可达) → 400 ✅");
    }

    @Test
    @Order(4)
    void unauthorized_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/agent-phone/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"Login\"}"))
                .andExpect(status().isUnauthorized());
        System.out.println(">>> [agent-phone] 无 token → 401 ✅");
    }
}
