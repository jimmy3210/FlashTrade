# FlashTrade CI/CD 流程

## 1. 范围与原则

仓库级流水线位于 `.github/workflows/ci-cd.yml`。此前位于 `flash-trade-api/.github/workflows/` 的 workflow 属于嵌套目录，GitHub 不会将它识别为本仓库的 Actions workflow；现已移至根目录并按仓库实际模块路径运行。

本流程包含持续集成和容器镜像发布，不会自动部署到服务器或生产环境。测试和构建使用 GitHub 托管 runner、临时 MySQL/Redis 服务及非生产测试凭证；不会连接券商账户或提交真实订单。

## 2. 触发条件

| 事件 | 执行内容 |
| --- | --- |
| 向 `main` / `develop` 发起或更新 Pull Request | 后端、Mock、Flutter、管理端检查 |
| 推送到 `main` / `develop` | 后端、Mock、Flutter、管理端检查 |
| 推送 `vX.Y.Z` 格式的版本标签 | 先执行全部 CI 检查，全部成功后构建并发布 API 容器镜像 |

普通分支和 PR 不会发布镜像。版本标签发布使用 `GITHUB_TOKEN`，权限仅限读取仓库和写入 Packages。

## 3. CI 检查

- **Backend tests (Java 21)**：在临时 MySQL 8 与 Redis 7 服务上使用测试 profile 执行 `mvn verify`，并上传 JaCoCo 报告。
- **Mock service tests (Java 21)**：对 `flash-trade-mock` 执行 `mvn verify`。
- **Flutter analyze and tests**：安装 Flutter 依赖，执行 `flutter analyze` 和 `flutter test`。
- **Admin typecheck and build**：在 `flash-trade-web` 使用锁文件执行 `npm ci`、`npm audit --audit-level=high`，再运行 `npm run build`（包括 TypeScript 检查与 Vite 构建）。

容器发布 job 依赖以上检查全部通过。它会先构建管理端静态资源并放入 API 资源目录，然后以 `flash-trade-api/` 为 Docker build context 构建 API 镜像，避免原 workflow 使用错误的仓库根目录上下文。

## 4. 镜像命名与发布

镜像地址为：

```text
ghcr.io/<repository-owner>/flashtradeapi:<version>
```

版本标签会生成 SemVer 版本、`major.minor` 版本别名和提交 SHA 标签。此次只发布主 API 镜像；Mock 服务不是生产镜像，不发布。流水线不创建 `latest` 标签，也不自动将镜像部署到任何环境。

首次发布后，应在 GitHub Packages 中检查镜像可见性与拉取权限。GHCR 包默认权限可能需要仓库管理员确认。

## 5. 仓库管理员需配置的保护

Workflow 文件本身不能替代 GitHub 仓库规则。建议管理员为 `main` 和 `develop` 配置 Rulesets/Branch protection：

1. 禁止直接推送或强制推送，要求通过 Pull Request 合并。
2. 要求以下检查成功后才允许合并：
   - `Backend tests (Java 21)`
   - `Mock service tests (Java 21)`
   - `Flutter analyze and tests`
   - `Admin typecheck and build`
3. 按团队风险策略要求 Code Owners/人工审批；高风险交易、安全、迁移或发布配置变更应由具备相应职责的人员审查。
4. 如启用 Merge Queue，将队列使用的目标分支纳入 workflow 的 PR 触发配置，并确保队列要求上述检查通过。
5. 如需 AI 审查，应在仓库设置中启用可用的 Copilot code review 功能，并将其作为独立审查意见；不能将 AI 审查视为人工审批或可重复的 CI 测试。

不同组织/套餐和仓库权限可用的保护功能不同，应在 GitHub 仓库设置中核实实际生效状态。

## 6. 发布操作

发布者确认版本与目标提交后，创建并推送稳定版本标签，例如：

```bash
git tag v1.2.3
git push origin v1.2.3
```

标签触发全套 CI。任一检查失败时，镜像不会发布；全部通过后才向 GHCR 推送版本、次版本和 SHA 标签。

## 7. 后续部署扩展

自动部署不属于当前配置。若要增加到 Staging/Production 的 CD，应先明确目标平台、健康检查/回滚方式、环境审批和密钥管理，并使用 GitHub Environments 分离环境与保护规则。不得把生产券商凭证、JWT 密钥或部署密钥放入 workflow 明文、测试配置或镜像层。

## 8. 本地等效验证

```powershell
# API（需按 application-test.yml 提供测试 MySQL/Redis）
Set-Location flash-trade-api
mvn verify -Dspring.profiles.active=test

# 模拟服务
Set-Location ..\flash-trade-mock
mvn verify

# Flutter
Set-Location ..\flash-trade-app
flutter pub get
flutter analyze
flutter test

# 管理端
Set-Location ..\flash-trade-web
npm ci
npm run build
```

管理端构建会复制静态资源到 API 项目，因此在本地执行后应检查工作区差异，避免误提交构建产物。
