// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Chinese (`zh`).
class AppLocalizationsZh extends AppLocalizations {
  AppLocalizationsZh([String locale = 'zh']) : super(locale);

  @override
  String get appName => 'FlashTrade';

  @override
  String get navHome => '首页';

  @override
  String get navPortfolio => '持仓';

  @override
  String get navTrade => '交易';

  @override
  String get navOrders => '订单';

  @override
  String get loginTitle => 'FlashTrade';

  @override
  String get loginSubtitle => '登录';

  @override
  String get loginUsername => '用户名';

  @override
  String get loginPassword => '密码';

  @override
  String get loginButton => '登录';

  @override
  String get loginDemoHint => '本地测试请使用种子管理员账号：admin / admin123。前端不再提供 Demo 自动填充。';

  @override
  String get loginErrorEmpty => '请输入用户名和密码';

  @override
  String get loginErrorInvalid => '用户名或密码错误';

  @override
  String get loginErrorNetwork => '请检查网络连接';

  @override
  String get loginErrorFailed => '登录失败';

  @override
  String get homeTitle => 'FlashTrade';

  @override
  String get homeTotalAssets => '总资产评估额';

  @override
  String get homeDailyPnl => '本日浮动盈亏：';

  @override
  String get homeTopPositions => '持仓（前5名）';

  @override
  String get homeNoPosions => '暂无持仓';

  @override
  String get homeDailyChart => '每日盈亏（历史）';

  @override
  String get homeNoChartData => '暂无盈亏数据';

  @override
  String get homeLogout => '退出登录';

  @override
  String get logoutServerRevocationUnconfirmed => '已清除本机凭证，但服务器会话撤销未确认。';

  @override
  String get homeRefresh => '刷新';

  @override
  String get portfolioTitle => '持仓';

  @override
  String get portfolioTotalValue => '总评估额';

  @override
  String get portfolioUnrealizedPnl => '浮动盈亏';

  @override
  String get portfolioCount => '持仓数';

  @override
  String get portfolioAvg => '均价';

  @override
  String get portfolioEmpty => '暂无持仓';

  @override
  String get tradeTitle => '下单';

  @override
  String get tradeSymbol => '股票代码';

  @override
  String get tradeSymbolHint => '例：AAPL';

  @override
  String get tradeSide => '买卖方向';

  @override
  String get tradeBuy => '买入';

  @override
  String get tradeSell => '卖出';

  @override
  String get tradeOrderType => '订单类型';

  @override
  String get tradeMarket => '市价';

  @override
  String get tradeLimit => '限价';

  @override
  String get tradeQuantity => '数量（股）';

  @override
  String get tradeQuantityHint => '例：100';

  @override
  String get tradePrice => '价格（¥）';

  @override
  String get tradePriceHint => '例：150.00';

  @override
  String get tradeSubmitBuy => '确认买入';

  @override
  String get tradeSubmitSell => '确认卖出';

  @override
  String tradeConfirmTitle(String side) {
    return '$side订单确认';
  }

  @override
  String tradeConfirmBodyMarket(String side, String symbol, String qty) {
    return '确认以$side $symbol $qty股（市价）？';
  }

  @override
  String tradeConfirmBodyLimit(
    String side,
    String symbol,
    String qty,
    String price,
  ) {
    return '确认以$side $symbol $qty股 ¥$price（限价）？';
  }

  @override
  String get tradeSuccess => '下单成功';

  @override
  String get tradeFailed => '下单失败';

  @override
  String get tradeCancel => '取消';

  @override
  String get tradeValidSymbolEmpty => '请输入股票代码';

  @override
  String get tradeValidSymbolFormat => '请输入1~5位英文大写字母';

  @override
  String get tradeValidQtyEmpty => '请输入数量';

  @override
  String get tradeValidQtyMin => '请输入大于0的整数';

  @override
  String get tradeValidPriceEmpty => '请输入价格';

  @override
  String get tradeValidPriceMin => '请输入大于0的数值';

  @override
  String get ordersTitle => '订单记录';

  @override
  String get ordersTabOrders => '委托列表';

  @override
  String get ordersTabTrades => '成交记录';

