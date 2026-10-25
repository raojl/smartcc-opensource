# WebRTC 语音集成要点（Asterisk 16.30 + sip.js 0.16.1）——经验传承

> 来源：SmartMS 混合方案 PoC（2026-08-22 全链路验证）+ Spring Boot 集成实战（2026-08-23）
> 本文件是所有已验证结论的唯一权威清单，**任何 WebRTC 集成改动前先读这里**，避免重复踩坑。

---

## 1. 环境前提（服务器一次性配置）

| # | 项 | 说明 | 验证命令 |
|---|---|---|---|
| 1 | **res_srtp.so** | Asterisk 编译时**必须带 libsrtp2**（`--with-external-srtp`），否则 DTLS-SRTP 无法协商（488） | `asterisk -rx 'module show like srtp'` 显示 Running |
| 2 | **pjproject 版本** | Asterisk 16.30 官方用 2.12.1；若用 2.13 必须用 **Asterisk 官方包**（github tag 缺 third_party 构建文件） | 编译时验证 |
| 3 | **云安全组 UDP 10000-20000** | RTP 媒体端口，未放行 = 通话建立但无声音（单通/全无） | 外网 UDP 探测 |
| 4 | **STUN（rtp.conf stunaddr）** | 云 NAT 后 ICE 必须用 STUN 检测公网 srflx candidate，否则 ICE Disconnected | `asterisk -rx 'rtp set debug on'` 看 Candidate srflx |

## 2. Asterisk 配置清单（关键项）

```ini
# pjsip.conf —— WebRTC endpoint（每个坐席分机一份）
[<exten>]
type=endpoint
context=from-sipreg
transport=transport-wss
disallow=all
allow=ulaw
allow=alaw
allow=opus
ice_support=yes
media_encryption=dtls        # 不是 dtls_srtp！
webrtc=yes
dtmf_mode=info
media_address=<公网IP>        # 关键！否则应答 SDP 是内网 IP，媒体不通
dtls_cert_file=/etc/ssl/smartms-ca/asterisk.pem
dtls_private_key=/etc/ssl/smartms-ca/asterisk.pem
auth=auth<exten>
aors=<exten>

[transport-wss]
type=transport
protocol=ws                  # 不是 websocket！
bind=0.0.0.0:8089
external_media_address=<公网IP>

# rtp.conf
[general]
rtpstart=10000
rtpend=20000
stunaddr=stun.l.google.com:19302
external_rtp_address=<公网IP>
local_net=172.17.0.0/16

# sip.conf（重要！chan_sip 会抢注 "sip" WS 协议导致 res_pjsip websocket 加载失败）
[general]
websocket_enabled=no
```

**注意**：`protocol=websocket` 会报错（合法值是 `ws`）；`media_encryption=dtls_srtp` 报错（合法值是 `dtls`）。

## 3. 前端要点（sip.js 0.16.1，核心 API 不是 SimpleUser）

### 3.1 必须遵守的 5 条
1. **密码必须传**：`authorizationPassword` 为空 → REGISTER 401 认证循环。密码从后端配置接口获取（不硬编码）。
2. **远端音频绑定时机**：**`await inviter.invite()` 之后再绑定 `peerConnection.ontrack`**（invite 前 pc 未创建，绑定无效 = 通话通但没声音）。
3. **必须 audio-only**：`constraints: { audio: true, video: false }`，否则 SDP 带 video m-line，Asterisk 无视频编解码返回 500/488。
4. **autoplay 兜底**：audio 元素 `el.play()` 可能被 Chrome 拦截 → 提供「解锁播放」按钮（用户手势内恢复）。
5. **残留通话清理**：重拨前挂断旧 session（否则 Asterisk 残留 Echo 通道）。

### 3.2 正确代码骨架（已验证）
```js
// 注册
const ua = new SIP.UserAgent({
  uri: SIP.UserAgent.makeURI('sip:' + exten + '@' + server),
  transportOptions: { server: wssUri },
  authorizationUsername: exten,
  authorizationPassword: cfg.password,   // ← 必须！从后端配置接口拿
  logLevel: 'warn'
});
await ua.start();
await new SIP.Registerer(ua).register();

// 外呼
const inviter = new SIP.Inviter(ua, targetURI, {
  sessionDescriptionHandlerOptions: { constraints: { audio: true, video: false } },  // ← audio-only
  delegate: { onTrack: (track) => playRemoteTrack(track) }
});
await inviter.invite();                                   // ← 先 invite
inviter.sessionDescriptionHandler.peerConnection.ontrack = (event) => {              // ← 后绑定
  const track = event.streams[0]?.getAudioTracks()[0];
  if (track) playRemoteTrack(track);
};

// 播放
function playRemoteTrack(track) {
  const el = document.createElement('audio');
  el.srcObject = new MediaStream([track]);
  el.autoplay = true;
  document.body.appendChild(el);
  el.play().catch(() => {});   // ← autoplay 策略兜底
}
```

## 4. 排障速查表（按症状定位）

| 症状 | 首选排查 | 常见根因 |
|---|---|---|
| res_pjsip_transport_websocket 加载失败 | 查 chan_sip | sip.conf 缺 `websocket_enabled=no` |
| 配置报 protocol 错误 | 查 pjsip.conf | 写成 `websocket`（应为 `ws`） |
| media_encryption 报错 | 查 pjsip.conf | 写成 `dtls_srtp`（应为 `dtls`） |
| 呼叫 500 | 查 endpoint transport | transport 逗号列表/无 wss transport |
| 呼叫 488 | 查 res_srtp | 编译无 SRTP 支持 |
| 通话通但无声音 | **先看 Asterisk RTP 日志**（Got/Sent）→ 再看浏览器 onTrack | ①媒体没通（STUN/安全组/media_address）②onTrack 绑定时机 |
| REGISTER 401 循环 | 查前端 authorizationPassword | 密码为空/不匹配 |
| ICE Disconnected | 查 rtp.conf stunaddr + media_address | 无公网 srflx candidate |
| 移动网络单通 | NAT 类型 | 对称 NAT 需 TURN |

## 5. 集成 Checklist（Spring Boot 项目已验证）

- [x] 后端：WebRtcController/Service —— 分机创建/删除/列表 + 配置查询（含密码）
- [x] 后端：SSH（JSch）写 pjsip.conf + `module reload res_pjsip.so`（**注意：JSch exec 下 `grep || echo` 不可靠，查重用 awk**）
- [x] 后端：登录返回坐席分机（qualityAgDN）自动带出
- [x] 前端：PhoneBar（sip.js 注册/拨打/接听/挂断 + 状态显示 + 解锁播放）
- [x] 前端：管理页（WebRTC 分机开通/删除/接入信息复制）
- [x] 测试：Edge headless + fake 麦克风自动化语音测试（auto_test.html?exten=xxx）
- [x] 验收：Asterisk Got/Sent RTP 双向 + 浏览器回声

## 6. 本机开发环境备忘

- 编译必须用 **PowerShell 的 mvn.cmd**（bash 里 mvn 静默失败：Launcher 找不到）
- Spring Boot jar repackage 失败（无主清单）→ 用 `java -cp "target/classes;<cp.txt>" com.smt.refactor.SmartmsRefactorApplication` 启动（`mvn dependency:build-classpath -Dmdep.outputFile=target/cp.txt`）
- 本机 MariaDB：`mysqld --datadir=E:/smartms-mariadb-data`（127.0.0.1:3306 root 无密码）
