package com.smt.refactor.controller;

import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.OrderQuery;
import com.smt.refactor.dto.OrderSaveRequest;
import com.smt.refactor.dto.ProductSaveRequest;
import com.smt.refactor.entity.Orderinfo;
import com.smt.refactor.entity.Productsinfo;
import com.smt.refactor.service.OrderService;
import com.smt.refactor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 订单管理接口（翻译自老系统 /orderinfo + /productsinfo）
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    private String myAgentCode(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Integer)) {
            throw new BusinessException("未登录");
        }
        return userService.agentCodeOf((Integer) auth.getPrincipal());
    }

    /* ==================== 订单 ==================== */

    /** 订单分页（agentId="my"=我的订单） */
    @GetMapping
    public PageResult<Orderinfo> orderPage(OrderQuery query, Authentication auth) {
        return orderService.orderPage(query, myAgentCode(auth));
    }

    /** 订单详情 */
    @GetMapping("/{id}")
    public Orderinfo orderDetail(@PathVariable Long id) {
        return orderService.orderDetail(id);
    }

    /** 新增/编辑 */
    @PostMapping
    public Map<String, Long> orderSave(@Valid @RequestBody OrderSaveRequest req) {
        Long id = orderService.orderSave(req);
        return Collections.singletonMap("id", id);
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public void orderDelete(@PathVariable Long id) {
        orderService.orderDelete(id);
    }

    /** 客户订单列表（弹屏工作台用） */
    @GetMapping("/by-custom/{customId}")
    public PageResult<Orderinfo> orderByCustom(@PathVariable String customId,
                                               @RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size) {
        return orderService.orderByCustom(customId, page, size);
    }

    /* ==================== 产品 ==================== */

    /** 产品分页 */
    @GetMapping("/products")
    public PageResult<Productsinfo> productPage(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "15") long size,
                                                @RequestParam(required = false) String productName,
                                                @RequestParam(required = false) String productId,
                                                @RequestParam(required = false) String productType) {
        return orderService.productPage(page, size, productName, productId, productType);
    }

    /** 全部产品（订单下拉用） */
    @GetMapping("/products/all")
    public List<Productsinfo> productAll() {
        return orderService.productAll();
    }

    /** 产品详情 */
    @GetMapping("/products/{id}")
    public Productsinfo productDetail(@PathVariable Long id) {
        return orderService.productDetail(id);
    }

    /** 产品新增/编辑 */
    @PostMapping("/products")
    public Map<String, Long> productSave(@Valid @RequestBody ProductSaveRequest req) {
        Long id = orderService.productSave(req);
        return Collections.singletonMap("id", id);
    }

    /** 产品删除 */
    @DeleteMapping("/products/{id}")
    public void productDelete(@PathVariable Long id) {
        orderService.productDelete(id);
    }
}
