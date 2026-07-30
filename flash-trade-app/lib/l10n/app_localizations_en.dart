// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get appName => 'FlashTrade';

  @override
  String get navHome => 'Home';

  @override
  String get navPortfolio => 'Portfolio';

  @override
  String get navTrade => 'Trade';

  @override
  String get navOrders => 'Orders';

  @override
  String get loginTitle => 'FlashTrade';

  @override
  String get loginSubtitle => 'Sign In';

  @override
  String get loginUsername => 'Username';

  @override
  String get loginPassword => 'Password';

  @override
  String get loginButton => 'Sign In';

  @override
  String get loginDemoHint => 'Demo mode: tap to fill demo / demo';

  @override
  String get loginErrorEmpty => 'Please enter username and password';

  @override
  String get loginErrorInvalid => 'Invalid username or password';

  @override
  String get loginErrorNetwork => 'Please check your network connection';

  @override
  String get loginErrorFailed => 'Login failed';

  @override
  String get homeTitle => 'FlashTrade';

  @override
  String get homeTotalAssets => 'Total Asset Value';

  @override
  String get homeDailyPnl => 'Unrealized P&L: ';

  @override
  String get homeTopPositions => 'Top 5 Positions';

  @override
  String get homeNoPosions => 'No positions';

  @override
  String get homeDailyChart => 'Daily P&L (History)';

  @override
  String get homeNoChartData => 'No P&L data';

  @override
  String get homeLogout => 'Logout';

  @override
  String get homeRefresh => 'Refresh';

  @override
  String get portfolioTitle => 'Portfolio';

  @override
  String get portfolioTotalValue => 'Total Value';

  @override
  String get portfolioUnrealizedPnl => 'Unrealized P&L';

  @override
  String get portfolioCount => 'Positions';

  @override
  String get portfolioAvg => 'Avg Cost';

  @override
  String get portfolioEmpty => 'No positions';

  @override
  String get tradeTitle => 'Place Order';

  @override
  String get tradeSymbol => 'Symbol';

  @override
  String get tradeSymbolHint => 'e.g. AAPL';

  @override
  String get tradeSide => 'Side';

  @override
  String get tradeBuy => 'Buy';

  @override
  String get tradeSell => 'Sell';

  @override
  String get tradeOrderType => 'Order Type';

  @override
  String get tradeMarket => 'Market';

  @override
  String get tradeLimit => 'Limit';

  @override
  String get tradeQuantity => 'Quantity (shares)';

  @override
  String get tradeQuantityHint => 'e.g. 100';

  @override
  String get tradePrice => 'Price (¥)';

  @override
  String get tradePriceHint => 'e.g. 150.00';

  @override
  String get tradeSubmitBuy => 'Confirm Buy';

  @override
  String get tradeSubmitSell => 'Confirm Sell';

  @override
  String tradeConfirmTitle(String side) {
    return 'Confirm $side Order';
  }

  @override
  String tradeConfirmBodyMarket(String side, String symbol, String qty) {
    return '$side $qty shares of $symbol at market price?';
  }

  @override
  String tradeConfirmBodyLimit(
    String side,
    String symbol,
    String qty,
    String price,
  ) {
    return '$side $qty shares of $symbol at ¥$price?';
  }

  @override
  String get tradeSuccess => 'Order placed successfully';

  @override
  String get tradeFailed => 'Order failed';

  @override
  String get tradeCancel => 'Cancel';

  @override
  String get tradeValidSymbolEmpty => 'Please enter a symbol';

  @override
  String get tradeValidSymbolFormat => '1–5 uppercase letters only';

  @override
  String get tradeValidQtyEmpty => 'Please enter quantity';

  @override
  String get tradeValidQtyMin => 'Must be a positive integer';

  @override
  String get tradeValidPriceEmpty => 'Please enter price';

  @override
  String get tradeValidPriceMin => 'Must be greater than 0';

  @override
  String get ordersTitle => 'Order History';

  @override
  String get ordersTabOrders => 'Orders';

  @override
  String get ordersTabTrades => 'Trades';

  @override
  String get ordersFilterAll => 'All';

  @override
  String get ordersFilterPending => 'Pending';

  @override
  String get ordersFilterFilled => 'Filled';

  @override
  String get ordersEmpty => 'No orders';

  @override
  String get tradesEmpty => 'No trades';

  @override
  String get ordersCancelTitle => 'Cancel Order';

  @override
  String get ordersCancelBody => 'Are you sure you want to cancel this order?';

  @override
  String get ordersCancelYes => 'Yes, Cancel';

  @override
  String get ordersCancelNo => 'No';

  @override
  String get ordersCancelSuccess => 'Order cancelled';

  @override
  String get ordersCancelFailed => 'Cancel failed';

  @override
  String get ordersFee => 'Fee';

  @override
  String get ordersBuy => 'Buy';

  @override
  String get ordersSell => 'Sell';

  @override
  String get statusPending => 'Pending';

  @override
  String get statusFilled => 'Filled';

  @override
  String get statusCancelled => 'Cancelled';

  @override
  String get statusUnknown => 'Unknown';

  @override
  String get orderTypeMarket => 'Market';

  @override
  String get orderTypeLimit => 'Limit';

  @override
  String get desktopAssetOverview => 'Asset Overview';

  @override
  String get desktopPositions => 'Positions';

  @override
  String get desktopDailyPnl => 'Daily P&L';

  @override
  String get desktopOrderPanel => 'Orders / Trades';

  @override
  String get desktopRefreshAll => 'Refresh All';

  @override
  String get desktopPlaceOrder => 'Place Order';

  @override
  String desktopTapToFill(String symbol) {
    return 'Trade $symbol';
  }

  @override
  String get desktopSearchStocksHint => 'Search Stocks';

  @override
  String get desktopMarket => 'Market';

  @override
  String get desktopBuyingPower => 'Buying Power';

  @override
  String get desktopCash => 'Cash';

  @override
  String get desktopWatchList => 'Watchlist';

  @override
  String get desktopSearchWatchListHint => 'Search Watchlist';

  @override
  String get desktopEditWatchlist => 'Edit';

  @override
  String get desktopLast => 'Last';

  @override
  String get desktopChangePercent => 'Change %';

  @override
  String get desktopBid => 'Bid';

  @override
  String get desktopAsk => 'Ask';

  @override
  String get desktopSpread => 'Spread';

  @override
  String get desktopVolume => 'Volume';

  @override
  String get desktopAvg10D => '10D Avg';

  @override
  String get desktopPrevClose => 'Prev Close';

  @override
  String get desktopPriceChartPlaceholder => 'Price Chart';

  @override
  String get desktopDayRange => 'Day Range';

  @override
  String get desktop52WRange => '52W Range';

  @override
  String get desktopOrderEntry => 'Order Entry';

  @override
  String get desktopQty => 'Qty';

  @override
  String get desktopTif => 'TIF';

  @override
  String get desktopEstimatedAmount => 'Est. Amount';

  @override
  String get desktopPreviewSell => 'Preview Sell';

  @override
  String get desktopPreviewBuy => 'Preview Buy';

  @override
  String get desktopTotalMarketValue => 'Total Market Value';

  @override
  String get desktopMarketValue => 'Market Value';

  @override
  String get desktopOrders => 'Orders';

  @override
  String get desktopOrderTabWorking => 'Working';

  @override
  String get desktopOrderTabFilled => 'Filled';

  @override
  String get desktopOrderTabCancelled => 'Cancelled';

  @override
  String get desktopViewAllOrders => 'View All Orders';

  @override
  String get desktopAccountSummary => 'Account Summary';

  @override
  String get desktopNetLiquidation => 'Net Liquidation';

  @override
  String get desktopCashBalance => 'Cash Balance';

  @override
  String get desktopYtdPnl => 'YTD P&L';

  @override
  String desktopDataAsOf(String time) {
    return 'Data as of $time';
  }

  @override
  String get desktopConnected => 'Connected';

  @override
  String get errorLoadFailed => 'Failed to load data';

  @override
  String get errorRetry => 'Retry';

  @override
  String get btnConfirm => 'Confirm';

  @override
  String get btnCancel => 'Cancel';
}
