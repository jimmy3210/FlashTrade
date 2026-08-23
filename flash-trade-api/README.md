# FlashTradeAPI

基于 Spring Boot 3.5.7 的多券商证券交易聚合 API 平台，提供统一的持仓查询、订单管理、实时推送及用户认证能力。

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Java |
| 框架 | Spring Boot |
| Web 服务 | Spring MVC (Tomcat 10.1.7) |
| 安全认证 | Spring Security + JWT |
| 数据库 | MySQL |
| 缓存 | Redis |
| ORM | Spring Data JPA (Hibernate 6) |
| DB 版本管理 | Flyway |
| 实时推送 | WebSocket (STOMP) |
| API 文档 | SpringDoc OpenAPI（Swagger UI）|
| 构建工具 | Maven |
| 容器化 | Docker Compose |

---

## 主要依赖版本

| 组件 | 版本 |
|------|------|
| Spring Boot | 3.5.7 |
| Spring Cloud Dependencies BOM | 2025.0.0 |
| QuickFIX/J | 2.3.1 |
| SpringDoc OpenAPI | 2.8.6 |
| JJWT | 0.12.6 |
| Logstash Logback Encoder | 8.0 |
| Java | 21 |
| MySQL | 8.0 |
| Redis | 7.x |

> 注意：`spring-retry` 和 `resilience4j` 的具体版本由 `spring-cloud-dependencies` BOM 管理，推荐使用该 BOM 管理的版本而不要显式覆盖。

---

## 快速启动

### 前置条件

- Java 21+（推荐 Amazon Corretto 21）
- Maven 3.9.11+
- Docker Desktop 4.33+

### 1. 启动依赖服务（MySQL + Redis）

```bash
cd docker
docker compose up -d
```

容器启动后自动创建以下数据库：

| 数据库名 | 用途 |
|---------|------|
| `flash_trade_dev` | 开发环境 |
| `flash_trade_test` | 测试环境 |
| `flash_trade_prod` | 生产环境 |

连接信息（开发用途）：
- MySQL Host: `localhost:3308`
- MySQL User: `flashtrade` / Password: `flashtrade123`
- Redis Host: `localhost:6379`

### 2. 启动应用（开发模式）

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. 访问 API 文档

```
http://localhost:8080/swagger-ui.html
```

### 4. 验证 Docker 服务状态

```bash
cd docker
docker compose ps
```

---

### 初始化应用与数据库迁移（Flyway）

本项目的数据库表由应用启动时通过 Flyway 执行 `src/main/resources/db/migration` 下的 SQL 迁移脚本创建。`docker/init-db.sql` 仅创建数据库（`flash_trade_dev`/`_test`/`_prod`）。下面提供三种可选方案：

1) 本地用 Maven 启动（开发推荐）

```bash
# 进入后端项目目录
cd ..\
cd flash-trade-api

# 使用 dev profile 启动，应用会连接到 Docker 中的 MySQL (localhost:3308)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

2) 在 `docker-compose` 中同时运行应用（容器化开发）

- 在 `flash-trade-api/docker/docker-compose.yml` 中启用 `flashtrade-api` 服务（已在仓库启用）。
- 确保 `secrets` 文件存在（首次运行）：

```powershell
mkdir secrets
echo flashtrade123 > .\secrets\db_password.txt
echo somerandomjwtsecret > .\secrets\jwt_secret.txt
```

```bash
cd docker
docker compose up -d --build
docker compose logs -f flashtrade-api
```

3) 手动将迁移 SQL 导入（当无法启动应用时）

```bash
# 在 Linux/macOS
cat src/main/resources/db/migration/V*.sql | docker exec -i flashtrade-mysql mysql -u flashtrade -pflashtrade123 flash_trade_dev

