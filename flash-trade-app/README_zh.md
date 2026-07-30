# FlashTrade - 股票交易应用

FlashTrade 是一个与后端 Spring Boot API 联动的 Flutter 股票交易应用，灵感来源 moomoo。支持 iOS、Android、macOS 和 Windows 的跨平台响应式 UI。

---

## 功能概览

| 功能 | 说明 |
|-----|------|
| 启动页 | 检查 Token 并自动登录，或跳转登录页 |
| 登录 | JWT 认证，Token 安全存储，自动刷新 |
| 首页 | 总资产展示、持仓 Top5、日收益柱状图 |
| 组合 | 持仓列表、市值盈亏展示、交易入口 |
| 下单 | 市价/限价、买入/卖出、表单验证与确认对话框 |
| 订单历史 | 订单列表、成交历史、筛选与撤单 |

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | Flutter 3.x |
| 状态管理 | Riverpod 2.x (`flutter_riverpod`) |
| HTTP 客户端 | Dio 5.x |
| 本地存储 | flutter_secure_storage 9.x |
| 路由 | go_router 13.x |
| 图表 | fl_chart 0.68.x |
| 格式化 | intl 0.19.x |

---

## 支持平台

| 平台 | 最低版本 | 导航 |
|------|----------|------|
| iOS | 14 以上 | BottomNavigationBar |
| Android | 9 (API 28) 以上 | BottomNavigationBar |
| macOS | 10.15 (Catalina) 以上 | NavigationRail |
| Windows | Windows 10 (1903) 以上 | NavigationRail |

> 响应式布局：当屏幕宽度 ≥ 800px 时自动切换为桌面布局（左侧导航栏）。

---

## 项目结构

```
lib/
├── main.dart                    # 入口文件（ProviderScope）
├── app.dart                     # MaterialApp.router + 主壳组件
├── core/
│   ├── constants/
│   │   ├── api_constants.dart   # API 端点与 BaseURL 逻辑
│   │   └── app_colors.dart      # 主题颜色常量
│   ├── network/
│   │   ├── dio_client.dart      # Dio 单例封装
│   │   └── api_interceptor.dart # JWT 自动附加与刷新
│   └── storage/
│       └── secure_storage.dart  # flutter_secure_storage 封装
├── features/
│   ├── auth/                    # 登录与启动页
│   ├── home/                    # 仪表盘与日收益图
│   ├── portfolio/               # 持仓页面
│   ├── trade/                   # 下单页面
│   └── orders/                  # 订单历史与成交记录
└── shared/
    └── widgets/                 # 共享组件，如 PriceText、StatusBadge、StockCard
```

---

## 设置与运行

### 前置条件

- 安装 Flutter 3.x SDK
- 启动 FlashTrade 后端 Spring Boot 服务

### 依赖安装

```bash
flutter pub get
```

### 运行方式

```bash
# iOS 模拟器
flutter run -d ios

# Android 模拟器
flutter run -d android

# macOS 桌面
flutter run -d macos

# Windows 桌面
flutter run -d windows
```

---

## 示例模式（无需后端）

应用支持 DEMO 模式，可快速查看 UI 和功能。

```bash
flutter run -d macos --dart-define=kDemoMode=true
```

其他平台同理：

```bash
flutter run -d android --dart-define=kDemoMode=true
```

### DEMO 模式说明

- 启动后无需后端服务
- 使用内置模拟数据
- 订单操作返回模拟成功

### DEMO 登录

| 项目 | 值 |
|------|----|
| 用户名 | `demo` |
| 密码 | `demo` |

---

## 实际模式 API 连接

| 平台 | BaseURL |
|------|--------|
| Android 模拟器 | `http://10.0.2.2:8080` |
| iOS / macOS / Windows | `http://localhost:8080` |

---

## 设计风格

主要采用深色主题，关键颜色：

| 用途 | 颜色 |
|------|------|
| 背景 | `#0D0D0D` |
| 卡片 | `#1A1A1A` |
| 涨 / 买 | `#00C853` |
| 跌 / 卖 | `#FF3D00` |
| 强调 | `#4A90E2` |

---

## macOS 说明

`flutter_secure_storage` 使用 macOS Keychain 存储敏感信息，已配置必要的 entitlements。

## Windows 说明

`flutter_secure_storage` 使用 Windows Credential Manager，无需额外配置。

---

## 不在范围内

- 实时行情 WebSocket 推送
- K 线图
- 推送通知
- 生物识别登录
- Linux 桌面支持
