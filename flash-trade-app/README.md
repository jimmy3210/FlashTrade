# FlashTrade - 株式取引アプリ

FlashTrade バックエンド API（Spring Boot）と連携する、moomoo インスパイアの株式取引 Flutter アプリ。  
**iOS / Android / macOS / Windows** のマルチプラットフォームに対応したレスポンシブ UI。

---

## 機能概要

| 機能 | 説明 |
|-----|------|
| スプラッシュ画面 | Token チェック → 自動ログイン or ログイン画面へ遷移 |
| ログイン | JWT 認証、Token を安全ストレージに保存、自動リフレッシュ |
| ホーム | 総資産表示、ポジション Top5、日次損益棒グラフ |
| ポートフォリオ | 保有銘柄一覧・評価損益表示、取引画面連携 |
| 発注 | 成行/指値、買い/売り、バリデーション・確認ダイアログ |
| 注文履歴 | 注文一覧 / 成約履歴、フィルタ、キャンセル機能 |

---

## 技術スタック

| カテゴリ | 採用技術 |
|---------|---------|
| フレームワーク | Flutter 3.x |
| 状態管理 | Riverpod 2.x (`flutter_riverpod`) |
| HTTP クライアント | Dio 5.x |
| ローカルストレージ | flutter_secure_storage 9.x |
| ルーティング | go_router 13.x |
| チャート | fl_chart 0.68.x |
| 数値・日付フォーマット | intl 0.19.x |

---

## ターゲットプラットフォーム

| プラットフォーム | 対応バージョン | ナビゲーション |
|---------------|-------------|-------------|
| iOS | 14 以上 | BottomNavigationBar |
| Android | 9 (API 28) 以上 | BottomNavigationBar |
| macOS | 10.15 (Catalina) 以上 | NavigationRail (デスクトップ) |
| Windows | Windows 10 (1903) 以上 | NavigationRail (デスクトップ) |

> **レスポンシブ対応**: 画面幅 ≥ 800px でデスクトップレイアウト（左サイドバー）に自動切替。

---

## プロジェクト構成

```
lib/
├── main.dart                    # エントリーポイント (ProviderScope)
├── app.dart                     # MaterialApp.router + MainShell (レスポンシブ)
├── core/
│   ├── constants/
│   │   ├── api_constants.dart   # API エンドポイント・BaseURL 選択ロジック
│   │   └── app_colors.dart      # ダークテーマカラー定数
│   ├── network/
│   │   ├── dio_client.dart      # Dio シングルトン
│   │   └── api_interceptor.dart # JWT 自動付与・Token リフレッシュ
│   └── storage/
│       └── secure_storage.dart  # flutter_secure_storage ラッパー
├── features/
│   ├── auth/                    # ログイン・スプラッシュ
│   ├── home/                    # ダッシュボード・日次損益グラフ
│   ├── portfolio/               # ポジション一覧
│   ├── trade/                   # 発注フォーム
│   └── orders/                  # 注文履歴・成約履歴
└── shared/
    └── widgets/                 # PriceText, StatusBadge, StockCard 等
```

---

## セットアップ・実行

### 前提条件
- Flutter 3.x SDK インストール済み
- FlashTrade バックエンド (Spring Boot) が起動中

### インストール

```bash
flutter pub get
```

### 実行

```bash
# iOS シミュレーター
flutter run -d ios

# Android エミュレーター
flutter run -d android

# macOS デスクトップ
flutter run -d macos

# Windows デスクトップ
flutter run -d windows
```

### デモモードで起動（バックエンド不要）

バックエンドなしで UI をすぐに確認できる **デモモード** が組み込まれています。

```bash
# macOS でデモ起動（推奨）
flutter run -d macos --dart-define=kDemoMode=true

# その他のプラットフォーム
flutter run -d ios     --dart-define=kDemoMode=true
flutter run -d android --dart-define=kDemoMode=true
flutter run -d windows --dart-define=kDemoMode=true
```

デモモードは `--dart-define` で制御します。ソースコードの変更は不要です:

```bash
# デモモード（モックデータ使用、API 不要）
flutter run -d macos --dart-define=kDemoMode=true

# 本番モード（Spring Boot バックエンドへ接続）
flutter run -d macos
```

**デモログイン:**

アプリ起動後、ログイン画面に表示されるバナーをタップすると資格情報が自動入力されます。

| 項目 | 値 |
|-----|-----|
| ユーザー名 | `demo` |
| パスワード | `demo` |

**デモデータ概要:**

| 画面 | 内容 |
|-----|------|
| ホーム | 総資産・AAPL/TSLA 等 Top5 ポジション・7 日間損益グラフ |
| ポートフォリオ | AAPL / TSLA / NVDA / MSFT / AMZN / GOOGL の 6 銘柄 |
| 発注 | 注文送信後にモック成功レスポンスを返す（API 通信なし） |
| 注文履歴 | PENDING / FILLED / CANCELLED 混在の 5 件 + 成約履歴 4 件 |

> 本番モードで起動する場合は `--dart-define=kDemoMode=true` を省略し、Spring Boot バックエンドを起動してください。

---

### API 接続先（本番モード）

| プラットフォーム | BaseURL |
|---------------|---------|
| Android エミュレーター | `http://10.0.2.2:8080` |
| iOS / macOS / Windows | `http://localhost:8080` |

---

## デザインシステム

ダークモード固定。主要カラー:

| 用途 | カラーコード |
|-----|-----------|
| 背景 (Primary) | `#0D0D0D` |
| カード背景 | `#1A1A1A` |
| 上昇 / 買い | `#00C853` (緑) |
| 下落 / 売り | `#FF3D00` (赤) |
| アクセント | `#4A90E2` (青) |

---

## macOS 設定

`flutter_secure_storage` が macOS Keychain を使用するため、entitlements に以下を設定済み:
- `com.apple.security.network.client` — API 通信許可

## Windows 設定

`flutter_secure_storage` が Windows Credential Manager を使用。追加設定不要。

---

## スコープ外（非実装）

- リアルタイム株価フィード（WebSocket外部 API）
- ローソク足チャート
- プッシュ通知
- 生体認証ログイン
- Linux デスクトップ対応

