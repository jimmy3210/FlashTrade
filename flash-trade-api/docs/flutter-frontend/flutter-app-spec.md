# FlashTrade Flutter フロントエンド 式様書

**バージョン**: 1.0.0  
**作成日**: 2026-03-31  
**対象**: FlashTrade iOS / Android アプリ（moomoo インスパイア）

---

## 1. 概要

### 1.1 目的
FlashTradeAPI バックエンドと連携する、シンプルな株式取引フロントエンドアプリ。  
moomoo の UI/UX を参考にしつつ、必要最低限の機能に絞った実装を行う。

### 1.2 ターゲットプラットフォーム
- iOS 14 以上
- Android 9 (API 28) 以上

### 1.3 技術スタック
| カテゴリ | 採用技術 |
|---------|---------|
| フレームワーク | Flutter 3.x |
| 状態管理 | Riverpod 2.x |
| HTTPクライアント | Dio + Retrofit |
| ローカルストレージ | flutter_secure_storage (Token保存) |
| ルーティング | go_router |
| チャート | fl_chart |
| WebSocket | stomp_dart_client |
| アイコン | Material Icons + cupertino_icons |

---

## 2. デザインシステム

### 2.1 カラーテーマ（ダークモード固定）
```
背景色（Primary BG）    : #0D0D0D
カード背景              : #1A1A1A
セカンダリ背景          : #242424
ボーダー               : #2E2E2E
テキスト（Primary）     : #FFFFFF
テキスト（Secondary）   : #8A8A8A
上昇色（Buy / Profit）  : #00C853  (緑)
下落色（Sell / Loss）   : #FF3D00  (赤)
アクセント              : #4A90E2  (青)
警告                   : #FFC107  (黄)
```

### 2.2 タイポグラフィ
- フォント: システムフォント（SF Pro / Roboto）
- 数値表示: `tabular-nums` （桁がずれない）
- 価格: `+2.34 (+1.56%)` 形式

### 2.3 共通コンポーネント
| コンポーネント | 説明 |
|-------------|------|
| `PriceText` | 価格・騰落率を色付きで表示 |
| `StockCard` | 銘柄カード（銘柄コード / 現在値 / 騰落） |
| `OrderTypeBadge` | LIMIT / MARKET バッジ |
| `StatusBadge` | 注文ステータスバッジ |
| `LoadingOverlay` | ローディング共通UI |
| `ErrorSnackBar` | エラーメッセージ |

---

## 3. 画面一覧

### 3.1 画面マップ
```
SplashScreen
    └── LoginScreen
            └── MainScreen (BottomNavBar 4タブ)
                    ├── Tab1: ホーム（ダッシュボード）
                    ├── Tab2: ポートフォリオ（持仓）
                    ├── Tab3: 取引（発注）
                    └── Tab4: 注文履歴
```

---

## 4. 各画面の詳細仕様

### 4.1 SplashScreen（起動画面）
**目的**: Token有無チェック → 自動遷移

| 要素 | 内容 |
|-----|------|
| ロゴ | FlashTrade ロゴ（テキスト） |
| 処理 | SecureStorage から Token 読込 → 有効なら MainScreen へ、無効なら LoginScreen へ |
| 遷移時間 | 最大 1.5 秒 |

---

### 4.2 LoginScreen（ログイン画面）
**API**: `POST /api/v1/auth/login`

| 要素 | 仕様 |
|-----|-----|
| ユーザー名入力欄 | TextFormField、必須バリデーション |
| パスワード入力欄 | TextFormField、obscureText、必須バリデーション |
| ログインボタン | ElevatedButton、ローディング表示付き |
| エラー表示 | APIエラーをSnackBarで表示 |

**フロー**:
1. ユーザーが認証情報を入力
2. `POST /api/v1/auth/login` を呼び出し
3. `accessToken` + `refreshToken` を SecureStorage に保存
4. MainScreen に遷移

---

### 4.3 HomeScreen（ホーム・ダッシュボード）
**API**: 
- `GET /api/v1/portfolio/asset-view`
- `GET /api/v1/analytics/pnl/daily?date=today`

#### レイアウト構成
```
┌─────────────────────────────────┐
│  AppBar: "FlashTrade"  [通知アイコン]  │
├─────────────────────────────────┤
│  資産サマリーカード                    │
│  ┌───────────────────────────┐  │
│  │ 総資産評価額                   │  │
│  │ ¥ 1,234,567               │  │
│  │ 本日損益: +¥12,345 (+1.2%) │  │
│  └───────────────────────────┘  │
├─────────────────────────────────┤
│  ポジション一覧（Top 5）              │
│  ┌─────────────────────────┐   │
│  │ AAPL  150.00 +2.34(+1.6%)│   │
│  │ TSLA  250.00 -5.00(-2.0%)│   │
│  └─────────────────────────┘   │
├─────────────────────────────────┤
│  日次損益グラフ（棒グラフ 7日分）        │
└─────────────────────────────────┘
```