  @override
  String get ordersFilterAll => '全部';

  @override
  String get ordersFilterPending => '待成交';

  @override
  String get ordersFilterFilled => '已成交';

  @override
  String get ordersEmpty => '暂无订单';

  @override
  String get tradesEmpty => '暂无成交记录';

  @override
  String get ordersCancelTitle => '取消订单';

  @override
  String get ordersCancelBody => '确认要取消此订单吗？';

  @override
  String get ordersCancelYes => '确认取消';

  @override
  String get ordersCancelNo => '不取消';

  @override
  String get ordersCancelSuccess => '订单已取消';

  @override
  String get ordersCancelFailed => '取消失败';

  @override
  String get ordersFee => '手续费';

  @override
  String get ordersBuy => '买';

  @override
  String get ordersSell => '卖';

  @override
  String get statusPending => '待成交';

  @override
  String get statusFilled => '已成交';

  @override
  String get statusCancelled => '已取消';

  @override
  String get statusUnknown => '未知';

  @override
  String get orderTypeMarket => '市价';

  @override
  String get orderTypeLimit => '限价';

  @override
  String get desktopAssetOverview => '资产概览';

  @override
  String get desktopPositions => '持仓';

  @override
  String get desktopDailyPnl => '每日盈亏';

  @override
  String get desktopOrderPanel => '委托/成交';

  @override
  String get desktopRefreshAll => '刷新全部数据';

  @override
  String get desktopPlaceOrder => '下单';

  @override
  String desktopTapToFill(String symbol) {
    return '$symbol 下单';
  }

  @override
  String get desktopSearchStocksHint => '搜索股票';

  @override
  String get desktopMarket => '市场';

  @override
  String get desktopBuyingPower => '购买力';

  @override
  String get desktopCash => '现金';

  @override
  String get desktopWatchList => '自选股';

  @override
  String get desktopSearchWatchListHint => '搜索自选股';

  @override
  String get desktopEditWatchlist => '编辑';

  @override
  String get desktopLast => '最新价';

  @override
  String get desktopChangePercent => '涨跌幅';

  @override
  String get desktopBid => '买价';

  @override
  String get desktopAsk => '卖价';

  @override
  String get desktopSpread => '点差';

  @override
  String get desktopVolume => '成交量';

  @override
  String get desktopAvg10D => '10日均价';

  @override
  String get desktopPrevClose => '前收价';

  @override
  String get desktopPriceChartPlaceholder => '价格走势图';

  @override
  String get desktopAddToWatchlist => '添加到自选股';

  @override
  String get desktopAlreadyInWatchlist => '已在自选股中';

  @override
  String get desktopDayRange => '日内波动';

  @override
  String get desktop52WRange => '52周波动';

  @override
  String get desktopOrderEntry => '下单';

  @override
  String get desktopQty => '数量';

  @override
  String get desktopTif => '时效';

  @override
  String get desktopEstimatedAmount => '估计金额';

  @override
  String get desktopPreviewSell => '预览卖出';

  @override
  String get desktopPreviewBuy => '预览买入';

  @override
  String get desktopTotalMarketValue => '总市值';

  @override
  String get desktopMarketValue => '市值';

  @override
  String get desktopOrders => '订单';

  @override
  String get desktopOrderTabWorking => '未成交';

  @override
  String get desktopOrderTabFilled => '已成交';

  @override
  String get desktopOrderTabCancelled => '已取消';

  @override
  String get desktopViewAllOrders => '查看全部订单';

  @override
  String get desktopAccountSummary => '账户摘要';

  @override
  String get desktopNetLiquidation => '净清算值';

  @override
  String get desktopCashBalance => '现金余额';

  @override
  String get desktopYtdPnl => '今年盈亏';

  @override
  String desktopDataAsOf(String time) {
    return '数据更新时间：$time';
  }

  @override
  String get desktopConnected => '已连接';

  @override
  String get errorLoadFailed => '数据加载失败';

  @override
  String get errorRetry => '重试';

  @override
  String get btnConfirm => '确认';

  @override
  String get btnCancel => '取消';
}

