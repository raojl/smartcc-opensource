package com.smt.refactor.service;

import com.smt.refactor.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 软电话代理服务（官方 WS 协议，见《软电话WebSocket协议.md》）
 *
 * 协议要点（2026-08-21 版）：
 *  - 入口 ws://<host>:8082/softphone/ws，text 帧 UTF-8 JSON
 *  - 命令请求为小写字段：{"action":"login","workno":"3002","exten":"3002","sessionid":"S...","skilldesc":"1002;1003"}
 *  - 响应 {"Command":"agentlogin","Result":200,"Succeed":true,"SessionID":"S...","ActionID":"..."}
 *  - 事件推送 JSON 数组：[{"EventName":"OnEvtAgentLogin",...}]，WS 长连接主动推送（前端无需 getstate 轮询）
 *  - Result 码：200 成功 / 201 会话不存在 / 203 已登录 / 204 参数缺失(缺 callid) / 205 会话过期 / 206 链路不可用
 *
 * 每坐席一个 WS 连接（workNo → CtiWsClient），事件队列缓存，断线自动重连。
 */
@Slf4j
@Service
public class AgentPhoneService {

    /** CTI WebSocket 地址（application.yml: cti.agent-ws） */
    @Value("${cti.agent-ws:ws://127.0.0.1:8082/softphone/ws}")
    private String agentWs;

    /** 每坐席的 WS 连接 */
    private final Map<String, CtiWsClient> clients = new ConcurrentHashMap<>();

    private static final long EVENT_POLL_TIMEOUT_MS = 35000;

    /** 老系统 action → 官方协议 action 映射（§5 命令参考） */
    private static final Map<String, String> ACTION_MAP = new LinkedHashMap<>();
    private static final Map<String, String> FIELD_MAP = new LinkedHashMap<>();

    static {
        ACTION_MAP.put("login", "login");
        ACTION_MAP.put("agentlogin", "login");
        ACTION_MAP.put("logout", "agentlogout");
        ACTION_MAP.put("agentlogout", "agentlogout");
        ACTION_MAP.put("idle", "setready");
        ACTION_MAP.put("setready", "setready");
        ACTION_MAP.put("busy", "setnotready");
        ACTION_MAP.put("setnotready", "setnotready");
        ACTION_MAP.put("otherwork", "setidle");
        ACTION_MAP.put("setidle", "setidle");
        ACTION_MAP.put("makecall", "makecall");
        ACTION_MAP.put("hangupcall", "hangup");
        ACTION_MAP.put("hangup", "hangup");
        ACTION_MAP.put("answercall", "answer");
        ACTION_MAP.put("answer", "answer");
        ACTION_MAP.put("consultcall", "consultcall");
        ACTION_MAP.put("singlestep transfer", "transfercall");
        ACTION_MAP.put("singlestep transfercall", "transfercall");
        ACTION_MAP.put("transfercall", "transfercall");
        ACTION_MAP.put("conferencecall", "conferencecall");
        ACTION_MAP.put("getcalldata", "getcalldata");
        ACTION_MAP.put("setcalldata", "setcalldata");
        ACTION_MAP.put("queryidleagent", "queryidleagent");
        ACTION_MAP.put("queryqueuesituation", "queryqueuesituation");
        ACTION_MAP.put("getstate", "getstate");

        FIELD_MAP.put("WorkNo", "workno");
        FIELD_MAP.put("Exten", "exten");
        FIELD_MAP.put("SessionID", "sessionid");
        FIELD_MAP.put("SkillDesc", "skilldesc");
        FIELD_MAP.put("AgentType", "agenttype");
        FIELD_MAP.put("CallerID", "callerid");
        FIELD_MAP.put("DestNum", "destnum");
        FIELD_MAP.put("ContactID", "contactid");
        FIELD_MAP.put("CallID", "callid");
        FIELD_MAP.put("ConsultType", "consulttype");
        FIELD_MAP.put("CallData", "calldata");
        FIELD_MAP.put("BusinessType", "businesstype");
    }

