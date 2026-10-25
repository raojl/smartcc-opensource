package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.DeviceSaveRequest;
import com.smt.refactor.entity.Device;
import com.smt.refactor.mapper.DeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * SIP 分机服务（pbx_device）
 * 翻译自老系统 SipInfoMgrController
 */
@Service
@RequiredArgsConstructor
@DS("conf")
public class DeviceService {

    private final DeviceMapper deviceMapper;

    /**
     * 分页查询（id/cid 模糊，可排序，与老系统 index 一致）
     */
    public PageResult<Device> page(long pageNo, long pageSize, String id, String cid,
                                   String orderBy, String orderDir) {
        QueryWrapper<Device> qw = new QueryWrapper<>();
        if (StringUtils.hasText(id)) {
            qw.like("id", id.trim());
        }
        if (StringUtils.hasText(cid)) {
            qw.like("cid", cid.trim());
        }
        // 白名单排序字段，防 SQL 注入（老系统直接拼 orderBy）
        String safeOrder = "id".equals(orderBy) || "cid".equals(orderBy) ? orderBy : "id";
        String safeDir = "desc".equalsIgnoreCase(orderDir) ? "desc" : "asc";
        qw.orderBy(true, safeDir.equals("asc"), safeOrder);
        Page<Device> p = deviceMapper.selectPage(new Page<>(pageNo, pageSize), qw);
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    /**
     * 详情
     */
    public Device detail(String id) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BusinessException("分机不存在");
        }
        return device;
    }

    /**
     * 新增（老系统 save）
     */
    public void create(DeviceSaveRequest req) {
        if (StringUtils.hasText(req.getId()) && deviceMapper.selectById(req.getId()) != null) {
            throw new BusinessException("分机号 " + req.getId() + " 已存在");
        }
        Device device = toEntity(req);
        deviceMapper.insert(device);
    }

    /**
     * 编辑（老系统 update 仅更新 cid，这里更新全字段更合理）
     */
    public void update(String id, DeviceSaveRequest req) {
        if (deviceMapper.selectById(id) == null) {
            throw new BusinessException("分机不存在");
        }
        if (req.getId() != null && !req.getId().equals(id)
                && deviceMapper.selectById(req.getId()) != null) {
            throw new BusinessException("分机号 " + req.getId() + " 已存在");
        }
        Device device = toEntity(req);
        device.setId(id);
        deviceMapper.updateById(device);
    }

    /**
     * 删除
     */
    public void delete(String id) {
        if (deviceMapper.selectById(id) == null) {
            throw new BusinessException("分机不存在");
        }
        deviceMapper.deleteById(id);
    }

    /**
     * 批量删除（老系统 mdelete）
     */
    public void batchDelete(List<String> ids) {
        if (ids != null && !ids.isEmpty()) {
            deviceMapper.deleteBatchIds(ids);
        }
    }

    /**
     * 分机号唯一性校验（老系统 checkSipDevice）
     */
    public boolean isIdAvailable(String id) {
        return StringUtils.hasText(id) && deviceMapper.selectById(id) == null;
    }

    private Device toEntity(DeviceSaveRequest req) {
        Device d = new Device();
        d.setId(req.getId());
        d.setCid(req.getCid());
        d.setSecret(req.getSecret());
        d.setType(req.getType());
        d.setPort(req.getPort());
        d.setDtmfmode(req.getDtmfmode());
        d.setDisallow(req.getDisallow());
        d.setAllow(req.getAllow());
        d.setNat(req.getNat());
        d.setCanreinvite(req.getCanreinvite());
        d.setContext(req.getContext());
        d.setBindIpaddr(req.getBindIpaddr());
        d.setCallerid(req.getCallerid());
        d.setUsername(req.getUsername());
        d.setHost(req.getHost());
        return d;
    }
}
