package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.OrderQuery;
import com.smt.refactor.dto.OrderSaveRequest;
import com.smt.refactor.dto.ProductSaveRequest;
import com.smt.refactor.entity.Orderinfo;
import com.smt.refactor.entity.Productsinfo;
import com.smt.refactor.mapper.OrderinfoMapper;
import com.smt.refactor.mapper.ProductsinfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 订单管理服务（crm_orderinfo + crm_productsinfo 业务库）
 * 翻译自老系统 OrderInfoController + ProductsManagerController
 */
@Service
@RequiredArgsConstructor
@DS("biz")
public class OrderService {

    private final OrderinfoMapper orderinfoMapper;
    private final ProductsinfoMapper productsinfoMapper;

    /**
     * 订单分页（老系统 index/myOrder）
     * @param myAgentId 当前坐席工号（agentId="my" 时使用，精确匹配）
     */
    public PageResult<Orderinfo> orderPage(OrderQuery q, String myAgentId) {
        QueryWrapper<Orderinfo> qw = new QueryWrapper<>();
        String agentId = q.getAgentId();
        boolean my = "my".equals(agentId);
        if (my) {
            agentId = myAgentId;
        }
        if (StringUtils.hasText(q.getNo())) {
            qw.like("no", q.getNo().trim());
        }
        if (StringUtils.hasText(q.getName())) {
            qw.like("name", q.getName().trim());
        }
        if (StringUtils.hasText(q.getProduct())) {
            qw.like("product", q.getProduct().trim());
        }
        String status = q.getStatus();
        if (StringUtils.hasText(status) && !"0".equals(status.trim())) {
            qw.like("status", status.trim());
        }
        if (StringUtils.hasText(agentId)) {
            if (my) {
                qw.eq("agentid", agentId.trim());
            } else {
                qw.like("agentid", agentId.trim());
            }
        }
        // 时间范围（默认 7 天前 ~ 今天+7，格式 yyyyMMddHHmmss）
        String start = StringUtils.hasText(q.getStartTime()) ? q.getStartTime() : defaultDay(-7) + "000000";
        String end = StringUtils.hasText(q.getEndTime()) ? q.getEndTime() : defaultDay(7) + "235959";
        qw.gt("createtime", start).lt("createtime", end);
        qw.orderByDesc("createtime");
        Page<Orderinfo> p = orderinfoMapper.selectPage(new Page<>(q.getPage(), q.getSize()), qw);
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    /**
     * 订单详情
     */
    public Orderinfo orderDetail(Long id) {
        Orderinfo o = orderinfoMapper.selectById(id);
        if (o == null) {
            throw new BusinessException("订单不存在");
        }
        return o;
    }

    /**
     * 订单新增/编辑（老系统 saveOrupdate：id 空=新增，createtime=yyyyMMddHHmmss）
     */
    @Transactional(rollbackFor = Exception.class)
    public Long orderSave(OrderSaveRequest req) {
        String now = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Orderinfo o = new Orderinfo();
        o.setStatus(req.getStatus());
        o.setNo(req.getNo());
        o.setCustomId(req.getCustomId());
        o.setName(req.getName());
        o.setMobile(req.getMobile());
        o.setContact(req.getContact());
        o.setPhone(req.getPhone());
        o.setType(req.getType());
        o.setMedia(req.getMedia());
        o.setSendType(req.getSendType());
        o.setPayType(req.getPayType());
        o.setBank(req.getBank());
        o.setAccount(req.getAccount());
        o.setAmount(req.getAmount());
        o.setProduct(req.getProduct());
        o.setQuantity(req.getQuantity());
        o.setAgentid(req.getAgentid());
        o.setAgent(req.getAgent());
        o.setDesc(req.getDesc());
        if (req.getId() != null) {
            o.setId(req.getId());
            orderinfoMapper.updateById(o);
            return req.getId();
        } else {
            o.setCreatetime(now);
            orderinfoMapper.insert(o);
            return o.getId();
        }
    }

    /**
     * 订单删除
     */
    public void orderDelete(Long id) {
        if (orderinfoMapper.selectById(id) == null) {
            throw new BusinessException("订单不存在");
        }
        orderinfoMapper.deleteById(id);
    }

    /**
     * 客户订单列表（老系统 orderlistPanel，弹屏用）
     */
    public PageResult<Orderinfo> orderByCustom(String customId, long page, long size) {
        QueryWrapper<Orderinfo> qw = new QueryWrapper<Orderinfo>()
                .eq("custom_id", customId).orderByDesc("createtime");
        Page<Orderinfo> p = orderinfoMapper.selectPage(new Page<>(page, size), qw);
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    /* ==================== 产品 ==================== */

    public PageResult<Productsinfo> productPage(long pageNo, long pageSize, String productName,
                                                String productId, String productType) {
        QueryWrapper<Productsinfo> qw = new QueryWrapper<>();
        if (StringUtils.hasText(productName)) {
            qw.like("product_name", productName.trim());
        }
        if (StringUtils.hasText(productId)) {
            qw.like("product_id", productId.trim());
        }
        if (StringUtils.hasText(productType)) {
            qw.eq("product_type", productType.trim());
        }
        qw.orderByAsc("id");
        Page<Productsinfo> p = productsinfoMapper.selectPage(new Page<>(pageNo, pageSize), qw);
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    public Productsinfo productDetail(Long id) {
        Productsinfo p = productsinfoMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("产品不存在");
        }
        return p;
    }

    /** 全部产品（订单下拉用） */
    public List<Productsinfo> productAll() {
        return productsinfoMapper.selectList(new QueryWrapper<Productsinfo>().orderByAsc("id"));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long productSave(ProductSaveRequest req) {
        Productsinfo p = new Productsinfo();
        p.setProductId(req.getProductId());
        p.setProductName(req.getProductName());
        p.setProductUnitPrice(req.getProductUnitPrice());
        p.setProductType(req.getProductType());
        if (req.getId() != null) {
            p.setId(req.getId());
            productsinfoMapper.updateById(p);
            return req.getId();
        } else {
            productsinfoMapper.insert(p);
            return p.getId();
        }
    }

    public void productDelete(Long id) {
        if (productsinfoMapper.selectById(id) == null) {
            throw new BusinessException("产品不存在");
        }
        productsinfoMapper.deleteById(id);
    }

    private String defaultDay(int offset) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DATE, offset);
        return new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
    }
}
