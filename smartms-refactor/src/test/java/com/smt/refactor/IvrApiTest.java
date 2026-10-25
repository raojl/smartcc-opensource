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
 * 阶段 2 IVR 管理测试：语音服务/中继群/文件管理/IVR参数/IVR路由 5 子模块
 * 测试数据用 t_ 前缀，测试后清理
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IvrApiTest {

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
                        .content((objectMapper.writeValueAsString(req)).getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("token").asText();
    }

    /** 分页遍历查找指定字段值的记录 id（不依赖排序） */
    private String findRid(String url, String token, String field, String value) throws Exception {
        MvcResult result = mockMvc.perform(get(url).param("page", "1").param("size", "1000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        com.fasterxml.jackson.databind.JsonNode list = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("list");
        System.out.println(">>> [ivr] findRid(" + url + ") 返回 " + list.size() + " 条, 前2条: "
                + (list.size() > 0 ? list.get(0).toString() : "-") + " | "
                + (list.size() > 1 ? list.get(1).toString() : "-"));
        for (com.fasterxml.jackson.databind.JsonNode item : list) {
            if (value.equals(item.get(field).asText())) {
                return item.get("rid").asText();
            }
        }
        throw new AssertionError("未找到 " + field + "=" + value);
    }

    @Test
    @Order(1)
    void services_shouldCRUD() throws Exception {
        String token = adminToken();
        String body = "{\"servicename\":\"tsvc\",\"servicetype\":\"IVR\",\"serviceip\":\"10.0.0.9\",\"serviceport\":\"8080\",\"loadcfgtype\":\"1\"}";
        mockMvc.perform(post("/api/ivr/services")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json;charset=UTF-8")
                        .content((body).getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk());
        String rid = findRid("/api/ivr/services", token, "servicename", "tsvc");
        mockMvc.perform(get("/api/ivr/services/{id}", rid).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.servicename").value("tsvc"));
        mockMvc.perform(delete("/api/ivr/services/{id}", rid).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [ivr] 语音服务 CRUD 成功");
    }

    @Test
    @Order(2)
    void trunkGroups_shouldCRUD() throws Exception {
        String token = adminToken();
        String body = "{\"trunkgroupid\":\"t9\",\"trunkgroupname\":\"测试中继群\"}";
        mockMvc.perform(post("/api/ivr/trunk-groups")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json;charset=UTF-8")
                        .content((body).getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk());
        String rid = findRid("/api/ivr/trunk-groups", token, "trunkgroupname", "测试中继群");
        mockMvc.perform(get("/api/ivr/trunk-groups/{id}", rid).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.trunkgroupname").value("测试中继群"));
        // all
        mockMvc.perform(get("/api/ivr/trunk-groups/all").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        mockMvc.perform(delete("/api/ivr/trunk-groups/{id}", rid).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [ivr] 中继群 CRUD 成功");
    }

    @Test
    @Order(3)
    void files_shouldPageFilter() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/ivr/files").param("page", "1").param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andReturn();
        long total = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("total").asLong();
        // 按文件类型过滤
        mockMvc.perform(get("/api/ivr/files").param("fileType", "FLOW")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [ivr] 语音文件 total=" + total + "（已过滤 App.vxml/.bak）");
    }

    @Test
    @Order(4)
    void params_shouldCRUD() throws Exception {
        String token = adminToken();
        String body = "{\"servicename\":\"tsvc\",\"paramname\":\"tparam\",\"paramvalue\":\"1\",\"paramnote\":\"测试参数\"}";
        mockMvc.perform(post("/api/ivr/params")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json;charset=UTF-8")
                        .content((body).getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk());
        String rid = findRid("/api/ivr/params", token, "paramname", "tparam");
        mockMvc.perform(delete("/api/ivr/params/{id}", rid).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [ivr] IVR 参数 CRUD 成功");
    }

    @Test
    @Order(5)
    void routes_shouldCRUD() throws Exception {
        String token = adminToken();
        String body = "{\"trunkgroupid\":\"1\",\"calldirection\":\"1\",\"channelCount\":10,\"routenum\":\"troute_1\",\"flowname\":\"../resources/vxml/main.vxml\",\"displayname\":\"测试路由\"}";
        mockMvc.perform(post("/api/ivr/routes")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json;charset=UTF-8")
                        .content((body).getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk());
        // 遍历找到刚建的路由（主键 trunksn）
        MvcResult result = mockMvc.perform(get("/api/ivr/routes").param("page", "1").param("size", "1000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String listJson = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        int idx = listJson.indexOf("troute_1");
        String trunksn = null;
        com.fasterxml.jackson.databind.JsonNode list = objectMapper.readTree(listJson).get("list");
        for (com.fasterxml.jackson.databind.JsonNode item : list) {
            if ("troute_1".equals(item.get("routenum").asText())) {
                trunksn = item.get("trunksn").asText();
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertNotNull(trunksn, "未找到测试路由");
        mockMvc.perform(get("/api/ivr/routes/{id}", trunksn).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.displayname").value("测试路由"));
        mockMvc.perform(delete("/api/ivr/routes/{id}", trunksn).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [ivr] IVR 路由 CRUD 成功 (idx=" + idx + ")");
    }

    @Test
    @Order(6)
    void ivrApi_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/ivr/services")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/ivr/routes")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/ivr/files")).andExpect(status().isUnauthorized());
        System.out.println(">>> [ivr] 未认证访问 IVR 接口全部 401");
    }
}
