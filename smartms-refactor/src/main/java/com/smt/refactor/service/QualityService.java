package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.QualityScoreRequest;
import com.smt.refactor.dto.QualityTemplateRequest;
import com.smt.refactor.entity.QualityResult;
import com.smt.refactor.entity.QualityTemplate;
import com.smt.refactor.entity.Talkinfo;
import com.smt.refactor.mapper.QualityResultMapper;
import com.smt.refactor.mapper.QualityTemplateMapper;
import com.smt.refactor.mapper.TalkinfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 质检管理服务（crm_quality_template / crm_quality_result / base_talkinfo 业务库）
 * 翻译自老系统 qualityinspect 包 3 个 Controller
 */
@Service
@RequiredArgsConstructor
@DS("biz")
public class QualityService {

    private final QualityTemplateMapper templateMapper;
    private final QualityResultMapper resultMapper;
    private final TalkinfoMapper talkinfoMapper;

    /* ==================== 质检模板 ==================== */

    public PageResult<QualityTemplate> templatePage(long pageNo, long pageSize, String name,
                                                    String status, String creater) {
        QueryWrapper<QualityTemplate> qw = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            qw.like("name", name.trim());
        }
        if (StringUtils.hasText(status)) {
            qw.like("status", status.trim());
        }
        if (StringUtils.hasText(creater)) {
            qw.like("creater", creater.trim());
        }
        qw.orderByDesc("create_time");
        Page<QualityTemplate> p = templateMapper.selectPage(new Page<>(pageNo, pageSize), qw);
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    /** 启用中的模板（质检操作用，status=2） */
    public List<QualityTemplate> activeTemplates() {
        return templateMapper.selectList(new QueryWrapper<QualityTemplate>().eq("status", "2"));
    }

