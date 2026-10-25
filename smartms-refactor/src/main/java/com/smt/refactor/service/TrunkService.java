package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.TrunkSaveRequest;
import com.smt.refactor.entity.Trunk;
import com.smt.refactor.mapper.TrunkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 局向分配服务（cc_core_trunk）
 * 翻译自老系统 ExtensionDistributeInfoController
 */
@Service
@RequiredArgsConstructor
@DS("conf")
public class TrunkService {

    private final TrunkMapper trunkMapper;

    public PageResult<Trunk> page(long pageNo, long pageSize) {
        Page<Trunk> p = trunkMapper.selectPage(new Page<>(pageNo, pageSize),
                new QueryWrapper<Trunk>().orderByAsc("rid"));
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    public Trunk detail(Integer rid) {
        Trunk trunk = trunkMapper.selectById(rid);
        if (trunk == null) {
            throw new BusinessException("局向不存在");
        }
        return trunk;
    }

    public Integer create(TrunkSaveRequest req) {
        Trunk trunk = toEntity(req);
        trunkMapper.insert(trunk);
        return trunk.getRid();
    }

    public void update(Integer rid, TrunkSaveRequest req) {
        if (trunkMapper.selectById(rid) == null) {
            throw new BusinessException("局向不存在");
        }
        Trunk trunk = toEntity(req);
        trunk.setRid(rid);
        trunkMapper.updateById(trunk);
    }

    public void delete(Integer rid) {
        if (trunkMapper.selectById(rid) == null) {
            throw new BusinessException("局向不存在");
        }
        trunkMapper.deleteById(rid);
    }

    private Trunk toEntity(TrunkSaveRequest req) {
        Trunk t = new Trunk();
        t.setTrunkId(req.getTrunkId());
        t.setTrunkName(req.getTrunkName());
        t.setTrunkType(req.getTrunkType());
        t.setTacCode(req.getTacCode());
        t.setCircuitCode(req.getCircuitCode());
        t.setExtnDn(req.getExtnDn());
        t.setTrunkDirection(req.getTrunkDirection());
        return t;
    }
}
