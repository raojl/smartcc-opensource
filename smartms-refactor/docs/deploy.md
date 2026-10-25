# SmartCC 部署指南

前后端分离部署。后端 Java 8 服务，前端静态资源由 Nginx 托管 + 反向代理。

## 1. 后端部署

### 环境要求
- JDK 8+
- MySQL 5.1+ / MariaDB（导入 `smartconf.sql`、`smartcc.sql`）

### 打包
```bash
cd smartms-refactor
mvn clean package -DskipTests
# 产物：target/smartms-refactor-0.1.0.jar
```

### 配置
编辑 `src/main/resources/application.properties`（或外部化 `--spring.config.location=`）：

```properties
# 主库（cc_core_*/pbx_*）
jdbcUrl=jdbc:mysql://<主库IP>:3306/smartconf?useUnicode=true&characterEncoding=utf8
# 业务库（base_*/crm_*/im_*）
dbhost=<业务库IP>
dbname=smartcc
dbuser=xxx
dbpassword=xxx
```

### 启动
```bash
java -jar smartms-refactor-0.1.0.jar --server.port=8080
# 健康检查：curl http://127.0.0.1:8080/actuator/health
```

> 生产环境建议：`nohup java -jar ... > run.log 2>&1 &`，或 systemd 托管。

## 2. 前端部署

### 构建
```bash
cd smartms-refactor-web
npm install
npm run build        # 产物 dist/
```

### Nginx（参考 deploy/nginx.conf）
```bash
sudo cp -r dist/* /var/www/smartcc/
sudo cp deploy/nginx.conf /etc/nginx/conf.d/smartcc.conf
sudo nginx -s reload
```

### 前端静态文件更新
每次 `npm run build` 后覆盖 `/var/www/smartcc/` 即可（带 hash 文件名自动失效缓存）。

## 3. 联调验证清单

| 项目 | 验证方式 |
|---|---|
| 后端健康 | `GET /actuator/health` → `{"status":"UP"}` |
| 登录 | `POST /api/auth/login` admin/admin123 → 返回 token |
| 菜单 | `GET /api/menu/tree` 带 token → 12 组 43 叶子 |
| 管理台 | 浏览器访问 → admin 登录 → 侧边菜单全部可点 |
| 坐席模式 | 3004/123456 登录 → 自动进入坐席工作台 |
| 弹屏 | 坐席工作台输入 15958006124 → 弹客户卡 |
| 统计图表 | 工作台话务趋势图 / 满意度条形图有数据 |

## 4. 外部依赖

| 服务 | 端口 | 说明 | 不可用时影响 |
|---|---|---|---|
| CTI 实时服务 | 8091 | 坐席状态/实时话务（`/api/monitor/*` 代理） | 监控页容错显示，业务不受影响 |
| Asterisk | — | 语音交换（AMI/SSH 配置下发） | 软电话（stage4） |

## 5. 常见问题

- **`cc_core_sys_user` 不存在**：主库连接串配置成了业务库，检查 `jdbcUrl`。
- **登录提示密码错误**：老系统密码为 MD5 存量，新密码用 BCrypt，系统自动兼容两种。
- **统计无数据**：默认时间窗口取近期，历史数据需手动调整日期范围。
- **监控 400「CTI 实时服务不可达」**：8091 未启动，属预期降级。
