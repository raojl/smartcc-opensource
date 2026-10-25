# SmartCC API 文档

统一前缀：`http://<host>:8080`
认证：`Authorization: Bearer <token>`（除 `/api/auth/login` 外全部需要）

---

## 认证 /auth

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/login` | 登录 `{username, password}` → `{token, user}` |
| GET | `/api/auth/me` | 当前用户信息（userId/userName/roleType/qualityAgCode/roles） |

## 菜单 /menu

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/menu/tree` | 当前用户菜单树（按角色过滤） |

## 用户 /users

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/users` | 分页（loginName/userName/phone 过滤） |
| GET | `/api/users/{id}` | 详情 |
| POST | `/api/users` | 新增（`{loginName,userName,password,roleType,qualityAgCode,userRoleIds[]}`） |
| PUT | `/api/users/{id}` | 编辑 |
| DELETE | `/api/users/{id}` | 删除 |
| POST | `/api/users/{id}/password` | 重置密码 |
| GET | `/api/users/check-login-name` | 登录名查重 |
| GET | `/api/users/check-agent-code` | 工号查重 |

## 角色 /roles

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/roles` | 分页 / 全量 |
| GET | `/api/roles/{id}` | 详情（含菜单 ids） |
| POST | `/api/roles` | 新增 `{roleName,menuIds[]}` |
| PUT | `/api/roles/{id}` | 编辑 |
| DELETE | `/api/roles/{id}` | 删除 |

## PBX /pbx（主库 pbx_*）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/pbx/devices` | 分机分页（id/deviceName/deviceNumber/type 过滤） |
| GET/POST/PUT/DELETE | `/api/pbx/devices[/{id}]` | 分机 CRUD |
| DELETE | `/api/pbx/devices` | 批量删除 `?ids=a,b` |
| GET | `/api/pbx/devices/check-id` | 分机号查重 |
| GET | `/api/pbx/trunks` `/api/pbx/trunks/all` | 中继分页 / 全量 |
| GET/POST/DELETE | `/api/pbx/trunks[/{id}]` | 中继 CRUD |
| GET | `/api/pbx/routes` | 路由分页 |
| GET/POST/DELETE | `/api/pbx/routes[/{id}]` | 路由 CRUD |
| GET | `/api/pbx/trunk-distributes` | 局向分配分页 |
| GET/POST/DELETE | `/api/pbx/trunk-distributes[/{id}]` | 局向分配 CRUD |
| GET | `/api/pbx/trunk-groups` `/all` | 中继群 |

## CTI /cti（业务库）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET/POST/DELETE | `/api/cti/links[/{id}]` | 链路配置 |
| GET/POST/DELETE | `/api/cti/extensions[/{id}]` | 号码配置 |
| GET/POST/DELETE | `/api/cti/profiles[/{id}]` | CTI 参数 |
| GET/POST/DELETE | `/api/cti/records[/{id}]` | 录音配置 |
| GET | `/api/cti/skills?keyword=` | 技能组分页 |
| GET/POST/DELETE | `/api/cti/skills[/{id}]` | 技能组 CRUD |
| GET | `/api/cti/skill-domains` | 技能组领域树 |

## IVR /ivr

| 方法 | 路径 | 说明 |
|---|---|---|
| GET/POST/DELETE | `/api/ivr/services[/{id}]` | 语音服务 |
| GET | `/api/ivr/trunk-groups` `/all` | 中继群 |
| GET/POST/DELETE | `/api/ivr/trunk-groups[/{id}]` | 中继群 CRUD |
| GET | `/api/ivr/files` | 语音文件列表 |
| GET/POST/DELETE | `/api/ivr/files[/{id}]` | 文件 CRUD |
| GET/POST/DELETE | `/api/ivr/params[/{id}]` | IVR 参数 |
| GET/POST/DELETE | `/api/ivr/routes[/{id}]` | IVR 路由 |

## 监控 /monitor（实时，CTI 8091 不可达时返回 400）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/monitor/rtdata-query?queryId=` | 实时数据代理（301=工作台 302=IVR通道 303=座席 304=技能 305=话务 306=整体） |
| GET | `/api/monitor/agents` | 在线坐席 |
| GET | `/api/monitor/agents/idle` | 空闲坐席 |
| GET | `/api/monitor/agents/results?agentId=` | 坐席实时统计 |
| GET | `/api/monitor/agents/dns?agentDN=` | 坐席 DN |
| GET | `/api/monitor/skill-domains` | 技能域（本地库） |

## 工单 /work-tasks（crm_worktaskinfo）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/work-tasks` | 分页：workId/customName/status/belongAgent/workAgent(="my"=我的)/时间范围 |
| GET | `/api/work-tasks/{workId}` | 详情（含客户名/手机号/耗时） |
| POST | `/api/work-tasks` | 新增（customName 存在时反查 custom_id） |
| PUT | `/api/work-tasks/{workId}` | 编辑（状态 3 写 close_time） |
| POST | `/api/work-tasks/{workId}/finish` | 办结 |
| POST | `/api/work-tasks/{workId}/transfer` | 转办 `{agentId}` |
| GET | `/api/work-tasks/agents?searchKey=` | 坐席候选 |

