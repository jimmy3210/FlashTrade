# FIX Gateway 設計書

> バージョン: 1.0.0  
> 作成日: 2026-04-18  
> 対象: FlashTradeAPI - FIX プロトコル接続層

---

## 1. 概要 / Overview

### 1.1 目的

FlashTradeAPI に **企業級 FIX Gateway** を組み込み、複数の FIX バージョン（4.2 / 4.4 / 5.0 SP2）を自動判別・透過的に処理する統一接続層を構築する。

### 1.2 スコープ

| フェーズ | 内容 |
|---------|------|
| **Phase 1（初期開発）** | QuickFIX/J 組み込みサーバー（Executor）を使ったローカルテスト |
| **Phase 2（統合テスト）** | 各券商の FIX テスト環境への接続検証 |
| **Phase 3（本番）** | 本番 FIX セッション接続・監視・監査 |

### 1.3 採用技術

| 技術 | バージョン | 用途 |
|------|-----------|------|
| QuickFIX/J | 2.3.x | FIX プロトコルエンジン |
| Spring Boot | 3.5.7 | アプリケーションフレームワーク |
| Java | 21 | 実装言語 |
| SLF4J + Logback | - | 監査ログ |

---

## 2. アーキテクチャ / Architecture

### 2.1 全体構成図

```
┌─────────────────────────────────────────────────────────────────┐
│                     外部 FIX ブローカー                           │
│   [Broker A / FIX 4.2]  [Broker B / FIX 4.4]  [Test Server]    │
└──────────────┬───────────────────┬──────────────────┬──────────┘
               │ TCP/SSL           │ TCP/SSL          │ TCP
               ▼                   ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                    FIX Gateway Layer                             │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │             FixApplicationHandler (QFJ Application)      │   │
│  │  onCreate │ onLogon │ onLogout │ toAdmin │ fromApp       │   │
│  └──────────────────────┬──────────────────────────────────┘   │
│                          │                                       │
│  ┌───────────────────────▼──────────────────────────────────┐  │
│  │                  FixVersionRouter                         │  │
│  │  BeginString 判定 → FIX.4.2 / FIX.4.4 / FIXT.1.1        │  │
│  └───────────┬──────────────┬──────────────────┬────────────┘  │
│              │               │                  │               │
│  ┌───────────▼──┐  ┌─────────▼──────┐  ┌───────▼──────────┐   │
│  │Fix42Adapter  │  │ Fix44Adapter   │  │ Fix50Adapter     │   │
│  │(FIX 4.2→内部)│  │(FIX 4.4→内部)  │  │(FIX 5.0→内部)    │   │
│  └───────────┬──┘  └─────────┬──────┘  └───────┬──────────┘   │
│              └───────────────┼──────────────────┘               │
│                              │ FixInternalOrder / ExecutionReport│
│  ┌───────────────────────────▼──────────────────────────────┐  │
│  │                  FixMessageDispatcher                     │  │
│  │            （注文処理・回報受信・行情配信）                  │  │
│  └───────────────────────────┬──────────────────────────────┘  │
│                              │                                   │
│  ┌────────────┐  ┌───────────▼──────────┐  ┌───────────────┐   │
│  │FixAuditLog │  │  FixRateLimiter      │  │FixSessionMgr  │   │
│  │(監査ログ)  │  │  (レート制限)         │  │(セッション管理) │   │
│  └────────────┘  └──────────────────────┘  └───────────────┘   │
└──────────────────────────────┬──────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────┐
│                   Internal Service Layer                         │
│                                                                  │
│      OrderService  │  MarketDataService  │  BrokerClient         │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 パッケージ構成

```
src/main/java/com/tradex/flashtrade/
└── fix/
    ├── config/
    │   ├── FixGatewayProperties.java      # @ConfigurationProperties
    │   └── FixGatewayAutoConfiguration.java
    ├── core/
    │   ├── FixGatewayService.java         # start/stop/status 制御
    │   ├── FixApplicationHandler.java     # QFJ Application 実装
    │   └── FixSessionManager.java         # Session CRUD・状態管理
    ├── routing/
    │   ├── FixVersionRouter.java          # バージョン判定・アダプター選択
    │   └── FixMessageDispatcher.java      # 内部サービスへのディスパッチ
    ├── adapter/
    │   ├── FixMessageAdapter.java         # 共通インタフェース
    │   ├── Fix42MessageAdapter.java       # FIX 4.2 → 内部モデル変換
    │   ├── Fix44MessageAdapter.java       # FIX 4.4 → 内部モデル変換
    │   └── Fix50MessageAdapter.java       # FIX 5.0 SP2 → 内部モデル変換
    ├── model/
    │   ├── FixInternalOrder.java          # 内部注文モデル
    │   ├── FixExecutionReport.java        # 約定回報
    │   ├── FixOrderStatus.java            # 注文ステータス Enum
    │   └── FixSessionStatus.java          # セッション状態 Enum
    ├── audit/
    │   └── FixAuditLogger.java            # 全 FIX メッセージ監査ログ
    ├── security/
    │   └── FixRateLimiter.java            # セッション単位レート制限
    ├── broker/
    │   └── FixBrokerClient.java           # BrokerClient の FIX 実装
    ├── test/
    │   └── FixTestAcceptor.java           # テスト用 Acceptor サーバー
    └── controller/
        └── FixGatewayController.java      # REST API（起動/停止/状態確認）