**表示項目**:
- 総資産評価額（`marketValue` の合計）
- 本日実現損益 + 未実現損益
- 保有ポジション Top5（`unrealizedPnl` 降順）
- 過去7日間の日次損益棒グラフ

---

### 4.4 PortfolioScreen（ポートフォリオ画面）
**API**: `GET /api/v1/portfolio/positions`

#### レイアウト構成
```
┌─────────────────────────────────┐
│  AppBar: "ポートフォリオ"              │
├─────────────────────────────────┤
│  サマリー行                          │
│  総評価額  未実現損益  実現損益          │
├─────────────────────────────────┤
│  ポジションリスト                      │
│  ┌─────────────────────────┐   │
│  │ [銘柄] [保有数] [評価額]      │   │
│  │ [平均コスト] [損益] [損益率]  │   │
│  └─────────────────────────┘   │
│  ・・・ (スクロール)               │
│                                 │
│  各行タップ → 取引画面へ遷移           │
└─────────────────────────────────┘
```

**表示項目** (PositionResponse から):
| フィールド | 表示 |
|---------|-----|
| `symbol` | 銘柄コード（太字） |
| `quantity` | 保有数量 |
| `averageCost` | 平均取得コスト |
| `currentPrice` | 現在値（リアルタイム推送） |
| `marketValue` | 評価額 |
| `unrealizedPnl` | 評価損益（色付き） |
| `brokerName` | ブローカー名（小さく） |

---

### 4.5 TradeScreen（取引・発注画面）
**API**:
- `POST /api/v1/trade/order`
- `GET /api/v1/trade/order/{id}/status`

#### レイアウト構成
```
┌─────────────────────────────────┐
│  AppBar: "発注"                    │
├─────────────────────────────────┤
│  銘柄入力欄: [      AAPL       ]   │
├─────────────────────────────────┤
│  [  買い  ] [  売り  ]  ← タブ切替  │
├─────────────────────────────────┤
│  注文種別:  [MARKET] [LIMIT]      │
├─────────────────────────────────┤
│  数量:  [  100  ] 株              │
├─────────────────────────────────┤
│  価格:  [ 150.00 ]               │
│  ※ MARKET選択時は非活性           │
├─────────────────────────────────┤
│  発注確認ダイアログ                  │
│  [    買い注文を確定する    ]       │
└─────────────────────────────────┘
```

**バリデーション**:
- 銘柄: 必須、英大文字1〜5文字
- 数量: 必須、1以上の整数
- 価格（LIMIT時）: 必須、0より大きい数値

**発注フロー**:
1. フォーム入力 → バリデーション
2. 確認ダイアログ表示  
   「AAPL を 100株 ¥150.00 で買いますか？」
3. 確定 → `POST /api/v1/trade/order`
4. 成功 → SnackBar「注文が完了しました」
5. 注文履歴タブへ遷移

---

### 4.6 OrdersScreen（注文履歴画面）
**API**: 
- `GET /api/v1/orders`
- `GET /api/v1/trades`
- `DELETE /api/v1/trade/order/{id}` （キャンセル）

#### レイアウト構成
```
┌─────────────────────────────────┐
│  AppBar: "注文履歴"                 │
├─────────────────────────────────┤
│  [注文一覧] [成約履歴]  ← タブ切替   │
├─────────────────────────────────┤
│  フィルタ: [全件] [未約定] [約定済]   │
├─────────────────────────────────┤
│  注文カード                         │
│  ┌─────────────────────────┐   │
│  │ AAPL  買  LIMIT ¥150.00  │   │
│  │ 100株  [PENDING]  [取消]  │   │
│  │ 2026-03-31 10:30:00     │   │
│  └─────────────────────────┘   │
│  ・・・ (スクロール / pull-to-refresh) │
└─────────────────────────────────┘
```

**注文カード**: OrderResponse から
- PENDING 状態の注文には「取消」ボタン表示  
- キャンセル確認ダイアログを表示してから実行

---

## 5. ナビゲーション設計

### 5.1 BottomNavigationBar
| タブ | アイコン | ラベル |
|-----|---------|-------|
| 0 | `Icons.home` | ホーム |
| 1 | `Icons.pie_chart` | 資産 |
| 2 | `Icons.add_circle` | 取引 |
| 3 | `Icons.history` | 履歴 |

### 5.2 ルート定義（go_router）
```
/splash          → SplashScreen
/login           → LoginScreen
/home            → MainScreen (shell route)
  /home/dashboard  → HomeScreen
  /home/portfolio  → PortfolioScreen
  /home/trade      → TradeScreen
  /home/orders     → OrdersScreen
```

