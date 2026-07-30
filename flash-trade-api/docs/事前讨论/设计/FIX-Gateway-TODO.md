# FIX Gateway TODO リスト

> 設計書: [FIX-Gateway-設計書.md](./FIX-Gateway-設計書.md)  
> 更新日: 2026-04-18

---

## Phase 1: 基盤構築 ✅ 完了

### [x] 1. 依存関係追加 (`pom.xml`)
- [x] `quickfixj-core` + `quickfixj-messages-fix42/44/50sp2` 追加
- [x] バージョン: `2.3.1`

### [x] 2. 設定クラス群
- [x] `FixGatewayProperties.java` — `@ConfigurationProperties("flashtrade.fix")`
- [x] `FixGatewayAutoConfiguration.java` — Bean 登録・条件付き起動

### [x] 3. 内部モデル
- [x] `FixInternalOrder.java` — 注文統一モデル（Record）
- [x] `FixExecutionReport.java` — 約定回報モデル（Record）
- [x] `FixOrderStatus.java` — 注文ステータス Enum
- [x] `FixSessionStatus.java` — セッション状態 Enum

### [x] 4. バージョンアダプター
- [x] `FixMessageAdapter.java` — インタフェース定義
- [x] `Fix42MessageAdapter.java` — FIX 4.2 実装
- [x] `Fix44MessageAdapter.java` — FIX 4.4 実装
- [x] `Fix50MessageAdapter.java` — FIX 5.0 SP2 実装（FIXT.1.1）

### [x] 5. コア層
- [x] `FixVersionRouter.java` — BeginString → Adapter 自動選択
- [x] `FixMessageDispatcher.java` — 内部サービスへのディスパッチ
- [x] `FixApplicationHandler.java` — QuickFIX/J Application 実装
- [x] `FixSessionManager.java` — セッション状態管理
- [x] `FixGatewayService.java` — start/stop/startAll/stopAll

### [x] 6. セキュリティ・監査
- [x] `FixAuditLogger.java` — 全メッセージ構造化ログ（Password Tag 554 マスク）
- [x] `FixRateLimiter.java` — レート制限（30 msg/sec デフォルト）

### [x] 7. テストサーバー（Acceptor）
- [x] `FixTestAcceptor.java` — ローカルテスト用 FIX サーバー（動的設定生成）
  - NewOrderSingle 受信 → NEW + FILLED ExecutionReport 返送
  - OrderCancelRequest 受信 → ExecutionReport (Canceled) 返送
  - HeartBeat / TestRequest 自動応答（QuickFIX/J が処理）

### [x] 8. REST 管理 API
- [x] `FixGatewayController.java`
  - `POST /api/fix/sessions/{key}/start`
  - `POST /api/fix/sessions/{key}/stop`
  - `POST /api/fix/sessions/start-all`
  - `POST /api/fix/sessions/stop-all`
  - `GET  /api/fix/sessions` — 全セッション状態一覧
  - `GET  /api/fix/sessions/{key}/status`

### [x] 9. BrokerClient 実装
- [x] `FixBrokerClient.java` — `BrokerClient` インタフェースの FIX 実装
  - `placeOrder()` → `NewOrderSingle` 送信 + ExecutionReport 非同期待機
  - `cancelOrder()` → `OrderCancelRequest` 送信

### [x] 10. 設定ファイル
- [x] `src/main/resources/fix/sessions/fix42-initiator.cfg`
- [x] `src/main/resources/fix/sessions/fix44-initiator.cfg`
- [x] `src/main/resources/fix/sessions/fix50sp2-initiator.cfg`
- [x] `src/main/resources/fix/test/test-initiator.cfg`
- [x] `application-dev.yml` に `flashtrade.fix.*` 設定追加

---

## Phase 2: 統合テスト（未着手）

### [ ] 11. 単体テスト
- [ ] `FixVersionRouterTest.java` — バージョン判定ロジックのテスト
- [ ] `Fix42MessageAdapterTest.java` — FIX 4.2 メッセージ変換テスト
- [ ] `Fix44MessageAdapterTest.java` — FIX 4.4 メッセージ変換テスト
- [ ] `FixRateLimiterTest.java` — レート制限テスト

### [ ] 12. 統合テスト（ローカル）
- [ ] `FixGatewayIntegrationTest.java`
  - TestAcceptor 起動 → Initiator 接続 → Logon 確認
  - NewOrderSingle 送信 → ExecutionReport 受信確認
  - OrderCancelRequest → ExecutionReport (Canceled) 確認
  - 再接続テスト（Acceptor 再起動）

---

## Phase 3: ブローカー接続（後日）

### [ ] 13. 各ブローカー固有設定
- [ ] Broker A（FIX 4.2）UAT 接続設定  
  → `fix/sessions/fix42-initiator.cfg` のホスト・認証情報を設定
- [ ] Broker B（FIX 4.4）UAT 接続設定  
  → `fix/sessions/fix44-initiator.cfg` のホスト・認証情報を設定
- [ ] 認証情報を環境変数 / Secrets Manager に移行

### [ ] 14. 監視・アラート
- [ ] Spring Actuator ヘルスインジケーター追加（FIX セッション状態）
- [ ] セッション切断時のアラート通知（Slack / メール等）

### [ ] 15. ブローカー固有アダプター拡張（必要な場合）
- [ ] カスタムタグ（Tag 9000 以上）の追加対応
- [ ] ブローカー独自の MsgType 処理追加
- [ ] 注文種別マッピング調整（ブローカーごとに OrdType の使い方が異なる場合）

---

## 実装済み

| クラス | 場所 | 完了日 |
|--------|------|--------|
| FixGatewayProperties | fix/config/ | 2026-04-18 |
| FixGatewayAutoConfiguration | fix/config/ | 2026-04-18 |
| FixMessageAdapter（IF） | fix/adapter/ | 2026-04-18 |
| Fix42MessageAdapter | fix/adapter/ | 2026-04-18 |
| Fix44MessageAdapter | fix/adapter/ | 2026-04-18 |
| Fix50MessageAdapter | fix/adapter/ | 2026-04-18 |
| FixVersionRouter | fix/routing/ | 2026-04-18 |
| FixMessageDispatcher | fix/routing/ | 2026-04-18 |
| FixApplicationHandler | fix/core/ | 2026-04-18 |
| FixSessionManager | fix/core/ | 2026-04-18 |
| FixGatewayService | fix/core/ | 2026-04-18 |
| FixAuditLogger | fix/audit/ | 2026-04-18 |
| FixRateLimiter | fix/security/ | 2026-04-18 |
| FixTestAcceptor | fix/test/ | 2026-04-18 |
| FixBrokerClient | fix/broker/ | 2026-04-18 |
| FixGatewayController | fix/controller/ | 2026-04-18 |

- [ ] `FixGatewayIntegrationTest.java`
  - TestAcceptor 起動 → Initiator 接続 → Logon 確認
  - NewOrderSingle 送信 → ExecutionReport 受信確認
  - 再接続テスト

---

## Phase 3: ブローカー接続（後日）

### [ ] 13. 各ブローカー固有設定
- [ ] Broker A（FIX 4.2）UAT 接続設定
- [ ] Broker B（FIX 4.4）UAT 接続設定
- [ ] 認証情報管理（環境変数 / Secret Manager）

### [ ] 14. 監視・アラート
- [ ] Actuator ヘルスインジケーター追加（FIX セッション状態）
- [ ] セッション切断時のアラート通知

---

## 実装済み

（実装完了後にここへ移動）