src/main/resources/
└── fix/
    ├── sessions/
    │   ├── fix42-initiator.cfg            # FIX 4.2 Initiator 設定
    │   ├── fix44-initiator.cfg            # FIX 4.4 Initiator 設定
    │   └── fix50sp2-initiator.cfg         # FIX 5.0 SP2 Initiator 設定
    ├── test/
    │   └── test-acceptor.cfg              # テスト Acceptor サーバー設定
    └── dict/                              # QFJ 標準辞書（jar から参照可能）
        ├── FIX42.xml
        ├── FIX44.xml
        ├── FIXT11.xml
        └── FIX50SP2.xml
```

---

## 3. コンポーネント詳細 / Component Details

### 3.1 FixGatewayService（コアサービス）

**責務**: Gateway の起動・停止・状態管理。Spring Bean として管理し、REST API / アプリ起動から制御可能。

```
起動フロー:
  loadConfiguration() → createApplication() → createSocketInitiator()
  → initiator.start() → セッション確立待機

停止フロー:
  initiator.stop(reset=true) → セッション解放 → リソース解放
```

**API**:
- `start(String sessionConfigKey)` — 指定セッション起動
- `stop(String sessionConfigKey)` — 指定セッション停止
- `startAll()` — 全セッション起動
- `stopAll()` — 全セッション停止
- `getSessionStatus(SessionID)` — セッション状態取得
- `getAllSessionStatuses()` — 全セッション状態一覧

### 3.2 FixApplicationHandler（QuickFIX/J Application 実装）

**責務**: QuickFIX/J の `Application` インタフェースを実装。全 FIX コールバックを受信し、適切なコンポーネントへ委譲。

| コールバック | 処理内容 |
|------------|---------|
| `onCreate` | セッション初期化、FixSessionManager 登録 |
| `onLogon` | 接続確立通知、状態更新、監査ログ |
| `onLogout` | 切断通知、状態更新、自動再接続キュー登録 |
| `toAdmin` | ログオン時の認証情報付与（Username/Password フィールド） |
| `fromAdmin` | 管理メッセージ受信（HeartBeat、TestRequest） |
| `toApp` | 送信前フィルタ（レート制限チェック） |
| `fromApp` | **メイン処理** → VersionRouter → Adapter → Dispatcher |

### 3.3 FixVersionRouter（バージョン自動判定）

**責務**: 受信メッセージの `BeginString` を読み取り、対応するアダプターを選択。

```
判定ロジック:
  FIX.4.2  → Fix42MessageAdapter
  FIX.4.4  → Fix44MessageAdapter
  FIXT.1.1 → Fix50MessageAdapter（ApplVerID で 5.0/SP1/SP2 区別）
  その他   → UnsupportedFixVersionException をスロー
