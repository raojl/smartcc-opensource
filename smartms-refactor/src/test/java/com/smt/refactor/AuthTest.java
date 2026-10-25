package com.smt.refactor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smt.refactor.dto.LoginRequest;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 阶段 1 认证模块测试：登录 / 校验 / 菜单树 / 无 token 拦截
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String login(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.loginName").value(username))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("token").asText();
    }

    @Test
    void login_shouldSucceed_withValidMd5Password() throws Exception {
        // admin/admin123 是存量 MD5 密码（0192023a...）
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.loginName").value("admin"))
                .andExpect(jsonPath("$.user.roleType").value(0))
                .andReturn();
        System.out.println(">>> [auth] admin 登录成功，token 前20字: "
                + objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("token").asText().substring(0, 20));
    }

    @Test
    void login_shouldFail_withWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    void me_shouldWork_withToken() throws Exception {
        String token = login("admin", "admin123");
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginName").value("admin"));
    }

    @Test
    void menuTree_shouldReturn_withToken() throws Exception {
        String token = login("admin", "admin123");
        mockMvc.perform(get("/api/menu/tree").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(13))
                .andExpect(jsonPath("$[0].menuName").value("账号管理"))
                .andExpect(jsonPath("$[0].children[0].menuName").value("用户管理"));
        System.out.println(">>> [auth] admin 菜单树 13 个顶级节点");
    }

    @Test
    void api_shouldReturn401_withoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/menu/tree"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void api_shouldReturn401_withInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer bad.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void health_shouldBePublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
