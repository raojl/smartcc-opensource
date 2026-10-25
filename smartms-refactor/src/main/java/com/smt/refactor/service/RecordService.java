package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.MissCallQuery;
import com.smt.refactor.dto.RecordQuery;
import com.smt.refactor.dto.RecordVO;
import com.smt.refactor.entity.Talkinfo;
import com.smt.refactor.mapper.TalkinfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 交互查询服务（base_talkinfo 业务库）
 * 翻译自老系统 RecordController + MissedCallController（record 包）
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS("biz")
public class RecordService {

    private final TalkinfoMapper talkinfoMapper;

    /**
     * 通话详单分页（老系统 RecordController index，TalkStartTime 非空=已接通）
     * @param myAgentCode 当前坐席工号（agentId="my" 时使用）
     */
    public PageResult<RecordVO> callRecords(RecordQuery q, String myAgentCode) {
        QueryWrapper<Talkinfo> qw = new QueryWrapper<>();
        // 已接通通话：TalkStartTime 非空
        qw.isNotNull("TalkStartTime").ne("TalkStartTime", "");
        // 坐席过滤（my_record 用精确匹配，普通查询用模糊）
        String agentId = q.getAgentId();
        boolean my = "my".equals(agentId);
        if (my) {
            agentId = myAgentCode;
        }
        if (StringUtils.hasText(agentId)) {
            if (my) {
                qw.eq("AgentID", agentId.trim());
            } else {
                qw.like("AgentID", agentId.trim());
            }
        }
        if (StringUtils.hasText(q.getDeviceid())) {
            qw.like("DeviceDN", q.getDeviceid().trim());
        }
        if (StringUtils.hasText(q.getOriAni())) {
            qw.like("BizANI", q.getOriAni().trim());
        }
        if (StringUtils.hasText(q.getOriDnis())) {
            qw.like("BizDNIS", q.getOriDnis().trim());
        }
        // 时间范围（默认昨天 ~ 今天+1，老系统 index）
        String start = StringUtils.hasText(q.getStartTime()) ? q.getStartTime() : defaultDay(-1);
        String end = StringUtils.hasText(q.getEndTime()) ? q.getEndTime() : defaultDay(1);
        qw.ge("TalkStartTime", start).le("TalkStartTime", end + " 23:59:59.999");
        // 时长过滤（毫秒）
        long lenStart = (q.getLenStart() == null ? 0 : q.getLenStart()) * 1000L;
        long lenEnd = (q.getLenEnd() == null ? 999999 : q.getLenEnd()) * 1000L;
        qw.ge("TimeLength", lenStart).le("TimeLength", lenEnd);
        qw.orderByDesc("TalkStartTime");

        Page<Talkinfo> p = talkinfoMapper.selectPage(new Page<>(q.getPage(), q.getSize()), qw);
        List<RecordVO> list = p.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(list, p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    /**
     * 未接记录分页（老系统 MissedCallController 4 类）
     * @param myAgentCode 当前坐席工号（agentId="my" 时使用）
     */
    public PageResult<RecordVO> missedCalls(MissCallQuery q, String myAgentCode) {
        QueryWrapper<Talkinfo> qw = new QueryWrapper<>();
        int type = q.getType() == null ? 1 : q.getType();
        // 坐席过滤
        String agentId = q.getAgentId();
        boolean my = "my".equals(agentId);
        if (my) {
            agentId = myAgentCode;
        }
        if (StringUtils.hasText(agentId)) {
            if (my || type == 1) {
                qw.eq("AgentID", agentId.trim());
            } else {
                qw.like("AgentID", agentId.trim());
            }
        }
        if (StringUtils.hasText(q.getCustomMobile())) {
            qw.like("BizANI", q.getCustomMobile().trim());
        }
        if (StringUtils.hasText(q.getAgentDn())) {
            qw.like("BizDNIS", q.getAgentDn().trim());
        }
        // 时间范围（默认 7 天前 ~ 今天+8）
        String start = StringUtils.hasText(q.getStartTime()) ? q.getStartTime() : defaultDay(-7);
        String end = StringUtils.hasText(q.getEndTime()) ? q.getEndTime() : defaultDay(8);
        qw.ge("StartTime", start).le("StartTime", end);

        switch (type) {
            case 1: // 坐席未接：未接通 + 呼入 + 有坐席
                qw.eq("TalkStartTime", "").eq("CallDirection", 1);
                qw.ne("AgentID", "");
                break;
            case 2: // 队列未接：未接通 + 呼入 + 无坐席 + 有技能组
                qw.eq("TalkStartTime", "").eq("CallDirection", 1).isNull("AgentID")
                        .ne("SkillDesc", "0").ne("SkillDesc", 0);
                break;
            case 3: // 客户未接：未接通 + 呼出未接 + 无录音
                qw.eq("TalkStartTime", "").eq("FileName", "").eq("CallDirection", 2);
                break;
            case 4: // 留言：有留言语音路径（本地库可能无 LeaveVoicePath 列，容错返回空）
                qw.ne("LeaveVoicePath", "").isNotNull("LeaveVoicePath");
                try {
                    Page<Talkinfo> pp = talkinfoMapper.selectPage(new Page<>(q.getPage(), q.getSize()), qw);
                    return new PageResult<>(pp.getRecords().stream().map(this::toVO).collect(Collectors.toList()),
                            pp.getTotal(), pp.getCurrent(), pp.getSize(), pp.getPages());
                } catch (Exception e) {
                    log.warn("base_talkinfo 无 LeaveVoicePath 列，留言查询返回空: {}", e.getMessage());
                    return new PageResult<>(java.util.Collections.emptyList(), 0, q.getPage(), q.getSize(), 0);
                }
            default:
                qw.eq("TalkStartTime", "").eq("CallDirection", 1).ne("AgentID", "");
        }
        qw.orderByDesc("StartTime");

        Page<Talkinfo> p = talkinfoMapper.selectPage(new Page<>(q.getPage(), q.getSize()), qw);
        List<RecordVO> list = p.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(list, p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    private RecordVO toVO(Talkinfo t) {
        RecordVO vo = new RecordVO();
        vo.setContactID(t.getContactID());
        vo.setAgentId(t.getAgentID());
        vo.setAgentName(t.getAgentName());
        vo.setDeviceDN(t.getDeviceDN());
        vo.setCallDirection(t.getCallDirection() == null ? null : String.valueOf(t.getCallDirection()));
        vo.setFileName(t.getFileName());
        vo.setBizANI(t.getBizANI());
        vo.setBizDNIS(t.getBizDNIS());
        vo.setTalkStartTime(t.getTalkStartTime());
        vo.setTimeLength(t.getTimeLength() == null ? null
                : t.getTimeLength().longValue() / 1000);
        vo.setSkillDesc(t.getSkillDesc());
        vo.setStartTime(t.getStartTime());
        vo.setEndTime(t.getEndTime());
        return vo;
    }

    private String defaultDay(int offset) {
        return java.time.LocalDate.now().plusDays(offset).toString();
    }
}
