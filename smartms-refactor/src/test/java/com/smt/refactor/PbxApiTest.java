package com.smt.refactor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smt.refactor.dto.*;
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

import java.util.Arrays;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 阶段 2 PBX 管理测试：SIP 分机 / 出局中继 / 路由 / 局向 生命周期
 * 测试数据用 t_ 前缀，测试后清理
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PbxApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String DEV_ID = "tdev_" + UUID.randomUUID().toString().substring(0, 6);
    private static int trunkId = -1;
    private static int routeId = -1;

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

    /* ==================== SIP 分机 ==================== */

    @Test
    @Order(1)
    void device_shouldCreateAndQuery() throws Exception {
        String token = adminToken();
        DeviceSaveRequest req = new DeviceSaveRequest();
        req.setId(DEV_ID);
        req.setCid("10086");
        req.setSecret("123456");
        req.setType("peer");
        req.setPort("5060");
        req.setDtmfmode("rfc2833");
        req.setNat("yes");
        req.setCanreinvite("no");
        req.setContext("from-internal");
        mockMvc.perform(post("/api/pbx/devices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        System.out.println(">>> [pbx] 新增 SIP 分机 " + DEV_ID);

        // 详情
        mockMvc.perform(get("/api/pbx/devices/{id}", DEV_ID).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.cid").value("10086"))
                .andExpect(jsonPath("$.context").value("from-internal"));
        // 唯一性校验（已存在 -> 不可用）
        mockMvc.perform(get("/api/pbx/devices/check-id").param("id", DEV_ID)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.available").value(false));
        // 分页
        MvcResult result = mockMvc.perform(get("/api/pbx/devices").param("page", "1").param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(">>> [pbx] 分机分页: " + objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("total").asLong() + " 条");
    }

    @Test
    @Order(2)
    void device_shouldUpdateAndDelete() throws Exception {
        String token = adminToken();
        DeviceSaveRequest req = new DeviceSaveRequest();
        req.setId(DEV_ID);
        req.setCid("10010");
        mockMvc.perform(put("/api/pbx/devices/{id}", DEV_ID)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/pbx/devices/{id}", DEV_ID).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.cid").value("10010"));
        mockMvc.perform(delete("/api/pbx/devices/{id}", DEV_ID)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [pbx] 分机编辑 + 删除成功");
    }

    /* ==================== PBX 出局中继 ==================== */

    @Test
    @Order(3)
    void trunk_shouldCreateQueryUpdateDelete() throws Exception {
        String token = adminToken();
        PbxTrunkSaveRequest req = new PbxTrunkSaveRequest();
        req.setName("ttrunk_" + UUID.randomUUID().toString().substring(0, 6));
        req.setType("SIP");
        req.setSipHost("10.0.0.1");
        req.setSipPort("5060");
        req.setDisable("off");

        MvcResult result = mockMvc.perform(post("/api/pbx/trunks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();
        trunkId = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("id").asInt();
        System.out.println(">>> [pbx] 新增中继 -> id=" + trunkId);

        mockMvc.perform(get("/api/pbx/trunks/{id}", trunkId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.sipHost").value("10.0.0.1"));

        req.setName(req.getName() + "改");
        req.setSipHost("10.0.0.2");
        mockMvc.perform(put("/api/pbx/trunks/{id}", trunkId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/pbx/trunks/{id}", trunkId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.sipHost").value("10.0.0.2"));

        // all（下拉）
        mockMvc.perform(get("/api/pbx/trunks/all").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        System.out.println(">>> [pbx] 中继编辑 + /all 成功");
    }

    /* ==================== PBX 路由 ==================== */

    @Test
    @Order(4)
    void route_shouldCreateWithTrunksAndDelete() throws Exception {
        String token = adminToken();
        RouteSaveRequest req = new RouteSaveRequest();
        req.setRouteName("troute_" + UUID.randomUUID().toString().substring(0, 6));
        req.setRouteDial("_9.");
        req.setRouteDemo("测试路由");
        req.setTrunkIds(Arrays.asList(1, trunkId));

        MvcResult result = mockMvc.perform(post("/api/pbx/routes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routeId").isNumber())
                .andReturn();
        routeId = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("routeId").asInt();
        System.out.println(">>> [pbx] 新增路由 -> routeId=" + routeId);

        // 详情含 2 个绑定中继（按顺序）
        mockMvc.perform(get("/api/pbx/routes/{id}", routeId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.trunkIds.length()").value(2))
                .andExpect(jsonPath("$.trunkIds[0]").value(1))
                .andExpect(jsonPath("$.trunkIds[1]").value(trunkId));

        // 覆盖绑定为 1 个
        req.setTrunkIds(Arrays.asList(1));
        mockMvc.perform(put("/api/pbx/routes/{id}", routeId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/pbx/routes/{id}", routeId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.trunkIds.length()").value(1));
        System.out.println(">>> [pbx] 路由中继覆盖绑定成功");

        // 删除路由
        mockMvc.perform(delete("/api/pbx/routes/{id}", routeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/pbx/routes/{id}", routeId).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("路由不存在"));
        System.out.println(">>> [pbx] 路由删除成功");
    }

    /* ==================== 局向分配 ==================== */

    @Test
    @Order(5)
    void trunkDistribute_shouldCreateAndDelete() throws Exception {
        String token = adminToken();
        TrunkSaveRequest req = new TrunkSaveRequest();
        // 注意：老表约束 trunk_id varchar(2)、trunk_type varchar(1)，必须用短值
        req.setTrunkId("9");
        req.setTrunkName("测试局向");
        req.setTrunkType("5");
        req.setTacCode("0086");

        MvcResult result = mockMvc.perform(post("/api/pbx/trunk-distributes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rid").isNumber())
                .andReturn();
        int rid = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("rid").asInt();
        System.out.println(">>> [pbx] 新增局向 -> rid=" + rid);

        mockMvc.perform(get("/api/pbx/trunk-distributes/{id}", rid).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.tacCode").value("0086"));

        mockMvc.perform(delete("/api/pbx/trunk-distributes/{id}", rid)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [pbx] 局向删除成功");
    }

    /* ==================== 权限 ==================== */

    @Test
    @Order(6)
    void pbxApi_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/pbx/devices")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/pbx/trunks")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/pbx/routes")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/pbx/trunk-distributes")).andExpect(status().isUnauthorized());
        System.out.println(">>> [pbx] 未认证访问 PBX 接口全部 401");
    }
}
