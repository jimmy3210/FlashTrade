# FlashTrade 阶段 0：现状核验与差距追踪

**核验日期**：2026-11-20  
**状态**：进行中；角色/Scope 基线已由用户确认，阶段 1/2 的公司 Scope 首个后端切片已实现并通过定向测试；其他资源端点、隔离数据库证据及产品数据保留责任仍待落实。  
**依据**：[全栈产品化开发计划](./FlashTrade全栈产品化实施计划.md) 阶段 0；本清单描述当前工作区证据，不代表部署环境或真实账户已验收。

## 1. 范围边界

- 当前只在本地/隔离 Mock 测试；不连接券商 API、真实账户或生产数据。API 默认允许模式为 `MOCK`，开发/测试 profile 也强制 `MOCK`。
- 用户此前确定的券商顺序保留在全栈计划的阶段 8：Tastytrade、Binance、Schwab、E*TRADE、Tastyworks。它不改变阶段 0–7 的项目依赖顺序，也不授权启用真实 API。
- 下表“已实现”仅表示在代码中找到实现；未通过相应集成/安全验收的能力标为“部分”或“未验证”。

## 2. 功能追踪基线

| 功能域 | 当前证据 | 状态与差距 |
|---|---|---|
| 认证与会话 | API 提供 JSON 客户端登录/刷新/登出及 Web Cookie 登录/刷新/登出；Flutter 使用 JSON 令牌对，Web 使用 HttpOnly Refresh Cookie | **部分**。Flutter 使用模拟 HTTP Adapter 验证登录、401 刷新、令牌对轮换、读取重试及登出；API 使用 MockMvc 验证 JSON 与 Web 会话流程、旧令牌重用拒绝、CSRF 失败、Cookie 属性和登出撤销。Redis、用户库和凭证认证仍为测试替身；真实 DB/Redis 部署端到端仍未验收。 |
| 用户、券商账户与授权 | API 有用户及账户管理、绑定端点；V28 增加 `users.company_id` 和内部账户 Scope 主键 | **部分/未验收**。公司管理员可读取本公司用户与账户；用户列表/详情按 `users.company_id` 过滤，账户列表/详情/用户当前绑定按账户所有者公司过滤，并避免返回其他公司的绑定用户名。用户、账户的创建/更新/停用/密码/绑定等写操作仍限平台管理员。缺失公司映射及跨公司详情默认拒绝；历史 `ROLE_ADMIN`/字符串 Scope 未自动回填或信任。新用户创建请求尚无公司归属字段，未映射用户对公司管理员不可见。28 项定向 Scope/MVC 测试通过，但 Flyway 尚未在隔离 MySQL 验证。账户环境字段 `LIVE`/`SANDBOX` 与部署券商模式是不同配置维度。 |
| 报价与搜索 | API 有股票搜索端点及市场数据服务；Flutter 搜索走 API Provider | **部分**。可确认股票搜索代码存在；未确认有完整、端到端可用的报价查询契约。Flutter Dashboard 底部指数显示固定值，买卖价等字段仍为空/占位。 |
| Watchlist | API 有按当前用户查询、添加、删除；数据使用用户 ID 持久化 | **部分**。Flutter 已实现查询/添加/搜索选择，但未见页面调用删除操作；远端券商 Watchlist 同步不等于本地列表闭环已验收。 |
| 下单、订单与成交 | API 有下单、撤单、订单状态查询和订单/成交查询；请求有 `clientOrderId`；V27 增加本地唯一索引；Tastytrade Mock 已支持账户隔离的下单、查单、撤单 | **部分**。Redis `SUBMITTING` 协调和本地成功订单持久化已有代码，但券商调用前没有持久化订单意图；调用异常后的 `UNKNOWN` 状态及恢复/对账路径尚未形成可验证闭环。Flutter 下单 UI 未提交 TIF。 |
| 持仓与账户摘要 | API 提供持仓、订单、成交及资产视图读取；Flutter 页面和 Dashboard Provider 存在 | **部分**。现金/购买力当前显示未提供；Dashboard 与 Portfolio 汇总一致性未联调。 |
| Web 管理工作流 | Web 有登录、用户/账户管理、订单/持仓/审计页面；风控变更和清仓操作处于禁用状态 | **部分**。页面/调用存在不代表服务端 Scope、审计持久性已验收；用户及账户列表当前为客户端分页。 |
| 风控与高风险操作 | API 有管理员风控查询；状态变更在账户 Scope 能力补齐前拒绝，清仓明确暂停 | **安全失败/未完成**。不能在缺少服务端账户范围授权和结果核验时开放写操作。 |
| 主题与偏好 | Flutter 当前固定深色主题 | **首发沿用现状**。主题系统、节日皮肤和跨设备偏好同步已确认延期。 |
| Mock 与隔离测试 | 独立 `flash-trade-mock` 有多券商静态策略和 HTTP 测试；API 适配器还存在进程内 Mock 分支 | **部分**。两种 Mock 数据/状态来源并存；独立 Mock 订单创建/撤销与静态订单列表并非统一的持久状态机，需在契约阶段确定权威路径和失败场景。 |
| 数据库与迁移 | 仓库含 V1–V28 Flyway 脚本；V28 新增用户公司归属和账户 Scope 内部主键 | **未验证部署状态**。本次没有连接数据库；未核实 V28 或目标环境已应用迁移、运行时 schema、新库安装或历史版本升级结果。 |
| 运行与 CI | 仓库有 API、Mock、Flutter、Web 的 CI 工作流及容器配置 | **部分**。本地没有执行完整 CI；Web 构建会复制静态资源到 API 目录，本次为避免覆盖工作区现有静态资源未运行构建。 |

