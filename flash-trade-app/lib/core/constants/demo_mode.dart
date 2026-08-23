/// 旧デモモード設定。
/// すべてのローカル開発はバックエンドの Mock / 実券商 API を経由する。
/// ここに偽データの分岐を残さない。kDemoMode は常に false とする。
const bool kDemoMode = false;

@Deprecated('Fake demo data is disabled; use backend mock broker APIs instead.')
const String kDemoUsername = 'demo';

@Deprecated('Fake demo data is disabled; use backend mock broker APIs instead.')
const String kDemoPassword = 'demo';