```

### 3.4 FixMessageAdapter（バージョン適配層）

**責務**: バージョン固有 FIX メッセージ → 統一内部モデルへの変換。

**共通インタフェース** (`FixMessageAdapter`):
```java
FixInternalOrder toInternalOrder(Message message, SessionID sessionId);
Message toFixNewOrder(FixInternalOrder order, SessionID sessionId);
Message toFixCancelOrder(String origClOrdId, SessionID sessionId);
FixExecutionReport toExecutionReport(Message message, SessionID sessionId);
String getSupportedVersion();  // "FIX.4.2" / "FIX.4.4" / "FIX.5.0SP2"
```

**バージョン間差異対応**:

| フィールド | FIX 4.2 | FIX 4.4 | FIX 5.0 SP2 |
|-----------|---------|---------|-------------|
| OrdType (注文種別) | Tag 40 | Tag 40 | Tag 40 |
| Price (価格) | Tag 44 | Tag 44 | Tag 44 |
| Account (口座) | Tag 1 | Tag 1 | Tag 1 |
| SecurityType | Tag 167（一部） | Tag 167 | Tag 167 |
| TradingSessionID | なし | Tag 336 | Tag 336 |
| PartyID (相手方識別) | なし | Tag 448 | Tag 448 |
| ApplVerID | なし | なし | Tag 1128 |

### 3.5 FixAuditLogger（監査ログ）

**責務**: 全 FIX メッセージの送受信を構造化ログとして記録。

**ログ項目**:
- タイムスタンプ（UTC ナノ秒）
- 方向（INBOUND / OUTBOUND）
- SessionID（SenderCompID / TargetCompID）
- FIX バージョン
- MsgType
- ClOrdID（注文 ID）
- 生 FIX メッセージ文字列（セキュリティ配慮：パスワードフィールドはマスク）

### 3.6 FixRateLimiter（レート制限）

**責務**: セッション単位でのメッセージ送信レート制限（DoS 保護）。

- デフォルト: 30 msg/sec per session（設定可変）
- 超過時: `RateLimitExceededException` スロー → `BusinessMessageReject` 送信

### 3.7 FixTestAcceptor（テストサーバー）

**責務**: Phase 1 テスト用のローカル FIX Acceptor（サーバー）。  
QuickFIX/J の `Executor` サンプルを参考に実装。

**動作**:
1. `NewOrderSingle` 受信 → 疑似約定処理 → `ExecutionReport` 返送
2. `OrderCancelRequest` 受信 → `ExecutionReport (Canceled)` 返送
3. `HeartBeat` / `TestRequest` → 自動応答

---

## 4. メッセージフロー / Message Flow

### 4.1 新規注文（NewOrderSingle）

```
FlashTradeAPI                  FIX Gateway              Broker FIX Server
     │                              │                          │
     │ placeOrder(FixInternalOrder) │                          │
     │─────────────────────────────▶│                          │
     │                              │ Adapter.toFixNewOrder()  │
     │                              │ → NewOrderSingle (D)     │
     │                              │─────────────────────────▶│
     │                              │                          │
     │                              │   ExecutionReport (8)    │
     │                              │◀─────────────────────────│
     │                              │ Adapter.toExecutionReport│
     │                              │ AuditLogger.log()        │
     │  onExecutionReport(report)   │                          │
     │◀─────────────────────────────│                          │
```

### 4.2 注文キャンセル（OrderCancelRequest）

```
FlashTradeAPI                  FIX Gateway              Broker FIX Server
     │                              │                          │
     │ cancelOrder(clOrdId)         │                          │
     │─────────────────────────────▶│                          │
     │                              │ → OrderCancelRequest (F) │
     │                              │─────────────────────────▶│
     │                              │   ExecutionReport        │
     │                              │   (OrdStatus=Canceled)   │
     │                              │◀─────────────────────────│
     │  onCancelConfirm(report)     │                          │
     │◀─────────────────────────────│                          │