### 2.1 Must 流程追踪矩阵（初始基线）

“当前证据”只表示仓库中找到对应实现；矩阵中的未验收项必须在对应阶段以隔离测试补证。角色/Scope 以阶段 1 的冻结矩阵为准，所有目标账户归属由后端从已认证身份解析。

| ID / Must 流程 | 角色与 Scope | 页面/状态 | API / Service / Adapter | DB / 缓存 | 验收缺口与阶段归属 | 实现归属 |
|---|---|---|---|---|---|---|
| AUTH-01 登录、会话恢复/失效、登出 | 未认证登录；刷新时轮换令牌；登出时校验 Refresh Token 与当前 Access Token 的主体一致 | Flutter JSON 登录及令牌存储；Web 内存 Access Token、HttpOnly Refresh Cookie、CSRF 与路由守卫 | JSON `/api/v1/auth/{login,refresh,me,logout}`；Web `/api/v1/auth/web/{csrf,login,refresh,logout}`；`AuthService`、JWT/Redis | `users`、会话/Redis | `AuthSessionIntegrationTest` 覆盖 JSON 与 Web 登录/刷新、缺失/无效 CSRF、缺少 Cookie、旧 Refresh Token 重用拒绝、登出撤销令牌与 Cookie 清除；使用内存 Redis 替身及 mock 用户库/认证管理器。真实 DB/Redis 及部署端到端仍未验收。阶段 2、4 | API + Flutter + Web |
| ACCT-01 当前授权账户、余额/购买力、持仓摘要 | 已认证用户；公司/租户、用户、账户 Scope 待阶段 1 冻结并由后端校验 | Flutter Dashboard、Portfolio；Web 持仓监控 | `/api/v1/trade/current-account`、`/api/v1/portfolio/{positions,asset-view}`；`PortfolioService`、Broker Adapter | `t_broker_account`、`positions`；余额/购买力来源待确认 | 当前账户归属尚未证明覆盖所有端点；现金/购买力未提供，Dashboard 汇总未联调。阶段 1–4 | API + Flutter + Web |
| MKT-01 标的搜索、行情与不可用/过期反馈 | 已认证用户；不信任客户端传入账户/Scope | Flutter 行情、搜索；Web 无首发行情管理要求 | `/api/v1/stocks/search`；`StockController`、`MarketDataService`、Mock/Adapter | `t_stock`、Mock 数据 | 搜索存在，完整报价契约、数据时间和失败/过期状态未验收；Dashboard 有固定指数值。阶段 1、3–4 | API + Mock + Flutter |
| WAT-01 自选列表查看、添加、删除 | 当前用户拥有其列表；所有权须由服务端确定 | Flutter Watchlist 查看、添加；删除入口未见 | `/api/v1/watchlist` GET/POST、`/api/v1/watchlist/{symbol}` DELETE；`WatchlistService` | `watchlists` | Flutter 删除流程未验收；验证跨用户读写拒绝、重复添加和错误状态。阶段 2–4 | API + Flutter |
| ORD-01 订单预览/确认、下单、撤单、结果未知 | 已认证用户 + 公司/用户/账户 Scope、绑定账户、风控和订单状态 | Flutter 下单、订单列表/详情；危险写操作需确认 | `/api/v1/trade/order` POST/DELETE、订单状态查询；`TradeExecutionService`、风险服务、Broker Adapter | `orders`、`trades`、Redis 协调、幂等键 | 现有 Redis `SUBMITTING` 后才持久化成功订单；调用前意图、异常后的 `UNKNOWN` 与对账未闭环；禁止自动盲重试。阶段 1–4 | API + Mock + Flutter |
| ORD-02 订单/成交历史及持仓查询 | 用户只能查看授权账户范围 | Flutter 订单/持仓；Web 订单/持仓监控 | `/api/v1/orders`、`/api/v1/trades`、`/api/v1/portfolio/positions`；`PortfolioController`/`PortfolioService` | `orders`、`trades`、`positions` | 分页、筛选、账户切换、端到端归属及结果未知呈现未完成验收。阶段 1–4 | API + Flutter + Web |
| ADM-01 管理员认证、用户/公司/账户管理与绑定 | 平台管理员跨公司；历史 `ROLE_ADMIN` 只作为有明确公司 Scope 的公司管理员；默认拒绝 | Web 用户、公司、券商账户及绑定页面；无权/失败状态 | `/users/**`、`/admin/broker-accounts/**`、`/api/v1/admin/companies/**`；相应管理服务 | V28：`users.company_id`、`t_user_scope.target_broker_account_id`；`t_broker_account.user_id`、`t_company` | 公司管理员的用户/账户 GET 端点已按公司 Scope 过滤；所有创建、更新、停用、密码和绑定写操作仍限平台管理员。跨公司详情按未找到处理，避免资源枚举；无公司映射默认拒绝。新用户创建契约尚不能设置公司归属；历史数据未回填，Flyway 未在隔离 DB 验证。阶段 1–2 | API + Web |
| ADM-02 订单/持仓/审计监控与安全失败的风控入口 | 公司管理员按公司；风控操作员按显式账户且只读；审计员按显式资源只读 | Web 监控、审计；风控写入口保持拒绝 | `/admin/orders/**`、`/admin/audit/**`、`/admin/risk/**`、管理员 `/positions` | 订单/持仓、审计日志表、`t_user_scope` | 当前相关管理端点暂限平台管理员；公司级数据过滤、账户显式授权、系统日志隔离、服务端分页与审计范围尚未落实。风控写操作继续拒绝；旧字符串授权不生效。阶段 1–2 | API + Web |
| MOCK-01 可重复隔离 Mock 纵向闭环 | 仅虚构用户/账户；外部 API 总开关关闭 | Flutter/Web 全流程经 QuickTrade API | API Adapter Mock 与 `flash-trade-mock` 服务 | 当前 Mock 状态来源分散；订单数据非统一持久状态机 | 统一权威路径并覆盖成功、拒绝、超时/未知、限流、部分失败；验证绝不回退真实 Adapter。阶段 2–4 | API + Mock + 客户端 |
| DATA-01 新库初始化与既有版本升级 | 仅隔离测试数据库 | 无直接 UI | Flyway V1–V27、实体及 Repository | MySQL/Flyway；目标环境迁移历史未知 | 不连接真实/生产 DB；需指定隔离测试 DB 并验证新建、升级和数据保留/清理策略。负责人：后端开发 + 运维；保留期限由产品/法务/运维确认。阶段 1–3 | API + 运维 + 产品 |

