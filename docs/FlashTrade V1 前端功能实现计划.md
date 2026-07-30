# FlashTrade V1 全栈功能实现计划

## 目的

本计划文档用于把当前系统中三大模块：`flash-trade-api`、`flash-trade-app`、`flash-trade-web` 对齐成一个统一的 QuickTrade V1 平台。要求：

- `flash-trade-app` 现有功能完整保留并接入统一后端接口；
- `flash-trade-web` 现有功能完整保留，但需要映射成本系统的业务模型；
- `flash-trade-api` 提供一致的 REST 接口，支持 App 与 Web 两端。

## 背景

当前前端主界面已接近视觉完成，基于深色 Material 3 桌面风格、紧凑卡片布局、统一间距与可见分隔线。`flash-trade-web` 目前是一个 Vue + Vite 的后台管理界面，包含在仓管理与用户管理等模块。`flash-trade-api` 已具备统一交易聚合后端架构。

下一步重点是实现：认证、Dashboard 数据、持仓/订单/交易、自选股、Quote 搜索等 V1 核心功能，并确保三端之间功能对应、接口统一。

## 适用范围

本计划覆盖：

- `flash-trade-api`：统一后端接口与数据服务层。
- `flash-trade-app`：Flutter 桌面客户端，现有功能完整保留并继续迭代。
- `flash-trade-web`：Vue Web 管理后台，现有功能完整保留并按本系统业务重映射。

## 系统对应关系

- `flash-trade-app`：主交易桌面终端，侧重 Dashboard、Watchlist、Trade、Portfolio、Orders 等用户交易体验。现有功能必须保留，不做删减。
- `flash-trade-web`：Web 管理后台，现有功能必须保留，按本系统业务模型映射成统一接口和管理视图。包括登录、在仓管理、用户管理、用户增删改查等功能。
- `flash-trade-api`：统一 REST 接口层，服务 App 与 Web 两端。既要支持 `flash-trade-app` 的交易和行情功能，也要支持 `flash-trade-web` 的管理功能。

## 三端对应策略

| 系统 | 现有功能 | 对应目标 | 说明 |
| --- | --- | --- | --- |
| flash-trade-app | Dashboard、Watchlist、Trade、Portfolio、Orders、Login | 用户交易终端 | 现有功能完整保留，接入统一后端接口；UI 风格统一为深色桌面卡片风格。 |
| flash-trade-web | Login、Dashboard、PositionManage、UserList/UserCreate/UserEdit/UserView | 管理后台 | 保留现有页面结构与功能，改为调用本系统统一 API；必要时扩展后台接口支持用户与风控管理。 |
| flash-trade-api | Auth、Portfolio、Orders、Trade、Market/Quote、User/Auth 管理 | 统一后端 | 提供 `/api/v1/*` 规范接口，满足 App 与 Web 数据需求。 |

## 保留原则

- `flash-trade-app` 现有功能不删减。任何功能调整只能增加或优化，不可砍掉已存在业务模块。
- `flash-trade-web` 现有功能全部保留，并映射到当前 QuickTrade 系统模型。若部分功能名或路径与系统有差异，优先以业务等价方式对齐。
- `flash-trade-api` 作为统一接口层，要兼容 App 的交易、行情与账户接口，同时兼容 Web 的管理/用户接口。

## 参考文档

- `docs/QuickTrade V1设计文档（copliot整理）/03-功能清单.md`
- `docs/QuickTrade V1设计文档（copliot整理）/09-接口设计说明.md`
- `flash-trade-app/lib/features/desktop/desktop_dashboard.dart`
- `flash-trade-app/lib/app.dart`
- `flash-trade-app/lib/features/*`

## V1 功能范围

### 必须完成
- 登录 / 会话管理
- 实时报价与股票搜索
- 自选股 Watchlist 管理
- 买入 / 卖出订单下单
- 撤单 / 订单列表 / 成交历史
- 当前持仓 / 浮盈亏 / 资产摘要
- 主界面 Dashboard 模块化显示
- 底部状态条显示主要指数与连接/更新时间

### V1 不包含
- K 线图
- 分时图
- Level 2 市场深度
- Scanner / News / Alerts
- 期权链

## 目标风格

- 深色卡片背景：`AppColors.cardBg`
- 统一卡片边框与边距：`AppColors.border`，突出层级关系
- 紧凑间距：主要使用 5px，较大间距控制在 10px
- 圆角风格统一：卡片 10~16px，按钮/输入框 10px
- 统一文本等级：标题、字段值、提示语风格一致
- 统一图标与交互按钮风格：浅色图标、hover/active 风格一致

