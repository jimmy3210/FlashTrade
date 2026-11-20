---
name: "FlashTrade 前端指令"
description: "修改 Flutter 客户端或 Vue/TypeScript 管理端时使用，涵盖状态管理、路由、API 集成、本地化和前端测试。"
applyTo:
  - "flash-trade-app/lib/**"
  - "flash-trade-app/test/**"
  - "flash-trade-web/src/**"
---

# 前端开发指令

- 保持客户端边界：Flutter 客户端和 Web 管理端均调用 QuickTrade 后端 API；不得从客户端直接调用券商 API。
- Flutter 代码沿用现有 Riverpod providers、Dio API 客户端、`go_router` 路由、安全令牌存储、本地化和生成模型模式。Widget 应聚焦于界面展示与用户交互。
- Web 管理端沿用现有 Vue 3 `<script setup>`、TypeScript、路由结构及 Axios/API 封装。API 类型和错误处理应与相邻代码保持一致。
- 前端是展示与交互层，不是授权边界；路由守卫、隐藏按钮和客户端校验只改善 UX，Java API 必须独立执行认证、角色/租户 Scope、资源归属、输入校验、风控和审计。不要在前端复制权威业务规则。
- 若新增 BFF、SSR 或服务端组件：仅传递页面必需 DTO；不得把服务端密钥、内部配置、其他用户数据或未授权字段放入 HTML、客户端 props/hydration payload、浏览器缓存或错误响应。涉及身份的数据须禁用共享缓存或按身份/Scope 隔离，并审查 Cookie/Token 的安全传递与撤销。
- CORS 不能代替后端鉴权；Cookie 会话须考虑 CSRF 与 SameSite/Secure/HttpOnly，Token 须避免泄露到 URL、日志和不受信存储。不得假设 JWT 可防重放，也不得将自有前端或 BFF 请求视为可信授权依据。
- 新增服务端代理时限制可访问目标与转发头，审查 SSRF、CSRF、XSS、缓存污染/跨用户泄漏、错误与日志脱敏；不允许前端/BFF 直连数据库或券商，也不在其中实现业务授权的权威判断。
- 将认证状态和账户标识视为不可信输入。不得在客户端存储券商凭证，也不得绕过后端授权与风险控制。
- 提供清晰可操作的加载、空数据和错误状态。修改面向用户的流程时，保留原有行为与本地化。
- UI 遵循现有设计系统、字体/图标语义、布局和无障碍模式；状态和风险不得只用颜色表达，皮肤、背景或装饰不能降低交易字段与危险提示的可读性。
- 覆盖无权限、离线、超时、限流、数据延迟/过期、结果未知等状态；不得将失败显示为成功，交易结果未知时不得由界面自动重放写请求。危险操作应清楚呈现完整参数并要求适当确认。
- 不在前端实现或复制安全边界：隐藏按钮、路由守卫、IP allowlist 不能替代后端鉴权、账户归属、输入校验和审计。不要把密码、Token、账户凭证或完整敏感请求写入浏览器日志。
- 新增 API 或表单调用时检查速率限制/攻击拦截响应并提供安全、可理解的反馈；不要自动重试会产生副作用的请求。
- 按现有项目模式新增或更新针对性测试。测试使用 mock/test 环境，严禁执行真实交易。
- 运行最小充分验证：在 `flash-trade-app/` 执行 `flutter analyze` 和相关 `flutter test`；或在 `flash-trade-web/` 执行 `npm run build`。Web 构建会复制产物到 API 静态资源目录；构建前后检查差异并保留已有文件。
