# FlashTradeAPI - 优化与改善建议

> 本文档为独立的技术优化建议，内容为基于现有实现方案的补充改进，不属于原始讨论范围。

---

## ⚠️ 实装缺陷修正（已修复）

以下问题在代码审查中被发现并已实装修复。

### 问题 1 — DB 表始终为空（最严重缺陷）

**现象**：`positions`、`orders`、`trades` 表的 Flyway 迁移文件（V1-V4）已存在，但实际从未有数据写入。`GET /api/portfolio/positions` 和 `GET /api/orders` 始终返回空列表。

**根因**：`BrokerService` 通过外部券商 API 获取数据并缓存到 Redis，但**从未调用任何 Repository 的 `save()` 方法**，数据不落地至 MySQL。

**修复**：新增 `BrokerSyncService`（定时任务，默认 5 分钟执行一次）：
- 读取 `app.broker-sync.accounts` 配置中的用户 ↔ 券商口座映射
- 调用 BrokerService 获取 portfolio / orders / trades
- Upsert 到本地 DB（positions / orders / trades 表）

```yaml
# application-dev.yml に追加
app:
  broker-sync:
    enabled: true
    interval-ms: 300000
    accounts:
      - userId: 1
        brokerName: SCHWAB
        accountId: dev-account-schwab-001
```

---

### 问题 2 — `trades` 表无 `user_id` + 无 `/api/trades` 端点

**现象**：`trades` 表没有 `user_id` 列，无法按用户查询成交记录。`GET /api/trades` 端点不存在。

**修复**：
- 新增 Flyway V5 迁移：`trades` 表添加 `user_id` 外键列 + unique 约束
- `Trade` entity 新增 `user` ManyToOne 关联
- `TradeRepository` 新增 `findByUserIdOrderByTradedAtDesc()` 等方法
- `PortfolioService` 新增 `getTrades()` / `getTradesByBroker()`
- `PortfolioController` 新增 `GET /api/trades`（支持 `brokerName` 过滤）

---

### 问题 3 — `GET /api/orders` 仅支持分页

**现象**：`GET /api/orders` 强制要求分页参数，无法获取全件列表。

**修复**：新增 `paginated` 参数（默认 `true`）：
- `paginated=false` → 返回 `List<OrderResponse>`（全件）
- `paginated=true`（或省略）→ 返回 `Page<OrderResponse>`（原有行为不变）

```
GET /api/orders?paginated=false          # 全件取得
GET /api/orders?page=0&size=20           # ページネーションあり（デフォルト）
GET /api/orders?paginated=false&status=FILLED  # 条件付き全件取得
```

---

## 一、API 设计层面

### 1-1. API 版本管理
- 当前：端点路径直接使用 `/api/portfolio/positions`，缺乏版本号
- 建议：统一采用 `/api/v1/` 前缀（URL 版本化）或 `Accept: application/vnd.flashtrade.v1+json` Header 方式
- 理由：将来券商接口或数据结构调整时，可在不中断现有客户端的前提下发布 v2

### 1-2. 分页与过滤统一规范
- 当前：`/api/orders` 备注"支持分页"，但 `/api/portfolio/positions` 未明确
- 建议：大列表接口统一实现 `?page=0&size=20&sort=createdAt,desc` 形式的分页；`positions` 支持按 `accountId`、`symbol` 过滤
- 进一步：返回结构统一封装 `Page<T>`，包含 `totalElements`、`totalPages`

### 1-3. API 幂等性保障
- 当前：订单提交等写入操作未提及幂等键
- 建议：POST 写入接口支持客户端提交 `X-Idempotency-Key` Header，服务端利用 Redis 30 秒内去重，防止网络重试造成重复操作

---

## 二、安全加固

### 2-1. JWT 并发刷新竞争条件
- 当前：`/api/auth/refresh` 实现中，多请求同时使用同一 Refresh Token 可能产生竞争
- 建议：刷新时对 Refresh Token 加 Redis 分布式锁（`SETNX`，TTL 3 秒），确保同一 Token 只被消费一次，旧 Token 立即加入黑名单

