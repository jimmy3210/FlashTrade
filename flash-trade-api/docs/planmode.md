# FlashTradeAPI - Plan Mode

## 项目信息
- **项目名称**：FlashTradeAPI
- **包名**：com.tradex.flashtrade
- **Maven版本**：3.9.11
- **Java版本**：Amazon corretto 21
- **Spring Boot版本**：3.5.7
- **Tomcat版本**：10.1.7
- **数据库**：MySQL8.0
- **缓存**：Redis 7.x
- **Docker Desktop版本**：4.33以上推奨
- **用户认证**：JWT
- **通信方式**：REST + WebSocket
- **外部券商接口**：RESTful API

---

## 阶段 0 - 项目准备
### 目标
搭建本地项目基础环境，确保 Maven 和 Spring Boot 可用，并建立基础目录结构。

### 任务
- [x] 创建本地项目文件夹 `FlashTradeAPI` 如果已有跳过
- [x] 在文件夹内初始化 Maven 项目
- [x] 设置 groupId 为 `com.tradex.flashtrade`，artifactId 为 `FlashTradeAPI`
- [x] 创建基础包结构：
  - `config`（配置类）
  - `controller`（控制器）
  - `service`（服务类）
  - `repository`（数据库操作）
  - `entity`（实体类）
- [x] 创建 `application.yml` 配置文件（dev / test / prod 三环境分离）：
  - MySQL 数据源配置（url、用户名、密码）
  - Redis 配置（host、port）
  - Server端口配置（默认8080）
- [x] 创建 `docker/docker-compose.yml`（MySQL 8.0 + Redis 7.x）
- [x] 创建 `README.md`（项目概述、快速启动说明）
- [x] 创建 `docs/requirements.md`（功能需求 / 非功能需求）
- [x] 验证 Maven 项目结构正常（`mvn validate`）

---

## 阶段 1 - Spring Boot 与依赖
### 目标
配置 Spring Boot 3.5.7 环境，并引入核心依赖

### 任务
- [x] 修改 `pom.xml` 父级为 Spring Boot 3.5.7
- [x] 添加依赖：
  - `spring-boot-starter-web`（Web服务）
  - `spring-boot-starter-data-jpa`（JPA 数据访问）
  - `spring-boot-starter-security`（安全认证）
  - `spring-boot-starter-data-redis`（Redis缓存）
  - `spring-boot-starter-websocket`（WebSocket）
  - `spring-boot-starter-validation`（参数校验）
  - `spring-boot-starter-actuator`（健康检查）
  - `mysql-connector-j`（MySQL驱动）
  - `flyway-core` / `flyway-mysql`（数据库迁移）
  - `jjwt-api/impl/jackson 0.12.6`（JWT）
  - `springdoc-openapi-starter-webmvc-ui 2.8.6`（Swagger UI）
  - `spring-retry`（重试机制）
- [x] 创建主类 `FlashTradeApiApplication.java`（含 `@EnableAsync`、`@EnableScheduling`、`@EnableRetry`）
- [x] 完善 `application.yml`（Actuator + SpringDoc 基础配置）
- [x] 创建 `docs/api-spec.md`（API 端点规划初版）
- [ ] 验证应用可以启动 (`mvn spring-boot:run`)

---

## 阶段 2 - 数据库迁移管理
### 目标
使用 Flyway 统一管理数据库表结构版本，便于团队协作和版本回滚

### 任务
- [x] 添加 Flyway 依赖到 `pom.xml`（flyway-core + flyway-mysql）
- [x] 创建 `db/migration` 目录存放 SQL 脚本
- [x] 编写初始迁移 SQL：
  - `V1__create_users.sql`：id、用户名、密码（BCrypt）、角色、启用状态
  - `V2__create_positions.sql`：持仓信息（账户、证券代码、数量、成本、市值、盈亏）
  - `V3__create_orders.sql`：订单信息（方向、数量、价格、状态、成交时间）
  - `V4__create_trades.sql`：成交信息（关联订单、成交价、数量、手续费）
