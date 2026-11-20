// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Japanese (`ja`).
class AppLocalizationsJa extends AppLocalizations {
  AppLocalizationsJa([String locale = 'ja']) : super(locale);

  @override
  String get appName => 'FlashTrade';

  @override
  String get navHome => 'ホーム';

  @override
  String get navPortfolio => '資産';

  @override
  String get navTrade => '取引';

  @override
  String get navOrders => '履歴';

  @override
  String get loginTitle => 'FlashTrade';

  @override
  String get loginSubtitle => 'ログイン';

  @override
  String get loginUsername => 'ユーザー名';

  @override
  String get loginPassword => 'パスワード';

  @override
  String get loginButton => 'ログイン';

  @override
  String get loginDemoHint => 'ローカルテストでは組み込みの管理者アカウント (admin / admin123) を使用してください。デモ自動入力は削除されました。';

  @override
  String get loginErrorEmpty => 'ユーザー名とパスワードを入力してください';

  @override
  String get loginErrorInvalid => 'ユーザー名またはパスワードが正しくありません';

  @override
  String get loginErrorNetwork => 'ネットワーク接続を確認してください';

  @override
  String get loginErrorFailed => 'ログインに失敗しました';

  @override
  String get homeTitle => 'FlashTrade';

  @override
  String get homeTotalAssets => '総資産評価額';

  @override
  String get homeDailyPnl => '本日評価損益：';

  @override
  String get homeTopPositions => 'ポジション（上位5件）';

  @override
  String get homeNoPosions => 'ポジションがありません';

  @override
  String get homeDailyChart => '日次損益（過去履歴）';

  @override
  String get homeNoChartData => '損益データがありません';

  @override
  String get homeLogout => 'ログアウト';

  @override
  String get logoutServerRevocationUnconfirmed =>
      '端末の認証情報は消去しましたが、サーバー側のセッション失効は確認できませんでした。';

  @override
  String get homeRefresh => '更新';

  @override
  String get portfolioTitle => 'ポートフォリオ';

  @override
  String get portfolioTotalValue => '総評価額';

  @override
  String get portfolioUnrealizedPnl => '評価損益';

  @override
  String get portfolioCount => '銘柄数';

  @override
  String get portfolioAvg => '平均';

  @override
  String get portfolioEmpty => 'ポジションがありません';

  @override
  String get tradeTitle => '発注';

  @override
  String get tradeSymbol => '銘柄コード';

  @override
  String get tradeSymbolHint => '例: AAPL';

  @override
  String get tradeSide => '売買';

  @override
  String get tradeBuy => '買い';

  @override
  String get tradeSell => '売り';

  @override
  String get tradeOrderType => '注文種別';

  @override
  String get tradeMarket => '成行';

  @override
  String get tradeLimit => '指値';

  @override
  String get tradeQuantity => '数量（株）';

  @override
  String get tradeQuantityHint => '例: 100';

  @override
  String get tradePrice => '価格（¥）';

  @override
  String get tradePriceHint => '例: 150.00';

  @override
  String get tradeSubmitBuy => '買い注文を確定する';

  @override
  String get tradeSubmitSell => '売り注文を確定する';

  @override
  String tradeConfirmTitle(String side) {
    return '$side注文の確認';
  }

  @override
  String tradeConfirmBodyMarket(String side, String symbol, String qty) {
    return '$symbol を $qty株（成行）で$sideますか？';
  }

  @override
  String tradeConfirmBodyLimit(
    String side,
    String symbol,
    String qty,
    String price,
  ) {
    return '$symbol を $qty株 ¥$price で$sideますか？';
  }

  @override
  String get tradeSuccess => '注文が完了しました';

  @override
  String get tradeFailed => '発注に失敗しました';

  @override
  String get tradeCancel => 'キャンセル';

  @override
  String get tradeValidSymbolEmpty => '銘柄コードを入力してください';

  @override
  String get tradeValidSymbolFormat => '英大文字1〜5文字で入力してください';

  @override
  String get tradeValidQtyEmpty => '数量を入力してください';

  @override
  String get tradeValidQtyMin => '1以上の整数を入力してください';

  @override
  String get tradeValidPriceEmpty => '価格を入力してください';

  @override
  String get tradeValidPriceMin => '0より大きい数値を入力してください';

  @override
  String get ordersTitle => '注文履歴';

  @override
  String get ordersTabOrders => '注文一覧';