    /**
     * 发送软电话指令（前端传语义化 action，后端映射官方协议小写格式）
     *
     * @param action Login/Logout/Idle/Busy/MakeCall/HangupCall/AnswerCall/
     *               ConsultCall/SingleStepTransfer/ConferenceCall/GetCallData/SetCallData/
     *               QueryIdleAgent/GetState（或官方小写名）
     * @param params 附加参数（WorkNo/Exten/SessionID/DestNum/CallID 等，键名兼容大小写）
     */
    public Map<String, Object> action(String action, Map<String, Object> params) {
        String protoAction = normalizeAction(action);
        Map<String, Object> req = buildRequest(protoAction, params);
        String workNo = String.valueOf(params == null ? "" : params.getOrDefault("workno",
                params.getOrDefault("WorkNo", "")));
        try {
            if ("login".equals(protoAction)) {
                return doLogin(workNo, req);
            }
            CtiWsClient client = clients.get(workNo);
            if (client == null) {
                throw new BusinessException("坐席尚未签入（请先登入软电话）");
            }
            if ("agentlogout".equals(protoAction)) {
                // IPC 对 agentlogout 常不返回 ActionID 匹配响应 → 超时也视为登出成功（CTI 侧已生效）
                try {
                    return client.send(req);
                } catch (BusinessException e) {
                    log.info("agentlogout 响应异常（按已登出处理）: {}", e.getMessage());
                    return resultMap(protoAction, 200, true);
                } finally {
                    clients.remove(workNo);
                    client.close();
                }
            }
            return client.send(req);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("软电话指令失败: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        }
    }

    /** 老 action 名 → 官方 action（§5）；未映射的按原样小写透传 */
    private String normalizeAction(String action) {
        if (action == null) return "";
        String key = action.toLowerCase().trim();
        String mapped = ACTION_MAP.get(key);
        return mapped != null ? mapped : key;
    }

    /** 构造官方小写格式请求体（§4.1） */
    private Map<String, Object> buildRequest(String protoAction, Map<String, Object> params) {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("action", protoAction);
        req.put("actionid", protoAction + UUID.randomUUID().toString().substring(0, 8));
        if (params != null) {
            for (Map.Entry<String, Object> e : params.entrySet()) {
                String field = FIELD_MAP.getOrDefault(e.getKey(), e.getKey());
                Object v = e.getValue();
                // skilldesc 清理：老格式 "1001=1;1002=1;" → 官方 "1001;1002"
                if ("skilldesc".equals(field) && v != null) {
                    v = cleanSkillDesc(String.valueOf(v));
                }
                if (v != null && !"".equals(String.valueOf(v))) {
                    req.put(field, v);
                }
            }
        }
        return req;
    }

    /** 技能描述清理：去 "=1" 与尾部分号 → "1001;1002" */
    private String cleanSkillDesc(String raw) {
        String s = raw.trim();
        if (s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder();
        for (String part : s.split("[;；]")) {
            String p = part.trim();
            if (p.isEmpty()) continue;
            int eq = p.indexOf('=');
            if (eq > 0) p = p.substring(0, eq).trim();
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(';');
            sb.append(p);
        }
        return sb.toString();
    }

    private Map<String, Object> resultMap(String action, int result, boolean succeed) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("Result", result);
        m.put("Succeed", succeed);
        m.put("_action", action);
        return m;
    }

    /** 登入：新建 WS 连接 → 发 login → 成功后按工号保存连接（§8.1 登录流程） */
    private Map<String, Object> doLogin(String workNo, Map<String, Object> payload) {
        // 防泄漏：同工号已有连接先释放
        CtiWsClient old = clients.remove(workNo);
        if (old != null) old.close();
        CtiWsClient client = new CtiWsClient(agentWs);
        client.connect();
        Map<String, Object> result = client.send(payload);
        if (result == null) {
            client.close();
            throw new BusinessException("CTI 无响应");
        }
        boolean ok = result.get("Succeed") == null || Boolean.TRUE.equals(result.get("Succeed"))
                || "true".equalsIgnoreCase(String.valueOf(result.get("Succeed")));
        if (!ok) {
            client.close();
            return result; // 响应给前端展示错误（含 Result 码），由前端翻译
        }
        clients.put(workNo, client);
        return result;
    }

    /** 事件拉取（前端长轮询；后端从 WS 事件队列即时返回，符合 §4.3 推送语义） */
    public Map<String, Object> pollEvents(String exten, String workNo, String sessionId) {
        CtiWsClient client = clients.get(workNo);
        Map<String, Object> out = new LinkedHashMap<>();
        if (client == null) {
            out.put("Result", 201);
            out.put("Succeed", false);
            out.put("Event", new ArrayList<>());
            return out;
        }
        List<Map<String, Object>> events = new ArrayList<>();
        long deadline = System.currentTimeMillis() + EVENT_POLL_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            Map<String, Object> ev = client.pollEvent(Math.min(5000, deadline - System.currentTimeMillis()));
            if (ev == null) break;
            events.add(ev);
            if (!events.isEmpty()) break; // 有事件立即返回
        }
        out.put("Succeed", true);
        out.put("Event", events);
        out.put("_action", "getstate");
        return out;
    }

    /** 释放连接（备用） */
    public void release(String workNo) {
        CtiWsClient client = clients.remove(workNo);
        if (client != null) client.close();
    }
}
