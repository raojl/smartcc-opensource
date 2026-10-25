package com.smt.refactor.service;

import com.smt.refactor.common.BusinessException;
import com.smt.refactor.utils.SshClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * WebRTC 分机管理服务
 *
 * 混合方案（WebRTC 语音 + IPC CTI 控制）：
 *  - 语音：浏览器 sip.js 直连 Asterisk WSS（DTLS-SRTP/ICE），媒体不经过本服务
 *  - 控制：坐席状态/呼叫事件仍走既有 CTI（AgentPhoneService，IPC 8082）
 *  - 本服务：坐席 WebRTC 分机生命周期管理 —— 生成 pjsip.conf 配置段（endpoint/aor/auth，
 *    复用 transport-wss）→ SSH 下发 + `asterisk -rx 'module reload res_pjsip.so'`
 *
 * 配置段模板（与 PoC 验证通过的 3102 一致）：
 *  media_encryption=dtls + webrtc=yes + ice_support=yes + dtls 证书 + media_address=公网 IP
 */
@Slf4j
@Service
public class WebRtcService {

    @Value("${webrtc.asterisk.host:127.0.0.1}")
    private String astHost;
    @Value("${webrtc.asterisk.port:22}")
    private int astPort;
    @Value("${webrtc.asterisk.user:root}")
    private String astUser;
    @Value("${webrtc.asterisk.password:}")
    private String astPassword;
    @Value("${webrtc.asterisk.pjsip-conf:/etc/asterisk/pjsip.conf}")
    private String pjsipConf;
    @Value("${webrtc.asterisk.cli:asterisk}")
    private String astCli;
    @Value("${webrtc.media-address:}")
    private String mediaAddress;
    @Value("${webrtc.dtls-cert:/etc/ssl/smartms-ca/asterisk.pem}")
    private String dtlsCert;
    @Value("${webrtc.context:from-sipreg}")
    private String context;

    private static final String WSS_TRANSPORT = "transport-wss";

