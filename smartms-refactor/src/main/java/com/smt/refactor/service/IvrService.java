package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.entity.*;
import com.smt.refactor.mapper.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * IVR 管理服务（5 个配置表通用 CRUD）
 * 翻译自老系统 ivrmanager 包 4 个 Controller + sysmanager IvrSystemParamsController
 */
@Service
@DS("conf")
public class IvrService {

    private final ServiceinfoMapper serviceinfoMapper;
    private final TrunkgroupMapper trunkgroupMapper;
    private final FileinfoMapper fileinfoMapper;
    private final SystemparamMapper systemparamMapper;
    private final IvrrouteMapper ivrrouteMapper;

    public IvrService(ServiceinfoMapper serviceinfoMapper, TrunkgroupMapper trunkgroupMapper,
                      FileinfoMapper fileinfoMapper, SystemparamMapper systemparamMapper,
                      IvrrouteMapper ivrrouteMapper) {
        this.serviceinfoMapper = serviceinfoMapper;
        this.trunkgroupMapper = trunkgroupMapper;
        this.fileinfoMapper = fileinfoMapper;
        this.systemparamMapper = systemparamMapper;
        this.ivrrouteMapper = ivrrouteMapper;
    }

    /* ==================== 语音服务（cc_core_serviceinfo）==================== */

    public PageResult<Serviceinfo> servicePage(long pageNo, long pageSize) {
        return doPage(serviceinfoMapper, pageNo, pageSize);
    }

    public Serviceinfo serviceDetail(Integer rid) {
        return require(serviceinfoMapper.selectById(rid), "语音服务不存在");
    }

    public void serviceSave(Serviceinfo entity) {
        if (entity.getRid() == null) {
            serviceinfoMapper.insert(entity);
        } else {
            serviceinfoMapper.updateById(entity);
        }
    }

    public void serviceDelete(Integer rid) {
        serviceinfoMapper.deleteById(rid);
    }

    /* ==================== 中继群（cc_core_trunkgroup）==================== */

    public PageResult<Trunkgroup> trunkgroupPage(long pageNo, long pageSize) {
        return doPage(trunkgroupMapper, pageNo, pageSize);
    }

    public List<Trunkgroup> trunkgroupAll() {
        return trunkgroupMapper.selectList(new QueryWrapper<Trunkgroup>().orderByAsc("rid"));
    }

    public Trunkgroup trunkgroupDetail(Integer rid) {
        return require(trunkgroupMapper.selectById(rid), "中继群不存在");
    }

    public void trunkgroupSave(Trunkgroup entity) {
        if (entity.getRid() == null) {
            trunkgroupMapper.insert(entity);
        } else {
            trunkgroupMapper.updateById(entity);
        }
    }

    public void trunkgroupDelete(Integer rid) {
        trunkgroupMapper.deleteById(rid);
    }

    /* ==================== 语音文件管理（cc_core_fileinfo）==================== */

    public PageResult<Fileinfo> filePage(long pageNo, long pageSize, String fileType,
                                         String fileName, String fileDesc, String orderBy, String orderDir) {
        QueryWrapper<Fileinfo> qw = new QueryWrapper<>();
        if (fileType != null && !fileType.trim().isEmpty()) {
            qw.like("file_type", fileType.trim());
        }
        if (fileName != null && !fileName.trim().isEmpty()) {
            qw.like("file_name", fileName.trim());
        }
        if (fileDesc != null && !fileDesc.trim().isEmpty()) {
            qw.and(w -> w.like("file_desc", fileDesc.trim()).or().isNull("file_desc"));
        }
        // 老系统过滤 App.vxml 与 .bak 备份文件
        qw.ne("file_name", "App.vxml").notLike("file_name", "%.bak%");
        // 白名单排序
        String safeOrder = "file_name".equals(orderBy) || "file_type".equals(orderBy)
                || "create_date".equals(orderBy) ? orderBy : "file_name";
        String safeDir = "desc".equalsIgnoreCase(orderDir) ? "desc" : "asc";
        qw.orderBy(true, safeDir.equals("asc"), safeOrder);
        Page<Fileinfo> p = fileinfoMapper.selectPage(new Page<>(pageNo, pageSize), qw);
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    public Fileinfo fileDetail(Integer fileId) {
        return require(fileinfoMapper.selectById(fileId), "语音文件不存在");
    }

    public void fileSave(Fileinfo entity) {
        if (entity.getFileId() == null) {
            fileinfoMapper.insert(entity);
        } else {
            fileinfoMapper.updateById(entity);
        }
    }

    public void fileDelete(Integer fileId) {
        fileinfoMapper.deleteById(fileId);
    }

    public void fileBatchDelete(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            fileinfoMapper.deleteBatchIds(ids);
        }
    }

    /* ==================== IVR 参数（cc_core_systemparam）==================== */

    public PageResult<Systemparam> paramPage(long pageNo, long pageSize) {
        return doPage(systemparamMapper, pageNo, pageSize);
    }

    public Systemparam paramDetail(Integer rid) {
        return require(systemparamMapper.selectById(rid), "IVR 参数不存在");
    }

    public void paramSave(Systemparam entity) {
        if (entity.getRid() == null) {
            systemparamMapper.insert(entity);
        } else {
            systemparamMapper.updateById(entity);
        }
    }

    public void paramDelete(Integer rid) {
        systemparamMapper.deleteById(rid);
    }

    /* ==================== IVR 呼入路由（cc_core_ivrroute）==================== */

    public PageResult<Ivrroute> routePage(long pageNo, long pageSize) {
        return doPage(ivrrouteMapper, pageNo, pageSize);
    }

    public Ivrroute routeDetail(Integer trunksn) {
        return require(ivrrouteMapper.selectById(trunksn), "IVR 路由不存在");
    }

    public void routeSave(Ivrroute entity) {
        if (entity.getTrunksn() == null) {
            ivrrouteMapper.insert(entity);
        } else {
            ivrrouteMapper.updateById(entity);
        }
    }

    public void routeDelete(Integer trunksn) {
        ivrrouteMapper.deleteById(trunksn);
    }

    /* ==================== 通用 ==================== */

    private <T> PageResult<T> doPage(BaseMapper<T> mapper, long pageNo, long pageSize) {
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