- [x] 创建 JPA 实体类：`User`、`Position`、`Order`、`Trade`
- [x] 创建枚举类：`Role`、`OrderSide`、`OrderStatus`
- [x] 创建 Repository：`UserRepository`、`PositionRepository`、`OrderRepository`、`TradeRepository`
- [x] 创建 `docs/database-design.md`（ER 图、表结构、Flyway 规则）
- [x] 配置 `application.yml` 自动执行迁移（flyway.enabled=true）
- [ ] 启动 Spring Boot 并确认表结构正确生成（Phase 1 + Phase 2 完成后验证）
- [ ] 数据库
  - 开发库：flash_trade_dev
  - 测试库：flash_trade_test
  - 生产库：flash_trade_prod

---

## 阶段 3 - 用户认证 (JWT)
### 目标
实现平台用户登录认证，使用 JWT 进行 Token 管理，保证 API 安全  

### 任务
- [x] 创建 `User` 实体类 + `UserRepository`（阶段 2 已完成）
- [x] 创建异常体系：`ErrorCode`、`BusinessException`、`GlobalExceptionHandler`
- [x] 创建统一响应包装类：`ApiResponse<T>`
- [x] 创建 DTO：`LoginRequest`、`LoginResponse`、`TokenRefreshRequest`
- [x] 创建 `JwtTokenProvider`（生成/验证 Access & Refresh Token）
- [x] 创建 `UserDetailsServiceImpl`（从数据库加载用户）
- [x] 创建 `JwtAuthenticationFilter`（JWT Filter 拦截请求）
- [x] 创建 `TokenBlacklistService`（使用 Redis 管理 Token 黑名单）
- [x] 创建 `AuthService`（登录、刷新、登出业务逻辑）
- [x] 创建 `AuthController`：
  - `POST /api/auth/login`：用户名/密码登录，返回 JWT
  - `POST /api/auth/refresh`：刷新 JWT
  - `POST /api/auth/logout`：登出（Token 加入黑名单）
- [x] 创建 `SecurityConfig`：
  - JWT Filter 拦截请求（`JwtAuthenticationFilter`）
  - 配置不同接口访问权限（Role-based，`hasRole("ADMIN")`）
  - 401/403 返回 JSON 错误响应
- [x] 创建 `RedisConfig`（配置 `RedisTemplate<String, String>`）
- [x] 创建 `OpenApiConfig`（Swagger UI Bearer Token 认证支持）
- [x] 编写单元测试 `AuthServiceTest`（6 个测试用例）
- [x] 编写控制器测试 `AuthControllerTest`（5 个测试用例，`@WebMvcTest`）
- [ ] 集成测试：启动 Docker，运行 `mvn test` 确认全部通过

---

## 阶段 4 - 内部 API + WebSocket 推送
### 目标
提供内部 REST API 获取数据，并通过 WebSocket 实现实时推送  

### 任务
- [x] 创建 `PositionResponse`、`OrderResponse`、`PositionPushMessage` DTO
- [x] 创建 `UserLookupService`（从 UserRepository 查询用户 ID）
- [x] 创建 `PortfolioService`（持仓 / 订单查询，Entity → DTO 转换）
- [x] 创建 REST API：
  - `GET /api/portfolio/positions`：返回用户持仓列表
  - `GET /api/orders`：返回用户订单信息（支持状态过滤 + 分页，最大 100 条）
- [x] 配置 WebSocket（`WebSocketConfig`）：
  - Endpoint: `/ws`（SockJS 支持）
  - Topic: `/topic/positions`
  - Publish Prefix: `/app`
- [x] 创建 `PositionPushService`：每 30 秒向 `/topic/positions` 广播全量持仓（可配置间隔）
- [x] 编写单元测试 `PortfolioServiceTest`（4 个测试用例）
- [x] 编写控制器测试 `PortfolioControllerTest`（4 个测试用例，`@WebMvcTest`）
- [ ] 集成测试：启动 Docker，运行 `mvn test` 确认全部通过