## 交互查询 /records（base_talkinfo）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/records` | 通话详单：agentId(="my")/deviceid/ani/dnis/时间/时长 |
| GET | `/api/records/missed?type=` | 未接：1=坐席 2=队列 3=客户 4=留言；agentId="my"=我的未接 |

## 客户 /customers（crm_custominfo）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/customers` | 分页：姓名/手机/公司/QQ/性别；agentId="my"；deleted=true=回收站 |
| GET | `/api/customers/{customId}` | 详情 |
| POST | `/api/customers` | 新增（UUID 主键）/ 编辑 |
| DELETE | `/api/customers/{customId}` | 软删除 |
| POST | `/api/customers/{customId}/restore` | 恢复 |
| GET | `/api/customers/dial-search?callNumber=` | 坐席弹屏查询 |
| GET | `/api/customers/export?size=` | Excel 导出（POI xlsx） |

## 订单 /orders（crm_orderinfo + crm_productsinfo）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/orders` | 分页：no/name/product/status/agentId(="my")/时间 |
| GET | `/api/orders/{id}` | 详情 |
| POST | `/api/orders` | 新增/编辑 |
| DELETE | `/api/orders/{id}` | 删除 |
| GET | `/api/orders/by-custom/{customId}` | 客户订单面板 |
| GET | `/api/orders/products` `/all` | 产品分页 / 全量 |
| GET/POST/DELETE | `/api/orders/products[/{id}]` | 产品 CRUD |

## 质检 /quality（crm_quality_*）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/quality/templates` | 模板分页 |
| POST | `/api/quality/templates` | 模板保存（excellent>fine>pass + 权重≤100 校验） |
| POST | `/api/quality/templates/{id}/status` | 启停用 |
| GET | `/api/quality/templates/active` | 启用模板（下拉） |
| GET | `/api/quality/calls?startTime=&endTime=` | 待质检通话 |
| POST | `/api/quality/results/generate` | 评分生成 `{contactId,agentId,talkStartTime,templateId,scoreItems[]}` |
| DELETE | `/api/quality/results/{id}` | 撤销 |
| GET | `/api/quality/results` | 成绩分页：agentId="my"=我的成绩 |
| GET | `/api/quality/results/{id}` | 成绩详情 |

## 外呼 /outbound（crm_outbound_task_info）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/outbound/tasks/import` | 号码导入 `{numbers, taskDesc, agentIds[]}`（逗号分隔 + 平均分配坐席） |
| GET | `/api/outbound/tasks` | 任务聚合列表（完成率/接通率）；agentId="my"=我的任务 |
| POST | `/api/outbound/tasks/{taskId}/start` `/stop` | 启停 |
| DELETE | `/api/outbound/tasks/{taskId}` | 删除 |
| GET | `/api/outbound/details` | 任务详单；agentId="my"=我的详单 |

## 统计 /report

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/report/talk/inbound` | 呼入话务：timeType=day/month/year/range + timeVal/timeStartVal/timeEndVal |
| GET | `/api/report/talk/outbound` | 外呼话务 |
| GET | `/api/report/talk/agent-kpi` | 坐席 KPI（join base_agentinfo） |
| GET | `/api/report/satisfaction?startTime=&endTime=` | 满意度统计 |
| GET | `/api/report/work-task` | 工单量统计 |
| GET | `/api/report/inbound` | 呼入统计（依赖 CallTrack 列，缺列时容错返回空） |

## 智能消息 /im（im_* 表）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/im/topics?keyword=` | 精准回复主题列表 |
| POST | `/api/im/topics` | 保存主题 |
| DELETE | `/api/im/topics/{id}` | 删除主题 |
| GET | `/api/im/replies?keyword=` | 自动回复知识（含关键词聚合） |
| POST | `/api/im/replies` | 保存知识 |
| DELETE | `/api/im/replies/{id}` | 删除知识 |
| GET | `/api/im/records?keyword=&limit=` | 消息记录（im_msg_received，缺表容错） |
| GET | `/api/im/interfaces` | 接入信息列表 |
| POST | `/api/im/interfaces` | 保存接入信息 |
| DELETE | `/api/im/interfaces/{id}` | 删除接入信息 |

## 分页约定

分页接口统一返回：

```json
{ "list": [...], "total": 0, "page": 1, "size": 15, "totalPages": 0 }
```

## 错误约定

```json
{ "code": 400, "message": "错误描述" }
```

| HTTP | code | 场景 |
|---|---|---|
| 400 | 400 | 业务校验失败 |
| 401 | 401 | 未登录 / token 过期 |
| 404 | 404 | 资源不存在 |
| 500 | 500 | 服务端异常 |