### 2.2 阶段 1 已确认的授权基线

| 角色 | 首发授权范围 |
|---|---|
| `ROLE_SUPER_ADMIN` | 唯一跨公司平台角色；可查看系统级日志 |
| `ROLE_COMPANY_ADMIN` | 仅限明确归属公司；历史 `ROLE_ADMIN` 仅作此角色兼容，不自动提升为平台管理员；无公司 Scope 默认拒绝 |
| `ROLE_RISK_OPERATOR` | 显式账户授权的只读监控；本阶段所有风控写操作保持拒绝 |
| `ROLE_AUDITOR` | 显式授权资源关联的审计日志只读；系统级日志仅平台管理员可见 |
| `ROLE_TRADER` / `ROLE_VIEWER` | 保持用户自身数据范围，不获得管理端跨用户访问 |

公司模型采用单公司用户，券商账户继承其所有者的公司归属；`t_group` 暂不作为授权来源。风险/审计账户授权按内部券商账户主键解析。V28 已新增 nullable `users.company_id` 和 `t_user_scope.target_broker_account_id`，不自动回填用户或将历史 `target_account_id` 字符串映射为有效授权。公司列表/详情及用户、账户读取端点已按公司 Scope 过滤；用户/账户写操作和其他管理资源仍限平台管理员。用户创建契约尚不能设置公司归属，新建用户在完成归属前对公司管理员不可见。V28 尚未在隔离 MySQL 验证。此基线与逐资源策略见[全栈计划 4.1.1](./FlashTrade全栈产品化实施计划.md)；记录契约不代表完整租户授权已落实。

## 3. 当前阻塞项与阶段归属

