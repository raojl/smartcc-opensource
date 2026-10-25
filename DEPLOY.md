# SmartCC 呼叫中心系统 —— 部署说明

> 开源版部署指南（后端 Spring Boot + 前端 Vue3 + WebRTC 软电话）

## 环境要求

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 8+ | 后端运行 |
| MySQL / MariaDB | 5.7+ | 双库：`smartconf`（配置）+ `smartcc`（业务） |
| Maven | 3.6+ | 后端构建 |
| Node.js | 18+ | 前端构建 |

## 一、数据库初始化

```bash
# 创建双库
mysql -uroot -p -e "CREATE DATABASE smartconf DEFAULT CHARSET utf8mb4; CREATE DATABASE smartcc DEFAULT CHARSET utf8mb4;"
# 导入脚本
mysql -uroot -p smartconf < sql/smartconf.sql
mysql -uroot -p smartcc < sql/smartcc.sql
```

## 二、后端（smartms-refactor）

### 1. 配置数据库连接

编辑 `src/main/resources/application.yml`，修改双数据源：

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        conf:   # 主库 smartconf
          url: jdbc:mysql://<DB_HOST>:3306/smartconf?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
          username: <DB_USER>
          password: <DB_PASSWORD>
        biz:    # 业务库 smartcc
          url: jdbc:mysql://<DB_HOST>:3306/smartcc?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
          username: <DB_USER>
          password: <DB_PASSWORD>
```

### 2. 环境变量（可选，覆盖默认配置）

| 变量 | 说明 |
|---|---|
| `CTI_AGENT_WS` / `CTI_AGENT_SERVER` / `CTI_MONITOR_SERVER` | CTI 中间件地址（无则默认 127.0.0.1:8082） |
| `WEBRTC_ASTERISK_HOST` / `WEBRTC_ASTERISK_USER` / `WEBRTC_ASTERISK_PASSWORD` | Asterisk SSH（WebRTC 分机管理，需启用） |
| `WEBRTC_MEDIA_ADDRESS` | 应答媒体地址（云 NAT 环境设公网 IP） |

### 3. 构建 & 启动

```bash
mvn clean package
java -jar target/smartms-refactor.jar
# 或开发模式：mvn spring-boot:run
# 默认端口 8080：http://127.0.0.1:8080/actuator/health
```

## 三、前端（smartms-refactor-web）

```bash
npm install

# 开发模式（Vite dev，代理 /api → 127.0.0.1:8080）
npm run dev          # http://localhost:3000

# 生产构建
npm run build        # 产物在 dist/
# dist 用 nginx 托管（参考 deploy/nginx.conf），/api 反向代理到后端 8080
```

## 四、WebRTC 软电话（可选）

坐席浏览器内直接拨打/接听（sip.js → Asterisk WSS，DTLS-SRTP/ICE）：

1. **Asterisk 16+ 启用 PJSIP WebSocket**，WebRTC endpoint 关键配置参考 `docs/webrtc-integration-notes.md`
2. 后端配置 `WEBRTC_ASTERISK_*` 环境变量（SSH 管理分机）
3. 管理台「WebRTC 分机」页面开通坐席分机
4. 坐席登录 → PhoneBar 自动注册语音 → 拨号拨打

> 无 Asterisk 环境时，系统核心（登录/客户/订单/质检/工单/报表）可独立运行，仅软电话语音不可用。

## 五、默认账号

初始化数据见 `sql/smartconf.sql`（管理员 role_type=0 / 坐席 role_type=1）。

| 账号 | 口令 | 角色 |
|------|------|------|
| `admin` | `123456` | 管理员 |
| `3004` | `123456` | 坐席（示例分机） |

> ⚠️ **上线前必须修改默认口令**：`UPDATE smartconf.cc_core_sys_user SET password = MD5('新密码') WHERE login_name = 'admin';`

## 六、目录结构

```
smartcc-opensource/
├── smartms-refactor/          # 后端（Spring Boot + MyBatis-Plus）
│   └── docs/                  #   API/WebRTC 集成文档
├── smartms-refactor-web/      # 前端（Vue3 + Element Plus + WebRTC 软电话）
│   └── webrtc-demo/           #   sip.js 独立 PoC 演示
└── sql/                       # 数据库初始化脚本
    ├── smartconf.sql
    └── smartcc.sql
```
