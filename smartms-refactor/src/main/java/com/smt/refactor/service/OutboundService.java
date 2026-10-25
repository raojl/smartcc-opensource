package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.entity.OutboundTaskInfo;
import com.smt.refactor.mapper.OutboundTaskInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 外呼任务服务（crm_outbound_task_info 业务库）
 * 翻译自老系统 outbound 包 2 个 Controller
 */
@Service
@RequiredArgsConstructor
@DS("biz")
public class OutboundService {

    private final OutboundTaskInfoMapper taskMapper;

    /**
     * 任务列表（按 task_id 分组聚合：完成率/接通率，老系统 index/myTask）
     * @param myUserId 当前登录用户（my_custom=true 时按 user_id 过滤）
     */
    public PageResult<Map<String, Object>> taskPage(long pageNo, long pageSize, String taskId,
                                                    String taskDesc, String agentId, String startTime, String endTime,
                                                    Integer myUserId, boolean myCustom) {
        QueryWrapper<OutboundTaskInfo> qw = new QueryWrapper<>();
        if (StringUtils.hasText(taskId)) {
            qw.like("task_id", taskId.trim());
        }
        if (StringUtils.hasText(taskDesc)) {
            qw.like("`desc`", taskDesc.trim());
        }
        if (StringUtils.hasText(agentId)) {
            qw.like("agentid", agentId.trim());
        }
        String start = StringUtils.hasText(startTime) ? startTime : defaultDay(-7);
        String end = StringUtils.hasText(endTime) ? endTime : defaultDay(8);
        qw.ge("timestamp", start).le("timestamp", end);
        if (myCustom && myUserId != null) {
            qw.eq("user_id", String.valueOf(myUserId));
        }
        List<OutboundTaskInfo> all = taskMapper.selectList(qw);
        // 按 task_id 分组聚合
        Map<String, List<OutboundTaskInfo>> groups = new java.util.LinkedHashMap<>();
        for (OutboundTaskInfo t : all) {
            groups.computeIfAbsent(t.getTaskId(), k -> new ArrayList<>()).add(t);
        }
        List<Map<String, Object>> taskList = new ArrayList<>();
        for (Map.Entry<String, List<OutboundTaskInfo>> e : groups.entrySet()) {
            List<OutboundTaskInfo> rows = e.getValue();
            Map<String, Object> m = new HashMap<>();
            m.put("task_id", e.getKey());
            m.put("desc", rows.get(0).getDesc());
            m.put("timestamp", rows.get(0).getTimestamp());
            m.put("user_id", rows.get(0).getUserId());
            // 完成率：task_status != 0 占比
            long done = rows.stream().filter(r -> !"0".equals(r.getTaskStatus())).count();
            m.put("status", String.format("%.2f%%", done * 100.0 / rows.size()));
            m.put("wcl", String.format("%.2f%%", done * 100.0 / rows.size()));
            // 接通率：outbound_status = ACTIVE 占比
            long active = rows.stream().filter(r -> "ACTIVE".equals(r.getOutboundStatus())).count();
            m.put("jtl", String.format("%.2f%%", active * 100.0 / rows.size()));
            // 坐席列表
            m.put("agentid", rows.stream().map(OutboundTaskInfo::getAgentid)
                    .filter(java.util.Objects::nonNull).distinct()
                    .reduce((a, b) -> a + "," + b).orElse(""));
            taskList.add(m);
        }
        // 时间倒序 + 内存分页
        taskList.sort((a, b) -> String.valueOf(b.get("timestamp")).compareTo(String.valueOf(a.get("timestamp"))));
        int from = (int) ((pageNo - 1) * pageSize);
        int to = Math.min(from + (int) pageSize, taskList.size());
        List<Map<String, Object>> pageList = from >= taskList.size() ? new ArrayList<>()
                : taskList.subList(from, to);
        long totalPages = (taskList.size() + pageSize - 1) / pageSize;
        return new PageResult<>(pageList, taskList.size(), pageNo, pageSize, totalPages);
    }

