**Phase 6 完了サマリー**

作成したファイル一覧：

| ファイル | 役割 |
|---|---|
| broker/schwab/config/SchwabProperties.java | `@ConfigurationProperties("app.schwab")`、mockEnabled フラグ |
| broker/schwab/dto/SchwabTokenResponse.java | OAuth2 Token 応答 |
| broker/schwab/dto/SchwabInstrument.java | 金融商品情報 |
| broker/schwab/dto/SchwabPositionItem.java | 持仓明細 |
| broker/schwab/dto/SchwabAccountPositions.java | 持仓レスポンスラッパー |
| broker/schwab/dto/SchwabOrderLeg.java | 注文レッグ |
| broker/schwab/dto/SchwabOrderResponse.java | 注文レスポンス |
| broker/schwab/dto/SchwabTransferItem.java | 成約明細 |
| broker/schwab/dto/SchwabTransaction.java | 成約レスポンス |
| broker/schwab/SchwabTokenService.java | OAuth2 トークン管理（Redis + 自動更新） |
| broker/schwab/SchwabRestClient.java | Spring `RestClient` による HTTP 通信 |
| broker/schwab/SchwabMapper.java | Schwab DTO → 内部 DTO 変換 |
| broker/schwab/SchwabBrokerClient.java | `BrokerClient` 実装（Mock / Real 両モード） |

**主要な設計ポイント：**
- `mockEnabled: true`（dev）で外部 API なしに開発・テスト可能
- `SchwabTokenService` が Redis に Access Token を TTL 付きでキャッシュし、期限切れ 120 秒前に自動 Refresh
- `BrokerService` による名前ルーティングで `"SCHWAB"` キーが自動マッチ
- `BUY_TO_COVER` → `BUY`、`SELL_SHORT` → `SELL` の正規化をマッパーで吸収

変更を行いました。