```

---

## 5. 設定 / Configuration

### 5.1 application.yml 追加設定

```yaml
flashtrade:
  fix:
    enabled: true
    log-directory: logs/fix
    file-store-path: data/fix
    sessions:
      - key: "test-local"
        config-file: "fix/test/test-initiator.cfg"
        enabled: true
        description: "ローカルテストサーバー接続"
      - key: "broker-a-uat"
        config-file: "fix/sessions/fix42-initiator.cfg"
        enabled: false
        description: "Broker A UAT 環境 (FIX 4.2)"
      - key: "broker-b-uat"
        config-file: "fix/sessions/fix44-initiator.cfg"
        enabled: false
        description: "Broker B UAT 環境 (FIX 4.4)"
    rate-limit:
      messages-per-second: 30
    test-server:
      enabled: true
      port: 9876
      fix-version: "FIX.4.2"
```

### 5.2 QuickFIX/J 設定ファイル（FIX 4.2 例）

```ini
[DEFAULT]
ConnectionType=initiator
ReconnectInterval=30
HeartBtInt=30
FileStorePath=data/fix
FileLogPath=logs/fix
StartTime=00:00:00
EndTime=00:00:00
UseDataDictionary=Y
DataDictionary=FIX42.xml
ValidateFieldsOutOfOrder=N
ValidateFieldsHaveValues=Y

[SESSION]
BeginString=FIX.4.2
SenderCompID=FLASHTRADE
TargetCompID=BROKER_A
SocketConnectHost=localhost
SocketConnectPort=9876
```

### 5.3 FIX 5.0 SP2 設定（FIXT.1.1）

```ini
[DEFAULT]
ConnectionType=initiator
...
DataDictionary=FIXT11.xml
AppDataDictionary=FIX50SP2.xml

[SESSION]
BeginString=FIXT.1.1
DefaultApplVerID=FIX.5.0SP2
SenderCompID=FLASHTRADE
TargetCompID=BROKER_C
...
```

---

## 6. セキュリティ設計 / Security Design

| 脅威 | 対策 |
|------|------|
| 不正アクセス | FIX Logon 時 Username/Password 検証、IP ホワイトリスト |
| リプレイ攻撃 | MsgSeqNum 検証（QuickFIX/J 標準） |
| DoS 攻撃 | FixRateLimiter（30 msg/sec） |
| 情報漏洩 | パスワードフィールド（Tag 554）はログマスク |
| 中間者攻撃 | 本番接続は TLS（SSL）必須 |
| OWASP A04 | 入力バリデーション（全フィールド型・範囲チェック） |

---

## 7. テスト戦略 / Test Strategy

### Phase 1: ローカルテスト（QuickFIX/J Acceptor）

```
[テスト環境]
  FixTestAcceptor (port 9876)  ←→  FixGatewayService (Initiator)
  
テストシナリオ:
  ✓ Logon / Logout
  ✓ HeartBeat / TestRequest
  ✓ NewOrderSingle (4.2) → ExecutionReport
  ✓ NewOrderSingle (4.4) → ExecutionReport  
  ✓ OrderCancelRequest → ExecutionReport (Canceled)
  ✓ レート制限超過
  ✓ 無効メッセージ処理
  ✓ 再接続動作
```

### Phase 2: ブローカー UAT テスト

- Broker 提供の FIX テスト環境（UAT/SIT）に接続
- 実際の SenderCompID / TargetCompID / Credentials を使用
- 本番と同等のシナリオテスト

---

## 8. TODO リスト / TODO List

詳細は [FIX-Gateway-TODO.md](./FIX-Gateway-TODO.md) を参照。

---

## 9. 制約・注意事項 / Constraints

- QuickFIX/J は **スレッドセーフ**。1 セッション = 1 スレッド。
- 設定ファイル変更は **再起動（または stop→start）が必要**。
- FIX メッセージは **UTF-8 非対応**（ASCII のみ）。証券コードは英数字に限定。
- FIX の `BeginString` は自動判定するが、**DataDictionary は事前ロードが必要**。
- テストサーバーは開発・テスト環境のみ起動（`spring.profiles.active=dev,test`）。
