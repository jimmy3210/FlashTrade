# FlashTradeAPI 项目计划说明

> 此文档已修复乱码，并统一为简体中文说明。

## 项目目标
- 使用 Java 21 + Spring Boot 3.5.7 构建后端服务。
- 提供统一的 REST API 与 WebSocket 能力。
- 结合 MySQL、Redis、JWT、Flyway 和 Docker Compose 完成可运行的基础架构。

## 推荐技术栈
- JDK：Microsoft Build of OpenJDK 21
- 构建工具：Maven 3.9+
- 后端框架：Spring Boot 3.5.7
- 数据库：MySQL 8.0
- 缓存：Redis 7.x
- 安全：Spring Security + JWT
- 消息与实时推送：WebSocket
- 容器化：Docker Desktop / Docker Compose

## 关键模块
1. 配置与环境管理
   - 统一使用 application.yml / application-dev.yml / application-prod.yml。
   - 将数据库、Redis、服务端口、JWT 配置纳入环境管理。
2. 数据持久化
   - 使用 Flyway 管理数据库迁移脚本。
   - 建立用户、持仓、订单、交易等核心表结构。
3. 认证与授权
   - 使用 JWT 完成登录、刷新、注销。
   - 通过 Spring Security 配置接口访问权限。
4. API 与 WebSocket
   - 提供组合账户、持仓、订单、成交等 REST 接口。
   - 通过 WebSocket 推送实时行情或账户状态。
5. 部署与测试
   - 使用 Docker Compose 启动 MySQL、Redis 与服务容器。
   - 配置 CI 流水线与基础测试任务。

## 实施阶段
- Phase 0：项目初始化与基础目录搭建
- Phase 1：Spring Boot 基础工程与依赖接入
- Phase 2：数据库迁移与数据模型
- Phase 3：认证授权与安全机制
- Phase 4：核心 API 与 WebSocket
- Phase 5：测试、文档与部署优化

## 验收要求
- 项目可在本地成功启动。
- API 接口可正常访问并返回统一响应结构。
- 用户认证与权限控制可正常工作。
- 数据库迁移脚本可顺利执行。
- Docker Compose 可完成基础环境部署。
