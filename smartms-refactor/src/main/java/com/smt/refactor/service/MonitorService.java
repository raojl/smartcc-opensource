package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.smt.refactor.entity.Skill;
import com.smt.refactor.mapper.SkillMapper;
import com.smt.refactor.utils.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 实时监控服务
 * 翻译自老系统 businessmonitor 包 5 个 Controller
 *
 * 架构说明：老系统监控页通过 JSONP 调 rtdataquery（后端代理）从 CTI 服务(8091)
 * 和 SQLite 实时库(Realtime.db) 获取 RT_Agent/RT_Channel 等实时数据。
 * 新系统提供等价的 REST 代理接口；CTI 服务不可用时返回明确错误而非 500。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS("biz")
public class MonitorService {

    /** CTI 实时监控地址（application.yml: cti.monitor-server） */
    @org.springframework.beans.factory.annotation.Value("${cti.monitor-server:http://127.0.0.1:8091}")
    private String ctiBase;

    private String ctiSqlQuery() { return ctiBase + "/sqlQuery"; }
    private String ctiRtQuery() { return ctiBase + "/rtdataquery"; }

    private final SkillMapper skillMapper;

    /**
     * 通用 SQL 查询代理（老系统 sqlQueryWithSS，GET 到 8091）
     * 注意：SQL 来自受控调用点（getAgents/getAllAgentDn/getAgentResults），不接收前端裸 SQL
     */
    public String sqlQuery(String sql) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("sql", sql);
        byte[] result = HttpUtils.httpFormRequest(ctiSqlQuery(), params, false, -1, 1000, 2000);
        if (result == null) {
            throw new com.smt.refactor.common.BusinessException("CTI 实时服务不可达");
        }
        String json = new String(result, StandardCharsets.UTF_8);
        // 老系统返回形如 ({"aaData":...})，去掉首尾括号给前端
        if (json.length() >= 2 && json.startsWith("(") && json.endsWith(")")) {
            return json.substring(1, json.length() - 1);
        }
        return json;
    }

    /**
     * 实时数据查询代理（老系统 rtdataquery，转发到 8091）
     */
    public String rtdataQuery(String queryId) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("queryid", queryId);
        byte[] result = HttpUtils.httpFormRequest(ctiRtQuery(), params, false, -1, 1000, 2000);
        if (result == null) {
            throw new com.smt.refactor.common.BusinessException("CTI 实时服务不可达");
        }
        String json = new String(result, StandardCharsets.UTF_8);
        if (json.length() >= 2 && json.startsWith("(") && json.endsWith(")")) {
            return json.substring(1, json.length() - 1);
        }
        return json;
    }

    /**
     * 全部坐席 ID（老系统 getAgents：RT_Agent where AgentLoginID != 'NoUse'）
     */
    public String getAgents() {
        return sqlQuery("SELECT Distinct AgentID FROM RT_Agent where AgentLoginID != 'NoUse'");
    }

    /**
     * 空闲坐席（老系统 getAllIdleAgent：SQLite Realtime.db 中 CurrStatus=3）
     * 新系统走 8091 sqlQuery（数据源等价）
     */
    public String getAllIdleAgent() {
        return sqlQuery("SELECT Distinct AgentID,AgentName FROM RT_Agent where AgentLoginID != 'NoUse' and CurrStatus = 3");
    }

    /**
     * 坐席详情统计（老系统 getAgentResults）
     */
    public String getAgentResults(String agentId) {
        String sql;
        if (agentId == null || agentId.trim().isEmpty()) {
            sql = "SELECT Distinct AgentID,AgentName FROM RT_Agent where AgentLoginID != 'NoUse' and CurrStatus = 3";
        } else {
            sql = "SELECT Distinct t1.AgentID, t1.AgentDN,t1.LoginTime, LOCKED_NUM, " +
                    "(IN_IB_CONN_NUM+EX_IB_CONN_NUM+IN_OB_CONN_NUM+EX_OB_CONN_NUM+IN_IB_BECNSCONN_NUM+EX_IB_BECNSCONN_NUM) CallNum," +
                    "round((IN_IB_CONN_TIME+EX_IB_CONN_TIME+IN_OB_CONN_TIME+EX_OB_CONN_TIME+IN_IB_HOD_TIME+EX_IB_HOD_TIME+IN_OB_HOD_TIME+EX_OB_HOD_TIME+IN_IB_CNSCONN_TIME+EX_IB_CNSCONN_TIME+IN_OB_CNSCONN_TIME+EX_OB_CNSCONN_TIME+IN_IB_BECNSCONN_TIME+EX_IB_BECNSCONN_TIME+IN_OB_BECNSCONN_TIME+EX_OB_BECNSCONN_TIME+IN_IB_CNF_TIME+EX_IB_CNF_TIME+IN_OB_CNF_TIME+EX_OB_CNF_TIME+IN_IB_BECNF_TIME+EX_IB_BECNF_TIME+IN_OB_BECNF_TIME+EX_OB_BECNF_TIME)*1.000/1000, 2) talkTimeLen, IDLE_NUM, BUSY_NUM,OW_NUM," +
                    "round(BUSY_TIME*1.000/1000,2) BusyTimeLen FROM RT_Agent t1 where t1.AgentID = " + agentId;
        }
        return sqlQuery(sql);
    }

    /**
     * 坐席 DN 查询（老系统 getAllAgentDn）
     */
    public String getAllAgentDn(String agentDN) {
        String sql = "SELECT Distinct AgentID,AgentDN,CurrStatus FROM RT_Agent where AgentLoginID != 'NoUse'";
        if (agentDN != null && !agentDN.trim().isEmpty()) {
            sql += " and AgentDN = " + agentDN;
        }
        return sqlQuery(sql);
    }

    /**
     * 技能组领域列表（老系统 SkillMonitorController index 的真实 DB 查询）
     * root 域 + 每个技能组描述作为一个子域
     * 注意：cc_core_skill 在 smartconf 库（conf），需方法级切换数据源
     */
    @com.baomidou.dynamic.datasource.annotation.DS("conf")
    public List<Map<String, Object>> skillDomains() {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("domain_id", "1");
        root.put("domain_name", "root");
        root.put("par_domain_id", "0");
        result.add(root);
        List<Skill> skills = skillMapper.selectList(null);
        int seq = 1;
        for (Skill skill : skills) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("domain_id", String.valueOf(++seq));
            node.put("domain_name", skill.getSkillDesc() == null ? "" : skill.getSkillDesc());
            node.put("par_domain_id", "1");
            result.add(node);
        }
        return result;
    }
}