1. **范围/契约冻结（阶段 0–1）**
   - 已冻结 API 响应为 `code/message/data`，稳定业务码、HTTP 映射及协调迁移策略由阶段 1 确认；后端、Flutter 与 Web 已完成代码迁移，完整 API 回归及已部署调用方升级验收仍待完成。
   - 用户已确认角色与 Scope 基线：`ROLE_SUPER_ADMIN` 跨公司；`ROLE_ADMIN` 仅可作为有公司 Scope 的公司管理员；用户单公司归属、账户继承所有者 Scope、组不参与授权；风控操作员只读、审计员只读显式授权资源日志。V28 和公司列表/详情首个后端切片已完成；逐端点服务端过滤、迁移执行验证和历史数据映射仍待落实。
   - 已确认首发按现有 Must/Should/Later 执行；外部 API 客户端凭证按阶段 8 评估。
   - 已确认 Flutter 首发沿用现有深色主题，主题系统/节日皮肤延期；跨设备偏好同步延期，本地偏好不新增后端字段。

2. **安全基线（阶段 2）**
   - 公司 Scope 后端首批切片：V28 新增用户公司归属及内部账户 Scope 主键；公司列表/详情、用户列表/详情、账户列表/详情/当前绑定按 Scope 过滤。用户/账户所有写操作和其他管理/持仓/FIX 管理端点暂限平台管理员；MVC 验证读路由和写路由角色边界，服务测试覆盖跨公司查询过滤及拒绝。最近一次定向 Scope/MVC 测试共 28 项通过；完整资源矩阵尚未实现，V28 未在隔离数据库执行。
   - 已新增共享 `ClientIpResolver`：仅当连接对端命中显式配置的可信代理 CIDR 时，才从 `X-Forwarded-For` 由右向左解析；默认不信任转发头，异常/歧义链回退到连接对端。认证限流和 API 审计共用该解析器。生产代理 CIDR 尚须由部署负责人配置并验证。
   - `RequestTracingFilter` 已拒绝不符合长度/字符白名单的客户端 `X-Trace-Id`，无效值改为服务端生成，并先于 Spring Security 执行；Security 401/403 与全局业务异常响应在响应头和错误体中使用同一 trace ID，缺少 MDC 时生成安全 ID。8 项定向测试通过。
   - `UNKNOWN` 订单状态和异常后查单/对账路径尚未闭环；当前不得因结果未知自动重放。
   - Flutter Dio 对可刷新的 401 轮换令牌后，仅自动重试 GET、HEAD、OPTIONS；并发刷新共用一次请求，订单等写请求不自动重放。拦截器测试覆盖安全读取重试、重试后仍 401 的限次处理、并发刷新、刷新失败/缺少刷新令牌时的清凭证与原错误保留，以及订单 POST 不重放。
   - Refresh Token 成功刷新后，API 现在返回新的 Access/Refresh Token 对并撤销旧 Refresh Token；Flutter 拦截器保存新令牌对，避免后续请求继续使用已撤销的 Refresh Token。
   - API 的 `JwtTokenProvider` 为每个 JWT 增加唯一 `jti`；会话集成测试发现同一秒内为同一用户签发的令牌原本可能完全相同，导致刷新返回的 Refresh Token 已被旧令牌黑名单命中。新增 `AuthSessionIntegrationTest` 覆盖该轮换路径及旧令牌拒绝。
   - Flutter 登出会调用 `/api/v1/auth/logout` 并提交当前 Refresh Token；本地凭证在请求完成或 Dio 失败后都会清除，远端撤销未确认时通过本地化提示告知用户。API 仍兼容不带请求体的旧客户端；有效 Refresh Token 必须与当前 Access Token 属于同一主体，其他主体的 Refresh Token 不会被撤销。
   - `MOCK` 默认配置已存在，但阶段 0–7 的外部 API 总开关还需要部署级不可覆盖的验证，不能仅依赖默认配置。
   - 本地开发配置存在便于开发的默认凭证；需在隔离环境规范中处理，并确保不复用到生产。
   - Web 管理端已迁移到独立 Cookie 会话接口：Refresh Token 仅通过 `HttpOnly; SameSite=Strict` Cookie 传输，生产启用 `Secure`；CSRF Cookie 与请求头校验覆盖 Web 登录/刷新/登出。Access Token 仅保存在内存中；安全写请求不会因 401 自动重放。Flutter 原 JSON 令牌契约保持兼容。MockMvc 已覆盖 Cookie 轮换、缺失 Refresh Cookie、旧 Cookie 重用、CSRF 拒绝及登出撤销；真实 DB/Redis 与部署端到端仍未验收。
   - 后端 `dto.common.ApiResponse`、`dto.ApiResponse`、全局异常、Spring Security 与 FIX Gateway 错误出口已迁移到 `code/message/data`；校验错误按稳定字段码写入 `data.fieldErrors`，并保留响应头/体一致的 trace ID。Flutter Dio 与 Web Axios 已解包成功数据并解析稳定错误码。响应相关定向测试及 API 全量 118 项回归通过；已部署调用方升级尚未验收。
   - 已移除 Controller 重复输出的登录用户名、IP 和原始异常堆栈；认证服务常规日志不再记录用户名、IP、密码长度或异常消息。结构化登录审计仍由审计流程单独记录，须按其访问控制和保留策略管理。
   - 已移除 Flutter 登录/持仓仓库对凭证、令牌、响应体和异常响应的日志输出；网络拦截器仅保留方法、路径、HTTP 状态和错误类型，未记录请求/响应体或请求头。

