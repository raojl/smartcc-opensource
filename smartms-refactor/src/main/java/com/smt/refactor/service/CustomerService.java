package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.CustomQuery;
import com.smt.refactor.dto.CustomSaveRequest;
import com.smt.refactor.entity.Custominfo;
import com.smt.refactor.mapper.CustominfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 客户管理服务（crm_custominfo 业务库）
 * 翻译自老系统 CustomInfoController（custominfo 包）
 */
@Service
@RequiredArgsConstructor
@DS("biz")
public class CustomerService {

    private final CustominfoMapper custominfoMapper;

    /**
     * 客户分页（老系统 index / myCustom / viewIsDeleteCustom）
     * @param myAgentId 当前登录坐席工号（agentId="my" 时使用，精确匹配）
     */
    public PageResult<Custominfo> page(CustomQuery q, String myAgentId) {
        QueryWrapper<Custominfo> qw = new QueryWrapper<>();
        String agentId = q.getAgentId();
        boolean my = "my".equals(agentId);
        if (my) {
            agentId = myAgentId;
        }
        if (StringUtils.hasText(q.getCustomName())) {
            qw.like("custom_name", q.getCustomName().trim());
        }
        if (StringUtils.hasText(q.getPhoneNumber())) {
            qw.like("custom_mobile", q.getPhoneNumber().trim());
        }
        if (StringUtils.hasText(q.getCompanyName())) {
            qw.like("company_name", q.getCompanyName().trim());
        }
        if (StringUtils.hasText(q.getQqNumber())) {
            qw.like("qq_number", q.getQqNumber().trim());
        }
        if (StringUtils.hasText(q.getPeopleSex())) {
            qw.like("custom_sex", q.getPeopleSex().trim());
        }
        if (StringUtils.hasText(agentId)) {
            if (my) {
                qw.eq("agentid", agentId.trim());
            } else {
                qw.like("agentid", agentId.trim());
            }
        }
        qw.eq("is_delete", q.isDeleted() ? 1 : 0);
        // 白名单排序
        String safeOrder = "custom_name".equals(q.getOrderBy()) || "update_time".equals(q.getOrderBy())
                || "custom_mobile".equals(q.getOrderBy()) ? q.getOrderBy() : "update_time";
        String safeDir = "asc".equalsIgnoreCase(q.getOrderDir()) ? "asc" : "desc";
        qw.orderBy(true, safeDir.equals("asc"), safeOrder);
        Page<Custominfo> p = custominfoMapper.selectPage(new Page<>(q.getPage(), q.getSize()), qw);
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    /**
     * 客户详情
     */
    public Custominfo detail(String customId) {
        Custominfo c = custominfoMapper.selectById(customId);
        if (c == null || (c.getIsDelete() != null && c.getIsDelete() == 1)) {
            throw new BusinessException("客户不存在");
        }
        return c;
    }

    /**
     * 新增/编辑（老系统 saveOrupdate：custom_id 空=新增 UUID 主键，否则更新）
     */
    @Transactional(rollbackFor = Exception.class)
    public String save(CustomSaveRequest req) {
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Custominfo c = new Custominfo();
        c.setCustomName(req.getCustomName());
        c.setCustomSex(req.getCustomSex());
        c.setCompanyName(req.getCompanyName());
        c.setCustType(req.getCustType());
        c.setCustomLevel(req.getCustomLevel() == null ? 0 : req.getCustomLevel().shortValue());
        c.setCustomMobile(req.getCustomMobile());
        c.setCustomPhone(req.getCustomPhone());
        c.setCustomPhone2(req.getCustomPhone2());
        c.setQqNumber(req.getQqNumber());
        c.setEmailAddr(req.getEmailAddr());
        c.setCustomCity(req.getCustomCity());
        c.setCustomAddr(req.getCustomAddr());
        c.setCustomDesc(req.getCustomDesc());
        c.setCustomDesc1(req.getCustomDesc1());
        c.setAgentid(req.getAgentid());
        if (StringUtils.hasText(req.getCustomId())) {
            c.setCustomId(req.getCustomId());
            c.setUpdateTime(now);
            custominfoMapper.updateById(c);
            return c.getCustomId();
        } else {
            String id = UUID.randomUUID().toString();
            c.setCustomId(id);
            c.setUpdateTime(now);
            c.setIsDelete((short) 0);
            custominfoMapper.insert(c);
            return id;
        }
    }

    /**
     * 删除/恢复（老系统 delete：status=0 软删 is_delete=1，status=1 恢复）
     */
    public void changeDelete(String customId, int status) {
        Custominfo c = custominfoMapper.selectById(customId);
        if (c == null) {
            throw new BusinessException("客户不存在");
        }
        c.setIsDelete((short) status);
        custominfoMapper.updateById(c);
    }

    /**
     * 弹屏客户查询（老系统 custinfoPanel 的查询顺序：custom_id → openid → 手机号）
     */
    public Custominfo findForDial(String customId, String customOpenId, String callNumber) {
        if (StringUtils.hasText(customId)) {
            Custominfo c = custominfoMapper.selectById(customId);
            if (c != null && (c.getIsDelete() == null || c.getIsDelete() == 0)) {
                return c;
            }
        }
        if (StringUtils.hasText(callNumber)) {
            Custominfo c = custominfoMapper.selectOne(new QueryWrapper<Custominfo>()
                    .and(w -> w.eq("custom_mobile", callNumber.trim()).or().eq("custom_phone", callNumber.trim()))
                    .eq("is_delete", 0)
                    .last("limit 1"));
            if (c != null) {
                return c;
            }
        }
        return null;
    }
}
