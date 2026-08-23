# FlashTrade

FlashTrade 是一个多券商证券交易平台工程，包含后端 API、Flutter 客户端、Web 管理后台以及本地模拟服务。该系统以 Spring Boot 为后端基础，提供统一持仓、订单管理、风险控制、JWT 认证、WebSocket 即时推送，以及多个券商接口适配。

## 项目结构

- `flash-trade-api/`：Spring Boot 后端服务，负责业务逻辑、数据库、券商接口、API 网关和 WebSocket 推送。
- `flash-trade-app/`：Flutter 多端应用，支持 iOS、Android、macOS、Windows，提供交易、持仓、行情展示和订单历史。
- `flash-trade-web/`：Vue 3 + TypeScript 管理后台，用于订单管理、用户管理和系统运维视图。
- `flash-trade-mock/`：本地模拟服务，用于快速验证券商接口或 API 通信逻辑。
- `docs/`：项目相关设计、需求、数据库和接口文档。
- `docker/`：容器化部署及环境构建配置。

## 主要技术栈

### 后端 (`flash-trade-api`)
- Java 21
- Spring Boot 3.5.7
- Spring Data JPA / Hibernate
- MySQL
- Redis
- Flyway 数据库迁移
- Spring Security + JWT
- WebSocket (STOMP)
- QuickFIX/J
- Maven

### Flutter 客户端 (`flash-trade-app`)
- Flutter 3.x
- Riverpod
- Dio
- go_router
- flutter_secure_storage
- freezed / json_serializable

### 管理后台 (`flash-trade-web`)
- Vue 3
- TypeScript
- Vite
- Axios

## 快速启动

### 1. 初始化仓库

```bash
cd FlashTrade
```

### 2. 后端开发环境

```bash
cd flash-trade-api
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端默认监听 `http://localhost:8080`。

### 3. Flutter 客户端

```bash
cd flash-trade-app
flutter pub get
flutter run
```

### 4. 管理后台

```bash
cd flash-trade-web
npm install
npm run dev
```

## Docker 本地运行

后端仓库内已提供 `docker/docker-compose.yml` 文件，可快速启动依赖服务：

```bash
cd flash-trade-api/docker
docker compose up -d
```

如果要同时启动后端服务，可在 Docker Compose 中启用 API 服务并执行：

```bash
docker compose up -d --build
```

### Docker 组成与使用说明（本地开发）

仓库内的 `flash-trade-api/docker/docker-compose.yml`（或根目录下的 `docker/`）定义了用于本地开发的服务集合。常见服务与说明：

- `flashtrade-mysql`（MySQL）
	- 镜像示例：`mysql:8.0`
	- 端口映射：宿主 `3308` -> 容器 `3306`（示例，可见 compose 文件）
	- 数据卷：持久化数据库数据（如 `./mysql_data:/var/lib/mysql`）
	- 初始化脚本：`docker/init-db.sql`（仅在首次创建卷时执行）
	- 常见 env：`MYSQL_ROOT_PASSWORD`、`MYSQL_USER`、`MYSQL_PASSWORD`、`MYSQL_DATABASE`

- `flashtrade-redis`（Redis）
	- 镜像示例：`redis:7`
	- 端口：`6379`
	- 可选持久化卷以保存数据

- `flashtrade-api`（后端服务）
	- 多数场景使用 Dockerfile 构建镜像；开发时也可挂载源代码到容器中以便热重载
	- 关键端口：`8080`
	- 依赖：MySQL 与 Redis 服务，使用 compose 内服务名或环境变量进行连接
	- profile：可通过 `SPRING_PROFILES_ACTIVE=dev` 或对应 `application-*.yml` 管理运行时配置

- `flashtrade-web`（管理后台， 可选）
	- 用于本地调试管理界面；生产环境一般构建后将静态文件部署到后端静态目录

- `flashtrade-mock`（模拟服务）
	- 模拟券商或外部 API，用于离线开发与集成测试

重要文件与目录：

- `flash-trade-api/docker/.env`：Compose 使用的环境文件（含 DB 密码等），已在 `.gitignore` 中忽略，**不要提交到远程仓库**。
- `flash-trade-api/docker/secrets/`：容器 secret 文件夹（JWT、DB 密钥等），已被忽略。

常用命令示例：

```bash
# 启动依赖服务（MySQL + Redis）
cd flash-trade-api/docker
docker compose up -d

# 构建并启动包含后端的完整环境
docker compose up -d --build

# 查看某个服务日志
docker compose logs -f flashtrade-api

# 停止并删除容器和卷（注意：会删除数据卷）
docker compose down -v
```

如果需要重新执行初始化脚本（例如 `init-db.sql`），请先删除卷再重建：

```bash
docker compose down -v
docker compose up -d --build
```

安全与部署注意事项：

- 开发环境可以使用 `docker/.env` 或 `docker/secrets` 存放明文密码，但**切勿**将这些文件提交到 Git。
- 生产环境请使用云提供商或 Kubernetes Secret 进行密钥管理，并禁止自动执行本地 `init-db.sql` 以避免误覆盖数据。
- 如果你希望在容器中直接构建并托管前端，可在 `flash-trade-web` 构建后将 `dist/` 内容复制到 `flash-trade-api/src/main/resources/static/admin`，或在 compose 中添加静态服务器服务。


## 运行说明

- `flash-trade-api` 为主业务后端，包含用户认证、券商 API 适配、订单匹配与审核、风险管理。
- `flash-trade-app` 是多端客户端，支持交易、持仓展示、行情图、订单查询。
- `flash-trade-web` 为管理运维控制面，用于订单查询、标记、取消、风控审核。
- `flash-trade-mock` 可用于本地调试券商接口与测试场景，不依赖真实券商环境。

## 配置说明

后端配置文件位于 `flash-trade-api/src/main/resources/`：
- `application.yml`
- `application-dev.yml`
- `application-test.yml`
- `application-prod.yml`

生产环境应使用环境变量或 Docker Secrets 注入数据库、Redis、JWT 密钥等敏感配置。

## 文档与设计资料

开发和设计文档保存在 `docs/` 目录中，包括：
- `database-etrade-assessment.md`
- `flutter-app-spec.md`
- `后端API文档-V1.2.md`
- `订单管理系统设计-V1.0.md`
- `系统功能改进总结-20260726.md`

## 开发建议

- 后端请优先使用 `dev` profile 进行本地调试。
- Flutter 客户端本地测试应走后端真实接口；必要时由后端 Mock 券商 API 模拟真实券商响应，不再使用前端假数据。
- 管理后台构建产物 `dist/` 可直接复制到后端静态资源目录以部署。

## 注意事项

- 请勿提交敏感信息到 Git 仓库，例如 `.env`、证书、Docker secrets。
- `flash-trade-api/docker/secrets/` 已在 `.gitignore` 中忽略。
- Flyway 迁移脚本一旦执行请勿修改已有版本，仅新增后续版本文件。

---

本 README 为当前存储库的系统说明，已覆盖后端、客户端和管理后台三个核心模块的结构与启动方式。