3. **工具链与验证**
   - API `mvn test`：125 项，0 失败、0 错误；包括 `AuthSessionIntegrationTest` 的 3 项 MockMvc 集成测试。其 Redis、用户库和凭证认证由测试替身提供，不等同于真实 DB/Redis 部署环境验收。
   - Tastytrade 适配器相关测试：5 项通过；Mock 服务 `mvn test`：4 项通过。
   - Flutter `flutter analyze` 无问题；本轮 `flutter test test/features/auth/providers/auth_provider_test.dart`：4 项通过，覆盖模拟 HTTP 登录、刷新、读取重试及登出使用轮换后的令牌对。该模拟链路不覆盖 API 服务端端到端、交易及账户闭环。
   - Web 没有页面级测试脚本；本轮 `npm run typecheck` 与 `npx vite build --outDir <临时目录>` 通过。未运行会复制构建产物的 `npm run build`，以避免覆盖工作区中已有的未跟踪 API 静态资源。

## 4. 首发决策状态

| 决策 | 当前建议/默认 | 状态 |
|---|---|---|
| 首发 Must/Should/Later 范围 | 按全栈计划现有优先级执行 | 已确认（2026-11-20） |
| API 成功/错误响应契约 | `code/message/data`；稳定业务码、独立 HTTP 状态；错误 `data.fieldErrors/traceId` | 业务码注册表、HTTP 映射及协调策略已冻结；后端、Flutter、Web 已迁移并通过定向验证；全量 API 回归已通过，已部署调用方升级仍待验收 |
| 管理员与公司 Scope | 首发公司/租户 Scope；平台管理员跨公司、公司管理员限分配公司、风控操作员限分配账户、审计员只读限授权范围 | Scope 基线已确认；逐端点矩阵阶段 1 冻结 |
| 外部 API 客户端 | 不纳入首发，阶段 8 单独评估 | 已确认（2026-11-20） |
| Flutter 主题范围 | 首发沿用现有深色主题；主题系统和节日皮肤延期 | 已确认（2026-11-20） |
| 数据保留周期及现有 DB 迁移状态 | 由产品/法务/运维确认保留周期；只在明确隔离的测试 DB 验证迁移 | 待落实；未连接数据库 |

## 5. 已执行验证

| 模块 | 命令/结果 | 限制 |
|---|---|---|
| API | `mvn -Dtest=TastytradeBrokerClientTest,TastytradeRestClientTest,TastytradeMapperTest test`：5 项通过 | 仅券商适配器定向测试 |
| API | `mvn test`：118 项，0 失败、0 错误 | 当前单元/切片套件通过；不等同于隔离数据库、端到端会话或券商集成验收 |
| API | `mvn -Dtest=RequestTracingFilterTest test`：2 项通过 | 仅验证可信 trace ID 白名单及无效值替换 |
| API | 响应契约、Security 与 trace 定向测试：10 项通过；`AuthControllerTest`：7 项通过；全量 API 测试：118 项通过 | 不等同于端到端或真实数据库/券商集成验收 |
| Mock | `mvn test`：4 项通过 | 当前测试主要覆盖静态查询和简单下单响应 |
| Flutter | `flutter analyze`：无问题；`flutter test`：14 项通过 | 不代表端到端验收 |
| Web | `npm run typecheck` 和临时输出目录的 Vite 构建：通过 | 未运行会复制静态资源的 npm build 脚本 |

## 6. 下一阶段门

阶段 0 只有在 Must 验收标准/归属追踪完成、DB 迁移现状限制和保留期限有明确责任人与后续期限后才可标为完成。之后按全栈计划顺序进入阶段 1；券商真实 API 顺序仅在阶段 8 启动时使用。
