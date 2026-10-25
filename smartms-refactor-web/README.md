# smartms-refactor-web — SmartCC 前端（Vue3）

呼叫中心管理系统前端，重构自老系统 JSP + jQuery + Bootstrap 2.x。

## 技术栈

- **Vite 5** + **Vue 3.4**（`<script setup>`）
- **Element Plus 2.7**（UI 组件库）
- **Pinia**（状态管理：auth / menus）
- **Vue Router 4**（路由守卫 + 深链）
- **Axios**（统一拦截器：token 注入 / 401 跳登录 / 错误 toast）
- **ECharts 5**（运营驾驶舱 / 统计图表）

## 快速开始

```bash
npm install
npm run dev        # http://localhost:3000（/api 代理到 127.0.0.1:8080）
npm run build      # 产物 dist/
npm run preview    # 本地预览产物
```

默认账号：`admin / admin123`（管理台）、`3004 / 123456`（坐席工作台）。

## 架构

```
src/
├── api/              # 后端接口封装（按模块拆分，统一走 http.js）
│   ├── auth.js user.js role.js pbx.js config.js ...
├── components/       # 通用组件（EChart.vue 图表封装）
├── layout/           # MainLayout（管理台框架：动态侧边菜单 + 顶栏）
├── router/           # 路由 + 守卫（坐席角色进入 /agent-work）
├── stores/           # Pinia（auth：token/user/menus，isAgent/agentCode getter）
├── utils/http.js     # Axios 实例 + 拦截器
└── views/
    ├── login/        # 登录页（按 roleType 分流：坐席→工作台 / 管理→管理台）
    ├── dashboard/    # 运营驾驶舱（统计卡片 + 话务趋势 + 满意度 + 待办工单）
    ├── agent/        # 坐席工作台（软电话面板 + 弹屏 + 快捷入口）
    ├── monitor/      # 实时监控（5 类看板）
    ├── system/       # 用户 / 角色 / 服务配置
    ├── crm/          # 客户管理
    ├── ...           # 工单/通话/质检/订单/外呼/统计/智能消息/PBX/CTI/IVR
```

## 关键设计

### 菜单动态化
侧边栏由 `/api/menu/tree` 驱动，老系统菜单 URL 通过 `MainLayout.routeOf()` 映射到
Vue 路由（支持 `?tab=&mine=` 深链）：

```
/usermanager          → /system/user
/worktaskinfo/belongMe → /worktask?tab=mine
/record/myCall        → /record?tab=record&mine=true
```

**新增页面必须遵守**：在 `routeOf` 注册映射 + 页面 onMounted 读取 `route.query.tab/mine`。

### 坐席模式（软电话）
- 坐席（`roleType=1`）登录后自动进入 `/agent-work` 坐席工作台
- 软电话面板翻译老系统 `softphone_constants.js` 状态机（0-10 状态）+ 拨号盘 + 技能组登入
- 来电弹屏：`/api/customers/dial-search`
- ⚠️ 实时通话链路（CTI 8091 + WebRTC/SIP）为 stage4 待办，页面已留接入点

### 数据隔离
"我的"系列（我的工单/受理/通话/客户/订单/成绩/任务）由后端按登录用户
`qualityAgCode` 自动过滤，前端只需传 `agentId=my` 或 `belongAgent=my`。

## 部署（Nginx）

```bash
npm run build
# dist/ 产物 + 反向代理 /api 到后端
```

Nginx 配置示例见 [`deploy/nginx.conf`](deploy/nginx.conf)。

## 目录约定

- `views/xxx/Xxx.vue`（二级目录）用 `../../api/` 导入；`views/a/b/Xxx.vue`（三级）用 `../../../api/`
- 测试/示例数据统一带 `t` 前缀，避免污染生产