  @override
  String get ordersTabTrades => '成約履歴';

  @override
  String get ordersFilterAll => '全件';

  @override
  String get ordersFilterPending => '未約定';

  @override
  String get ordersFilterFilled => '約定済';

  @override
  String get ordersEmpty => '注文がありません';

  @override
  String get tradesEmpty => '成約履歴がありません';

  @override
  String get ordersCancelTitle => '注文キャンセル';

  @override
  String get ordersCancelBody => 'この注文をキャンセルしますか？';

  @override
  String get ordersCancelYes => 'キャンセルする';

  @override
  String get ordersCancelNo => 'いいえ';

  @override
  String get ordersCancelSuccess => '注文をキャンセルしました';

  @override
  String get ordersCancelFailed => 'キャンセルに失敗しました';

  @override
  String get ordersFee => '手数料';

  @override
  String get ordersBuy => '買';

  @override
  String get ordersSell => '売';

  @override
  String get statusPending => '未約定';

  @override
  String get statusFilled => '約定済';

  @override
  String get statusCancelled => '取消済';

  @override
  String get statusUnknown => '不明';

  @override
  String get orderTypeMarket => '成行';

  @override
  String get orderTypeLimit => '指値';

  @override
  String get desktopAssetOverview => '資産概要';

  @override
  String get desktopPositions => '保有ポジション';

  @override
  String get desktopDailyPnl => '日次損益';

  @override
  String get desktopOrderPanel => '注文・成約';

  @override
  String get desktopRefreshAll => '全データ更新';

  @override
  String get desktopPlaceOrder => '発注';

  @override
  String desktopTapToFill(String symbol) {
    return '$symbolで発注';
  }

  @override
  String get desktopSearchStocksHint => '銘柄を検索';

  @override
  String get desktopMarket => '市場';

  @override
  String get desktopBuyingPower => '買付余力';

  @override
  String get desktopCash => 'キャッシュ';

  @override
  String get desktopWatchList => 'ウォッチリスト';

  @override
  String get desktopSearchWatchListHint => 'ウォッチリストを検索';

  @override
  String get desktopEditWatchlist => '編集';

  @override
  String get desktopLast => '終値';

  @override
  String get desktopChangePercent => '変化率';

  @override
  String get desktopBid => 'ビッド';

  @override
  String get desktopAsk => 'アスク';

  @override
  String get desktopSpread => 'スプレッド';

  @override
  String get desktopVolume => '出来高';

  @override
  String get desktopAvg10D => '10日平均';

  @override
  String get desktopPrevClose => '前日終値';

  @override
  String get desktopPriceChartPlaceholder => '価格チャート';

  @override
  String get desktopAddToWatchlist => 'ウォッチリストに追加';

  @override
  String get desktopAlreadyInWatchlist => 'ウォッチリストに追加済み';

  @override
  String get desktopDayRange => '日中変動';

  @override
  String get desktop52WRange => '52週変動';

  @override
  String get desktopOrderEntry => '発注';

  @override
  String get desktopQty => '数量';

  @override
  String get desktopTif => '有効期限';

  @override
  String get desktopEstimatedAmount => '予定金額';

  @override
  String get desktopPreviewSell => '売却プレビュー';

  @override
  String get desktopPreviewBuy => '買付プレビュー';

  @override
  String get desktopTotalMarketValue => '総時価総額';

  @override
  String get desktopMarketValue => '時価総額';

  @override
  String get desktopOrders => '注文';

  @override
  String get desktopOrderTabWorking => '執行中';

  @override
  String get desktopOrderTabFilled => '約定済';

  @override
  String get desktopOrderTabCancelled => 'キャンセル';

  @override
  String get desktopViewAllOrders => 'すべての注文を表示';

  @override
  String get desktopAccountSummary => 'アカウント概要';

  @override
  String get desktopNetLiquidation => '純資産';

  @override
  String get desktopCashBalance => 'キャッシュ残高';

  @override
  String get desktopYtdPnl => '年初来損益';

  @override
  String desktopDataAsOf(String time) {
    return '$time 時点のデータ';
  }

  @override
  String get desktopConnected => '接続済';

  @override
  String get errorLoadFailed => 'データを取得できませんでした';

  @override
  String get errorRetry => '再試行';

  @override
  String get btnConfirm => '確認';

  @override
  String get btnCancel => 'キャンセル';
}