---

## 阶段 5 - 外部券商 API 通信（RESTful）
### 目标
设计统一接口，实现与多个券商 REST API 的交互，并缓存数据减少请求压力  

### 任务
- [x] 定义 `BrokerClient` 接口：
  - `getOrders(String accountId)`
  - `getTrades(String accountId)`
  - `getPortfolio(String accountId)`
- [x] 创建 Broker DTO：`BrokerPortfolio`、`BrokerPosition`、`BrokerOrder`、`BrokerTrade`
- [x] 创建 `BrokerApiException`（含 brokerName + statusCode）
- [x] 创建 `BrokerService`（按 brokerName 路由 + Redis 缓存 TTL 5分钟）
- [x] 使用 Redis 缓存券商接口返回数据（`broker:portfolio:` / `broker:orders:` / `broker:trades:`）
- [x] 异步处理请求（`@Async("brokerTaskExecutor")`，专用线程池 5-10 线程）
- [x] 重试机制（`@Retryable(maxAttempts=3, backoff=指数退避 1s→2s→4s)`）
- [x] 创建 `AsyncConfig`（`brokerTaskExecutor` Bean，核心5/最大10/队列100）
- [x] 更新 `GlobalExceptionHandler`（添加 `BrokerApiException` 处理器）
- [x] 编写单元测试 `BrokerServiceTest`（5 个测试用例）

---

## 阶段 6 - 具体券商 API 实现
### 目标
实现各个具体券商接口（如嘉信schwab证券 ），统一返回内部系统可识别的数据结构  

### 任务
- [x] 创建 `SchwabProperties`（`@ConfigurationProperties("app.schwab")`）：
  - clientId / clientSecret / redirectUri（环境变量注入）
  - baseUrl / tokenUrl / tokenCacheKeyPrefix
  - mockEnabled（开发环境使用 Mock 数据）
- [x] 创建 Schwab API 响应 DTO 群（`broker/schwab/dto/`）：
  - `SchwabTokenResponse`（OAuth2 Token 响应）
  - `SchwabInstrument`（金融工具）
  - `SchwabPositionItem`（持仓条目）
  - `SchwabAccountPositions`（持仓包装器）
  - `SchwabOrderLeg`（订单委托腿）
  - `SchwabOrderResponse`（订单响应）
  - `SchwabTransferItem`（成交转移项）
  - `SchwabTransaction`（成交记录）
- [x] 创建 `SchwabTokenService`（OAuth2 Token 管理）：
  - Redis 缓存 Access Token（TTL = expiresIn）
  - Refresh Token 自动刷新（提前 120 秒）
  - 支持授权码换取 Token（`exchangeAuthorizationCode`）
- [x] 创建 `SchwabRestClient`（HTTP 通信层）：
  - Spring `RestClient` 调用 Schwab Trader API v1
  - `getPositions` / `getOrders` / `getTransactions`
  - HTTP 状态码 → `BrokerApiException` 转换
- [x] 创建 `SchwabMapper`（数据映射层）：
  - Schwab 持仓 → `BrokerPortfolio` / `BrokerPosition`
  - Schwab 订单 → `BrokerOrder`（instruction 正规化 BUY/SELL）
  - Schwab 成交 → `BrokerTrade`（过滤 TRADE 类型）
- [x] 实现 `SchwabBrokerClient`（`BrokerClient` 接口）：
  - `getBrokerName()` 返回 "SCHWAB"
  - Mock 模式（`mockEnabled=true`）返回硬编码测试数据
  - Real 模式调用 `SchwabRestClient` + `SchwabMapper`
- [x] 更新 `application-dev.yml`（`app.schwab.*`，`mock-enabled: true`）
- [x] 更新 `application-prod.yml`（全部通过环境变量注入）
- [x] 编写单元测试 `SchwabMapperTest`（6 个测试用例）
- [x] 编写单元测试 `SchwabBrokerClientTest`（5 个测试用例）