/// The translations for Chinese, as used in Taiwan (`zh_TW`).
class AppLocalizationsZhTw extends AppLocalizationsZh {
  AppLocalizationsZhTw() : super('zh_TW');

  @override
  String get appName => 'FlashTrade';

  @override
  String get navHome => '首頁';

  @override
  String get navPortfolio => '持倉';

  @override
  String get navTrade => '交易';

  @override
  String get navOrders => '訂單';

  @override
  String get loginTitle => 'FlashTrade';

  @override
  String get loginSubtitle => '登入';

  @override
  String get loginUsername => '使用者名稱';

  @override
  String get loginPassword => '密碼';

  @override
  String get loginButton => '登入';

  @override
  String get loginDemoHint => '本地測試請使用種子管理員帳號：admin / admin123。前端不再提供 Demo 自動填充。';

  @override
  String get loginErrorEmpty => '請輸入使用者名稱和密碼';

  @override
  String get loginErrorInvalid => '使用者名稱或密碼錯誤';

  @override
  String get loginErrorNetwork => '請檢查網路連線';

  @override
  String get loginErrorFailed => '登入失敗';

  @override
  String get homeTitle => 'FlashTrade';

  @override
  String get homeTotalAssets => '總資產評估額';

  @override
  String get homeDailyPnl => '本日浮動損益：';

  @override
  String get homeTopPositions => '持倉（前5名）';

  @override
  String get homeNoPosions => '暫無持倉';

  @override
  String get homeDailyChart => '每日損益（歷史）';

  @override
  String get homeNoChartData => '暫無損益資料';

  @override
  String get homeLogout => '登出';

  @override
  String get logoutServerRevocationUnconfirmed => '已清除本機憑證，但伺服器工作階段撤銷未確認。';

  @override
  String get homeRefresh => '重新整理';

  @override
  String get portfolioTitle => '持倉';

  @override
  String get portfolioTotalValue => '總評估額';

  @override
  String get portfolioUnrealizedPnl => '浮動損益';

  @override
  String get portfolioCount => '持倉數';

  @override
  String get portfolioAvg => '均價';

  @override
  String get portfolioEmpty => '暫無持倉';

  @override
  String get tradeTitle => '下單';

  @override
  String get tradeSymbol => '股票代碼';

  @override
  String get tradeSymbolHint => '例：AAPL';

  @override
  String get tradeSide => '買賣方向';

  @override
  String get tradeBuy => '買入';

  @override
  String get tradeSell => '賣出';

  @override
  String get tradeOrderType => '訂單類型';

  @override
  String get tradeMarket => '市價';

  @override
  String get tradeLimit => '限價';

  @override
  String get tradeQuantity => '數量（股）';

  @override
  String get tradeQuantityHint => '例：100';

  @override
  String get tradePrice => '價格（¥）';

  @override
  String get tradePriceHint => '例：150.00';

  @override
  String get tradeSubmitBuy => '確認買入';

  @override
  String get tradeSubmitSell => '確認賣出';

  @override
  String tradeConfirmTitle(String side) {
    return '$side訂單確認';
  }

  @override
  String tradeConfirmBodyMarket(String side, String symbol, String qty) {
    return '確認以$side $symbol $qty股（市價）？';
  }

  @override
  String tradeConfirmBodyLimit(
    String side,
    String symbol,
    String qty,
    String price,
  ) {
    return '確認以$side $symbol $qty股 ¥$price（限價）？';
  }

  @override
  String get tradeSuccess => '下單成功';

  @override
  String get tradeFailed => '下單失敗';

  @override
  String get tradeCancel => '取消';

  @override
  String get tradeValidSymbolEmpty => '請輸入股票代碼';

  @override
  String get tradeValidSymbolFormat => '請輸入1~5位英文大寫字母';

  @override
  String get tradeValidQtyEmpty => '請輸入數量';

  @override
  String get tradeValidQtyMin => '請輸入大於0的整數';

  @override
  String get tradeValidPriceEmpty => '請輸入價格';

  @override
  String get tradeValidPriceMin => '請輸入大於0的數值';

  @override
  String get ordersTitle => '訂單記錄';

