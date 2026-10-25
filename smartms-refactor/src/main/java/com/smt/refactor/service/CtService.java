package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.entity.*;
import com.smt.refactor.mapper.*;
import org.springframework.stereotype.Service;

/**
 * CTI 管理服务（5 个配置表通用 CRUD）
 * 翻译自老系统 ctimanager 包 5 个 Controller（LinkInfo/ExtensionDN/RecordPath/Skill）+ sysmanager CtiSystemParams
 */
@Service
@DS("conf")
public class CtService {

    private final LinkinfoMapper linkinfoMapper;
    private final ExtensionMapper extensionMapper;
    private final ProfileMapper profileMapper;
    private final RecordmemberMapper recordmemberMapper;
    private final SkillMapper skillMapper;

    public CtService(LinkinfoMapper linkinfoMapper, ExtensionMapper extensionMapper,
                     ProfileMapper profileMapper, RecordmemberMapper recordmemberMapper,
                     SkillMapper skillMapper) {
        this.linkinfoMapper = linkinfoMapper;
        this.extensionMapper = extensionMapper;
        this.profileMapper = profileMapper;
        this.recordmemberMapper = recordmemberMapper;
        this.skillMapper = skillMapper;
    }

    /* ==================== 链路配置（cc_core_linkinfo）==================== */

    public PageResult<Linkinfo> linkPage(long pageNo, long pageSize) {
        return doPage(linkinfoMapper, pageNo, pageSize);
    }

    public Linkinfo linkDetail(Integer rid) {
        return require(linkinfoMapper.selectById(rid), "链路不存在");
    }

    public void linkSave(Linkinfo entity) {
        if (entity.getRid() == null) {
            linkinfoMapper.insert(entity);
        } else {
            linkinfoMapper.updateById(entity);
        }
    }

    public void linkDelete(Integer rid) {
        linkinfoMapper.deleteById(rid);
    }

    /* ==================== 号码配置（cc_core_extension）==================== */

    public PageResult<Extension> extenPage(long pageNo, long pageSize) {
        return doPage(extensionMapper, pageNo, pageSize);
    }

    public Extension extenDetail(Integer rid) {
        return require(extensionMapper.selectById(rid), "号码不存在");
    }

    public void extenSave(Extension entity) {
        if (entity.getRid() == null) {
            extensionMapper.insert(entity);
        } else {
            extensionMapper.updateById(entity);
        }
    }

    public void extenDelete(Integer rid) {
        extensionMapper.deleteById(rid);
    }

    /* ==================== CTI 参数（cc_core_profile）==================== */

    public PageResult<Profile> profilePage(long pageNo, long pageSize) {
        return doPage(profileMapper, pageNo, pageSize);
    }

    public Profile profileDetail(Integer rid) {
        return require(profileMapper.selectById(rid), "参数不存在");
    }

    public void profileSave(Profile entity) {
        if (entity.getRid() == null) {
            profileMapper.insert(entity);
        } else {
            profileMapper.updateById(entity);
        }
    }

    public void profileDelete(Integer rid) {
        profileMapper.deleteById(rid);
    }

    /* ==================== 录音配置（cc_core_recordmember）==================== */

    public PageResult<Recordmember> recordPage(long pageNo, long pageSize) {
        return doPage(recordmemberMapper, pageNo, pageSize);
    }

    public Recordmember recordDetail(Integer rid) {
        return require(recordmemberMapper.selectById(rid), "录音配置不存在");
    }

    public void recordSave(Recordmember entity) {
        if (entity.getRid() == null) {
            recordmemberMapper.insert(entity);
        } else {
            recordmemberMapper.updateById(entity);
        }
    }

    public void recordDelete(Integer rid) {
        recordmemberMapper.deleteById(rid);
    }

    /* ==================== 技能组配置（cc_core_skill）==================== */

    public PageResult<Skill> skillPage(long pageNo, long pageSize, String keyword) {
        QueryWrapper<Skill> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            qw.and(w -> w.like("skill_name", keyword.trim())
                    .or().like("skill_desc", keyword.trim()));
        }
        qw.orderByAsc("rid");
        Page<Skill> p = skillMapper.selectPage(new Page<>(pageNo, pageSize), qw);
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    public Skill skillDetail(Integer rid) {
        return require(skillMapper.selectById(rid), "技能组不存在");
    }

    public void skillSave(Skill entity) {
        if (entity.getRid() == null) {
            skillMapper.insert(entity);
        } else {
            skillMapper.updateById(entity);
        }
    }

    public void skillDelete(Integer rid) {
        skillMapper.deleteById(rid);
    }

    /* ==================== 通用 ==================== */

    private <T> PageResult<T> doPage(com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper,
                                     long pageNo, long pageSize) {
        Page<T> p = mapper.selectPage(new Page<>(pageNo, pageSize), new QueryWrapper<T>());
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    private <T> T require(T entity, String msg) {
        if (entity == null) {
            throw new BusinessException(msg);
        }
        return entity;
    }
}
