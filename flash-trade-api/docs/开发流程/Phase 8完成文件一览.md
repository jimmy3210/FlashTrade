## Phase 8 完了サマリー

### E*TRADE 実装（12ファイル）

| ファイル | 内容 |
|---|---|
| config/EtradeProperties.java | `@RefreshScope` + OAuth 1.0a 設定 |
| `dto/EtradeInstrument.java` 〜 EtradeTransactionResponse.java | API レスポンス DTO 群 |
| EtradeTokenService.java | OAuth 1.0a Token を Redis 管理（TTL 23h） |
| EtradeRestClient.java | HMAC-SHA1 署名生成 + HTTP 呼び出し |
| EtradeMapper.java | DTO → 内部 BrokerDTO 変換（BUY_TO_COVER→BUY 正規化等） |
| EtradeBrokerClient.java | `BrokerClient` 実装（Mock/Real 両対応） |

### Tastyworks 実装（12ファイル）

| ファイル | 内容 |
|---|---|
| config/TastyworksProperties.java | `@RefreshScope` + Session 認証設定 |
| `dto/TastyworksSession/Position/Order/Leg/Fill*.java` | API レスポンス DTO 群 |
| TastyworksTokenService.java | Session Token Redis 管理（TTL 24h） |
| TastyworksRestClient.java | Bearer Token 認証 + 401 時自動再ログイン |
| TastyworksMapper.java | Short ポジション qty 符号反転、fills から取引抽出 |
| TastyworksBrokerClient.java | `BrokerClient` 実装（Mock/Real 両対応） |

### テスト：25件 全パス ✅

- `EtradeMapperTest`（6件）、`EtradeBrokerClientTest`（5件）  
- `TastyworksMapperTest`（5件）、`TastyworksBrokerClientTest`（5件）

変更を行いました。