---

## 阶段 7 - 系统环境变量与配置管理
### 目标
统一管理系统配置和环境变量，保证安全性、灵活性和多环境支持  

### 核心原则
1. **安全第一**  
   - 敏感信息（数据库密码、券商 API Key、OAuth token）**不存入 `.env` 文件或 Git**  
   - 开发环境可使用 `.env` 存放非敏感变量  
   - 生产环境使用：
     - 系统环境变量
     - Docker Secret
     - Vault 等安全存储  

2. **统一配置管理**  
   - 公共配置通过 **Spring Cloud Config Server** 管理  
   - 支持多环境：`dev` / `prod` / `test`  
   - 支持动态刷新：使用 `@RefreshScope` + `/actuator/refresh`  

3. **多环境灵活切换**  
   - `.env` 文件仅存非敏感变量，如：
     - `SPRING_PROFILES_ACTIVE`
     - 内部服务地址  
   - Docker Compose 启动容器时注入环境变量  

### 任务
- [x] 搭建 Spring Cloud Config Server（`config-server/` 独立 Spring Boot 应用）：
  - `@EnableConfigServer` + native/git 双模式
  - `Dockerfile` 多阶段构建（Amazon Corretto 21）
  - 通过 Docker Compose volume 挂载 `config-repo/`
- [x] 创建集中配置仓库 `docker/config-repo/`：
  - `flashtradeapi.yml`：所有环境通用配置（非敏感）
  - `flashtradeapi-dev.yml`：开发环境专用（mock-enabled=true, DEBUG 日志）
  - `flashtradeapi-prod.yml`：生产环境（仅占位符，敏感信息由环境变量注入）
- [x] 在主应用添加 Spring Cloud Config Client：
  - `pom.xml` 添加 Spring Cloud BOM（`2025.0.0`）+ `spring-cloud-starter-config`
  - `application.yml` 添加 `spring.config.import: optional:configserver:${CONFIG_SERVER_URL}`
  - Actuator 暴露 `refresh` 端点（`/actuator/refresh`）
- [x] `@RefreshScope` 注解：
  - `SchwabProperties`（动态切换 mock/real 模式）
  - `JwtTokenProvider`（支持 JWT 密钥热更新）
- [x] 安全访问控制：`/actuator/refresh` 限 ADMIN 角色（`SecurityConfig` 更新）
- [x] 敏感信息管理：
  - `docker/secrets/` 目录 + README（本地 Docker Secret 文件，已 .gitignore）
  - `docker-compose.yml` 使用 `secrets:` 声明（`db_password` / `jwt_secret` / `schwab_*`）
  - MySQL 使用 `MYSQL_PASSWORD_FILE` 从 Secret 读取密码
- [x] 非敏感变量管理：
  - `docker/.env.example` 模板（含安全说明）
  - `.gitignore` 排除 `.env`、`secrets/*.txt`、`*.key` 等
- [x] 更新 `docker-compose.yml`：
  - 新增 `config-server` 服务（健康检查、volume 挂载）
  - 应用服务模板（注释形式，含 `depends_on` / `secrets` / `env_file` 配置）
---

## 阶段 8 - E*TRADE 与 Tastyworks (Taste) API 实现

### 目标

实现 E*TRADE 和 Tastyworks 券商接口，统一返回内部系统可识别的数据结构

### 任务

* [x] 为 **E*TRADE** 实现 `EtradeBrokerClient`，继承 `BrokerClient`

  * 支持 OAuth 授权（OAuth 1.0a：HMAC-SHA1 签名）
  * 获取账户信息、仓位、订单、成交
  * 映射为内部统一实体（Position、Order、Trade）
  * 异常处理和重试机制（由 BrokerService @Retryable 统一覆盖）
  * 编写单元测试，验证接口和数据映射正确（EtradeMapperTest 6件、EtradeBrokerClientTest 5件）

