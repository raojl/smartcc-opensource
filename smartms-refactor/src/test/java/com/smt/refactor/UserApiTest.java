package com.smt.refactor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smt.refactor.dto.LoginRequest;
import java.nio.charset.StandardCharsets;
import com.smt.refactor.dto.UserSaveRequest;
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

import java.util.Arrays;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 阶段 2 用户管理测试：CRUD + 角色绑定 + 密码重置 + 唯一性校验
 * 测试用户用 utest_ 前缀，测试后清理
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** 唯一测试登录名：utest_ + 短随机串 */
    private static final String LOGIN = "utest_" + UUID.randomUUID().toString().substring(0, 8);
    private static int createdUserId = -1;

    private String login(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("token").asText();
    }

    private String adminToken() throws Exception {
        return login("admin", "admin123");
    }

    @Test
    @Order(1)
    void create_shouldWork_andDefaultPasswordIsLoginName() throws Exception {
        String token = adminToken();
        UserSaveRequest req = new UserSaveRequest();
        req.setUserName("测试用户");
        req.setLoginName(LOGIN);
        req.setTelephone("13800138000");
        req.setEmail(LOGIN + "@test.com");
        req.setRoleType(1);
        req.setRoleIds(Arrays.asList(6, 4)); // 坐席 + 运营者

        MvcResult result = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber())
                .andReturn();
        createdUserId = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("userId").asInt();
        System.out.println(">>> [user] 新增用户 " + LOGIN + " -> userId=" + createdUserId);

        // 默认密码 = 登录名，可用其登录（与老系统 save 行为一致）
        String newToken = login(LOGIN, LOGIN);
        System.out.println(">>> [user] 默认密码(登录名)登录成功，token 前20字: " + newToken.substring(0, 20));
    }

    @Test
    @Order(2)
    void detail_shouldReturnRoles() throws Exception {
        String token = adminToken();
        mockMvc.perform(get("/api/users/{id}", createdUserId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginName").value(LOGIN))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles.length()").value(2));
        System.out.println(">>> [user] 详情包含 2 个角色");
    }

    @Test
    @Order(3)
    void update_shouldChangeProfile_andRoles() throws Exception {
        String token = adminToken();
        UserSaveRequest req = new UserSaveRequest();
        req.setUserName("测试用户改名");
        req.setLoginName(LOGIN);
        req.setTelephone("13900139000");
        req.setPassword("newpass123");   // 改密码
        req.setRoleIds(Arrays.asList(5)); // 角色覆盖为管理员
        mockMvc.perform(put("/api/users/{id}", createdUserId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // 角色已覆盖为 1 个
        mockMvc.perform(get("/api/users/{id}", createdUserId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.telephone").value("13900139000"));
        // 新密码可登录
        login(LOGIN, "newpass123");
        System.out.println(">>> [user] 编辑成功：改名/改密/角色覆盖为1个");
    }

    @Test
    @Order(4)
    void resetPassword_shouldResetToLoginName() throws Exception {
        String token = adminToken();
        mockMvc.perform(post("/api/users/{id}/reset-password", createdUserId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        // 重置后密码 = 登录名
        login(LOGIN, LOGIN);
        System.out.println(">>> [user] 重置密码成功，密码恢复为登录名");
    }

    @Test
    @Order(5)
    void checkLoginName_shouldDetectDuplicate() throws Exception {
        String token = adminToken();
        // 已存在 -> 不可用
        mockMvc.perform(get("/api/users/check-login-name")
                        .param("loginName", LOGIN)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.available").value(false));
        // 不存在 -> 可用
        mockMvc.perform(get("/api/users/check-login-name")
                        .param("loginName", "nonexist_" + UUID.randomUUID().toString().substring(0, 6))
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.available").value(true));
        System.out.println(">>> [user] 登录名唯一性校验正确");
    }

    @Test
    @Order(6)
    void page_shouldReturnUsers() throws Exception {
        String token = adminToken();
        mockMvc.perform(get("/api/users")
                        .param("page", "1").param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.total").isNumber());
        // 按用户名模糊搜索测试用户
        MvcResult result = mockMvc.perform(get("/api/users")
                        .param("username", "测试用户")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        System.out.println(">>> [user] 分页列表 total=" + json.get("total").asLong()
                + ", 搜'测试用户'命中=" + json.get("list").size());
    }

    @Test
    @Order(7)
    void delete_shouldWork() throws Exception {
        String token = adminToken();
        mockMvc.perform(delete("/api/users/{id}", createdUserId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        // 删除后详情 404（返回业务错误）
        mockMvc.perform(get("/api/users/{id}", createdUserId).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("用户不存在"));
        System.out.println(">>> [user] 删除成功，测试用户已清理");
    }

    @Test
    @Order(8)
    void userApi_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{}")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/users/1")).andExpect(status().isUnauthorized());
        System.out.println(">>> [user] 未认证访问用户接口全部 401");
    }
}