---

## 6. 状態管理設計（Riverpod）

### 6.1 主要 Provider
```dart
// 認証状態
authProvider            : AsyncNotifierProvider<AuthNotifier, AuthState>

// ポジション一覧
positionsProvider       : FutureProvider<List<PositionResponse>>

// 注文一覧
ordersProvider          : FutureProvider<List<OrderResponse>>

// 成約履歴
tradesProvider          : FutureProvider<List<TradeResponse>>

// 資産ビュー
assetViewProvider       : FutureProvider<List<PositionView>>

// 日次損益（7日分）
dailyPnlProvider        : FutureProvider<List<DailyPnlResponse>>

// 発注フォーム状態
tradeFormProvider       : StateNotifierProvider<TradeFormNotifier, TradeFormState>
```

---

## 7. API 通信設計

### 7.1 Dio 設定
- BaseURL: `http://10.0.2.2:8080` (Android エミュレータ) / `http://localhost:8080` (iOS)
- Interceptor: JWT 自動付与、Token 期限切れ時自動リフレッシュ
- タイムアウト: connectTimeout 5s / receiveTimeout 10s

### 7.2 エラーハンドリング
| HTTPステータス | 処理 |
|-------------|------|
| 401 | Token リフレッシュ → 失敗時ログイン画面へ |
| 403 | 「アクセス権限がありません」表示 |
| 4xx | エラーメッセージを SnackBar 表示 |
| 5xx | 「サーバーエラーが発生しました」表示 |
| Network Error | 「ネットワーク接続を確認してください」表示 |

### 7.3 Token 管理
- `accessToken` / `refreshToken` → `flutter_secure_storage` に保存
- Access Token 期限切れ (401) → Refresh Token で自動更新
- Refresh Token も期限切れ → ログイン画面へ強制遷移

---

## 8. フォルダ構成

```
lib/
├── main.dart
├── app.dart                  # MaterialApp + ルーティング
├── core/
│   ├── constants/
│   │   ├── api_constants.dart
│   │   └── app_colors.dart
│   ├── network/
│   │   ├── dio_client.dart
│   │   └── api_interceptor.dart
│   └── storage/
│       └── secure_storage.dart
├── features/
│   ├── auth/
│   │   ├── data/
│   │   │   └── auth_repository.dart
│   │   ├── domain/
│   │   │   └── auth_state.dart
│   │   ├── presentation/
│   │   │   ├── login_screen.dart
│   │   │   └── splash_screen.dart
│   │   └── providers/
│   │       └── auth_provider.dart
│   ├── home/
│   │   ├── data/
│   │   ├── presentation/
│   │   │   └── home_screen.dart
│   │   └── providers/
│   ├── portfolio/
│   │   ├── data/
│   │   │   └── portfolio_repository.dart
│   │   ├── domain/
│   │   │   └── models.dart
│   │   ├── presentation/
│   │   │   └── portfolio_screen.dart
│   │   └── providers/
│   │       └── portfolio_provider.dart
│   ├── trade/
│   │   ├── data/
│   │   │   └── trade_repository.dart
│   │   ├── domain/
│   │   │   └── trade_form_state.dart
│   │   ├── presentation/
│   │   │   └── trade_screen.dart
│   │   └── providers/
│   │       └── trade_provider.dart
│   └── orders/
│       ├── data/
│       │   └── orders_repository.dart
│       ├── presentation/
│       │   └── orders_screen.dart
│       └── providers/
│           └── orders_provider.dart
└── shared/
    └── widgets/
        ├── price_text.dart
        ├── status_badge.dart
        └── loading_overlay.dart
```

---

## 9. pubspec.yaml 依存関係

```yaml
dependencies:
  flutter:
    sdk: flutter
  flutter_riverpod: ^2.5.1
  riverpod_annotation: ^2.3.5
  go_router: ^13.2.0
  dio: ^5.4.3
  flutter_secure_storage: ^9.0.0
  fl_chart: ^0.68.0
  intl: ^0.19.0
  freezed_annotation: ^2.4.1
  json_annotation: ^4.9.0

dev_dependencies:
  build_runner: ^2.4.9
  freezed: ^2.5.2
  json_serializable: ^6.8.0
  riverpod_generator: ^2.4.0
```

---

## 10. スコープ外（非実装）

以下の機能は今回のスコープ外とする：

- リアルタイム価格データ（株価フィード外部API統合）
- チャート（ローソク足）
- プッシュ通知
- 管理画面（AdminController）
- 風控機能（RiskController）
- 生体認証ログイン
- ダークモード/ライトモード切替（ダーク固定）