    /**
     * 任务详单（按行查询，老系统 index/myTaskInfo）
     */
    public PageResult<OutboundTaskInfo> detailPage(long pageNo, long pageSize, String taskId,
                                                   String phoneNumber, String agentId, String startTime, String endTime,
                                                   String myAgentCode) {
        QueryWrapper<OutboundTaskInfo> qw = new QueryWrapper<>();
        if (StringUtils.hasText(taskId)) {
            qw.like("task_id", taskId.trim());
        }
        if (StringUtils.hasText(phoneNumber)) {
            qw.like("phone_number", phoneNumber.trim());
        }
        if (StringUtils.hasText(agentId)) {
            if ("my".equals(agentId) && myAgentCode != null) {
                qw.eq("agentid", myAgentCode.trim());
            } else {
                qw.like("agentid", agentId.trim());
            }
        }
        String start = StringUtils.hasText(startTime) ? startTime : defaultDay(-7);
        String end = StringUtils.hasText(endTime) ? endTime : defaultDay(8);
        qw.ge("timestamp", start).le("timestamp", end);
        qw.orderByDesc("timestamp");
        Page<OutboundTaskInfo> p = taskMapper.selectPage(new Page<>(pageNo, pageSize), qw);
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    /** 启动任务（老系统 startOutbound：task_status=1） */
    public void start(String taskId) {
        updateTaskStatus(taskId, "1");
    }

    /** 停止任务（老系统 stopOutbound：task_status=0） */
    public void stop(String taskId) {
        updateTaskStatus(taskId, "0");
    }

    /** 删除任务（老系统 delete） */
    public void deleteTask(String taskId) {
        taskMapper.delete(new QueryWrapper<OutboundTaskInfo>().eq("task_id", taskId));
    }

    /**
     * 号码导入（老系统 phoneNumListImport：文本文件每行一个号码，可带 callingNumber,phoneNumber 逗号格式）
     * @param numbers 号码列表（每行）
     * @param taskDesc 任务描述
     * @param agentIds 平均分配坐席列表（空=当前坐席）
     * @param userId 当前用户 ID
     * @param myAgentCode 当前坐席工号
     */
    @Transactional(rollbackFor = Exception.class)
    public int importNumbers(List<String> numbers, String taskDesc, List<String> agentIds,
                             Integer userId, String myAgentCode) {
        if (numbers == null || numbers.isEmpty()) {
            throw new BusinessException("号码列表为空");
        }
        if (!StringUtils.hasText(taskDesc)) {
            throw new BusinessException("请输入任务描述");
        }
        String taskId = UUID.randomUUID().toString();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        List<String> agents = (agentIds == null || agentIds.isEmpty())
                ? java.util.Collections.singletonList(myAgentCode)
                : agentIds;
        int agentIdx = 0;
        int count = 0;
        for (String line : numbers) {
            String num = line == null ? "" : line.trim();
            if (num.isEmpty()) {
                continue;
            }
            String callingNumber = "";
            String phoneNumber = num;
            if (num.contains(",")) {
                String[] parts = num.split(",");
                callingNumber = parts[0].trim();
                phoneNumber = parts[1].trim();
            }
            OutboundTaskInfo t = new OutboundTaskInfo();
            t.setCallingNumber(callingNumber);
            t.setPhoneNumber(phoneNumber);
            t.setTaskId(taskId);
            t.setTaskStatus("0");
            t.setTimestamp(now);
            t.setDesc(taskDesc);
            t.setUserId(userId == null ? null : String.valueOf(userId));
            t.setAgentid(agents.get(agentIdx % agents.size()));
            taskMapper.insert(t);
            count++;
            agentIdx++;
        }
        return count;
    }

    private void updateTaskStatus(String taskId, String status) {
        if (!StringUtils.hasText(taskId)) {
            throw new BusinessException("任务 ID 不能为空");
        }
        taskMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<OutboundTaskInfo>()
                .set("task_status", status).eq("task_id", taskId));
    }

    private String defaultDay(int offset) {
        return java.time.LocalDate.now().plusDays(offset).toString();
    }
}
