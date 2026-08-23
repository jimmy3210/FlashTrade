import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_en.dart';
import 'app_localizations_ja.dart';
import 'app_localizations_zh.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'l10n/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
    : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations)!;
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
        delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('en'),
    Locale('ja'),
    Locale('zh'),
    Locale('zh', 'TW'),
  ];

  /// 应用名称
  ///
  /// In zh, this message translates to:
  /// **'FlashTrade'**
  String get appName;

  /// No description provided for @navHome.
  ///
  /// In zh, this message translates to:
  /// **'首页'**
  String get navHome;

  /// No description provided for @navPortfolio.
  ///
  /// In zh, this message translates to:
  /// **'持仓'**
  String get navPortfolio;

  /// No description provided for @navTrade.
  ///
  /// In zh, this message translates to:
  /// **'交易'**
  String get navTrade;

  /// No description provided for @navOrders.
  ///
  /// In zh, this message translates to:
  /// **'订单'**
  String get navOrders;

  /// No description provided for @loginTitle.
  ///
  /// In zh, this message translates to:
  /// **'FlashTrade'**
  String get loginTitle;

  /// No description provided for @loginSubtitle.
  ///
  /// In zh, this message translates to:
  /// **'登录'**
  String get loginSubtitle;

  /// No description provided for @loginUsername.
  ///
  /// In zh, this message translates to:
  /// **'用户名'**
  String get loginUsername;

  /// No description provided for @loginPassword.
  ///
  /// In zh, this message translates to:
  /// **'密码'**
  String get loginPassword;

  /// No description provided for @loginButton.
  ///
  /// In zh, this message translates to:
  /// **'登录'**
  String get loginButton;

  /// No description provided for @loginDemoHint.
  ///
  /// In zh, this message translates to:
  /// **'演示模式：点击输入 demo / demo'**
  String get loginDemoHint;

  /// No description provided for @loginErrorEmpty.
  ///
  /// In zh, this message translates to:
  /// **'请输入用户名和密码'**
  String get loginErrorEmpty;

  /// No description provided for @loginErrorInvalid.
  ///
  /// In zh, this message translates to:
  /// **'用户名或密码错误'**
  String get loginErrorInvalid;

  /// No description provided for @loginErrorNetwork.
  ///
  /// In zh, this message translates to:
  /// **'请检查网络连接'**
  String get loginErrorNetwork;

  /// No description provided for @loginErrorFailed.
  ///
  /// In zh, this message translates to:
  /// **'登录失败'**
  String get loginErrorFailed;

  /// No description provided for @homeTitle.
  ///
  /// In zh, this message translates to:
  /// **'FlashTrade'**
  String get homeTitle;

  /// No description provided for @homeTotalAssets.
  ///
  /// In zh, this message translates to:
  /// **'总资产评估额'**
  String get homeTotalAssets;

  /// No description provided for @homeDailyPnl.
  ///
  /// In zh, this message translates to:
  /// **'本日浮动盈亏：'**
  String get homeDailyPnl;

  /// No description provided for @homeTopPositions.
  ///
  /// In zh, this message translates to:
  /// **'持仓（前5名）'**
  String get homeTopPositions;

  /// No description provided for @homeNoPosions.
  ///
  /// In zh, this message translates to:
  /// **'暂无持仓'**
  String get homeNoPosions;

  /// No description provided for @homeDailyChart.
  ///
  /// In zh, this message translates to:
  /// **'每日盈亏（历史）'**
  String get homeDailyChart;

  /// No description provided for @homeNoChartData.
  ///
  /// In zh, this message translates to:
  /// **'暂无盈亏数据'**
  String get homeNoChartData;

  /// No description provided for @homeLogout.
  ///
  /// In zh, this message translates to:
  /// **'退出登录'**
  String get homeLogout;

  /// No description provided for @homeRefresh.
  ///
  /// In zh, this message translates to:
  /// **'刷新'**
  String get homeRefresh;

  /// No description provided for @portfolioTitle.
  ///
  /// In zh, this message translates to:
  /// **'持仓'**
  String get portfolioTitle;

  /// No description provided for @portfolioTotalValue.
  ///
  /// In zh, this message translates to:
  /// **'总评估额'**
  String get portfolioTotalValue;

  /// No description provided for @portfolioUnrealizedPnl.
  ///
  /// In zh, this message translates to:
  /// **'浮动盈亏'**
  String get portfolioUnrealizedPnl;

  /// No description provided for @portfolioCount.
  ///
  /// In zh, this message translates to:
  /// **'持仓数'**
  String get portfolioCount;

  /// No description provided for @portfolioAvg.
  ///
  /// In zh, this message translates to:
  /// **'均价'**
  String get portfolioAvg;

  /// No description provided for @portfolioEmpty.
  ///
  /// In zh, this message translates to:
  /// **'暂无持仓'**
  String get portfolioEmpty;

  /// No description provided for @tradeTitle.
  ///
  /// In zh, this message translates to:
  /// **'下单'**
  String get tradeTitle;

  /// No description provided for @tradeSymbol.
  ///
  /// In zh, this message translates to:
  /// **'股票代码'**
  String get tradeSymbol;

  /// No description provided for @tradeSymbolHint.
  ///
  /// In zh, this message translates to:
  /// **'例：AAPL'**
  String get tradeSymbolHint;

  /// No description provided for @tradeSide.
  ///
  /// In zh, this message translates to:
  /// **'买卖方向'**
  String get tradeSide;

  /// No description provided for @tradeBuy.
  ///
  /// In zh, this message translates to:
  /// **'买入'**
  String get tradeBuy;

  /// No description provided for @tradeSell.
  ///
  /// In zh, this message translates to:
  /// **'卖出'**
  String get tradeSell;

  /// No description provided for @tradeOrderType.
  ///
  /// In zh, this message translates to:
  /// **'订单类型'**
  String get tradeOrderType;

  /// No description provided for @tradeMarket.
  ///
  /// In zh, this message translates to:
  /// **'市价'**
  String get tradeMarket;

  /// No description provided for @tradeLimit.
  ///
  /// In zh, this message translates to:
  /// **'限价'**
  String get tradeLimit;

  /// No description provided for @tradeQuantity.
  ///
  /// In zh, this message translates to:
  /// **'数量（股）'**
  String get tradeQuantity;

  /// No description provided for @tradeQuantityHint.
  ///
  /// In zh, this message translates to:
  /// **'例：100'**
  String get tradeQuantityHint;

  /// No description provided for @tradePrice.
  ///
  /// In zh, this message translates to:
  /// **'价格（¥）'**
  String get tradePrice;

  /// No description provided for @tradePriceHint.
  ///
  /// In zh, this message translates to:
  /// **'例：150.00'**
  String get tradePriceHint;

  /// No description provided for @tradeSubmitBuy.
  ///
  /// In zh, this message translates to:
  /// **'确认买入'**
  String get tradeSubmitBuy;

  /// No description provided for @tradeSubmitSell.
  ///
  /// In zh, this message translates to:
  /// **'确认卖出'**
  String get tradeSubmitSell;

  /// No description provided for @tradeConfirmTitle.
  ///
  /// In zh, this message translates to:
  /// **'{side}订单确认'**
  String tradeConfirmTitle(String side);

  /// No description provided for @tradeConfirmBodyMarket.
  ///
  /// In zh, this message translates to:
  /// **'确认以{side} {symbol} {qty}股（市价）？'**
  String tradeConfirmBodyMarket(String side, String symbol, String qty);

  /// No description provided for @tradeConfirmBodyLimit.
  ///
  /// In zh, this message translates to:
  /// **'确认以{side} {symbol} {qty}股 ¥{price}（限价）？'**
  String tradeConfirmBodyLimit(
    String side,
    String symbol,
    String qty,
    String price,
  );

  /// No description provided for @tradeSuccess.
  ///
  /// In zh, this message translates to:
  /// **'下单成功'**
  String get tradeSuccess;

  /// No description provided for @tradeFailed.
  ///
  /// In zh, this message translates to:
  /// **'下单失败'**
  String get tradeFailed;

  /// No description provided for @tradeCancel.
  ///
  /// In zh, this message translates to:
  /// **'取消'**
  String get tradeCancel;

  /// No description provided for @tradeValidSymbolEmpty.
  ///
  /// In zh, this message translates to:
  /// **'请输入股票代码'**
  String get tradeValidSymbolEmpty;

  /// No description provided for @tradeValidSymbolFormat.
  ///
  /// In zh, this message translates to:
  /// **'请输入1~5位英文大写字母'**
  String get tradeValidSymbolFormat;

  /// No description provided for @tradeValidQtyEmpty.
  ///
  /// In zh, this message translates to:
  /// **'请输入数量'**
  String get tradeValidQtyEmpty;

  /// No description provided for @tradeValidQtyMin.
  ///
  /// In zh, this message translates to:
  /// **'请输入大于0的整数'**
  String get tradeValidQtyMin;

  /// No description provided for @tradeValidPriceEmpty.
  ///
  /// In zh, this message translates to:
  /// **'请输入价格'**
  String get tradeValidPriceEmpty;

  /// No description provided for @tradeValidPriceMin.
  ///
  /// In zh, this message translates to:
  /// **'请输入大于0的数值'**
  String get tradeValidPriceMin;

  /// No description provided for @ordersTitle.
  ///
  /// In zh, this message translates to:
  /// **'订单记录'**
  String get ordersTitle;

  /// No description provided for @ordersTabOrders.
  ///
  /// In zh, this message translates to:
  /// **'委托列表'**
  String get ordersTabOrders;

  /// No description provided for @ordersTabTrades.
  ///
  /// In zh, this message translates to:
  /// **'成交记录'**
  String get ordersTabTrades;

  /// No description provided for @ordersFilterAll.
  ///
  /// In zh, this message translates to:
  /// **'全部'**
  String get ordersFilterAll;

  /// No description provided for @ordersFilterPending.
  ///
  /// In zh, this message translates to:
  /// **'待成交'**
  String get ordersFilterPending;

  /// No description provided for @ordersFilterFilled.
  ///
  /// In zh, this message translates to:
  /// **'已成交'**
  String get ordersFilterFilled;

  /// No description provided for @ordersEmpty.
  ///
  /// In zh, this message translates to:
  /// **'暂无订单'**
  String get ordersEmpty;

  /// No description provided for @tradesEmpty.
  ///
  /// In zh, this message translates to:
  /// **'暂无成交记录'**
  String get tradesEmpty;

  /// No description provided for @ordersCancelTitle.
  ///
  /// In zh, this message translates to:
  /// **'取消订单'**
  String get ordersCancelTitle;

  /// No description provided for @ordersCancelBody.
  ///
  /// In zh, this message translates to:
  /// **'确认要取消此订单吗？'**
  String get ordersCancelBody;

  /// No description provided for @ordersCancelYes.
  ///
  /// In zh, this message translates to:
  /// **'确认取消'**
  String get ordersCancelYes;

  /// No description provided for @ordersCancelNo.
  ///
  /// In zh, this message translates to:
  /// **'不取消'**
  String get ordersCancelNo;

  /// No description provided for @ordersCancelSuccess.
  ///
  /// In zh, this message translates to:
  /// **'订单已取消'**
  String get ordersCancelSuccess;

  /// No description provided for @ordersCancelFailed.
  ///
  /// In zh, this message translates to:
  /// **'取消失败'**
  String get ordersCancelFailed;

  /// No description provided for @ordersFee.
  ///
  /// In zh, this message translates to:
  /// **'手续费'**
  String get ordersFee;

  /// No description provided for @ordersBuy.
  ///
  /// In zh, this message translates to:
  /// **'买'**
  String get ordersBuy;

  /// No description provided for @ordersSell.
  ///
  /// In zh, this message translates to:
  /// **'卖'**
  String get ordersSell;

  /// No description provided for @statusPending.
  ///
  /// In zh, this message translates to:
  /// **'待成交'**
  String get statusPending;

  /// No description provided for @statusFilled.
  ///
  /// In zh, this message translates to:
  /// **'已成交'**
  String get statusFilled;

  /// No description provided for @statusCancelled.
  ///
  /// In zh, this message translates to:
  /// **'已取消'**
  String get statusCancelled;

  /// No description provided for @statusUnknown.
  ///
  /// In zh, this message translates to:
  /// **'未知'**
  String get statusUnknown;

  /// No description provided for @orderTypeMarket.
  ///
  /// In zh, this message translates to:
  /// **'市价'**
  String get orderTypeMarket;

  /// No description provided for @orderTypeLimit.
  ///
  /// In zh, this message translates to:
  /// **'限价'**
  String get orderTypeLimit;

  /// No description provided for @desktopAssetOverview.
  ///
  /// In zh, this message translates to:
  /// **'资产概览'**
  String get desktopAssetOverview;

  /// No description provided for @desktopPositions.
  ///
  /// In zh, this message translates to:
  /// **'持仓'**
  String get desktopPositions;

  /// No description provided for @desktopDailyPnl.
  ///
  /// In zh, this message translates to:
  /// **'每日盈亏'**
  String get desktopDailyPnl;

  /// No description provided for @desktopOrderPanel.
  ///
  /// In zh, this message translates to:
  /// **'委托/成交'**
  String get desktopOrderPanel;

  /// No description provided for @desktopRefreshAll.
  ///
  /// In zh, this message translates to:
  /// **'刷新全部数据'**
  String get desktopRefreshAll;

  /// No description provided for @desktopPlaceOrder.
  ///
  /// In zh, this message translates to:
  /// **'下单'**
  String get desktopPlaceOrder;

  /// No description provided for @desktopTapToFill.
  ///
  /// In zh, this message translates to:
  /// **'{symbol} 下单'**
  String desktopTapToFill(String symbol);

  /// No description provided for @desktopSearchStocksHint.
  ///
  /// In zh, this message translates to:
  /// **'搜索股票'**
  String get desktopSearchStocksHint;

  /// No description provided for @desktopMarket.
  ///
  /// In zh, this message translates to:
  /// **'市场'**
  String get desktopMarket;

  /// No description provided for @desktopBuyingPower.
  ///
  /// In zh, this message translates to:
  /// **'购买力'**
  String get desktopBuyingPower;

  /// No description provided for @desktopCash.
  ///
  /// In zh, this message translates to:
  /// **'现金'**
  String get desktopCash;

  /// No description provided for @desktopWatchList.
  ///
  /// In zh, this message translates to:
  /// **'自选股'**
  String get desktopWatchList;

  /// No description provided for @desktopSearchWatchListHint.
  ///
  /// In zh, this message translates to:
  /// **'搜索自选股'**
  String get desktopSearchWatchListHint;

  /// No description provided for @desktopEditWatchlist.
  ///
  /// In zh, this message translates to:
  /// **'编辑'**
  String get desktopEditWatchlist;

  /// No description provided for @desktopLast.
  ///
  /// In zh, this message translates to:
  /// **'最新价'**
  String get desktopLast;

  /// No description provided for @desktopChangePercent.
  ///
  /// In zh, this message translates to:
  /// **'涨跌幅'**
  String get desktopChangePercent;

  /// No description provided for @desktopBid.
  ///
  /// In zh, this message translates to:
  /// **'买价'**
  String get desktopBid;

  /// No description provided for @desktopAsk.
  ///
  /// In zh, this message translates to:
  /// **'卖价'**
  String get desktopAsk;

  /// No description provided for @desktopSpread.
  ///
  /// In zh, this message translates to:
  /// **'点差'**
  String get desktopSpread;

  /// No description provided for @desktopVolume.
  ///
  /// In zh, this message translates to:
  /// **'成交量'**
  String get desktopVolume;

  /// No description provided for @desktopAvg10D.
  ///
  /// In zh, this message translates to:
  /// **'10日均价'**
  String get desktopAvg10D;

  /// No description provided for @desktopPrevClose.
  ///
  /// In zh, this message translates to:
  /// **'前收价'**
  String get desktopPrevClose;

  /// No description provided for @desktopPriceChartPlaceholder.
  ///
  /// In zh, this message translates to:
  /// **'价格走势图'**
  String get desktopPriceChartPlaceholder;

  /// No description provided for @desktopAddToWatchlist.
  ///
  /// In zh, this message translates to:
  /// **'添加到自选股'**
  String get desktopAddToWatchlist;

  /// No description provided for @desktopAlreadyInWatchlist.
  ///
  /// In zh, this message translates to:
  /// **'已在自选股中'**
  String get desktopAlreadyInWatchlist;

  /// No description provided for @desktopDayRange.
  ///
  /// In zh, this message translates to:
  /// **'日内波动'**
  String get desktopDayRange;

  /// No description provided for @desktop52WRange.
  ///
  /// In zh, this message translates to:
  /// **'52周波动'**
  String get desktop52WRange;

  /// No description provided for @desktopOrderEntry.
  ///
  /// In zh, this message translates to:
  /// **'下单'**
  String get desktopOrderEntry;

  /// No description provided for @desktopQty.
  ///
  /// In zh, this message translates to:
  /// **'数量'**
  String get desktopQty;

  /// No description provided for @desktopTif.
  ///
  /// In zh, this message translates to:
  /// **'时效'**
  String get desktopTif;

  /// No description provided for @desktopEstimatedAmount.
  ///
  /// In zh, this message translates to:
  /// **'估计金额'**
  String get desktopEstimatedAmount;

  /// No description provided for @desktopPreviewSell.
  ///
  /// In zh, this message translates to:
  /// **'预览卖出'**
  String get desktopPreviewSell;

  /// No description provided for @desktopPreviewBuy.
  ///
  /// In zh, this message translates to:
  /// **'预览买入'**
  String get desktopPreviewBuy;

  /// No description provided for @desktopTotalMarketValue.
  ///
  /// In zh, this message translates to:
  /// **'总市值'**
  String get desktopTotalMarketValue;

  /// No description provided for @desktopMarketValue.
  ///
  /// In zh, this message translates to:
  /// **'市值'**
  String get desktopMarketValue;

  /// No description provided for @desktopOrders.
  ///
  /// In zh, this message translates to:
  /// **'订单'**
  String get desktopOrders;

  /// No description provided for @desktopOrderTabWorking.
  ///
  /// In zh, this message translates to:
  /// **'未成交'**
  String get desktopOrderTabWorking;

  /// No description provided for @desktopOrderTabFilled.
  ///
  /// In zh, this message translates to:
  /// **'已成交'**
  String get desktopOrderTabFilled;

  /// No description provided for @desktopOrderTabCancelled.
  ///
  /// In zh, this message translates to:
  /// **'已取消'**
  String get desktopOrderTabCancelled;

  /// No description provided for @desktopViewAllOrders.
  ///
  /// In zh, this message translates to:
  /// **'查看全部订单'**
  String get desktopViewAllOrders;

  /// No description provided for @desktopAccountSummary.
  ///
  /// In zh, this message translates to:
  /// **'账户摘要'**
  String get desktopAccountSummary;

  /// No description provided for @desktopNetLiquidation.
  ///
  /// In zh, this message translates to:
  /// **'净清算值'**
  String get desktopNetLiquidation;

  /// No description provided for @desktopCashBalance.
  ///
  /// In zh, this message translates to:
  /// **'现金余额'**
  String get desktopCashBalance;

  /// No description provided for @desktopYtdPnl.
  ///
  /// In zh, this message translates to:
  /// **'今年盈亏'**
  String get desktopYtdPnl;

  /// No description provided for @desktopDataAsOf.
  ///
  /// In zh, this message translates to:
  /// **'数据更新时间：{time}'**
  String desktopDataAsOf(String time);

  /// No description provided for @desktopConnected.
  ///
  /// In zh, this message translates to:
  /// **'已连接'**
  String get desktopConnected;

  /// No description provided for @errorLoadFailed.
  ///
  /// In zh, this message translates to:
  /// **'数据加载失败'**
  String get errorLoadFailed;

  /// No description provided for @errorRetry.
  ///
  /// In zh, this message translates to:
  /// **'重试'**
  String get errorRetry;

  /// No description provided for @btnConfirm.
  ///
  /// In zh, this message translates to:
  /// **'确认'**
  String get btnConfirm;

  /// No description provided for @btnCancel.
  ///
  /// In zh, this message translates to:
  /// **'取消'**
  String get btnCancel;
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) =>
      <String>['en', 'ja', 'zh'].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when language+country codes are specified.
  switch (locale.languageCode) {
    case 'zh':
      {
        switch (locale.countryCode) {
          case 'TW':
            return AppLocalizationsZhTw();
        }
        break;
      }
  }

  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'en':
      return AppLocalizationsEn();
    case 'ja':
      return AppLocalizationsJa();
    case 'zh':
      return AppLocalizationsZh();
  }

  throw FlutterError(
    'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
    'an issue with the localizations generation tool. Please file an issue '
    'on GitHub with a reproducible sample app and the gen-l10n configuration '
    'that was used.',
  );
}
