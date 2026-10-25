package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计分析服务（原生 SQL 聚合，业务库）
 * 翻译自老系统 report 包 TalkStaticController + SatisfactionStaticController
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS("biz")
public class ReportService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 呼入话务统计（老系统 talkStatic type=hrhwltj）
     * @param timeType day/month/year，timeVal 日期，bizDnis 中继号(逗号分隔)
     */
    public List<Map<String, Object>> inboundTalk(String timeType, String timeVal,
                                                 String timeStartVal, String timeEndVal, String bizDnis) {
        TimeDim tm = new TimeDim(timeType, timeVal, timeStartVal, timeEndVal);
        String bizCond = StringUtils.hasText(bizDnis)
                ? (bizDnis.contains(",") ? " and b.BizDNIS in ('" + bizDnis.replace(",", "','") + "')"
                : " and b.BizDNIS=" + bizDnis)
                : "";
        String sql = "select t.*,"
                + "ifnull(cast(concat(round(s10/s4*100,2),'%') as char),'0.00%') s11,"
                + "ifnull(cast(concat(round(s5/c*100,2),'%') as char),'0.00%') s12"
                + " from (select "
                + "date_format(starttime," + tm.dbDimension() + ") starttime_f, "
                + "date_format(starttime," + tm.dbOrder() + ") starttime_order, "
                + "ifnull(sum(case when b.bizType = 0 then 1 else 0 end),0) c, "
                + "ifnull(sum(case when b.bizType = 0 and b.SkillDesc is not null then 1 else 0 end),0) s1, "
                + "ifnull(sum(case when b.bizType = 0 and b.SkillDesc is null then 1 else 0 end),0) s2, "
                + "ifnull(cast(concat(round(sum(case when b.bizType = 0 and b.SkillDesc is null then 1 else 0 end) /sum(case when b.bizType = 0 then 1 else 0 end)*100,2),'%') as char),'0.00%') s3, "
                + "ifnull(sum(case when b.bizType = 0 and b.AgentID is not null and b.firstEnterSkillgroupAt is not NULL then 1 else 0 end),0) s4, "
                + "ifnull(sum(case when b.bizType = 0 and b.AgentID is not null and talkstarttime <> '' then 1 else 0 end),0) s5, "
                + "ifnull(cast(SEC_TO_TIME(sum(case when b.bizType = 0 then TIMESTAMPDIFF(SECOND,starttime,endTime) else 0 end)) as char),'00:00:00') s6, "
                + "ifnull(cast(SEC_TO_TIME(avg(case when b.bizType = 0 then TIMESTAMPDIFF(SECOND,starttime,endTime) else 0 end)) as char),'00:00:00') s7, "
                + "ifnull(cast(SEC_TO_TIME(avg(case when b.bizType = 0 and b.TalkStartTime is not null then TIMESTAMPDIFF(SECOND,firstEnterSkillgroupAt,TalkStartTime) else 0 end)) as char),'00:00:00') s8, "
                + "ifnull(cast(SEC_TO_TIME(sum(b.TimeLength)/1000/sum(case when b.bizType = 0 and b.timelength >0 then 1 else 0 end)) as char),'00:00:00') s9, "
                + "ifnull(sum(case when b.bizType = 0 and b.AgentID is not null and talkstarttime <> '' and DATE_SUB(date_format(talkstarttime,'%Y-%m-%d %H:%i:%s'),INTERVAL 20 second) < date_format(firstEnterSkillgroupAt,'%Y-%m-%d %H:%i:%s') then 1 else 0 end),0) s10 "
                + " from base_talkinfo b "
                + " where b.bizDNIS <>'1234567890' and b.ivrchanneldn is not null and b.CallDirection=1 and b.startTime >=? and b.startTime<=? "
                + bizCond
                + " group by date_format(starttime," + tm.dbOrder() + ") with rollup)t "
                + " order by t.starttime_order is null, t.starttime_order";
        return jdbcTemplate.queryForList(sql, tm.startTime(), tm.endTime());
    }

    /**
     * 外呼话务统计（老系统 talkStatic type=whhwltj）
     */
    public List<Map<String, Object>> outboundTalk(String timeType, String timeVal,
                                                  String timeStartVal, String timeEndVal) {
        TimeDim tm = new TimeDim(timeType, timeVal, timeStartVal, timeEndVal);
        String sql = "select t.* from (select "
                + "date_format(starttime," + tm.dbDimension() + ") starttime_f, "
                + "date_format(starttime," + tm.dbOrder() + ") starttime_order, "
                + "ifnull(sum(case when b.bizType = 0 then 1 else 0 end),0) c, "
                + "ifnull(sum(case when b.bizType = 0 and b.timelength >0 then 1 else 0 end),0) s1, "
                + "ifnull(cast(concat(round(sum(case when b.bizType = 0 and b.timelength >0 then 1 else 0 end) /sum(case when b.bizType = 0 then 1 else 0 end)*100,2),'%') as char),'0.00%') s2, "
                + "ifnull(cast(SEC_TO_TIME(sum(b.TimeLength)/1000/sum(case when b.bizType = 0 and b.timelength >0 then 1 else 0 end)) as char),'00:00:00') s3 "
                + " from base_talkinfo b "
                + " where b.bizDNIS <>'1234567890' and b.CallDirection=2 and b.startTime >=? and b.startTime<=? "
                + " group by date_format(starttime," + tm.dbOrder() + ") with rollup)t "
                + " order by t.starttime_order is null, t.starttime_order";
        return jdbcTemplate.queryForList(sql, tm.startTime(), tm.endTime());
    }

    /**
     * 坐席 KPI 统计（老系统 talkStatic type=zxkpitj）
     */
    public List<Map<String, Object>> agentKpi(String timeType, String timeVal,
                                              String timeStartVal, String timeEndVal,
                                              String agentId, String skillId) {
        TimeDim tm = new TimeDim(timeType, timeVal, timeStartVal, timeEndVal);
        String skillCond = StringUtils.hasText(skillId)
                ? (skillId.contains(",") ? " and b.skillId in (" + skillId + ")" : " and b.skillId=" + skillId) : "";
        String agentCond = StringUtils.hasText(agentId)
                ? "agentid = '" + agentId.trim() + "'" : " AgentID is not null";
        String sql = "select "
                + "(case when s.agentid is null then '' else s.starttime_f end) starttime_f,"
                + "(case when s.agentid is null then '' else s.agentid end) agentid,"
                + "(case when s.agentid is null then '' else s.agentname end) agentname,"
                + "ifnull(t.s1,'00:00:00') s1,ifnull(t.s2,0) s2,ifnull(t.s3,0) s3,ifnull(t.s4,0) s4,ifnull(t.s7,0) s7,"
                + "ifnull(s.s5,'00:00:00') s5,ifnull(s.s6,'0') s6,"
                + "ifnull(cast(concat(round((time_to_sec(s1)+time_to_sec(s6))/time_to_sec(s5)*100,2),'%') as char),'0.00%') s8,"
                + "ifnull(cast(concat(round(s9/s2*100,2),'%') as char),'0.00%') s9 "
                + " from"
                + "(select date_format(starttime," + tm.dbDimension() + ") starttime_f,"
                + "date_format(starttime," + tm.dbOrder() + ") starttime_order,agentid,"
                + "ifnull(cast(SEC_TO_TIME(sum(b.timelength)/1000) as char),'00:00:00') s1,"
                + "ifnull(sum(case when b.bizType = 0 and b.CallDirection=1 and b.firstEnterSkillgroupAt is not NULL then 1 else 0 end),0) s2,"
                + "ifnull(sum(case when b.bizType = 0 and b.CallDirection=1 and b.AgentID is not null and b.talkstarttime <> '' then 1 else 0 end),0) s3,"
                + "ifnull(sum(case when b.bizType = 0 and b.CallDirection=2 and b.timelength > 0 then 1 else 0 end),0) s7,"
                + "ifnull(sum(case when b.bizType = 2 and b.timelength>0 then 1 else 0 end),0) s4,"
                + "ifnull(sum(case when b.bizType = 0 and b.CallDirection=1 and b.AgentID is not null and b.talkstarttime <> '' and DATE_SUB(date_format(talkstarttime,'%Y-%m-%d %H:%i:%s'),INTERVAL 20 second) < date_format(firstEnterSkillgroupAt,'%Y-%m-%d %H:%i:%s') then 1 else 0 end),0) s9 "
                + " from base_talkinfo b where " + agentCond + " and b.bizDNIS <>'1234567890' and b.startTime >=? and b.startTime<=? "
                + skillCond
                + " group by date_format(starttime," + tm.dbOrder() + "),agentid with rollup "
                + " having ((starttime_order is not null and " + agentCond + ") or (starttime_order is null and agentid is null))"
                + ") t right outer join "
                + "(select i.agentid,i.agentname,date_format(starttime," + tm.dbDimension() + ") starttime_f,"
                + "date_format(starttime," + tm.dbOrder() + ") starttime_order,"
                + "ifnull(cast(sec_to_time(sum(s.laststatustimelen)/1000) as char),'00:00:00') s5,"
                + "ifnull(cast(sec_to_time(sum(case when s.laststatus =6 then s.laststatustimelen else 0 end)/1000) as char),'0') s6 "
                + " from base_agentinfo i, base_agentstate s"
                + " where i.agentloginid = s.agentloginid and s.startTime >=? and s.startTime<=? "
                + (StringUtils.hasText(skillId)
                ? (skillId.contains(",") ? " and s.skillId in (" + skillId + ")" : " and s.skillId=" + skillId) : "")
                + " group by date_format(s.starttime," + tm.dbOrder() + "),i.agentid with rollup"
                + " having ((" + agentCond + " and starttime_order is not null) or (agentid is null and starttime_order is null))"
                + ") s on (ifnull(t.agentid,-999) = ifnull(s.agentid,-999) and ifnull(t.starttime_order,'99') = ifnull(s.starttime_order,'99'))"
                + " order by s.starttime_order is null, s.starttime_order, s.agentid";
        return jdbcTemplate.queryForList(sql, tm.startTime(), tm.endTime(), tm.startTime(), tm.endTime());
    }

    /**
     * 满意度统计（老系统 satisfactionStatic type=mydtj）
     */
    public List<Map<String, Object>> satisfaction(String startTime, String endTime,
                                                  String agentId, String skillId) {
        String start = StringUtils.hasText(startTime) ? startTime : "1970-01-01 00:00:00";
        String end = StringUtils.hasText(endTime) ? endTime : LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 23:59:59";
        String agentCond = StringUtils.hasText(agentId)
                ? "agentid = '" + agentId.trim() + "'" : "agentid is not null and agentid <>''";
        String skillCond = StringUtils.hasText(skillId)
                ? (skillId.contains(",") ? " and b.skillId in (" + skillId + ")" : " and b.skillId=" + skillId) : "";
        String sql = "select t.*, ifnull(cast(concat(round(s1/(c1+c2)*100,2),'%') as char),'0.00%') s2 "
                + " from (select agentid,agentname,"
                + "ifnull(sum(case when b.CallDirection = 1 then 1 else 0 end),0) c1,"
                + "ifnull(sum(case when b.CallDirection = 2 then 1 else 0 end),0) c2,"
                + "ifnull(sum(case when t.satisfaction_m is not null and t.satisfaction_m >0 and t.satisfaction_m <5 then 1 else 0 end),0) s1,"
                + "ifnull(sum(case when t.satisfaction_m =1 then 1 else 0 end),0) s3,"
                + "ifnull(sum(case when t.satisfaction_m =2 then 1 else 0 end),0) s4,"
                + "ifnull(sum(case when t.satisfaction_m =3 then 1 else 0 end),0) s5,"
                + "ifnull(sum(case when t.satisfaction_m =4 then 1 else 0 end),0) s6 "
                + " from base_talkinfo b "
                + " left join (select contactid, max(satisfaction) satisfaction_m from base_talkinfo b"
                + " where b.bizDNIS <>'1234567890' and b.satisfaction is not null and " + agentCond + " and b.timelength>0 and b.startTime >=? and b.startTime<=? "
                + skillCond + " group by contactid) t on t.contactid = b.contactid and b.agentid is not null"
                + " where b.bizDNIS <>'1234567890' and " + agentCond + " and b.timelength>0 and b.startTime >=? and b.startTime<=? "
                + skillCond + " group by agentid with rollup) t order by t.agentid is null";
        return jdbcTemplate.queryForList(sql, start, end, start, end);
    }

    /**
     * 工单量统计（老系统 satisfactionStatic type=gzltj）
     */
    public List<Map<String, Object>> workTaskStat(String timeType, String timeVal,
                                                  String timeStartVal, String timeEndVal, String agentId) {
        TimeDim tm = new TimeDim(timeType, timeVal, timeStartVal, timeEndVal);
        String agentCond = StringUtils.hasText(agentId)
                ? "work_agent = '" + agentId.trim() + "'" : "work_agent is not null and work_agent <>''";
        String sql = "select ss.*, t.agentname from ("
                + "SELECT work_agent AS agentid, count(1) as s2,"
                + "ifnull(cast(sec_to_time(Avg(CASE WHEN close_time <> '' THEN Timestampdiff(second, create_time, close_time) ELSE 0 END)) as char),'无') s1,"
                + "Date_format(create_time, " + tm.dbOrder() + ") starttime_order,"
                + "Date_format(create_time, " + tm.dbDimension() + ") starttime_f"
                + " FROM crm_worktaskinfo"
                + " where " + agentCond + " and create_time >= ? and create_time <=? and close_time <> ''"
                + " group by starttime_order, agentid) ss"
                + " left join (select distinct agentid, agentname from base_agentinfo where LoginTime >= ? and LoginTime <= ?) t on t.agentid = ss.agentid"
                + " order by starttime_order";
        return jdbcTemplate.queryForList(sql, tm.startTime(), tm.endTime(), tm.startTime(), tm.endTime());
    }

    /**
     * 呼入统计（老系统 satisfactionStatic type=hrtj）
     * 依赖 CallTrack/repeatCallFlag/workStatus 列，本地库可能缺失，容错降级
     */
    public List<Map<String, Object>> inboundStat(String timeType, String timeVal,
                                                 String timeStartVal, String timeEndVal) {
        TimeDim tm = new TimeDim(timeType, timeVal, timeStartVal, timeEndVal);
        String sql = "SELECT ss.* FROM ("
                + "SELECT b.CallTrack incall_type,"
                + "IFNULL(SUM(CASE WHEN b.bizType = 0 AND b.firstEnterSkillgroupAt IS NOT NULL THEN 1 ELSE 0 END),0) incall_qty,"
                + "IFNULL(SUM(CASE WHEN b.bizType = 0 AND b.AgentID IS NOT NULL AND b.talkstarttime <> '' THEN 1 ELSE 0 END),0) incall_success_qty,"
                + "IFNULL(CAST(SEC_TO_TIME(AVG(CASE WHEN b.bizType = 0 AND b.CallDirection = 1 AND b.TalkStartTime IS NOT NULL THEN TIMESTAMPDIFF(SECOND,firstEnterSkillgroupAt,TalkStartTime) ELSE 0 END)) AS CHAR),'00:00:00') waittime_avg,"
                + "IFNULL(CAST(SEC_TO_TIME(SUM(b.TimeLength)/1000/SUM(CASE WHEN b.bizType = 0 AND b.timelength >0 THEN 1 ELSE 0 END)) AS CHAR),'00:00:00') calltime_avg,"
                + "IFNULL(SUM(CASE WHEN b.bizType = 0 AND b.AgentID IS NOT NULL AND b.talkstarttime <> ''"
                + " AND DATE_SUB(DATE_FORMAT(talkstarttime,'%Y-%m-%d %H:%i:%s'),INTERVAL 20 SECOND) < DATE_FORMAT(firstEnterSkillgroupAt,'%Y-%m-%d %H:%i:%s') THEN 1 ELSE 0 END),0) 20sec_qty,"
                + "IFNULL(SUM(CASE WHEN b.bizType = 0 AND b.AgentID IS NOT NULL AND b.talkstarttime <> ''"
                + " AND DATE_SUB(DATE_FORMAT(talkstarttime,'%Y-%m-%d %H:%i:%s'),INTERVAL 10 SECOND) < DATE_FORMAT(firstEnterSkillgroupAt,'%Y-%m-%d %H:%i:%s') THEN 1 ELSE 0 END),0) 10sec_qty,"
                + "IFNULL(SUM(CASE WHEN b.bizType = 0 AND b.firstEnterSkillgroupAt IS NOT NULL AND workStatus = '2' THEN 1 ELSE 0 END),0) transWork_qty,"
                + "IFNULL(SUM(CASE WHEN b.bizType = 0 AND b.firstEnterSkillgroupAt IS NOT NULL AND repeatCallFlag = '0' and workStatus = '1' THEN 1 ELSE 0 END),0) firstClose_qty,"
                + "IFNULL(SUM(CASE WHEN b.bizType = 0 AND b.firstEnterSkillgroupAt IS NOT NULL AND repeatCallFlag = '1' THEN 1 ELSE 0 END),0) repeatCall_qty"
                + " FROM base_talkinfo b"
                + " WHERE AgentID IS NOT NULL AND b.bizDNIS <>'1234567890' and b.startTime >=? and b.startTime<=? and b.CallDirection=1"
                + " GROUP BY b.CallTrack WITH ROLLUP) ss";
        try {
            return jdbcTemplate.queryForList(sql, tm.startTime(), tm.endTime());
        } catch (Exception e) {
            log.warn("base_talkinfo 缺 CallTrack/repeatCallFlag/workStatus 列，呼入统计返回空: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /** 时间维度翻译（老系统 TimeDimension） */
    static class TimeDim {
        private final String dimension;
        private final String date;
        private final String startFix;
        private final String endFix;

        TimeDim(String dimension, String date, String startFix, String endFix) {
            this.dimension = (dimension == null || dimension.isEmpty()) ? "day" : dimension;
            this.date = (date == null || date.isEmpty())
                    ? LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : date;
            this.startFix = startFix;
            this.endFix = endFix;
        }

        String startTime() {
            // 区间查询：直接用起止日期（驾驶舱/工作台图表用）
            if ("range".equals(dimension)) {
                String s = (startFix == null || startFix.isEmpty())
                        ? LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : startFix;
                return s + " 00:00:00";
            }
            if ("year".equals(dimension)) {
                return this.date.substring(0, 4) + "-01-01 00:00:00";
            }
            if ("month".equals(dimension)) {
                return this.date.substring(0, 7) + "-01 00:00:00";
            }
            return LocalDate.parse(this.date).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00";
        }

        String endTime() {
            if ("range".equals(dimension)) {
                String e = (endFix == null || endFix.isEmpty())
                        ? LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : endFix;
                return e + " 23:59:59";
            }
            if ("year".equals(dimension)) {
                return this.date.substring(0, 4) + "-12-31 23:59:59";
            }
            if ("month".equals(dimension)) {
                LocalDate d = LocalDate.parse(this.date.substring(0, 7) + "-01");
                int day = d.lengthOfMonth();
                return this.date.substring(0, 7) + "-" + day + " 23:59:59";
            }
            return LocalDate.parse(this.date).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 23:59:59";
        }

        /** 展示维度（老系统 getDBDimension） */
        String dbDimension() {
            if ("year".equals(dimension)) {
                return "'%c月'";
            }
            if ("month".equals(dimension)) {
                return "'%e日'";
            }
            return "'%k:00'";
        }

        /** 排序维度（老系统 getDBDimensionOrder） */
        String dbOrder() {
            if ("year".equals(dimension)) {
                return "'%m'";
            }
            if ("month".equals(dimension)) {
                return "'%d'";
            }
            return "'%H'";
        }
    }
}
