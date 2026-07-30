# 17-WebAdmin 后台管理端设计

## 1. 目标与定位

`flash-trade-web` 是当前系统的后台管理端，面向内部管理员和风控人员。它不是交易客户端，而是用于管理、监控、审计、风控和运维的 WebAdmin 控制台。

设计目标：
- 保留现有系统已有的后台管理功能
- 统一前端页面与后端管理接口
- 明确页面结构、功能模块与权限边界
- 支持快速迭代后续日志、风控、配置和 RBAC 扩展

## 2. 当前系统现状

### 前端现状
`flash-trade-web` 当前实现基于 Vue 3 + TypeScript + Vite。

当前页面与路由：
- `/login`：管理员登录页面
- `/dashboard`：后台主容器
- `/dashboard/positions`：在仓管理
- `/dashboard/users`：用户管理（包含用户列表、新建、编辑、预览）

当前后台页导航显示：
- 在仓管理
- 用户管理（部分页面已实现）
- 业绩管理（留空/待实装）

前端请求基础路径：
- `baseURL: '/QuickTrade/web'`
- JWT Token 存储在 `localStorage.token`
- Authorization header：`Bearer <token>`

### 后端现状
`flash-trade-api` 提供了管理端相关接口：
- `GET /admin/positions`：全量仓位监控
- `GET /admin/users`：系统用户列表
- `GET /admin/logs/api`：API 调用日志
- `GET /admin/logs/operations`：操作日志
- `GET /admin/logs/login`：登录日志
- `GET /admin/logs/errors`：错误日志
- `GET /admin/risk/status`：账户风控状态
- `POST /admin/risk/disable`：关停账户
- `POST /admin/risk/enable`：恢复账户
- `POST /admin/risk/limit`：限制账户交易
- `POST /admin/risk/liquidate`：强制清仓

后端路由说明：
- 管理类接口集中于 `/admin` 和 `/admin/risk`
- 管理接口需 `ROLE_ADMIN` 权限
- 当前管理页面登录接口尚未与后端 `/api/v1/auth/login` 完全集成

## 3. WebAdmin 功能模块

### 3.1 登录与认证
- 管理员登录页
- 支持 JWT 登录与登出
- 登录失败提示/网络异常提示
- 登录后跳转到管理控制台首页

### 3.2 仪表盘 + 在仓管理
- 核心监控模块
- 按用户分组展示仓位
- 显示仓位汇总数据：当前毛利、出场毛利、总净利、总股数
- 支持“折叠/展开”用户仓位
- 支持“一键平仓”操作

### 3.3 用户管理
- 用户列表
- 新建用户
- 编辑用户
- 预览用户
- 启用/停用账户
- 修改用户密码

### 3.4 风控管理
- 风控状态查询
- 修改账户风险状态：关停 / 恢复 / 限制 / 强制清仓
- 未来可补充：风控规则管理、触发原因展示、风险事件通知

### 3.5 日志与审计
- API 调用日志
- 操作日志
- 登录日志
- 错误日志
- 未来可补充：搜索、过滤、导出、报警规则

### 3.6 系统管理（建议）
- 管理员账户与 RBAC
- 角色、公司、组、Scope 管理
- 券商配置、策略费率配置
- 系统参数与告警阈值

## 4. 当前页面和接口映射

| 页面 | 前端路由 | 后端接口 | 说明 |
|---|---|---|---|
| 登录 | `/login` | `POST /api/v1/auth/login` 或 `POST /admin/login`（需统一） | 管理员登录，返回 JWT Token |
| 在仓管理 | `/dashboard/positions` | `GET /admin/positions` | 返回系统内所有持仓，按 user/account 归并 |
| 用户管理 | `/dashboard/users` | `GET /admin/users` | 返回系统用户列表 |
| 用户编辑 | `/dashboard/users/:id/edit` | `PUT /admin/users/:id` 或 `POST /users/{id}/password` | 编辑用户信息 / 改密码 |
| 用户预览 | `/dashboard/users/:id/view` | `GET /admin/users/:id` | 查看用户详情 |
| 风控查询 | 待补 | `GET /admin/risk/status` | 查询账户风控状态 |
| 风控操作 | 待补 | `POST /admin/risk/disable|enable|limit|liquidate` | 账户关停、恢复、限制、清仓 |
| 日志查询 | 待补 | `GET /admin/logs/api` / `operations` / `login` / `errors` | 日志审计视图 |

## 5. 问题与改进点

### 5.1 登录接口不一致
当前 `Login.vue` 使用 `/admin/login`，但后端实际认证接口是 `/api/v1/auth/login`。
建议：
- 方案 A：修改前端为 `/api/v1/auth/login`
- 方案 B：后端新增 `/admin/login` 管理端入口

### 5.2 用户管理接口缺口
当前前端实现依赖更多用户 CRUD 接口，如：
- `GET /users/{id}`
- `PUT /users/{id}`
- `POST /users/status`
- `POST /users/{id}/password`

建议补全后台用户管理 REST API，或改造前端仅使用 `/admin/users`与统一管理接口。

### 5.3 风控与日志界面未落地
前端已有用户与仓位管理，后台系统应补全：
- 风控操作页面
- 日志查询页面
- 角色权限与 Scope 控制页面

### 5.4 页面结构建议
当前侧栏仅显示“在仓管理”和“用户管理/业绩管理”。
建议保留现有功能，并扩展为：
- 仪表盘
- 在仓监控
- 用户管理
- 风控管理
- 日志审计
- 系统配置

## 6. WebAdmin 设计方案

### 6.1 保留现有功能
当前系统已实现的功能必须保留：
- 管理员登录
- 在仓监控与一键平仓
- 用户列表与启用/禁用
- 用户新增与编辑
- 密码修改
- 基础 `ROLE_ADMIN` 保护的管理接口

### 6.2 扩展能力
建议新增或分阶段实现：
- 管理员权限与角色组
- 管理端日志审计视图
- 账户风控操作面板
- 帐户与券商关系表、Scope 过滤
- 后端管理 API 文档化与 Swagger 可视化

### 6.3 统一接口与静态部署
`flash-trade-web` 通过 `vite build` 构建静态文件，输出到：
- `flash-trade-web/dist`，并复制到 `flash-trade-api/src/main/resources/static/admin`

建议保持：
- WebAdmin 静态资源托管在 `flash-trade-api` 内部
- 前端路由前缀 `/quicktrade/admin/`
- 后端接口前缀 `/admin` + `/admin/risk`

### 6.4 推荐 WebAdmin 页面目录
- 登录页
- 仪表盘
- 在仓监控
- 用户管理
- 风控管理
- 日志审计
- 系统配置

## 7. 交付输出

建议将本设计整理为 WebAdmin 文档并纳入项目文档库：
- `docs/QuickTrade V1设计文档（copliot整理）/17-WebAdmin后台管理端设计.md`
- 未来可将此文档拆分成“WebAdmin 需求”、“WebAdmin 接口契约”、“WebAdmin 数据模型”三部分

## 8. 结论

`flash-trade-web` 应定位为后台管理端，并保留现有在仓管理、用户管理功能。
当前系统已具备管理接口的基础能力，但需要补齐登录认证一致性、用户 CRUD、风控页面和日志审计页面。

后续方案应以“管理端功能完整性”为核心，优先补齐：
- 登录/认证一致性
- 用户管理 CRUD
- 仓位监控与风控操作
- 日志审计页
- RBAC/Scope 管理