    public QualityTemplate templateDetail(Long id) {
        QualityTemplate t = templateMapper.selectById(id);
        if (t == null) {
            throw new BusinessException("模板不存在");
        }
        return t;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long templateSave(QualityTemplateRequest req) {
        // 校验分数区间（老系统：excellent > fine > pass）
        if (StringUtils.hasText(req.getExcellent()) && StringUtils.hasText(req.getFine())
                && StringUtils.hasText(req.getPass())) {
            int ex = Integer.parseInt(req.getExcellent());
            int fi = Integer.parseInt(req.getFine());
            int pa = Integer.parseInt(req.getPass());
            if (ex <= fi || fi <= pa) {
                throw new BusinessException("高评价分数必须大于低评价分数");
            }
        }
        // 校验质检项权重总和 <= 100
        String columns = req.getQualityColumns() == null ? "" : req.getQualityColumns();
        if (StringUtils.hasText(columns)) {
            int sum = 0;
            for (String item : columns.split(";")) {
                if (!StringUtils.hasText(item)) {
                    continue;
                }
                String[] kv = item.split("=");
                if (kv.length == 2) {
                    sum += Integer.parseInt(kv[1]);
                }
            }
            if (sum > 100) {
                throw new BusinessException("质检项权重总和不得超过100");
            }
        }
        QualityTemplate t = new QualityTemplate();
        t.setName(req.getName());
        t.setDesc(req.getDesc());
        t.setExcellent(req.getExcellent());
        t.setFine(req.getFine());
        t.setPass(req.getPass());
        t.setQualityColumns(columns);
        t.setCreater(req.getCreater());
        if (req.getId() != null) {
            t.setId(req.getId());
            templateMapper.updateById(t);
            return req.getId();
        } else {
            t.setStatus(req.getStatus() == null ? "2" : req.getStatus());
            t.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            templateMapper.insert(t);
            return t.getId();
        }
    }

    /** 启停用（老系统 changeStatus） */
    public void templateChangeStatus(Long id, String status) {
        QualityTemplate t = templateMapper.selectById(id);
        if (t == null) {
            throw new BusinessException("模板不存在");
        }
        t.setStatus(status);
        templateMapper.updateById(t);
    }

    /* ==================== 待质检通话列表 ==================== */

    /**
     * 待质检通话（老系统 InspectController index：已接通 + 有坐席 + 未质检或可重检）
     */
    public PageResult<Talkinfo> inspectCalls(long pageNo, long pageSize, String ani, String dnis,
                                             Integer callDirection, String agentId, String startTime, String endTime,
                                             Long minTime, Long maxTime) {
        QueryWrapper<Talkinfo> qw = new QueryWrapper<>();
        qw.ne("AgentID", "").isNotNull("AgentID");
        qw.gt("TimeLength", 0);
        int dir = callDirection == null ? 3 : callDirection;
        // CallDirection & dir > 0（位与过滤：1 呼入 2 呼出 3 双向）
        qw.apply("CallDirection & {0} > 0", dir);
        if (StringUtils.hasText(ani)) {
            qw.like("BizANI", ani.trim());
        }
        if (StringUtils.hasText(dnis)) {
            qw.like("BizDNIS", dnis.trim());
        }
        String start = StringUtils.hasText(startTime) ? startTime : defaultDay(-30);
        String end = StringUtils.hasText(endTime) ? endTime : defaultDay(30);
        qw.gt("StartTime", start + " 23:59:59.999").lt("StartTime", end + " 23:59:59.999");
        if (StringUtils.hasText(agentId)) {
            qw.like("AgentID", agentId.trim());
        }
        long minMs = (minTime == null ? 0 : minTime) * 1000L;
        long maxMs = (maxTime == null ? 999999 : maxTime) * 1000L;
        qw.ge("TimeLength", minMs).le("TimeLength", maxMs);
        qw.orderByDesc("StartTime");
        Page<Talkinfo> p = talkinfoMapper.selectPage(new Page<>(pageNo, pageSize), qw);
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    /* ==================== 质检成绩 ==================== */

    public PageResult<QualityResult> resultPage(long pageNo, long pageSize, String agentId,
                                                String inspector, String grade, Long templateId,
                                                String startTime, String endTime) {
        QueryWrapper<QualityResult> qw = new QueryWrapper<>();
        if (StringUtils.hasText(agentId)) {
            qw.like("agentid", agentId.trim());
        }
        if (StringUtils.hasText(inspector)) {
            qw.like("inspector", inspector.trim());
        }
        if (StringUtils.hasText(grade)) {
            qw.like("result_grade", grade.trim());
        }
        if (templateId != null && templateId > 0) {
            qw.eq("template_id", templateId);
        }
        String start = StringUtils.hasText(startTime) ? startTime : defaultDay(-30);
        String end = StringUtils.hasText(endTime) ? endTime : defaultDay(30);
        qw.gt("create_time", start + " 23:59:59.999").lt("create_time", end + " 23:59:59.999");
        qw.orderByDesc("create_time");
        Page<QualityResult> p = resultMapper.selectPage(new Page<>(pageNo, pageSize), qw);
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    public QualityResult resultDetail(Long id) {
        QualityResult r = resultMapper.selectById(id);
        if (r == null) {
            throw new BusinessException("质检成绩不存在");
        }
        return r;
    }

    /**
     * 生成质检成绩（老系统 generateResult：加权求和 + 按模板分档定级 + 标记已质检）
     */
    @Transactional(rollbackFor = Exception.class)
    public void generateResult(QualityScoreRequest req) {
        if (req.getColumnNames() == null || req.getColumnWeights() == null) {
            throw new BusinessException("生成错误，请检查网络");
        }
        if (req.getColumnScores() == null) {
            throw new BusinessException("请为质检项打分");
        }
        QualityTemplate template = templateMapper.selectById(req.getTemplateId());
        if (template == null) {
            throw new BusinessException("该模板不存在");
        }
        if (!StringUtils.hasText(template.getQualityColumns())) {
            throw new BusinessException("该模板不存在质检项");
        }
        // 加权求和
        double sum = 0;
        StringBuilder scoreStr = new StringBuilder();
        for (int i = 0; i < req.getColumnScores().size(); i++) {
            String name = req.getColumnNames().get(i);
            int score = req.getColumnScores().get(i);
            int weight = req.getColumnWeights().get(i);
            if (score > 100 || score < 0) {
                throw new BusinessException(name + "分值必须在0-100之间");
            }
            double itemScore = score * weight / 100.00;
            sum += itemScore;
            scoreStr.append(name).append("=")
                    .append(BigDecimal.valueOf(itemScore).setScale(2, RoundingMode.HALF_UP))
                    .append(";");
        }
        BigDecimal sumScore = BigDecimal.valueOf(sum).setScale(2, RoundingMode.HALF_UP);
        // 定级：1不及格 2及格 3良好 4优秀
        String grade = "0";
        double pass = Double.parseDouble(template.getPass());
        double fine = Double.parseDouble(template.getFine());
        double excellent = Double.parseDouble(template.getExcellent());
        if (sum < pass) {
            grade = "1";
        } else if (sum < fine) {
            grade = "2";
        } else if (sum < excellent) {
            grade = "3";
        } else {
            grade = "4";
        }
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        QualityResult r = new QualityResult();
        r.setTemplateId(req.getTemplateId());
        r.setContactId(req.getContactId());
        r.setAgentid(req.getAgentid());
        r.setTalkstarttime(req.getTalkstarttime());
        r.setInspector(req.getAgentid());
        r.setCreateTime(now);
        r.setResultGrade(grade);
        r.setResultScore(sumScore.toString());
        r.setQualityColumnsScore(scoreStr.toString());
        resultMapper.insert(r);
        // 标记通话已质检
        talkinfoMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Talkinfo>()
                .set("Inspected", 1)
                .eq("ContactID", req.getContactId())
                .eq("AgentID", req.getAgentid())
                .eq("TalkStartTime", req.getTalkstarttime()));
    }

    /**
     * 撤销质检（老系统 reInspect：删成绩 + 重置通话未质检）
     */
    @Transactional(rollbackFor = Exception.class)
    public void reInspect(String contactId, String agentId, String talkStartTime) {
        resultMapper.delete(new QueryWrapper<QualityResult>()
                .eq("contact_id", contactId).eq("agentid", agentId).eq("talkstarttime", talkStartTime));
        talkinfoMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Talkinfo>()
                .set("Inspected", 0)
                .eq("ContactID", contactId)
                .eq("AgentID", agentId)
                .eq("TalkStartTime", talkStartTime));
    }

    private String defaultDay(int offset) {
        return java.time.LocalDate.now().plusDays(offset).toString();
    }
}