* [x] 为 **Tastyworks** 实现 `TastyworksBrokerClient`，继承 `BrokerClient`

  * 使用 REST API + Session Token 认证（POST /sessions → session-token）
  * 获取账户、仓位、订单和成交信息（成交从 Order.legs[].fills[] 提取）
  * 映射为内部统一实体
  * Session Token 失效时自动重登（401 → clearToken → refreshToken 重试一次）
  * 编写单元测试验证接口和数据映射正确（TastyworksMapperTest 5件、TastyworksBrokerClientTest 5件）

* [x] 与内部 API 和 WebSocket 推送兼容：

  * 所有券商数据转换后可通过 `/portfolio/positions` 返回
  * 实时更新推送到 `/topic/positions`

* [x] 确保敏感信息（API Key、Token）通过 Docker Secret 注入

  * E*TRADE：`consumerKey` / `consumerSecret` → Docker Secret 注入
  * Tastyworks：`username` / `password` → Docker Secret 注入
  * Access Token / Session Token → Redis 缓存（TTL 23h / 24h）

### 实现文件清单

| 文件路径（`broker/etrade/`） | 说明 |
|---|---|
| `config/EtradeProperties.java` | `@RefreshScope` + `@ConfigurationProperties("app.etrade")` |
| `dto/EtradeInstrument.java` | 证券基本信息 |
| `dto/EtradePosition.java` | 单条持仓 |
| `dto/EtradePortfolioResponse.java` | 持仓响应（3 层嵌套） |
| `dto/EtradeOrder.java` | 委托订单 |
| `dto/EtradeOrderDetail.java` | 订单明细 |
| `dto/EtradeOrdersResponse.java` | 订单列表响应 |
| `dto/EtradeTransactionResponse.java` | 成交记录响应 |
| `EtradeTokenService.java` | OAuth 1.0a Token Redis 缓存管理 |
| `EtradeRestClient.java` | HTTP 层（OAuth 1.0a 签名生成） |
| `EtradeMapper.java` | DTO → 内部 Broker DTO 转换 |
| `EtradeBrokerClient.java` | BrokerClient 实现，Mock/Real 两种模式 |

| 文件路径（`broker/tastyworks/`） | 说明 |
|---|---|
| `config/TastyworksProperties.java` | `@RefreshScope` + `@ConfigurationProperties("app.tastyworks")` |
| `dto/TastyworksSessionResponse.java` | 登录响应 |
| `dto/TastyworksPositionsResponse.java` | 持仓列表响应 |
| `dto/TastyworksPosition.java` | 单条持仓 |
| `dto/TastyworksOrdersResponse.java` | 订单列表响应 |
| `dto/TastyworksOrder.java` | 委托订单 |
| `dto/TastyworksOrderLeg.java` | 订单腿 |
| `dto/TastyworksFill.java` | 成交明细 |
| `TastyworksTokenService.java` | Session Token 管理（Redis TTL 24h） |
| `TastyworksRestClient.java` | HTTP 层（Bearer Token + 401 自动重试） |
| `TastyworksMapper.java` | DTO → 内部 Broker DTO 转换 |
| `TastyworksBrokerClient.java` | BrokerClient 实现，Mock/Real 两种模式 |

### 注意事项

* 所有新增券商接口必须遵循 `BrokerClient` 统一接口
* 返回的数据结构保持一致，方便上层服务处理
* E*TRADE OAuth 1.0a 签名：参数需按字典序排列，不含 `oauth_signature`
* Tastyworks 成交记录通过 `Order.legs[].fills[]` 提取，非独立接口

---

## 使用 Plan Mode 的注意事项
- 可以直接在 VSCode 打开项目文件夹，导入 `planmode.md`  
- 每个阶段任务可单独生成代码骨架  
- 不依赖 Git，可以先本地开发  
- 完成阶段 0-6 后，可选择初始化 Git 并推送到远程仓库  
- Copilot 会根据任务生成对应的类、接口、方法和目录结构  

---