# Windows PowerShell（示例）
Get-Content .\src\main\resources\db\migration\V*.sql -Raw | docker exec -i flashtrade-mysql mysql -u flashtrade -pflashtrade123 flash_trade_dev
```

验证数据库与 Flyway 历史：

```bash
docker exec -it flashtrade-mysql mysql -u flashtrade -pflashtrade123 -e "USE flash_trade_dev; SHOW TABLES;"
docker exec -it flashtrade-mysql mysql -u flashtrade -pflashtrade123 -e "SELECT * FROM flash_trade_dev.flyway_schema_history ORDER BY installed_rank DESC LIMIT 20;"
```

注意：MySQL 容器中 `/docker-entrypoint-initdb.d` 下的脚本（如 `init-db.sql`）仅在数据库卷首次创建时运行；若想重新运行，请在 `docker` 目录执行：

```bash
docker compose down -v
docker compose up -d --build
```


---

## 项目结构

```
FlashTradeAPI/
├── docker/
│   ├── docker-compose.yml      # 本地开发 Docker 环境
│   └── init-db.sql             # 数据库初始化脚本
├── docs/
│   ├── planmode.md             # 开发阶段计划与进度
│   ├── requirements.md         # 需求定义书
│   ├── database-design.md      # 数据库设计文档（阶段 2 生成）
│   └── api-spec.md             # API 规格书（阶段 1 生成）
├── src/
│   ├── main/
│   │   ├── java/com/tradex/flashtrade/
│   │   │   ├── FlashTradeApiApplication.java   # 主类（阶段 1）
│   │   │   ├── config/        # 配置类
│   │   │   ├── controller/    # REST 控制器
│   │   │   ├── service/       # 业务逻辑
│   │   │   ├── repository/    # 数据库访问
│   │   │   ├── entity/        # JPA 实体类
│   │   │   ├── dto/           # 数据传输对象
│   │   │   ├── security/      # JWT 认证组件
│   │   │   ├── broker/        # 券商接口及实现
│   │   │   └── exception/     # 异常处理
│   │   └── resources/
│   │       ├── application.yml            # 基础配置
│   │       ├── application-dev.yml        # 开发环境
│   │       ├── application-test.yml       # 测试环境
│   │       ├── application-prod.yml       # 生产环境
│   │       └── db/migration/              # Flyway SQL 迁移脚本
│   └── test/
│       └── java/com/tradex/flashtrade/    # 测试代码
├── pom.xml
└── README.md
```

---

## 开发阶段

详见 [docs/planmode.md](docs/planmode.md)

| 阶段 | 内容 | 状态 |
|------|------|------|
| Phase 0 | 项目准备（Maven、Docker、目录结构） | ✅ 完成 |
| Phase 1 | Spring Boot 依赖 + 主类 | ⬜ 待开始 |
| Phase 2 | 数据库迁移（Flyway + 实体类） | ⬜ 待开始 |
| Phase 3 | 用户认证（JWT） | ⬜ 待开始 |
| Phase 4 | 内部 API + WebSocket 推送 | ⬜ 待开始 |
| Phase 5 | 外部券商 API 通信 | ⬜ 待开始 |
| Phase 6 | 嘉信 Schwab 券商实现 | ⬜ 待开始 |

---

## 品牌命名统一说明：Tastytrade（Legacy: Tastyworks）

在项目中，统一使用品牌名 `Tastytrade` 作为对外命名。

原因：

- 现在官方品牌已统一为 `tastytrade`
- `tastyworks` 是历史遗留名称，仍然会出现在旧 API 域名、历史文档和部分代码字段中
- 这两者指向同一家公司和同一平台，但为了避免概念混淆，项目内统一采用 `Tastytrade` 作为标准命名

> 注意：底层真实请求地址仍可能使用 `api.cert.tastyworks.com` / `api.tastyworks.com` 这类 legacy 域名，因为这是官方历史 API 入口；因此项目文档中应明确区分：品牌名统一为 `Tastytrade`，而 endpoint 域名可能保留旧值。

---

## 券商环境隔离策略（Sandbox / Production）

本项目遵循统一的券商环境分层原则：每个券商都同时支持两套环境，并通过配置文件或环境变量控制。原则是：

- Sandbox：供测试使用，数据和交易行为均为模拟或测试数据，适合开发和联调。
- Production：真实交易环境，真实资金和真实订单。
- 配置层级：环境配置属于部署层，不属于用户数据；因此优先放在 Docker / Spring Cloud Config / 环境变量中，而不是放进数据库。

### 统一规则

对于每个券商，均遵循以下配置模式：

```yaml
app:
  <broker>:
    mock-enabled: true|false
    base-url: ${BROKER_BASE_URL:https://sandbox-or-production-url}
```

其中：

- `mock-enabled: true` 表示开发/测试环境，通常返回 Mock 数据或跳过真实鉴权。
- `mock-enabled: false` 表示真实环境，调用真实券商 API。
- `base-url` 应根据环境切换到相应地址。

### Tastytrade 参考配置

```yaml
# 开发 / Sandbox
app:
  tastytrade:
    mock-enabled: true
    base-url: ${TASTYTRADE_BASE_URL:https://api.cert.tastyworks.com}

# 生产环境
app:
  tastytrade:
    mock-enabled: false
    base-url: ${TASTYTRADE_BASE_URL:https://api.tastyworks.com}
```

> 说明：项目内部统一使用 `tastytrade` 命名；真实 API 域名仍可能保留 `api.cert.tastyworks.com` / `api.tastyworks.com`，这是官方历史域名，属于同一平台。

### 其他券商同样适用

- Schwab：开发/测试时使用 Sandbox/Test 地址或 Mock；生产时使用真实地址。
- E*TRADE：开发/测试时可使用测试环境或 Mock；生产时使用正式 API 地址。
- Tastyworks：Sandbox = `https://api.cert.tastyworks.com`，Production = `https://api.tastyworks.com`。

> 结论：环境切换是系统级配置，不属于用户表字段；如果未来需要让某个用户绑定“自己使用 Sandbox/Production”，再通过 DB 字段扩展，但默认应由部署配置决定。

---

## 配置说明

### 环境变量（生产环境必须设置）

| 变量名 | 描述 | 示例 |
|--------|------|------|
| `SPRING_PROFILES_ACTIVE` | 激活的配置文件 | `prod` |
| `DB_HOST` | MySQL 主机地址 | `db.example.com` |
| `DB_PORT` | MySQL 端口 | `3306` |
| `DB_USERNAME` | MySQL 用户名 | `flashtrade` |
| `DB_PASSWORD` | MySQL 密码 | `*****` |
| `REDIS_HOST` | Redis 主机地址 | `redis.example.com` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密码（可选） | `*****` |
| `JWT_SECRET` | JWT 签名密钥（256 位以上）| `*****` |

---

## 测试

```bash
# 运行全部测试
mvn test

# 运行指定测试环境
mvn test -Dspring.profiles.active=test
```

---

## 注意事项

- 生产环境所有密钥、密码均通过环境变量注入，禁止硬编码于配置文件
- JWT 密钥长度须为 256 位以上，生产环境请替换 `dev-secret-key`
- Flyway 迁移脚本一旦执行，不可修改，只能新增版本
---

## FIX Gateway（プロトコル接続）

FlashTradeAPI は **QuickFIX/J** を使った企業級 FIX Gateway を内蔵しています。  
FIX 4.2 / 4.4 / 5.0 SP2 の自動バージョン切換えに対応しています。

---

### Phase 1: ローカルテストサーバーで動作確認

開発環境ではテスト用 FIX Acceptor サーバーが自動起動します。追加インストール不要です。

**1. `application-dev.yml` の設定確認**（デフォルトで有効）

```yaml
flashtrade:
  fix:
    enabled: true
    test-server:
      enabled: true
      port: 9876
      fix-version: "FIX.4.2"
    sessions:
      - key: "test-local"
        config-file: "fix/test/test-initiator.cfg"
        enabled: true
```

**2. アプリを起動するだけで自動接続**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

起動ログに以下が表示されれば接続成功です：

```
FIX テストサーバー起動完了: port=9876
FIX セッション LOGON 完了: FIX.4.2:FLASHTRADE->TEST_SERVER
```

**3. 動作テスト（REST API）**

```bash
# 全セッション状態確認（要認証トークン）
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/fix/sessions

# 個別セッション停止
curl -X POST -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/fix/sessions/test-local/stop

# 個別セッション再起動
curl -X POST -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/fix/sessions/test-local/start
```

---

### Phase 2: 具体的な券商 FIX テスト環境への接続

**ステップ 1: 券商から接続情報を入手する**

券商の FIX テスト環境（UAT/SIT）申請時に以下の情報が提供されます：

| 項目 | 説明 | 例 |
|------|------|-----|
| `SocketConnectHost` | FIX サーバーのホスト名/IP | `fix-uat.broker.com` |
| `SocketConnectPort` | ポート番号 | `9878` |
| `SenderCompID` | 自社の CompID（券商が割り当て） | `FLASHTRADE01` |
| `TargetCompID` | 券商側の CompID | `BROKER_UAT` |
| `Username` | ログオン認証ユーザー名 | `ft_user` |
| `Password` | ログオン認証パスワード | `*****` |
| `FIX バージョン` | 使用するバージョン | `FIX.4.2` or `FIX.4.4` |
| `SSL 要否` | TLS 接続が必要か | ほとんどの場合 `Y` |

**ステップ 2: 設定ファイルを編集する**

FIX バージョンに対応するテンプレートファイルを編集します：

- FIX 4.2 → [src/main/resources/fix/sessions/fix42-initiator.cfg](src/main/resources/fix/sessions/fix42-initiator.cfg)
- FIX 4.4 → [src/main/resources/fix/sessions/fix44-initiator.cfg](src/main/resources/fix/sessions/fix44-initiator.cfg)
- FIX 5.0 SP2 → [src/main/resources/fix/sessions/fix50sp2-initiator.cfg](src/main/resources/fix/sessions/fix50sp2-initiator.cfg)

編集例（FIX 4.2 の場合）：

```ini
[SESSION]
BeginString=FIX.4.2
SenderCompID=FLASHTRADE01          # ← 券商から割り当てられた値
TargetCompID=BROKER_UAT            # ← 券商の CompID
SocketConnectHost=fix-uat.broker.com  # ← 券商の UAT サーバー
SocketConnectPort=9878             # ← 券商指定ポート
SocketUseSSL=Y                     # ← 通常 SSL 必須
Username=ft_user                   # ← 券商から提供
Password=your_password             # ← 実際は環境変数で渡す
```

> **セキュリティ注意**: `Username` / `Password` は設定ファイルに直書きせず、  
> 環境変数または Spring Cloud Config で外部注入してください。

**ステップ 3: `application.yml` にセッションを追加する**

```yaml
flashtrade:
  fix:
    enabled: true
    sessions:
      - key: "test-local"           # Phase 1 ローカルテスト（常時有効）
        config-file: "fix/test/test-initiator.cfg"
        enabled: true
        description: "ローカルテストサーバー"

      - key: "broker-a-uat"         # ← 新規追加：券商 A の UAT 環境
        config-file: "fix/sessions/fix42-initiator.cfg"
        enabled: true               # ← true にすると起動時に自動接続
        description: "Broker A UAT (FIX 4.2)"

      - key: "broker-b-uat"
        config-file: "fix/sessions/fix44-initiator.cfg"
        enabled: false              # ← false のままにしておき必要時に手動起動
        description: "Broker B UAT (FIX 4.4)"
```

**ステップ 4: 接続確認**

```bash
# アプリ起動
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# セッション状態確認
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/fix/sessions

# 期待レスポンス（例）
{
  "sessions": [
    { "key": "broker-a-uat", "running": true },
    { "key": "test-local",   "running": true }
  ],
  "loggedOnCount": 2,
  "totalCount": 2
}
```

---

### Phase 3: 本番券商接続での追加カスタマイズ

券商によっては標準 FIX に加えて独自の処理が必要な場合があります。

#### カスタムフィールド（Tag 9000 以上）の追加

`FixApplicationHandler.fromApp()` でメッセージを受信後、バージョンアダプターの `toExecutionReport()` を拡張します：

```java
// Fix42MessageAdapter.java の toExecutionReport() 内に追加
// ブローカー独自タグ（例: Tag 9001 = ブローカー内部 ID）
String brokerId = safeGetString(execReport, 9001);
```

#### ブローカー固有の `DataDictionary` を使う

ブローカーから提供された `BrokerCustom.xml`（カスタム DataDictionary）がある場合：

```ini
# fix42-initiator.cfg 内
[SESSION]
DataDictionary=fix/dict/BrokerA_FIX42_Custom.xml  # カスタム辞書を指定
```

辞書ファイルを `src/main/resources/fix/dict/` に配置してください。

#### SSL 証明書の設定

```ini
[SESSION]
SocketUseSSL=Y
SocketKeyStore=certs/flashtrade.jks
SocketKeyStorePassword=${FIX_KEYSTORE_PASSWORD}
SocketTrustStore=certs/broker-ca.jks
SocketTrustStorePassword=${FIX_TRUSTSTORE_PASSWORD}
```

---

### FIX Gateway 関連ファイル一覧

```
src/main/java/com/tradex/flashtrade/fix/
├── adapter/          # FIX 4.2 / 4.4 / 5.0 メッセージ変換（バージョン自動切換）
├── audit/            # 全メッセージ監査ログ（Password マスク済み）
├── broker/           # FixBrokerClient（BrokerClient 実装）
├── config/           # Spring Boot 自動設定
├── controller/       # REST 管理 API（start / stop / status）
├── core/             # FixGatewayService（起動・停止制御）
├── model/            # 内部モデル（バージョン非依存）
├── routing/          # バージョンルーター・ディスパッチャー
├── security/         # レート制限（DoS 保護）
└── test/             # ローカルテスト用 Acceptor サーバー

src/main/resources/fix/
├── sessions/         # 本番/UAT 接続設定テンプレート
└── test/             # テスト用 Initiator 設定

docs/事前讨论/设计/
├── FIX-Gateway-設計書.md  # アーキテクチャ・コンポーネント詳細
└── FIX-Gateway-TODO.md    # 実装進捗 TODO
```
