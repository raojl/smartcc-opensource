package com.smt.refactor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smt.refactor.dto.LoginRequest;
import java.nio.charset.StandardCharsets;
import com.smt.refactor.dto.RoleSaveRequest;
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
 * 阶段 2 角色管理测试：CRUD + 菜单授权 + 删除保护 + 菜单树
 * 测试角色用 rtest_ 前缀，测试后清理
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoleApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ROLE_NAME = "rtest_" + UUID.randomUUID().toString().substring(0, 8);
    private static int createdRoleId = -1;

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
    void create_shouldWork_withMenuAuth() throws Exception {
        String token = adminToken();
        RoleSaveRequest req = new RoleSaveRequest();
        req.setRoleName(ROLE_NAME);
        req.setDescription("测试角色");
        req.setMenuIds(Arrays.asList(1, 2, 3)); // 授权 3 个菜单

        MvcResult result = mockMvc.perform(post("/api/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleId").isNumber())
                .andReturn();
        createdRoleId = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("roleId").asInt();
        System.out.println(">>> [role] 新增角色 " + ROLE_NAME + " -> roleId=" + createdRoleId);

        // 详情回显 3 个菜单
        mockMvc.perform(get("/api/roles/{id}", createdRoleId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.menuIds.length()").value(3));
        System.out.println(">>> [role] 详情回显 3 个授权菜单");
    }

    @Test
    @Order(2)
    void update_shouldOverrideMenus() throws Exception {
        String token = adminToken();
        RoleSaveRequest req = new RoleSaveRequest();
        req.setRoleName(ROLE_NAME + "改");
        req.setDescription("改后描述");
        req.setMenuIds(Arrays.asList(4, 5)); // 覆盖为 2 个
        mockMvc.perform(put("/api/roles/{id}", createdRoleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/roles/{id}", createdRoleId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.roleName").value(ROLE_NAME + "改"))
                .andExpect(jsonPath("$.menuIds.length()").value(2));
        System.out.println(">>> [role] 编辑成功：改名 + 菜单覆盖为 2 个");
    }

    @Test
    @Order(3)
    void menuTree_shouldReturnAllMenus() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/roles/menu-tree")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        JsonNode tree = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        System.out.println(">>> [role] 全部菜单树顶级节点数: " + tree.size());
    }

    @Test
    @Order(4)
    void all_shouldReturnRoles() throws Exception {
        String token = adminToken();
        mockMvc.perform(get("/api/roles/all").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$[0].roleName").isNotEmpty());
        System.out.println(">>> [role] /all 返回角色列表（>=5 个）");
    }

    @Test
    @Order(5)
    void page_shouldReturnRoles() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/roles")
                        .param("page", "1").param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        System.out.println(">>> [role] 分页 total=" + json.get("total").asLong());
    }

    @Test
    @Order(6)
    void delete_shouldReject_whenRoleInUse() throws Exception {
        String token = adminToken();
        // 角色 4=运营者 被 admin(user_id=1) 引用，应拒绝删除
        mockMvc.perform(get("/api/roles/{id}/deletable", 4).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.deletable").value(false));
        mockMvc.perform(delete("/api/roles/{id}", 4).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("该角色已被用户引用，无法删除"));
        System.out.println(">>> [role] 被引用角色拒绝删除 ✓");
    }

    @Test
    @Order(7)
    void delete_shouldWork_whenUnused() throws Exception {
        String token = adminToken();
        mockMvc.perform(delete("/api/roles/{id}", createdRoleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/roles/{id}", createdRoleId).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("角色不存在"));
        System.out.println(">>> [role] 未引用角色删除成功，测试角色已清理");
    }

    @Test
    @Order(8)
    void roleApi_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/roles")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/roles").contentType(MediaType.APPLICATION_JSON)
                        .content("{}")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/roles/menu-tree")).andExpect(status().isUnauthorized());
        System.out.println(">>> [role] 未认证访问角色接口全部 401");
    }
}
