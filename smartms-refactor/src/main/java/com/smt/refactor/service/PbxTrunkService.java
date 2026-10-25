package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.PbxTrunkSaveRequest;
import com.smt.refactor.entity.PbxTrunk;
import com.smt.refactor.mapper.PbxTrunkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PBX 出局中继服务（pbx_trunk）
 * 翻译自老系统 TrunkController
 */
@Service
@RequiredArgsConstructor
@DS("conf")
public class PbxTrunkService {

    private final PbxTrunkMapper pbxTrunkMapper;

    public PageResult<PbxTrunk> page(long pageNo, long pageSize) {
        Page<PbxTrunk> p = pbxTrunkMapper.selectPage(new Page<>(pageNo, pageSize),
                new QueryWrapper<PbxTrunk>().orderByAsc("id"));
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    /** 全部中继（路由绑定下拉用） */
    public List<PbxTrunk> all() {
        return pbxTrunkMapper.selectList(new QueryWrapper<PbxTrunk>().orderByAsc("id"));
    }

    public PbxTrunk detail(Integer id) {
        PbxTrunk trunk = pbxTrunkMapper.selectById(id);
        if (trunk == null) {
            throw new BusinessException("中继不存在");
        }
        return trunk;
    }

    public Integer create(PbxTrunkSaveRequest req) {
        PbxTrunk trunk = toEntity(req);
        pbxTrunkMapper.insert(trunk);
        return trunk.getId();
    }

    public void update(Integer id, PbxTrunkSaveRequest req) {
        if (pbxTrunkMapper.selectById(id) == null) {
            throw new BusinessException("中继不存在");
        }
        PbxTrunk trunk = toEntity(req);
        trunk.setId(id);
        pbxTrunkMapper.updateById(trunk);
    }

    public void delete(Integer id) {
        if (pbxTrunkMapper.selectById(id) == null) {
            throw new BusinessException("中继不存在");
        }
        pbxTrunkMapper.deleteById(id);
    }

    private PbxTrunk toEntity(PbxTrunkSaveRequest req) {
        PbxTrunk t = new PbxTrunk();
        t.setName(req.getName());
        t.setType(req.getType());
        t.setCid(req.getCid());
        t.setPrefix(req.getPrefix());
        t.setKeepcid(req.getKeepcid());
        t.setMaxchans(req.getMaxchans());
        t.setFail(req.getFail());
        t.setDisable(req.getDisable());
        t.setSipAccount(req.getSipAccount());
        t.setSipHost(req.getSipHost());
        t.setSipPort(req.getSipPort());
        t.setSipUsername(req.getSipUsername());
        t.setSipSecret(req.getSipSecret());
        t.setSipType(req.getSipType());
        t.setSipContext(req.getSipContext());
        t.setSipNat(req.getSipNat());
        t.setCallerchange(req.getCallerchange());
        return t;
    }
}