  @override
  String get ordersTabOrders => '委託列表';

  @override
  String get ordersTabTrades => '成交記錄';

  @override
  String get ordersFilterAll => '全部';

  @override
  String get ordersFilterPending => '待成交';

  @override
  String get ordersFilterFilled => '已成交';

  @override
  String get ordersEmpty => '暫無訂單';

  @override
  String get tradesEmpty => '暫無成交記錄';

  @override
  String get ordersCancelTitle => '取消訂單';

  @override
  String get ordersCancelBody => '確認要取消此訂單嗎？';

  @override
  String get ordersCancelYes => '確認取消';

  @override
  String get ordersCancelNo => '不取消';

  @override
  String get ordersCancelSuccess => '訂單已取消';

  @override
  String get ordersCancelFailed => '取消失敗';

  @override
  String get ordersFee => '手續費';

  @override
  String get ordersBuy => '買';

  @override
  String get ordersSell => '賣';

  @override
  String get statusPending => '待成交';

  @override
  String get statusFilled => '已成交';

  @override
  String get statusCancelled => '已取消';

  @override
  String get statusUnknown => '未知';

  @override
  String get orderTypeMarket => '市價';

  @override
  String get orderTypeLimit => '限價';

  @override
  String get desktopAssetOverview => '資產概覽';

  @override
  String get desktopPositions => '持倉';

  @override
  String get desktopDailyPnl => '每日損益';

  @override
  String get desktopOrderPanel => '委託/成交';

  @override
  String get desktopRefreshAll => '重新整理全部資料';

  @override
  String get desktopPlaceOrder => '下單';

  @override
  String desktopTapToFill(String symbol) {
    return '$symbol 下單';
  }

  @override
  String get desktopSearchStocksHint => '搜尋股票';

  @override
  String get desktopMarket => '市場';

  @override
  String get desktopBuyingPower => '購買力';

  @override
  String get desktopCash => '現金';

  @override
  String get desktopWatchList => '自選股';

  @override
  String get desktopSearchWatchListHint => '搜尋自選股';

  @override
  String get desktopEditWatchlist => '編輯';

  @override
  String get desktopLast => '最新價';

  @override
  String get desktopChangePercent => '漲跌幅';

  @override
  String get desktopBid => '買價';

  @override
  String get desktopAsk => '賣價';

  @override
  String get desktopSpread => '點差';

  @override
  String get desktopVolume => '成交量';

  @override
  String get desktopAvg10D => '10日均價';

  @override
  String get desktopPrevClose => '前收價';

  @override
  String get desktopPriceChartPlaceholder => '價格走勢圖';

  @override
  String get desktopDayRange => '日內波動';

  @override
  String get desktop52WRange => '52周波動';

  @override
  String get desktopOrderEntry => '下單';

  @override
  String get desktopQty => '數量';

  @override
  String get desktopTif => '時效';

  @override
  String get desktopEstimatedAmount => '估計金額';

  @override
  String get desktopPreviewSell => '預覽賣出';

  @override
  String get desktopPreviewBuy => '預覽買入';

  @override
  String get desktopTotalMarketValue => '總市值';

  @override
  String get desktopMarketValue => '市值';

  @override
  String get desktopOrders => '訂單';

  @override
  String get desktopOrderTabWorking => '未成交';

  @override
  String get desktopOrderTabFilled => '已成交';

  @override
  String get desktopOrderTabCancelled => '已取消';

  @override
  String get desktopViewAllOrders => '查看全部訂單';

  @override
  String get desktopAccountSummary => '帳戶摘要';

  @override
  String get desktopNetLiquidation => '淨清算值';

  @override
  String get desktopCashBalance => '現金餘額';

  @override
  String get desktopYtdPnl => '今年損益';

  @override
  String desktopDataAsOf(String time) {
    return '資料更新時間：$time';
  }

  @override
  String get desktopConnected => '已連接';

  @override
  String get errorLoadFailed => '資料載入失敗';

  @override
  String get errorRetry => '重試';

  @override
  String get btnConfirm => '確認';

  @override
  String get btnCancel => '取消';
}