### 2-2. Rate Limiting（防刷限流）
- 当前：登录接口和券商 Proxy 接口无频率限制
- 建议：
  - 登录接口：基于 IP + 用户名，5 分钟内失败 5 次即锁定，使用 Redis `INCR` + `EXPIRE` 实现
  - 通用 API：使用 Spring 的 `HandlerInterceptor` 或 Bucket4j + Redis 实现令牌桶限流

### 2-3. 敏感日志脱敏
- 当前：Spring Boot 默认日志可能输出 HTTP 请求体，含 `password` 字段或 Authorization Header
- 建议：配置 `MaskingPatternLayout`（Logback 自定义 Pattern）或 Spring Boot Actuator Sanitize 配置，对 `password`、`token`、`apiKey`、`secret` 字段输出脱敏占位符

### 2-4. CORS 策略明确化
- 当前：WebSocket 和 REST 的跨域策略未见明确配置
- 建议：`SecurityConfig` 中明确限制 `allowedOrigins`，生产环境禁用通配符 `*`；WebSocket `setAllowedOriginPatterns` 使用白名单

---

## 三、可观测性（Observability）

### 3-1. 结构化日志与追踪 ID
- 建议：引入 **Spring Boot Actuator + Micrometer Tracing**（Brave / OpenTelemetry）
- 每个 HTTP 请求和 WebSocket 连接分配 `traceId`（写入 `MDC`），日志使用 JSON 格式（Logback `JsonEncoder`）
- 便于将来对接 ELK Stack（Elasticsearch + Logstash + Kibana）或 Grafana Loki

### 3-2. 指标暴露（Metrics）
- 建议：通过 Micrometer 暴露自定义指标：
  - `broker.api.call.duration`（各券商 API 响应时间）
  - `broker.api.error.count`（各券商 API 失败次数）
  - `broker.cache.hit.rate`（Redis 缓存命中率）
- 搭配 Prometheus + Grafana 仪表盘，实现生产环境实时监控

### 3-3. 健康检查细化
- 当前：仅有 Spring Boot Actuator 默认 `/actuator/health`
- 建议：为每家券商 API 实现自定义 `HealthIndicator`，展示各券商连接状态
- 生产部署时 Docker Compose `healthcheck` 可依赖此端点执行容器健康探测

---

## 四、弹性与容错

### 4-1. 断路器（Circuit Breaker）
- 当前：使用 Spring Retry 做重试，但无断路器保护
- 建议：引入 **Resilience4j**（`spring-cloud-starter-circuitbreaker-resilience4j`）
  - 各券商 `BrokerClient` 方法标注 `@CircuitBreaker(name="schwab", fallbackMethod="fallback")`
  - 断路器开启时直接返回缓存数据或空对象，不再继续请求外部 API
  - 设置 `slidingWindowSize=10`、`failureRateThreshold=50`、`waitDurationInOpenState=30s`

### 4-2. 券商 API 降级策略
- 建议：在 `BrokerService` 层为每个方法定义降级分支：
  1. 先走断路器保护
  2. 断路器打开后返回最近一次 Redis 缓存数据
  3. 缓存也不存在时返回 `503 Service Unavailable` 并携带 `Retry-After` Header

### 4-3. 超时配置统一化
- 当前：`RestClient` 超时配置分散或未见集中管理
- 建议：为各 `BrokerClient` 的 `RestClient` 实例独立配置 `connectTimeout`（5s）和 `readTimeout`（15s），与 Resilience4j TimeLimiter 联动

---

## 五、性能优化

### 5-1. 券商 API 并行聚合
- 当前：多账户多券商数据聚合采用顺序异步 `@Async`
- 建议：使用 `CompletableFuture.allOf()` 同时触发所有券商 API 调用，等所有完成后聚合结果，减少总等待时间（总耗时 ≈ 最慢的单个请求，而非叠加）

### 5-2. WebSocket 推送防抖（Debounce）
- 当前：定时或触发式推送 `/topic/positions`，高频 API 回调时可能产生推送风暴
- 建议：在 Service 层对同一账户的推送加入防抖缓冲（如 500ms 内合并多次变化为一次推送），利用 `ScheduledExecutorService` 或 Reactor `Flux.buffer(Duration.ofMillis(500))` 实现