    /** 生成坐席 WebRTC 分机配置段（pjsip.conf 追加内容） */
    public String buildConfigSection(String exten, String password) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n; ============ WebRTC 坐席分机 ").append(exten).append("（自动生成）============\n");
        sb.append("[").append(exten).append("]\n");
        sb.append("type=endpoint\n");
        sb.append("context=").append(context).append("\n");
        sb.append("transport=").append(WSS_TRANSPORT).append("\n");
        sb.append("disallow=all\n");
        sb.append("allow=ulaw\nallow=alaw\nallow=opus\n");
        sb.append("ice_support=yes\n");
        sb.append("media_encryption=dtls\n");
        sb.append("webrtc=yes\n");
        sb.append("dtmf_mode=info\n");
        sb.append("rtp_symmetric=yes\n");
        sb.append("force_rport=yes\n");
        sb.append("rewrite_contact=yes\n");
        if (mediaAddress != null && !mediaAddress.isEmpty()) {
            sb.append("media_address=").append(mediaAddress).append("\n");
        }
        sb.append("dtls_verify=fingerprint\n");
        sb.append("dtls_setup=actpass\n");
        sb.append("dtls_rekey=0\n");
        sb.append("dtls_cert_file=").append(dtlsCert).append("\n");
        sb.append("dtls_private_key=").append(dtlsCert).append("\n");
        sb.append("auth=auth").append(exten).append("\n");
        sb.append("aors=").append(exten).append("\n");
        sb.append("\n[auth").append(exten).append("]\n");
        sb.append("type=auth\n");
        sb.append("auth_type=userpass\n");
        sb.append("username=").append(exten).append("\n");
        sb.append("password=").append(password).append("\n");
        sb.append("\n[").append(exten).append("]\n");
        sb.append("type=aor\n");
        sb.append("max_contacts=3\n");
        sb.append("remove_existing=yes\n");
        return sb.toString();
    }

    /** 创建 WebRTC 分机：追加配置 + reload */
    public Map<String, Object> createExtension(String exten, String password, String displayName) {
        if (exten == null || !exten.matches("\\d{2,8}")) {
            throw new BusinessException("分机号须为 2-8 位数字");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException("密码至少 6 位");
        }
        try (SshClient ssh = connect()) {
            // 查重：pjsip.conf 是否已有该分机（awk 始终输出数字，不依赖 shell exit code）
            String existing = ssh.exec("awk '/^\\[" + exten + "\\]$/{c++} END{print c+0}' " + pjsipConf).trim();
            if (!"0".equals(existing)) {
                throw new BusinessException("分机 " + exten + " 已存在");
            }
            ssh.appendFile(pjsipConf, buildConfigSection(exten, password));
            String reload = ssh.exec(astCli + " -rx 'module reload res_pjsip.so' 2>&1 | tail -1").trim();
            log.info("WebRTC 分机 {} 已创建，reload: {}", exten, reload);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("exten", exten);
            result.put("password", password);
            result.put("displayName", displayName == null ? "" : displayName);
            result.put("status", "created");
            result.put("reload", reload);
            return result;
        }
    }

    /** 删除 WebRTC 分机（endpoint/auth/aor 三段） */
    public Map<String, Object> deleteExtension(String exten) {
        if (exten == null || !exten.matches("\\d{2,8}")) {
            throw new BusinessException("分机号须为 2-8 位数字");
        }
        try (SshClient ssh = connect()) {
            String cmd = "sed -i '/^\\[" + exten + "\\]$/,/^$/d; /^\\[auth" + exten + "\\]$/,/^$/d' " + pjsipConf;
            ssh.exec(cmd);
            String reload = ssh.exec(astCli + " -rx 'module reload res_pjsip.so' 2>&1 | tail -1").trim();
            log.info("WebRTC 分机 {} 已删除，reload: {}", exten, reload);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("exten", exten);
            result.put("status", "deleted");
            result.put("reload", reload);
            return result;
        }
    }

    /** 列出所有 WebRTC 分机（endpoint 段 id） */
    public List<String> listExtensions() {
        try (SshClient ssh = connect()) {
            String out = ssh.exec("awk '/^\\[[0-9]+\\]/{sec=$0} /^type=endpoint/{print sec}' " + pjsipConf + " 2>/dev/null | tr -d '[]'");
            List<String> list = new ArrayList<>();
            for (String line : out.split("\n")) {
                String t = line.trim();
                if (!t.isEmpty()) {
                    list.add(t);
                }
            }
            return list;
        }
    }

    /** 坐席 WebRTC 接入配置（前端 PhoneBar 用）：WSS 地址 + 分机信息 + 认证密码 */
    public Map<String, Object> getAccessConfig(String exten) {
        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("server", astHost);
        cfg.put("wssUri", "wss://" + astHost + ":" + wssPort() + "/ws");
        cfg.put("exten", exten);
        cfg.put("displayName", "WebRTC-" + exten);
        // 从 Asterisk pjsip.conf 读取分机认证密码（前端 sip.js 注册 WSS 必需）
        if (exten != null && exten.matches("\\d{2,8}")) {
            try (SshClient ssh = connect()) {
                String pwd = ssh.exec("awk '/^\\[auth" + exten + "\\]$/{f=1} f&&/^password=/{print $0; exit}' "
                        + pjsipConf).replace("password=", "").trim();
                cfg.put("password", pwd);
            } catch (Exception e) {
                log.warn("读取分机 {} 密码失败: {}", exten, e.getMessage());
                cfg.put("password", "");
            }
        } else {
            cfg.put("password", "");
        }
        return cfg;
    }

    private String wssPort() {
        return "8089";
    }

    private SshClient connect() {
        if (astPassword == null || astPassword.isEmpty()) {
            throw new BusinessException("未配置 webrtc.asterisk.password（application.yml）");
        }
        return new SshClient(astHost, astPort, astUser, astPassword);
    }
}
