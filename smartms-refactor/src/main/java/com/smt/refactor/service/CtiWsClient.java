package com.smt.refactor.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smt.refactor.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * CTI 软电话 WebSocket 客户端（CTI /softphone/ws）
 *
 * 每坐席一个长连接：
 *  - send()：发送 AMI 风格指令，按 ActionID 匹配响应（CompletableFuture）
 *  - 事件：CTI 主动推送数组 [{EventName:...}]，进入事件队列（pollEvent 消费）
 *  - 心跳：每 30s 发 GetState 保活；断线自动重连（指数退避）
 *
 * 实测协议（<CTI_HOST>:8082/softphone/ws）：
 *  发送 {"Command":"Action","Action":"Login",...,"ActionID":"x"}
 *  响应 {"Command":"agentlogin","Result":200,"SessionID":"S155260","Succeed":true,"ActionID":"x"}
 *  事件推送 [{"EventName":"OnEvtAgentLogin","workno":"3002","status":2,...}]
 */
@Slf4j
public class CtiWsClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final long SEND_TIMEOUT_MS = 15000;
    private static final int MAX_EVENT_QUEUE = 200;
    private static final long HEARTBEAT_SEC = 30;

    private final String url;
    private final String idPrefix;
    private final ConcurrentHashMap<String, CompletableFuture<Map<String, Object>>> pending = new ConcurrentHashMap<>();
    private final BlockingQueue<Map<String, Object>> eventQueue = new LinkedBlockingQueue<>(MAX_EVENT_QUEUE);
    private final ObjectMapper mapper = new ObjectMapper();

    private volatile WebSocketSession session;
    private volatile boolean closed = false;
    private ScheduledExecutorService heartbeat;
    private final Object lock = new Object();

    public CtiWsClient(String url) {
        this.url = url;
        this.idPrefix = "w" + ThreadLocalRandom.current().nextInt(100000, 999999) + "-";
    }

    /** 建立连接（同步等待握手完成） */
    public void connect() {
        synchronized (lock) {
            if (session != null && session.isOpen()) return;
            try {
                StandardWebSocketClient client = new StandardWebSocketClient();
                session = client.doHandshake(new WsHandler(), url).get(10, TimeUnit.SECONDS);
                log.info("CTI WS 已连接: {}", url);
                startHeartbeat();
            } catch (Exception e) {
                throw new BusinessException("CTI WebSocket 连接失败: " + url + " (" + shortMsg(e) + ")");
            }
        }
    }

    /** 发送指令并等待响应（ActionID 匹配） */
    public Map<String, Object> send(Map<String, Object> payload) {
        ensureConnected();
        String actionId = String.valueOf(payload.getOrDefault("ActionID", idPrefix + "act"));
        payload.put("ActionID", actionId);
        CompletableFuture<Map<String, Object>> fut = new CompletableFuture<>();
        pending.put(actionId, fut);
        try {
            String json = MAPPER.writeValueAsString(payload);
            session.sendMessage(new TextMessage(json));
            Map<String, Object> resp = fut.get(SEND_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            Object act = payload.get("action") != null ? payload.get("action") : payload.get("Action");
            resp.put("_action", act);
            return resp;
        } catch (TimeoutException e) {
            pending.remove(actionId);
            Object act = payload.get("action") != null ? payload.get("action") : payload.get("Action");
            throw new BusinessException("CTI 响应超时（" + act + "）");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            pending.remove(actionId);
            throw new BusinessException("CTI WS 发送失败: " + shortMsg(e));
        }
    }

    /** 取一条事件（无事件时阻塞等待 timeoutMs，超时返回 null） */
    public Map<String, Object> pollEvent(long timeoutMs) {
        try {
            return eventQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /** 关闭连接并清理 */
    public void close() {
        closed = true;
        if (heartbeat != null) heartbeat.shutdownNow();
        try {
            if (session != null && session.isOpen()) session.close();
        } catch (Exception ignored) {
        }
        pending.values().forEach(f -> f.completeExceptionally(new BusinessException("连接已关闭")));
        pending.clear();
        eventQueue.clear();
        log.info("CTI WS 已关闭: {}", url);
    }

    // ============ 内部 ============

    private void ensureConnected() {
        if (session == null || !session.isOpen()) {
            connect();
        }
    }

    private void startHeartbeat() {
        if (heartbeat != null) heartbeat.shutdownNow();
        heartbeat = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cti-ws-heartbeat");
            t.setDaemon(true);
            return t;
        });
        heartbeat.scheduleAtFixedRate(() -> {
            try {
                if (session != null && session.isOpen()) {
                    Map<String, Object> ping = new ConcurrentHashMap<>();
                    ping.put("Command", "Action");
                    ping.put("Action", "GetState");
                    ping.put("ActionID", idPrefix + "hb");
                    session.sendMessage(new TextMessage(MAPPER.writeValueAsString(ping)));
                }
            } catch (Exception e) {
                log.warn("CTI WS 心跳失败: {}", e.getMessage());
            }
        }, HEARTBEAT_SEC, HEARTBEAT_SEC, TimeUnit.SECONDS);
    }

    private void reconnect() {
        if (closed) return;
        log.warn("CTI WS 断开，3 秒后重连: {}", url);
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
                return;
            }
            if (closed) return;
            try {
                connect();
            } catch (Exception e) {
                log.warn("CTI WS 重连失败: {}", shortMsg(e));
                reconnect();
            }
        }, "cti-ws-reconnect").start();
    }

    private static String shortMsg(Exception e) {
        String m = e.getMessage();
        return m == null ? e.getClass().getSimpleName() : m.length() > 120 ? m.substring(0, 120) : m;
    }

    /** 消息处理：有 ActionID → 匹配请求响应；数组/含 EventName → 事件入队 */
    private class WsHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession s) {
            session = s;
        }

        @Override
        protected void handleTextMessage(WebSocketSession s, TextMessage message) {
            String text = message.getPayload();
            try {
                Object obj = MAPPER.readValue(text, new TypeReference<Object>() {
                });
                if (obj instanceof List) {
                    for (Object item : (List<?>) obj) {
                        if (item instanceof Map) {
                            if (!eventQueue.offer((Map<String, Object>) item)) {
                                eventQueue.poll(); // 队列满丢最旧
                                eventQueue.offer((Map<String, Object>) item);
                            }
                        }
                    }
                } else if (obj instanceof Map) {
                    Map<String, Object> m = (Map<String, Object>) obj;
                    String actionId = (String) m.get("ActionID");
                    if (actionId != null && pending.containsKey(actionId)) {
                        pending.remove(actionId).complete(m);
                    } else if (m.containsKey("EventName") || m.containsKey("Event")) {
                        eventQueue.offer(m);
                    } else {
                        log.debug("CTI WS 未匹配消息: {}", text.length() > 200 ? text.substring(0, 200) : text);
                    }
                }
            } catch (Exception e) {
                log.warn("CTI WS 消息解析失败: {}", text.length() > 200 ? text.substring(0, 200) : text);
            }
        }

        @Override
        public void handleTransportError(WebSocketSession s, Throwable exception) {
            log.warn("CTI WS 传输错误: {}", exception.getMessage());
            reconnect();
        }

        @Override
        public void afterConnectionClosed(WebSocketSession s, CloseStatus status) {
            if (!closed) reconnect();
        }
    }
}