### 5-3. 数据库连接池调优
- 建议：显式配置 HikariCP 连接池参数（`maximum-pool-size`、`minimum-idle`、`connection-timeout`），不依赖默认值，根据生产并发量调整

---

## 六、测试质量

### 6-1. 集成测试缺失补充
- 建议：使用 **Testcontainers** 在 CI 中拉起真实 MySQL + Redis 容器，执行完整端到端集成测试，避免仅依赖 Mock
- 对 WebSocket 推送路径补充集成测试（`StompClient` + `WebSocketStompClient`）

### 6-2. 契约测试（Consumer-Driven Contracts）
- 建议：使用 Spring Cloud Contract 或 Pact 为各 `BrokerClient` 建立契约测试，确保 Mock 数据与真实券商 API 结构一致

### 6-3. 测试覆盖率 CI 强制门槁
- 建议：在 `pom.xml` 引入 JaCoCo 并配置 `minimum` 覆盖率（Instruction ≥ 70%，Branch ≥ 60%），将其作为 Maven build 失败条件，在 CI Pipeline 中强制执行

---

## 七、DevOps 与部署

### 7-1. Spring Cloud Config 高可用
- 当前：单节点 Config Server，存在单点故障
- 建议：
  - 生产环境部署 2 个 Config Server 实例，客户端配置多个地址（`spring.cloud.config.uri` 支持逗号分隔）
  - 或考虑迁移至 HashiCorp Vault（Spring Cloud Vault 原生支持），实现更完善的秘密管理 + 动态轮换

### 7-2. CI/CD Pipeline
- 建议：配置 GitHub Actions 或 GitLab CI：
  1. Push 触发：单元测试 → 集成测试 → JaCoCo 覆盖率检查
  2. Tag 触发：Maven 构建 → Docker 镜像 Build + Push → Ubuntu 服务器自动更新
  
### 7-3. 数据库迁移生产安全
- 建议：生产环境启用 Flyway `outOfOrder=false`（默认）并配置 `baselineOnMigrate=false`，同时添加 `validateOnMigrate=true`，防止因 SQL 文件被修改导致数据不一致
- CI Pipeline 中加入 `flyway:validate` 步骤，在部署前验证迁移脚本完整性

### 7-4. Docker 镜像精简
- 当前：推测使用标准 JRE 或 JDK 基础镜像
- 建议：使用多阶段构建（`maven:3.9-amazoncorretto-21` 编译，`amazoncorretto:21-alpine` 运行），减少镜像体积；运行阶段使用非 root 用户（`USER 1001`）

---

## 八、API 文档

### 8-1. OpenAPI / Swagger 完善
- 建议：在各 Controller、DTO、Exception 类上添加 SpringDoc 注解（`@Tag`、`@Operation`、`@ApiResponse`、`@Schema`），使 Swagger UI 自动生成完整、准确的交互式文档
- 区分 dev 环境（Swagger UI 开放）和 prod 环境（Swagger UI 关闭或需认证）

### 8-2. WebSocket 协议文档
- 建议：在 `api-spec.md` 中补充 STOMP Frame 示例（CONNECT、SUBSCRIBE、MESSAGE），方便前端开发者接入，无需阅读源码

---

## 九、代码架构改善

### 9-1. DTO 与 Domain Entity 严格分离
- 建议：Controller 层只暴露 DTO（Request/Response 对象），Service 层使用 MapStruct 自动映射 DTO ↔ Entity，避免 JPA Entity 直接作为 HTTP 响应体（防止意外序列化懒加载字段或敏感字段）

### 9-2. 统一错误响应格式
- 建议：定义全局 `ErrorResponse` DTO（`code`、`message`、`timestamp`、`traceId`），通过 `@RestControllerAdvice` 统一捕获并响应，前端固定处理逻辑

### 9-3. 券商模块拆包结构
- 当前：三家券商实现推测在同一 `broker` 包下
- 建议：按券商分子包（如 `broker.schwab`、`broker.etrade`、`broker.tastyworks`），便于将来独立提取为微服务模块