## 开发阶段

### 阶段 1：基础架构校准
- [ ] 确认项目路由：`app.dart` 中的 `/home/dashboard`、`/home/portfolio`、`/home/trade`、`/home/orders`
- [ ] 确认 `main.dart` 与 `window_manager` 桌面窗口配置
- [ ] 确认主题与 UI 基线：`app.dart` 主题设置、`AppColors`
- [ ] 确认 Demo 模式开关与 mock 数据入口

### 阶段 2：认证与会话
- [ ] 完善登录页面 `login_screen.dart`
- [ ] 验证 `auth_provider.dart` 登录/登出流程
- [ ] 验证 `ApiInterceptor` token 注入与 401 刷新逻辑
- [ ] 确保登录后进入 `/home/dashboard`

### 阶段 3：Dashboard 数据模块
- [ ] 设计并实现 WatchList 数据 provider
- [ ] 设计并实现 Quote / Search 数据 provider
- [ ] 设计并实现 Account Summary / Buying Power / Cash 数据 provider
- [ ] 设计并实现市场状态与指数底部状态条数据 provider
- [ ] 将 `desktop_dashboard.dart` 各区域与 provider 连接
- [ ] 处理加载中 / 错误提示展示

### 阶段 4：交易与订单
- [ ] 验证 `trade_provider.dart` 下单逻辑
- [ ] 实现 `TradeScreen` V1 下单交互（BUY/SELL、Market/LIMIT、Qty、Price、TIF）
- [ ] 提交订单后刷新 `ordersProvider`
- [ ] 实现 `OrdersScreen` 的订单列表、成交历史、取消操作
- [ ] Dashboard 中显示简化订单摘要与“View All Orders”入口

### 阶段 5：持仓与资产展示
- [ ] 验证 `portfolio_provider.dart` 及 `PortfolioScreen`
- [ ] 实现持仓列表展示与总资产汇总
- [ ] Dashboard 中补齐 Positions / Account Summary 模块
- [ ] 资产摘要数据与 Portfolio 页面保持一致

### 阶段 6：Watchlist 与搜索体验
- [ ] 在 Dashboard 搜索框中增加 symbol 搜索逻辑
- [ ] 实现 Watchlist 行项点击切换 symbol
- [ ] 实现 Watchlist 的新增/删除操作（如 V1 可用 mock JSON）
- [ ] 完成“Edit Watchlist”入口或页面占位

### 阶段 7：统一样式与收尾
- [ ] 审核所有页面的卡片、边距与文本样式
- [ ] 统一 `AppColors.cardBg` / `AppColors.border` / `AppColors.textSecondary`
- [ ] 统一 5px/10px 间距规则
- [ ] 修复布局异常与组件错位

## 详细 TODO List

### 必做任务
- [ ] 复现并确认 Dashboard 当前 UI 风格
- [ ] 制定 Desktop 主界面的数据模块拆分方案
- [ ] 定义并实现 Demo 数据 provider
- [ ] 完成登录与会话状态流
- [ ] 打通 Watchlist / Quote / Account Summary / Status Bar
- [ ] 完成 Trade 下单，并验证订单列表刷新
- [ ] 完成 Portfolio 持仓页面并确保与 Dashboard 数据一致
- [ ] 完成 Orders 页面未成交与成交历史展示
- [ ] 编写界面级错误与 loading 展示样式
- [ ] 运行 `flutter analyze` 并修复问题

### 次要任务
- [ ] 将 `desktop_dashboard.dart` 拆分为更小的可复用组件
- [ ] 统一按钮、输入框、标签、卡片组件样式
- [ ] 书写组件注释与文档说明
- [ ] 在 `docs` 中补充最终 UI 与功能实现说明

## 验收标准

- 登录后可进入 Dashboard
- Dashboard 展示 Watchlist、行情、买卖力、账户信息、持仓/订单汇总、底部状态
- 订单下单成功后 Order 列表能看到新订单
- Portfolio 页面显示持仓与资产汇总
- Orders 页面可过滤 pending/filled
- 所有页面样式与主界面风格一致
- `flutter analyze` 无错误

## 交付文件

- `docs/FlashTrade V1 前端功能实现计划.md`
- `flash-trade-app/lib/features/desktop/desktop_dashboard.dart`
- `flash-trade-app/lib/app.dart`
- `flash-trade-app/lib/features/auth/**`
- `flash-trade-app/lib/features/trade/**`
- `flash-trade-app/lib/features/orders/**`
- `flash-trade-app/lib/features/portfolio/**`
- `flash-trade-app/lib/core/**`
