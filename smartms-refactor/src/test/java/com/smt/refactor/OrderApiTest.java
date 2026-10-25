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
 * 阶段 2 订单管理测试：订单 CRUD + 产品 CRUD + 我的订单 + 客户订单
 * 测试数据 no/productName 用 t_ 前缀，测试后清理
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static Long orderId = null;
    private static Long productId = null;
    private static final String TEST_NO = "torder_" + System.currentTimeMillis() % 1000000;
    private static final String TEST_PRODUCT = "tprod_" + System.currentTimeMillis() % 100000;

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
    void product_shouldCRUD() throws Exception {
        String token = adminToken();
        String body = "{\"productId\":\"" + TEST_PRODUCT + "\",\"productName\":\"测试产品\",\"productUnitPrice\":99.9,\"productType\":\"1\"}";
        MvcResult result = mockMvc.perform(post("/api/orders/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json;charset=UTF-8")
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();
        productId = objectMapper.readTree(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("id").asLong();
        System.out.println(">>> [order] 新增产品 " + TEST_PRODUCT + " -> id=" + productId);
        // 详情
        mockMvc.perform(get("/api/orders/products/{id}", productId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.productName").value("测试产品"))
                .andExpect(jsonPath("$.productUnitPrice").value(99.9));
        // 搜索
        MvcResult search = mockMvc.perform(get("/api/orders/products").param("productName", "测试产品")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(">>> [order] 产品搜索 total=" + objectMapper.readTree(
                search.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("total").asLong());
    }

    @Test
    @Order(2)
    void order_shouldCreate() throws Exception {
        String token = adminToken();
        String body = "{\"status\":\"1\",\"no\":\"" + TEST_NO + "\",\"name\":\"先生\",\"mobile\":\"15958006124\","
                + "\"product\":\"" + TEST_PRODUCT + "\",\"amount\":199.9,\"quantity\":2,\"agentid\":\"3007\","
                + "\"agent\":\"测试\",\"type\":\"1\",\"media\":\"1\",\"desc\":\"t\"}";
        MvcResult result = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json;charset=UTF-8")
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();
        orderId = objectMapper.readTree(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("id").asLong();
        System.out.println(">>> [order] 新增订单 " + TEST_NO + " -> id=" + orderId);
        // 详情
        mockMvc.perform(get("/api/orders/{id}", orderId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.no").value(TEST_NO))
                .andExpect(jsonPath("$.amount").value(199.9));
    }

    @Test
    @Order(3)
    void order_shouldPageFilter() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(get("/api/orders")
                        .param("no", TEST_NO).param("size", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        long total = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("total").asLong();
        org.junit.jupiter.api.Assertions.assertTrue(total >= 1, "按单号搜索应命中");
        System.out.println(">>> [order] 订单搜索命中 " + total + " 条");
        // 全部订单
        MvcResult all = mockMvc.perform(get("/api/orders").param("size", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(">>> [order] 全部订单 total=" + objectMapper.readTree(
                all.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("total").asLong());
    }

    @Test
    @Order(4)
    void order_shouldUpdate() throws Exception {
        String token = adminToken();
        String body = "{\"id\":" + orderId + ",\"status\":\"2\",\"no\":\"" + TEST_NO + "\",\"name\":\"先生改\","
                + "\"product\":\"" + TEST_PRODUCT + "\",\"amount\":299.9,\"quantity\":3,\"agentid\":\"3007\",\"desc\":\"t\"}";
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json;charset=UTF-8")
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/orders/{id}", orderId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.status").value("2"))
                .andExpect(jsonPath("$.amount").value(299.9));
        System.out.println(">>> [order] 订单编辑成功");
    }

    @Test
    @Order(5)
    void orderByCustom_shouldReturnPanelOrders() throws Exception {
        String token = adminToken();
        // 用真实客户 custom_id 查询其订单
        MvcResult cust = mockMvc.perform(get("/api/customers/dial-search")
                        .param("callNumber", "15958006124")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String customId = objectMapper.readTree(cust.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("customId").asText();
        MvcResult result = mockMvc.perform(get("/api/orders/by-custom/{customId}", customId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(">>> [order] 客户订单面板 total=" + objectMapper.readTree(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("total").asLong());
    }

    @Test
    @Order(6)
    void myOrder_shouldNotBreak() throws Exception {
        String token = adminToken();
        // admin 无工号，my 过滤返回空但不报错
        mockMvc.perform(get("/api/orders").param("agentId", "my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        System.out.println(">>> [order] 我的订单接口正常");
    }

    @Test
    @Order(7)
    void cleanup_shouldDeleteTestData() throws Exception {
        if (orderId != null) {
            java.sql.DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/smartcc?characterEncoding=utf8&useSSL=false",
                            "root", "")
                    .createStatement()
                    .executeUpdate("DELETE FROM crm_orderinfo WHERE id = " + orderId);
        }
        if (productId != null) {
            java.sql.DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/smartcc?characterEncoding=utf8&useSSL=false",
                            "root", "")
                    .createStatement()
                    .executeUpdate("DELETE FROM crm_productsinfo WHERE id = " + productId);
        }
        System.out.println(">>> [order] 测试数据已清理");
    }

    @Test
    @Order(8)
    void orderApi_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/orders")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/orders/products")).andExpect(status().isUnauthorized());
        System.out.println(">>> [order] 未认证访问订单接口全部 401");
    }
}
