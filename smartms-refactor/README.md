# smartms-refactor — SmartCC 呼叫中心系统（后端重构版）

基于 Spring Boot + MyBatis-Plus 的呼叫中心 / CTI 管理系统后端，逐模块翻译自老系统
（JFinal 4.9.08 + JSP），保持业务语义与数据模型等价，REST API 化。

## 技术栈

| 组件 | 版本 | 说明 |
|---|---|---|
| Java | 8 | 兼容老系统部署环境 |
| Spring Boot | 2.7.x | Web / Security / Actuator |
| MyBatis-Plus | 3.5.x | ORM + 分页插件 |
| dynamic-datasource | 3.6.x | 双数据源路由（`@DS`） |
| JWT (jjwt) | 0.11.x | 无状态认证 |
| POI | 4.1.2 | Excel 导出 |
| Lombok | 1.18.x | 样板代码 |

## 双数据源架构

| 数据源 | 配置名 | 库 | 表 |
|---|---|---|---|
| 主库 | 默认 | `smartconf` | `cc_core_*`（用户/角色/菜单）、`pbx_*`（分机/中继/路由） |
| 业务库 | `biz` | `smartcc` | `base_*`（话务）、`crm_*`（客户/订单/工单/质检/外呼）、`im_*`（智能消息） |

```yaml
spring:
  datasource:
    dynamic:
      primary: conf        # 默认主库
      datasource:
        conf:  # smartconf
        biz:   # smartcc
```

> ⚠️ **Spring 代理陷阱**：`@DS` 基于 AOP 代理，**同类内部方法调用不生效**。
> 跨库查询必须走 `DynamicDataSourceContextHolder.push("biz")/poll()` 手动切换，
> 或调用另一个 Service 的 `@DS` 方法（经代理）。

## 快速开始

```bash
# 1. 准备数据库（MySQL/MariaDB 5.1+）
#    导入项目根目录 smartconf.sql / smartcc.sql

# 2. 配置（src/main/resources/application.yml）
#    - 数据库：默认 127.0.0.1:3306（smartconf/smartcc），root 无密码
#    - 敏感配置通过环境变量注入（见下表），不提交真实凭据

# 3. 编译 & 测试
mvn clean package
mvn test                     # API 级测试（MockMvc + 真实库）

# 4. 运行
mvn spring-boot:run          # 或 java -jar target/smartms-refactor-0.1.0.jar
# 默认端口 8080，健康检查 http://127.0.0.1:8080/actuator/health
```

初始化账号见 `smartconf.sql`（管理员 / 坐席，role_type 0/1）。

### 敏感配置（环境变量注入）

| 变量 | 说明 | 默认 |
|---|---|---|
| `CTI_AGENT_WS` / `CTI_AGENT_SERVER` / `CTI_MONITOR_SERVER` | CTI 服务地址 | `127.0.0.1:8082` |
| `WEBRTC_ASTERISK_HOST` / `WEBRTC_ASTERISK_PORT` / `WEBRTC_ASTERISK_USER` / `WEBRTC_ASTERISK_PASSWORD` | Asterisk SSH（WebRTC 分机管理） | `127.0.0.1` / `22` / `root` / 空 |
| `WEBRTC_MEDIA_ADDRESS` | 应答媒体地址（云 NAT 环境设公网 IP） | 空 |
| `WEBRTC_DTLS_CERT` / `WEBRTC_CONTEXT` | WebRTC DTLS 证书 / 拨号计划 context | `/etc/ssl/smartms-ca/asterisk.pem` / `from-sipreg` |

## 前端

WebRTC 坐席工作台 / 管理台前端（Vue3 + Element Plus）：[smartms-refactor-web](https://github.com/)（Vite dev 代理 `/api` → `127.0.0.1:8080`）。

## 模块与接口

完整接口清单见 [`docs/API.md`](docs/API.md)。模块分布：

| 模块 | 前缀 | 说明 |
|---|---|---|
| 认证 | `/api/auth` | 登录（MD5 存量兼容）/ 当前用户 |
| 菜单 | `/api/menu` | 按角色返回菜单树 |
| 用户/角色 | `/api/users` `/api/roles` | 用户 CRUD / 角色+菜单授权 |
| PBX | `/api/pbx` | 分机 / 中继 / 路由 / 局向 |
| CTI | `/api/cti` | 链路 / 号码 / 参数 / 录音 / 技能组 |
| IVR | `/api/ivr` | 语音服务 / 中继群 / 文件 / 参数 / 路由 |
| 监控 | `/api/monitor` | 实时数据代理（CTI 8091）/ 坐席 / 技能域 |
| 工单 | `/api/work-tasks` | 工单 CRUD / 办结 / 转办 / 我的工单/受理 |
| 交互查询 | `/api/records` | 通话详单 / 未接 4 类 / 我的通话 |
| 客户 | `/api/customers` | CRUD / 回收站 / 弹屏查询 / Excel 导出 |
| 订单 | `/api/orders` | 订单 / 产品 / 我的订单 |
| 质检 | `/api/quality` | 模板 / 待检通话 / 评分定级 / 成绩 |
| 外呼 | `/api/outbound` | 任务聚合 / 号码导入 / 启停 / 详单 |
| 统计 | `/api/report` | 呼入/外呼话务 / 坐席 KPI / 满意度 / 工单量 |
| 智能消息 | `/api/im` | 精准回复 / 自动回复 / 消息记录 / 接入信息 |

## 认证

- 登录接口返回 `{ token, user }`，后续请求带 `Authorization: Bearer <token>`
- `user.roleType`：`0`=管理员（管理台）、`1`=坐席（坐席工作台 + 软电话）
- `user.qualityAgCode`：坐席工号（软电话登入 / 我的数据隔离）

## 测试

```bash
mvn test   # 103 个测试，覆盖 15 个模块的 API 级验证（真实数据库，非 mock）
```

测试约定：所有测试数据使用 `t` 前缀标记，测试结束后自清理，不污染生产数据。

## License

[Apache License 2.0](LICENSE)

## 目录结构

```
src/main/java/com/smt/refactor/
├── common/        # 全局异常 / 统一响应
├── config/        # Security / 分页 / 数据源配置
├── controller/    # REST 控制器（15 个）
├── dto/           # 请求/响应对象
├── entity/        # 57 张表 Entity
├── mapper/        # MyBatis-Plus Mapper
├── security/      # JWT 过滤器 / 工具
├── service/       # 业务逻辑（15 个）
└── utils/         # 工具类
```

## 与老系统的对应关系

| 老系统（JFinal） | 新系统 | 说明 |
|---|---|---|
| `@Permission` + Session | JWT + Security | 无状态化 |
| JSP 页面 | Vue3 前端（见 smartms-refactor-web） | API 化 |
| 菜单 URL（/usermanager 等） | 前端路由深链（?tab=&mine=） | 语义保留 |
| rtdataquery JSONP | `/api/monitor/rtdata-query` | REST 代理，CTI 8091 |
