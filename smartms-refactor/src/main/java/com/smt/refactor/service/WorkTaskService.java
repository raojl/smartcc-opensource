package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.WorkTaskQuery;
import com.smt.refactor.dto.WorkTaskSaveRequest;
import com.smt.refactor.dto.WorkTaskVO;
import com.smt.refactor.entity.Worktaskinfo;
import com.smt.refactor.mapper.WorktaskinfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工单服务（crm_worktaskinfo，业务库）
 * 翻译自老系统 WorkTaskController（worktaskinfo 包）
 */
@Service
@RequiredArgsConstructor
@DS("biz")
public class WorkTaskService {

    private final WorktaskinfoMapper worktaskinfoMapper;
    private final com.smt.refactor.mapper.CustominfoMapper custominfoMapper;
    private final com.smt.refactor.mapper.SysUserMapper sysUserMapper;

    /**
     * 工单分页查询（老系统 index，含客户名/手机号关联）
     * @param myUserId 当前登录用户 ID（belongAgent/workAgent 为 "my" 时查其坐席工号）
     */
    public PageResult<WorkTaskVO> page(WorkTaskQuery q, Integer myUserId) {
        String myAgentCode = resolveAgentCodeManual(myUserId);
        QueryWrapper<Worktaskinfo> qw = new QueryWrapper<>();
        if (StringUtils.hasText(q.getCustomName())) {
            // 通过客户名查 custom_id（老系统是 left join，这里等价转换）
            List<String> customIds = custominfoMapper.selectList(
                            new QueryWrapper<com.smt.refactor.entity.Custominfo>()
                                    .like("custom_name", q.getCustomName().trim())
                                    .select("custom_id"))
                    .stream().map(com.smt.refactor.entity.Custominfo::getCustomId)
                    .collect(Collectors.toList());
            if (customIds.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), 0, q.getPage(), q.getSize(), 0);
            }
            qw.in("custom_id", customIds);
        }
        if (StringUtils.hasText(q.getWorkId())) {
            qw.like("work_id", q.getWorkId().trim());
        }
        String status = q.getWorkStatus();
        if (StringUtils.hasText(status) && !"0".equals(status.trim())) {
            qw.like("work_status", status.trim());
        }
        String belongAgent = q.getBelongAgent();
        if ("my".equals(belongAgent)) {
            belongAgent = myAgentCode;
        }
        if (StringUtils.hasText(belongAgent)) {
            qw.like("belong_agent", belongAgent.trim());
        }
        String workAgent = q.getWorkAgent();
        if ("my".equals(workAgent)) {
            workAgent = myAgentCode;
        }
        if (StringUtils.hasText(workAgent)) {
            qw.like("work_agent", workAgent.trim());
        }
        // 时间范围（默认昨天 ~ 今天+2）
        String start = StringUtils.hasText(q.getStartTime()) ? q.getStartTime() : defaultDay(-1);
        String end = StringUtils.hasText(q.getEndTime()) ? q.getEndTime() : defaultDay(2);
        qw.ge("create_time", start + " 00:00:00").le("create_time", end + " 24:00:00");
        qw.orderByDesc("create_time");

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Worktaskinfo> p =
                worktaskinfoMapper.selectPage(
                        new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(q.getPage(), q.getSize()), qw);

        // 客户手机号 map（老系统循环匹配，这里批量查询）
        List<String> customIds = p.getRecords().stream()
                .map(Worktaskinfo::getCustomId).filter(StringUtils::hasText).distinct()
                .collect(Collectors.toList());
        Map<String, String> mobileMap = new HashMap<>();
        Map<String, String> nameMap = new HashMap<>();
        if (!customIds.isEmpty()) {
            custominfoMapper.selectList(new QueryWrapper<com.smt.refactor.entity.Custominfo>()
                            .in("custom_id", customIds))
                    .forEach(c -> {
                        mobileMap.put(c.getCustomId(), c.getCustomMobile());
                        nameMap.put(c.getCustomId(), c.getCustomName());
                    });
        }
        List<WorkTaskVO> list = p.getRecords().stream()
                .map(w -> toVO(w, nameMap.get(w.getCustomId()), mobileMap.get(w.getCustomId())))
                .collect(Collectors.toList());
        return new PageResult<>(list, p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    /**
     * 工单详情（老系统 view，含客户名）
     */
    public WorkTaskVO detail(String workId) {
        Worktaskinfo w = worktaskinfoMapper.selectById(workId);
        if (w == null) {
            throw new BusinessException("工单不存在");
        }
        com.smt.refactor.entity.Custominfo c = null;
        if (StringUtils.hasText(w.getCustomId())) {
            c = custominfoMapper.selectById(w.getCustomId());
        }
        return toVO(w, c != null ? c.getCustomName() : null, c != null ? c.getCustomMobile() : null);
    }

    /**
     * 新增/更新（老系统 saveOrupdate，option=create/update）
     */
    @Transactional(rollbackFor = Exception.class)
    public String save(WorkTaskSaveRequest req, String option) {
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Worktaskinfo work = new Worktaskinfo();
        work.setWorkId(req.getWorkId());
        // custom_name 存在时反查 custom_id（老系统 saveOrupdate 逻辑）
        String customId = req.getCustomId();
        if (!StringUtils.hasText(customId) && StringUtils.hasText(req.getCustomName())) {
            com.smt.refactor.entity.Custominfo c = custominfoMapper.selectOne(
                    new QueryWrapper<com.smt.refactor.entity.Custominfo>()
                            .eq("custom_name", req.getCustomName()).last("limit 1"));
            if (c != null) {
                customId = c.getCustomId();
            }
        }
        work.setCustomId(customId);
        work.setWorkType(req.getWorkType());
        work.setBussinessType(req.getBussinessType());
        work.setBelongAgent(req.getBelongAgent());
        work.setWorkAgent(req.getWorkAgent());
        work.setWorkStatus(req.getWorkStatus());
        work.setBussinessDesc(req.getBussinessDesc());
        work.setSolution(req.getSolution());
        work.setMark(req.getMark());

        if ("create".equals(option)) {
            work.setCreateTime(now);
            work.setWorkTime(now);
            worktaskinfoMapper.insert(work);
        } else {
            worktaskinfoMapper.updateById(work);
        }
        // 实时办结（老系统：状态 3 时写 close_time）
        if ("3".equals(req.getWorkStatus())) {
            work.setCloseTime(now);
            worktaskinfoMapper.updateById(work);
        }
        return option;
    }

    /**
     * 办结（老系统 finish：状态置 3 + close_time）
     */
    @Transactional(rollbackFor = Exception.class)
    public void finish(String workId) {
        Worktaskinfo work = worktaskinfoMapper.selectById(workId);
        if (work == null) {
            throw new BusinessException("工单不存在");
        }
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        work.setWorkStatus("3");
        work.setCloseTime(now);
        worktaskinfoMapper.updateById(work);
    }

    /**
     * 转办（老系统 zhuanban：work_agent 转移 + 状态置 2）
     */
    @Transactional(rollbackFor = Exception.class)
    public void transfer(String workId, String transferAgent) {
        if (!StringUtils.hasText(transferAgent)) {
            throw new BusinessException("请选择转办坐席");
        }
        Worktaskinfo work = worktaskinfoMapper.selectById(workId);
        if (work == null) {
            throw new BusinessException("工单不存在");
        }
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        work.setWorkAgent(transferAgent);
        work.setWorkTime(now);
        work.setWorkStatus("2");
        worktaskinfoMapper.updateById(work);
    }

    /** 查询用户坐席工号（cc_core_sys_user 在 conf 库；同类内部调用 @DS 不生效，手动切换） */
    private String resolveAgentCodeManual(Integer userId) {
        if (userId == null) {
            return null;
        }
        com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder.push("conf");
        try {
            com.smt.refactor.entity.SysUser user = sysUserMapper.selectById(userId);
            return user != null ? user.getQualityAgCode() : null;
        } finally {
            com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder.poll();
        }
    }

    /**
     * 坐席分页（老系统 allagent，转办选人）
     * 注意：cc_core_sys_user 在 conf 库，方法级 @DS 切换
     */
    @com.baomidou.dynamic.datasource.annotation.DS("conf")
    public List<com.smt.refactor.entity.SysUser> allAgents(String searchKey) {
        QueryWrapper<com.smt.refactor.entity.SysUser> qw = new QueryWrapper<>();
        if (StringUtils.hasText(searchKey)) {
            qw.and(w -> w.like("user_name", searchKey.trim())
                    .or().like("qualityAgCode", searchKey.trim()));
        }
        qw.orderByAsc("qualityAgCode").last("limit 50");
        return sysUserMapper.selectList(qw);
    }

    private WorkTaskVO toVO(Worktaskinfo w, String customName, String customMobile) {
        WorkTaskVO vo = new WorkTaskVO();
        vo.setWorkId(w.getWorkId());
        vo.setCustomId(w.getCustomId());
        vo.setCustomName(customName);
        vo.setCustomMobile(customMobile);
        vo.setWorkType(w.getWorkType());
        vo.setBussinessType(w.getBussinessType());
        vo.setBelongAgent(w.getBelongAgent());
        vo.setWorkAgent(w.getWorkAgent());
        vo.setCreateTime(w.getCreateTime());
        vo.setWorkTime(w.getWorkTime());
        vo.setWorkStatus(w.getWorkStatus());
        vo.setBussinessDesc(w.getBussinessDesc());
        vo.setSolution(w.getSolution());
        vo.setMark(w.getMark());
        vo.setCloseTime(w.getCloseTime());
        vo.setTl(calcTl(w));
        return vo;
    }

    /** 处理耗时（老系统 sec_to_time(Timestampdiff(second, create_time, close_time))） */
    private String calcTl(Worktaskinfo w) {
        if (!StringUtils.hasText(w.getCloseTime()) || !StringUtils.hasText(w.getCreateTime())) {
            return "无";
        }
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(w.getCreateTime(), fmt);
            LocalDateTime end = LocalDateTime.parse(w.getCloseTime(), fmt);
            long secs = Math.max(0, Duration.between(start, end).getSeconds());
            long h = secs / 3600, m = (secs % 3600) / 60, s = secs % 60;
            return String.format("%02d:%02d:%02d", h, m, s);
        } catch (Exception e) {
            return "无";
        }
    }

    private String defaultDay(int offset) {
        return java.time.LocalDate.now().plusDays(offset).toString();
    }
}
