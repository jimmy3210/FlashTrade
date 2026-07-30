/// デモモード設定
/// --dart-define=kDemoMode=true で有効化（デフォルト: false）
/// 例: flutter run -d macos --dart-define=kDemoMode=true
const bool kDemoMode = bool.fromEnvironment('kDemoMode', defaultValue: false);

/// デモログインの認証情報
const String kDemoUsername = 'demo';
const String kDemoPassword = 'demo';
