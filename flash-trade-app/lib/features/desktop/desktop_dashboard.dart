import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:window_manager/window_manager.dart';
import '../../core/constants/app_colors.dart';
import '../../features/orders/domain/models.dart';
import '../../features/orders/providers/orders_provider.dart';
import '../../features/portfolio/domain/models.dart';
import '../../features/portfolio/providers/portfolio_provider.dart';
import '../../features/stock/domain/stock_suggestion.dart';
import '../../features/stock/providers/stock_provider.dart';
import '../../features/trade/providers/trade_provider.dart';
import '../../features/watchlist/domain/watchlist_item.dart';
import '../../features/watchlist/providers/watchlist_provider.dart';
import '../../l10n/app_localizations.dart';

class DesktopDashboard extends ConsumerStatefulWidget {
  const DesktopDashboard({super.key});

  @override
  ConsumerState<DesktopDashboard> createState() => _DesktopDashboardState();
}

class _DesktopDashboardState extends ConsumerState<DesktopDashboard> {
  final _searchController = TextEditingController();
  String _searchQuery = '';
  String _selectedSymbol = '';
  String _currentView = 'dashboard'; // 当前视图

  @override
  void initState() {
    super.initState();
    _searchController.addListener(() {
      setState(() {
        _searchQuery = _searchController.text.trim();
      });
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  void _selectSymbol(String symbol) {
    setState(() {
      _selectedSymbol = symbol;
      _searchController.text = symbol;
      _searchQuery = symbol;
    });
  }

  void _switchView(String view) {
    setState(() {
      _currentView = view;
    });
  }

  void _openTrade(String side) {
    if (_selectedSymbol.isEmpty) return;
    ref.read(selectedSymbolProvider.notifier).state = _selectedSymbol;
    ref.read(tradeFormProvider.notifier).setSymbol(_selectedSymbol);
    ref.read(tradeFormProvider.notifier).setSide(side);
    ref.read(tradeFormProvider.notifier).setOrderType('MARKET');
    context.go('/home/trade');
  }

  Future<void> _addSelectedSymbolToWatchlist() async {
    if (_selectedSymbol.isEmpty) return;
    await _addSymbolToWatchlist(_selectedSymbol);
  }

  Future<void> _addSymbolToWatchlist(String symbol) async {
    if (symbol.isEmpty) return;
    try {
      await ref.read(watchlistRepositoryProvider).addToWatchlist(symbol);
      // ignore: unused_result
      ref.refresh(watchlistProvider);
    } catch (_) {
      // ignore failure for now
    }
  }

  Widget _buildViewContent({
    required String view,
    required String selectedSymbol,
    required AssetViewResponse? selectedAsset,
    required String estimatedAmount,
    required bool isSelectedSymbolInWatchlist,
    required AsyncValue<List<PositionResponse>> positionsAsync,
    required AsyncValue<List<OrderResponse>> ordersAsync,
    required AsyncValue<List<AssetViewResponse>> assetViewAsync,
    required AsyncValue<List<WatchlistItem>> watchlistAsync,
    required String searchQuery,
    required void Function(String) onSelectSymbol,
    required Set<String> watchlistSymbols,
  }) {
    switch (view) {
      case 'dashboard':
        return Row(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Flexible(
              flex: 22,
              child: _WatchListPanel(
                watchlistAsync: watchlistAsync,
                searchQuery: searchQuery,
                selectedSymbol: selectedSymbol,
                onSelectSymbol: onSelectSymbol,
              ),
            ),
            const SizedBox(width: 5),
            Flexible(
              flex: 78,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Expanded(
                    flex: 2,
                    child: _QuoteAndTradeRow(
                      selectedSymbol: selectedSymbol,
                      selectedAsset: selectedAsset,
                      estimatedAmount: estimatedAmount,
                      isInWatchlist: isSelectedSymbolInWatchlist,
                      onAddToWatchlist: _addSelectedSymbolToWatchlist,
                      onBuy: () => _openTrade('BUY'),
                      onSell: () => _openTrade('SELL'),
                    ),
                  ),
                  const SizedBox(height: 5),
                  Expanded(
                    flex: 1,
                    child: _BottomCardsRow(
                      positionsAsync: positionsAsync,
                      ordersAsync: ordersAsync,
                      assetViewAsync: assetViewAsync,
                    ),
                  ),
                ],
              ),
            ),
          ],
        );

      case 'portfolio':
        return _PortfolioView(positionsAsync: positionsAsync);

      case 'orders':
        return _OrdersView(ordersAsync: ordersAsync);

      case 'trade':
        return _TradeView(
          selectedSymbol: selectedSymbol,
          selectedAsset: selectedAsset,
          estimatedAmount: estimatedAmount,
          onBuy: () => _openTrade('BUY'),
          onSell: () => _openTrade('SELL'),
        );

      default:
        return const Center(child: Text('Unknown view'));
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final positionsAsync = ref.watch(positionsProvider);
    final assetViewAsync = ref.watch(assetViewProvider);
    final ordersAsync = ref.watch(ordersProvider);
    final suggestionsAsync = _searchQuery.isNotEmpty
        ? ref.watch(stockSearchProvider(_searchQuery))
        : const AsyncValue.data(<StockSuggestion>[]);

    final watchlistAsync = ref.watch(watchlistProvider);
    final selectedSymbol = _selectedSymbol.isNotEmpty
        ? _selectedSymbol
        : assetViewAsync.maybeWhen(
            data: (assets) => assets.isNotEmpty ? assets.first.symbol : '',
            orElse: () => '',
          );
    final selectedAsset = assetViewAsync.maybeWhen(
      data: (assets) {
        if (assets.isEmpty) return null;
        return assets.firstWhere(
          (item) => item.symbol == selectedSymbol,
          orElse: () => assets.first,
        );
      },
      orElse: () => null,
    );
    final isSelectedSymbolInWatchlist = watchlistAsync.maybeWhen(
      data: (items) => items.any((item) => item.symbol == selectedSymbol),
      orElse: () => false,
    );
    final estimatedAmount = selectedAsset != null
        ? NumberFormat.currency(
            symbol: '\$',
            decimalDigits: 2,
          ).format(selectedAsset.currentPrice * 100)
        : '-';

    return Scaffold(
      backgroundColor: AppColors.primaryBg,
      body: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Listener(
              onPointerDown: (_) => windowManager.startDragging(),
              child: Container(
                height: 48,
                padding: const EdgeInsets.symmetric(horizontal: 14),
                decoration: BoxDecoration(
                  color: AppColors.cardBg,
                  borderRadius: BorderRadius.circular(5),
                  border: Border.all(color: AppColors.border, width: 1),
                ),
                child: Row(
                  children: [
                    SizedBox(
                      width: 24,
                      height: 24,
                      child: Image.asset(
                        'assets/icons/ft_icon.png',
                        fit: BoxFit.contain,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Text(
                      l10n.appName,
                      style: const TextStyle(
                        color: AppColors.textPrimary,
                        fontWeight: FontWeight.bold,
                        fontSize: 18,
                      ),
                    ),
                    const SizedBox(width: 15),
                    SizedBox(
                      width: 220,
                      child: Container(
                        height: 34,
                        decoration: BoxDecoration(
                          color: AppColors.secondaryBg,
                          borderRadius: BorderRadius.circular(5),
                          border: Border.all(color: AppColors.border, width: 1),
                        ),
                        child: TextField(
                          controller: _searchController,
                          style: const TextStyle(
                            color: AppColors.textPrimary,
                            fontSize: 13,
                          ),
                          decoration: InputDecoration(
                            hintText: l10n.desktopSearchStocksHint,
                            hintStyle: const TextStyle(
                              color: AppColors.textSecondary,
                              fontSize: 13,
                            ),
                            prefixIcon: const Icon(
                              Icons.search,
                              size: 20,
                              color: AppColors.textSecondary,
                            ),
                            border: InputBorder.none,
                            contentPadding: const EdgeInsets.symmetric(
                              vertical: 6,
                              horizontal: 10,
                            ),
                          ),
                        ),
                      ),
                    ),
                    const Spacer(),
                    _TopInfoColumn(
                      label: l10n.desktopMarket,
                      value: 'Open · 09:30',
                    ),
                    const _StatusDivider(),
                    _TopInfoColumn(
                      label: l10n.desktopBuyingPower,
                      value: '\$12,345',
                    ),
                    const _StatusDivider(),
                    _TopInfoColumn(label: l10n.desktopCash, value: '\$8,900'),
                    Flexible(child: SizedBox(width: 200)),
                    IconButton(
                      onPressed: null,
                      icon: const Icon(
                        Icons.notifications_none,
                        color: AppColors.textPrimary,
                      ),
                    ),
                    const _StatusDivider(),
                    IconButton(
                      onPressed: null,
                      icon: const Icon(
                        Icons.settings,
                        color: AppColors.textPrimary,
                      ),
                    ),
                    const _StatusDivider(),
                    const SizedBox(width: 15),
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 12,
                        vertical: 8,
                      ),
                      decoration: BoxDecoration(
                        color: AppColors.secondaryBg,
                        borderRadius: BorderRadius.circular(5),
                      ),
                      child: Row(
                        children: [
                          const CircleAvatar(
                            radius: 14,
                            backgroundColor: AppColors.accent,
                            child: Icon(
                              Icons.person,
                              size: 16,
                              color: AppColors.textPrimary,
                            ),
                          ),
                          const SizedBox(width: 5),
                          Text(
                            l10n.loginUsername,
                            style: const TextStyle(
                              color: AppColors.textPrimary,
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(width: 50),
                    const _WindowControls(),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 5),
            if (_searchQuery.isNotEmpty)
              _SearchSuggestionPanel(
                searchResults: suggestionsAsync,
                onSelectSymbol: _selectSymbol,
                onAddToWatchlist: _addSymbolToWatchlist,
                watchlistSymbols: watchlistAsync.maybeWhen(
                  data: (items) => items.map((item) => item.symbol).toSet(),
                  orElse: () => <String>{},
                ),
              ),
            const SizedBox(height: 5),
            Expanded(
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  _SideBarMenu(
                    currentView: _currentView,
                    onViewChange: _switchView,
                  ),
                  const SizedBox(width: 5),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        Expanded(
                          child: _buildViewContent(
                            view: _currentView,
                            selectedSymbol: selectedSymbol,
                            selectedAsset: selectedAsset,
                            estimatedAmount: estimatedAmount,
                            isSelectedSymbolInWatchlist:
                                isSelectedSymbolInWatchlist,
                            positionsAsync: positionsAsync,
                            ordersAsync: ordersAsync,
                            assetViewAsync: assetViewAsync,
                            watchlistAsync: watchlistAsync,
                            searchQuery: _searchQuery,
                            onSelectSymbol: _selectSymbol,
                            watchlistSymbols: watchlistAsync.maybeWhen(
                              data: (items) =>
                                  items.map((item) => item.symbol).toSet(),
                              orElse: () => <String>{},
                            ),
                          ),
                        ),
                        const SizedBox(height: 5),
                        const _BottomStatusBar(),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _WindowControls extends StatefulWidget {
  const _WindowControls();

  @override
  State<_WindowControls> createState() => _WindowControlsState();
}

class _WindowControlsState extends State<_WindowControls> {
  bool _isMaximized = false;

  @override
  void initState() {
    super.initState();
    _loadWindowState();
  }

  Future<void> _loadWindowState() async {
    final maximized = await windowManager.isMaximized();
    if (!mounted) return;
    setState(() {
      _isMaximized = maximized;
    });
  }

  Future<void> _toggleMaximize() async {
    if (_isMaximized) {
      await windowManager.unmaximize();
    } else {
      await windowManager.maximize();
    }
    if (!mounted) return;
    setState(() {
      _isMaximized = !_isMaximized;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        _WindowIconButton(
          icon: Icons.remove,
          tooltip: '最小化',
          onPressed: () async {
            await windowManager.minimize();
          },
        ),
        const SizedBox(width: 8),
        _WindowIconButton(
          icon: _isMaximized
              ? Icons.crop_square
              : Icons.check_box_outline_blank,
          tooltip: _isMaximized ? '恢复' : '最大化',
          onPressed: _toggleMaximize,
        ),
        const SizedBox(width: 8),
        _WindowIconButton(
          icon: Icons.close,
          tooltip: '关闭',
          onPressed: () async {
            await windowManager.close();
          },
          iconColor: AppColors.loss,
        ),
      ],
    );
  }
}

class _WindowIconButton extends StatelessWidget {
  const _WindowIconButton({
    required this.icon,
    required this.tooltip,
    required this.onPressed,
    this.iconColor = AppColors.textSecondary,
  });

  final IconData icon;
  final String tooltip;
  final Future Function() onPressed;
  final Color iconColor;

  @override
  Widget build(BuildContext context) {
    return Tooltip(
      message: tooltip,
      child: InkResponse(
        onTap: onPressed,
        radius: 20,
        borderRadius: BorderRadius.circular(5),
        splashColor: AppColors.accent.withValues(alpha: 0.15),
        child: Container(
          width: 34,
          height: 34,
          decoration: BoxDecoration(
            color: AppColors.secondaryBg,
            borderRadius: BorderRadius.circular(5),
            border: Border.all(color: AppColors.border),
          ),
          child: Icon(icon, size: 18, color: iconColor),
        ),
      ),
    );
  }
}

class _SearchSuggestionPanel extends StatelessWidget {
  const _SearchSuggestionPanel({
    required this.searchResults,
    required this.onSelectSymbol,
    required this.onAddToWatchlist,
    required this.watchlistSymbols,
  });

  final AsyncValue<List<StockSuggestion>> searchResults;
  final void Function(String) onSelectSymbol;
  final void Function(String) onAddToWatchlist;
  final Set<String> watchlistSymbols;

  @override
  Widget build(BuildContext context) {
    return searchResults.when(
      data: (items) {
        if (items.isEmpty) {
          return Container(
            width: double.infinity,
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: AppColors.cardBg,
              borderRadius: BorderRadius.circular(5),
              border: Border.all(color: AppColors.border),
            ),
            child: const Text(
              'No results',
              style: TextStyle(color: AppColors.textSecondary),
            ),
          );
        }
        return Container(
          width: double.infinity,
          constraints: const BoxConstraints(maxHeight: 220),
          decoration: BoxDecoration(
            color: AppColors.cardBg,
            borderRadius: BorderRadius.circular(5),
            border: Border.all(color: AppColors.border),
          ),
          child: ListView.separated(
            padding: const EdgeInsets.all(8),
            itemCount: items.length,
            itemBuilder: (context, index) {
              final item = items[index];
              return InkWell(
                onTap: () => onSelectSymbol(item.symbol),
                child: Padding(
                  padding: const EdgeInsets.symmetric(
                    vertical: 8,
                    horizontal: 10,
                  ),
                  child: Row(
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              item.symbol,
                              style: const TextStyle(
                                color: AppColors.textPrimary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              item.companyName,
                              style: const TextStyle(
                                color: AppColors.textSecondary,
                                fontSize: 12,
                              ),
                            ),
                          ],
                        ),
                      ),
                      Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(
                            item.exchange,
                            style: const TextStyle(
                              color: AppColors.textSecondary,
                              fontSize: 12,
                            ),
                          ),
                          const SizedBox(width: 10),
                          InkResponse(
                            onTap: watchlistSymbols.contains(item.symbol)
                                ? null
                                : () => onAddToWatchlist(item.symbol),
                            radius: 18,
                            borderRadius: BorderRadius.circular(18),
                            splashColor: AppColors.accent.withValues(
                              alpha: 0.15,
                            ),
                            child: Container(
                              width: 32,
                              height: 32,
                              decoration: BoxDecoration(
                                color: AppColors.secondaryBg,
                                borderRadius: BorderRadius.circular(16),
                                border: Border.all(color: AppColors.border),
                              ),
                              child: Icon(
                                watchlistSymbols.contains(item.symbol)
                                    ? Icons.favorite
                                    : Icons.favorite_border,
                                size: 18,
                                color: watchlistSymbols.contains(item.symbol)
                                    ? AppColors.accent
                                    : AppColors.textSecondary,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              );
            },
            separatorBuilder: (context, index) =>
                const Divider(height: 1, color: AppColors.border),
          ),
        );
      },
      loading: () => Container(
        width: double.infinity,
        padding: const EdgeInsets.all(10),
        decoration: BoxDecoration(
          color: AppColors.cardBg,
          borderRadius: BorderRadius.circular(5),
          border: Border.all(color: AppColors.border),
        ),
        child: const Text(
          'Searching...',
          style: TextStyle(color: AppColors.textSecondary),
        ),
      ),
      error: (error, stack) => Container(
        width: double.infinity,
        padding: const EdgeInsets.all(10),
        decoration: BoxDecoration(
          color: AppColors.cardBg,
          borderRadius: BorderRadius.circular(5),
          border: Border.all(color: AppColors.border),
        ),
        child: Text(
          'Search failed: ${error.toString()}',
          style: const TextStyle(color: AppColors.textSecondary),
        ),
      ),
    );
  }
}

class _TopInfoColumn extends StatelessWidget {
  const _TopInfoColumn({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label,
            style: const TextStyle(
              color: AppColors.textSecondary,
              fontSize: 11,
            ),
          ),
          const SizedBox(height: 2),
          Text(
            value,
            style: const TextStyle(
              color: AppColors.textPrimary,
              fontSize: 12,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}

class _SideBarNavIcon extends StatelessWidget {
  const _SideBarNavIcon({
    required this.icon,
    required this.label,
    required this.onTap,
    required this.active,
  });

  final IconData icon;
  final String label;
  final VoidCallback onTap;
  final bool active;

  @override
  Widget build(BuildContext context) {
    return Tooltip(
      message: label,
      child: InkResponse(
        onTap: onTap,
        radius: 30,
        borderRadius: BorderRadius.circular(5),
        splashColor: AppColors.accent.withValues(alpha: 0.15),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 10),
          decoration: BoxDecoration(
            color: active
                ? AppColors.accent.withValues(alpha: 0.2)
                : Colors.transparent,
            borderRadius: BorderRadius.circular(5),
          ),
          child: Column(
            children: [
              Icon(
                icon,
                color: active ? AppColors.accent : AppColors.textSecondary,
                size: 24,
              ),
              const SizedBox(height: 4),
              Text(
                label,
                style: TextStyle(
                  color: active ? AppColors.accent : AppColors.textSecondary,
                  fontSize: 9,
                  fontWeight: active ? FontWeight.w600 : FontWeight.normal,
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SideBarLogout extends StatelessWidget {
  const _SideBarLogout();

  @override
  Widget build(BuildContext context) {
    return Tooltip(
      message: '登出',
      child: InkResponse(
        onTap: () {
          context.go('/login');
        },
        radius: 30,
        borderRadius: BorderRadius.circular(5),
        splashColor: AppColors.loss.withValues(alpha: 0.15),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 10),
          decoration: BoxDecoration(borderRadius: BorderRadius.circular(5)),
          child: Column(
            children: [
              Icon(Icons.logout, color: AppColors.loss, size: 24),
              const SizedBox(height: 4),
              const Text(
                '登出',
                style: TextStyle(
                  color: AppColors.loss,
                  fontSize: 9,
                  fontWeight: FontWeight.normal,
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SideBarMenu extends StatelessWidget {
  const _SideBarMenu({required this.currentView, required this.onViewChange});

  final String currentView;
  final Function(String) onViewChange;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);

    return Container(
      width: 64,
      padding: const EdgeInsets.symmetric(vertical: 16),
      decoration: BoxDecoration(
        color: AppColors.cardBg,
        borderRadius: BorderRadius.circular(5),
        border: Border.all(color: AppColors.border),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withAlpha(60),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Column(
            children: [
              _SideBarNavIcon(
                icon: Icons.dashboard,
                label: l10n.navHome,
                active: currentView == 'dashboard',
                onTap: () => onViewChange('dashboard'),
              ),
              const SizedBox(height: 5),
              _SideBarNavIcon(
                icon: Icons.pie_chart_outline,
                label: l10n.navPortfolio,
                active: currentView == 'portfolio',
                onTap: () => onViewChange('portfolio'),
              ),
              const SizedBox(height: 5),
              _SideBarNavIcon(
                icon: Icons.add_circle_outline,
                label: l10n.navTrade,
                active: currentView == 'trade',
                onTap: () => onViewChange('trade'),
              ),
              const SizedBox(height: 5),
              _SideBarNavIcon(
                icon: Icons.history,
                label: l10n.navOrders,
                active: currentView == 'orders',
                onTap: () => onViewChange('orders'),
              ),
            ],
          ),
          const _SideBarLogout(),
        ],
      ),
    );
  }
}

class _WatchListPanel extends StatelessWidget {
  const _WatchListPanel({
    required this.watchlistAsync,
    required this.searchQuery,
    required this.selectedSymbol,
    required this.onSelectSymbol,
  });

  final AsyncValue<List<WatchlistItem>> watchlistAsync;
  final String searchQuery;
  final String selectedSymbol;
  final Function(String) onSelectSymbol;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.cardBg,
        borderRadius: BorderRadius.circular(5),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(
            l10n.desktopWatchList,
            style: const TextStyle(
              color: AppColors.textPrimary,
              fontSize: 16,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 10),
          const _WatchListHeader(),
          const SizedBox(height: 8),
          Expanded(
            child: watchlistAsync.when(
              loading: () => const Center(
                child: CircularProgressIndicator(color: AppColors.accent),
              ),
              error: (_, _) => Center(
                child: Text(
                  'Unable to load watchlist',
                  style: const TextStyle(color: AppColors.textSecondary),
                ),
              ),
              data: (items) {
                final filtered = searchQuery.isEmpty
                    ? items
                    : items
                          .where(
                            (item) =>
                                item.symbol.toUpperCase().contains(
                                  searchQuery.toUpperCase(),
                                ) ||
                                item.companyName.toLowerCase().contains(
                                  searchQuery.toLowerCase(),
                                ),
                          )
                          .toList();

                if (filtered.isEmpty) {
                  return Center(
                    child: Text(
                      searchQuery.isNotEmpty
                          ? 'No assets matching "$searchQuery"'
                          : 'No assets available',
                      style: const TextStyle(color: AppColors.textSecondary),
                    ),
                  );
                }

                return SingleChildScrollView(
                  child: Column(
                    children: filtered
                        .map(
                          (item) => _WatchListRow(
                            item: item,
                            isSelected: item.symbol == selectedSymbol,
                            onTap: () => onSelectSymbol(item.symbol),
                          ),
                        )
                        .toList(),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

class _WatchListHeader extends StatelessWidget {
  const _WatchListHeader();

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8.0),
      child: Row(
        children: [
          Expanded(
            flex: 30,
            child: Text(
              'Symbol',
              style: const TextStyle(
                color: AppColors.textSecondary,
                fontSize: 12,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          Expanded(
            flex: 45,
            child: Text(
              'Name',
              style: const TextStyle(
                color: AppColors.textSecondary,
                fontSize: 12,
                fontWeight: FontWeight.w600,
              ),
              textAlign: TextAlign.left,
            ),
          ),
          Expanded(
            flex: 25,
            child: Text(
              'Exchange',
              style: const TextStyle(
                color: AppColors.textSecondary,
                fontSize: 12,
                fontWeight: FontWeight.w600,
              ),
              textAlign: TextAlign.right,
            ),
          ),
        ],
      ),
    );
  }
}

class _WatchListRow extends StatelessWidget {
  const _WatchListRow({
    required this.item,
    required this.isSelected,
    required this.onTap,
  });

  final WatchlistItem item;
  final bool isSelected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
        decoration: BoxDecoration(
          color: isSelected
              ? AppColors.accent.withValues(alpha: 0.15)
              : Colors.transparent,
          borderRadius: BorderRadius.circular(5),
        ),
        child: Row(
          children: [
            Expanded(
              flex: 30,
              child: Text(
                item.symbol,
                style: TextStyle(
                  color: isSelected ? AppColors.accent : AppColors.textPrimary,
                  fontSize: 12,
                  fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                ),
              ),
            ),
            Expanded(
              flex: 45,
              child: Text(
                item.companyName,
                style: const TextStyle(
                  color: AppColors.textPrimary,
                  fontSize: 12,
                ),
                textAlign: TextAlign.left,
              ),
            ),
            Expanded(
              flex: 25,
              child: Text(
                item.exchange,
                style: const TextStyle(
                  color: AppColors.textPrimary,
                  fontSize: 12,
                ),
                textAlign: TextAlign.right,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _QuoteAndTradeRow extends StatelessWidget {
  const _QuoteAndTradeRow({
    required this.selectedSymbol,
    required this.selectedAsset,
    required this.estimatedAmount,
    required this.isInWatchlist,
    required this.onAddToWatchlist,
    required this.onBuy,
    required this.onSell,
  });

  final String selectedSymbol;
  final AssetViewResponse? selectedAsset;
  final String estimatedAmount;
  final bool isInWatchlist;
  final VoidCallback onAddToWatchlist;
  final VoidCallback onBuy;
  final VoidCallback onSell;

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Flexible(
          flex: 65,
          child: _QuotePanel(
            selectedSymbol: selectedSymbol,
            asset: selectedAsset,
            isInWatchlist: isInWatchlist,
            onAddToWatchlist: onAddToWatchlist,
          ),
        ),
        const SizedBox(width: 5),
        Flexible(
          flex: 35,
          child: _TradePanel(
            symbol: selectedSymbol,
            estimatedAmount: estimatedAmount,
            onBuy: onBuy,
            onSell: onSell,
          ),
        ),
      ],
    );
  }
}

class _QuotePanel extends StatelessWidget {
  const _QuotePanel({
    required this.selectedSymbol,
    required this.asset,
    required this.isInWatchlist,
    required this.onAddToWatchlist,
  });

  final String selectedSymbol;
  final AssetViewResponse? asset;
  final bool isInWatchlist;
  final VoidCallback onAddToWatchlist;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final symbol = selectedSymbol.isNotEmpty ? selectedSymbol : 'N/A';
    final price = asset != null
        ? NumberFormat.currency(
            symbol: '\$',
            decimalDigits: 2,
          ).format(asset!.currentPrice)
        : '-';
    final changeValue = asset?.unrealizedPnlRate ?? 0;
    final changeText = asset != null
        ? '${changeValue >= 0 ? '+' : ''}${(changeValue * 100).toStringAsFixed(2)}%'
        : '-';
    final changeAmount = asset != null
        ? NumberFormat.currency(
            symbol: '\$',
            decimalDigits: 2,
          ).format(asset!.currentPrice * asset!.quantity)
        : '-';

    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBg,
        borderRadius: BorderRadius.circular(5),
        border: Border.all(color: AppColors.border),
      ),
      child: LayoutBuilder(
        builder: (context, constraints) {
          return SingleChildScrollView(
            child: ConstrainedBox(
              constraints: BoxConstraints(minHeight: constraints.maxHeight),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '$symbol · QuickTrade',
                              style: const TextStyle(
                                color: AppColors.textPrimary,
                                fontSize: 18,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            const SizedBox(height: 6),
                            Text(
                              l10n.tradeMarket,
                              style: const TextStyle(
                                color: AppColors.textSecondary,
                                fontSize: 12,
                              ),
                            ),
                          ],
                        ),
                      ),
                      IconButton(
                        onPressed: selectedSymbol.isEmpty || isInWatchlist
                            ? null
                            : onAddToWatchlist,
                        icon: Icon(
                          isInWatchlist
                              ? Icons.favorite
                              : Icons.favorite_border,
                          color: isInWatchlist
                              ? AppColors.accent
                              : AppColors.textSecondary,
                        ),
                        tooltip: isInWatchlist
                            ? l10n.desktopAlreadyInWatchlist
                            : l10n.desktopAddToWatchlist,
                      ),
                    ],
                  ),
                  const SizedBox(height: 5),
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Text(
                        price,
                        style: const TextStyle(
                          color: AppColors.textPrimary,
                          fontSize: 40,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(width: 5),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            changeText,
                            style: TextStyle(
                              color: changeValue >= 0
                                  ? AppColors.gain
                                  : AppColors.loss,
                              fontSize: 16,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            '($changeText)',
                            style: TextStyle(
                              color: changeValue >= 0
                                  ? AppColors.gain
                                  : AppColors.loss,
                              fontSize: 13,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                  const SizedBox(height: 5),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      _StatItem(
                        label: l10n.desktopBid,
                        value: asset != null ? price : '-',
                      ),
                      _StatItem(
                        label: l10n.desktopAsk,
                        value: asset != null ? price : '-',
                      ),
                      _StatItem(
                        label: l10n.desktopSpread,
                        value: asset != null ? '0.05' : '-',
                      ),
                    ],
                  ),
                  const SizedBox(height: 5),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      _StatItem(
                        label: l10n.desktopVolume,
                        value: asset != null ? changeAmount : '-',
                      ),
                      _StatItem(
                        label: l10n.desktopAvg10D,
                        value: asset != null ? '-' : '-',
                      ),
                      _StatItem(
                        label: l10n.desktopPrevClose,
                        value: asset != null ? price : '-',
                      ),
                    ],
                  ),
                  const SizedBox(height: 5),
                  Row(
                    children: [
                      _RangeChip(label: '1D', active: true),
                      const SizedBox(width: 8),
                      _RangeChip(label: '5D'),
                      const SizedBox(width: 8),
                      _RangeChip(label: '1M'),
                      const SizedBox(width: 8),
                      _RangeChip(label: '3M'),
                    ],
                  ),
                  const SizedBox(height: 5),
                  SizedBox(
                    height: 220,
                    child: Container(
                      decoration: BoxDecoration(
                        color: AppColors.secondaryBg,
                        borderRadius: BorderRadius.circular(5),
                      ),
                      child: Center(
                        child: Text(
                          l10n.desktopPriceChartPlaceholder,
                          style: const TextStyle(
                            color: AppColors.textSecondary,
                          ),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 5),
                  _RangeIndicator(
                    label: l10n.desktopDayRange,
                    range: asset != null ? 'N/A' : '-',
                  ),
                  const SizedBox(height: 5),
                  _RangeIndicator(
                    label: l10n.desktop52WRange,
                    range: asset != null ? 'N/A' : '-',
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}

class _StatItem extends StatelessWidget {
  const _StatItem({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label,
            style: const TextStyle(
              color: AppColors.textSecondary,
              fontSize: 12,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            value,
            style: const TextStyle(
              color: AppColors.textPrimary,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}

class _RangeChip extends StatelessWidget {
  const _RangeChip({required this.label, this.active = false});

  final String label;
  final bool active;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 34,
      padding: const EdgeInsets.symmetric(horizontal: 14),
      decoration: BoxDecoration(
        color: active ? AppColors.accent : AppColors.secondaryBg,
        borderRadius: BorderRadius.circular(5),
      ),
      alignment: Alignment.center,
      child: Text(
        label,
        style: TextStyle(
          color: active ? AppColors.primaryBg : AppColors.textPrimary,
          fontWeight: active ? FontWeight.w600 : FontWeight.normal,
        ),
      ),
    );
  }
}

class _RangeIndicator extends StatelessWidget {
  const _RangeIndicator({required this.label, required this.range});

  final String label;
  final String range;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(color: AppColors.textSecondary, fontSize: 12),
        ),
        const SizedBox(height: 6),
        Text(
          range,
          style: const TextStyle(
            color: AppColors.textPrimary,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 8),
        Container(
          height: 8,
          decoration: BoxDecoration(
            color: AppColors.secondaryBg,
            borderRadius: BorderRadius.circular(5),
          ),
          child: Stack(
            children: [
              Positioned.fill(
                child: Container(
                  margin: const EdgeInsets.symmetric(horizontal: 4),
                  decoration: BoxDecoration(
                    color: AppColors.border,
                    borderRadius: BorderRadius.circular(5),
                  ),
                ),
              ),
              Positioned(
                left: 48,
                right: 48,
                top: 0,
                bottom: 0,
                child: Container(
                  decoration: BoxDecoration(
                    color: AppColors.accent,
                    borderRadius: BorderRadius.circular(5),
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class _TradePanel extends StatelessWidget {
  const _TradePanel({
    required this.symbol,
    required this.estimatedAmount,
    required this.onBuy,
    required this.onSell,
  });

  final String symbol;
  final String estimatedAmount;
  final VoidCallback onBuy;
  final VoidCallback onSell;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final canTrade = symbol.isNotEmpty;
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBg,
        borderRadius: BorderRadius.circular(5),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(
            l10n.desktopOrderEntry,
            style: const TextStyle(
              color: AppColors.textPrimary,
              fontSize: 16,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 5),
          Row(
            children: [
              Expanded(
                child: _TradeTypeChip(label: l10n.tradeBuy, active: true),
              ),
              const SizedBox(width: 5),
              Expanded(child: _TradeTypeChip(label: l10n.tradeSell)),
            ],
          ),
          const SizedBox(height: 5),
          _FieldLabel(label: l10n.tradeSymbol),
          const SizedBox(height: 8),
          _InputBox(text: symbol.isEmpty ? l10n.tradeSymbolHint : symbol),
          const SizedBox(height: 5),
          _FieldLabel(label: l10n.tradeOrderType),
          const SizedBox(height: 8),
          _InputBox(text: l10n.tradeMarket),
          const SizedBox(height: 5),
          Row(
            children: [
              Expanded(
                child: _FieldWithValue(label: l10n.desktopQty, value: '100'),
              ),
              const SizedBox(width: 5),
              Expanded(
                child: _FieldWithValue(label: l10n.desktopTif, value: 'Day'),
              ),
            ],
          ),
          const SizedBox(height: 5),
          _FieldLabel(label: l10n.desktopEstimatedAmount),
          const SizedBox(height: 8),
          Text(
            estimatedAmount,
            style: const TextStyle(
              color: AppColors.textPrimary,
              fontSize: 18,
              fontWeight: FontWeight.w600,
            ),
          ),
          const Spacer(),
          Row(
            children: [
              Expanded(
                child: OutlinedButton(
                  onPressed: canTrade ? onSell : null,
                  style: OutlinedButton.styleFrom(
                    foregroundColor: AppColors.textPrimary,
                    side: const BorderSide(color: AppColors.border),
                    minimumSize: const Size.fromHeight(38),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(5),
                    ),
                  ),
                  child: Text(l10n.desktopPreviewSell),
                ),
              ),
              const SizedBox(width: 5),
              Expanded(
                child: ElevatedButton(
                  onPressed: canTrade ? onBuy : null,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.accent,
                    foregroundColor: AppColors.textPrimary,
                    minimumSize: const Size.fromHeight(38),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(5),
                    ),
                  ),
                  child: Text(l10n.desktopPreviewBuy),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _TradeTypeChip extends StatelessWidget {
  const _TradeTypeChip({required this.label, this.active = false});

  final String label;
  final bool active;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 42,
      alignment: Alignment.center,
      decoration: BoxDecoration(
        color: active ? AppColors.accent : AppColors.secondaryBg,
        borderRadius: BorderRadius.circular(5),
      ),
      child: Text(
        label,
        style: TextStyle(
          color: active ? AppColors.primaryBg : AppColors.textPrimary,
          fontWeight: active ? FontWeight.w600 : FontWeight.normal,
        ),
      ),
    );
  }
}

class _FieldLabel extends StatelessWidget {
  const _FieldLabel({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) => Text(
    label,
    style: const TextStyle(color: AppColors.textSecondary, fontSize: 12),
  );
}

class _InputBox extends StatelessWidget {
  const _InputBox({required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 44,
      padding: const EdgeInsets.symmetric(horizontal: 14),
      decoration: BoxDecoration(
        color: AppColors.secondaryBg,
        borderRadius: BorderRadius.circular(5),
      ),
      alignment: Alignment.centerLeft,
      child: Text(text, style: const TextStyle(color: AppColors.textPrimary)),
    );
  }
}

class _FieldWithValue extends StatelessWidget {
  const _FieldWithValue({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(color: AppColors.textSecondary, fontSize: 12),
        ),
        const SizedBox(height: 8),
        Container(
          height: 44,
          decoration: BoxDecoration(
            color: AppColors.secondaryBg,
            borderRadius: BorderRadius.circular(5),
          ),
          alignment: Alignment.centerLeft,
          padding: const EdgeInsets.symmetric(horizontal: 14),
          child: Text(
            value,
            style: const TextStyle(color: AppColors.textPrimary),
          ),
        ),
      ],
    );
  }
}

class _BottomCardsRow extends StatelessWidget {
  const _BottomCardsRow({
    required this.positionsAsync,
    required this.ordersAsync,
    required this.assetViewAsync,
  });

  final AsyncValue<List<PositionResponse>> positionsAsync;
  final AsyncValue<List<OrderResponse>> ordersAsync;
  final AsyncValue<List<AssetViewResponse>> assetViewAsync;

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Flexible(
          flex: 34,
          child: _PositionsPanel(positionsAsync: positionsAsync),
        ),
        const SizedBox(width: 5),
        Flexible(flex: 38, child: _OrdersPanel(ordersAsync: ordersAsync)),
        const SizedBox(width: 5),
        Flexible(
          flex: 28,
          child: _AccountSummaryPanel(assetViewAsync: assetViewAsync),
        ),
      ],
    );
  }
}

class _PositionsPanel extends StatelessWidget {
  const _PositionsPanel({required this.positionsAsync});

  final AsyncValue<List<PositionResponse>> positionsAsync;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBg,
        borderRadius: BorderRadius.circular(5),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(
            l10n.desktopPositions,
            style: const TextStyle(
              color: AppColors.textPrimary,
              fontSize: 16,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 5),
          const _PositionHeader(),
          const SizedBox(height: 5),
          Expanded(
            child: positionsAsync.when(
              loading: () => const Center(
                child: CircularProgressIndicator(color: AppColors.accent),
              ),
              error: (_, _) => Center(
                child: Text(
                  'Unable to load positions',
                  style: const TextStyle(color: AppColors.textSecondary),
                ),
              ),
              data: (positions) {
                if (positions.isEmpty) {
                  return Center(
                    child: Text(
                      'No positions available',
                      style: const TextStyle(color: AppColors.textSecondary),
                    ),
                  );
                }
                return SingleChildScrollView(
                  child: DataTable(
                    dataRowMinHeight: 40,
                    dataRowMaxHeight: 40,
                    headingRowHeight: 40,
                    dividerThickness: 1,
                    headingRowColor: WidgetStateColor.resolveWith(
                      (states) => AppColors.secondaryBg,
                    ),
                    headingTextStyle: const TextStyle(
                      color: AppColors.textSecondary,
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                    ),
                    dataRowColor: WidgetStateColor.resolveWith(
                      (states) => AppColors.cardBg,
                    ),
                    dataTextStyle: const TextStyle(
                      color: AppColors.textPrimary,
                      fontSize: 13,
                    ),
                    columns: const [
                      DataColumn(label: Text('代码')),
                      DataColumn(label: Text('数量')),
                      DataColumn(label: Text('市值')),
                      DataColumn(label: Text('損益')),
                    ],
                    rows: positions
                        .map(
                          (position) => DataRow(
                            cells: [
                              DataCell(Text(position.symbol)),
                              DataCell(Text('${position.quantity}')),
                              DataCell(
                                Text(
                                  NumberFormat.currency(
                                    symbol: '\$',
                                    decimalDigits: 0,
                                  ).format(position.marketValue),
                                ),
                              ),
                              DataCell(
                                Text(
                                  '${position.unrealizedPnlRate >= 0 ? '+' : ''}${(position.unrealizedPnlRate * 100).toStringAsFixed(2)}%',
                                  style: TextStyle(
                                    color: position.unrealizedPnlRate >= 0
                                        ? AppColors.gain
                                        : AppColors.loss,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        )
                        .toList(),
                  ),
                );
              },
            ),
          ),
          const SizedBox(height: 5),
          Text(
            '${l10n.desktopTotalMarketValue}: ${positionsAsync.maybeWhen(
              data: (positions) => NumberFormat.currency(symbol: '\$', decimalDigits: 0).format(positions.fold(0.0, (sum, item) => sum + item.marketValue)),
              orElse: () => '-',
            )}',
            style: const TextStyle(
              color: AppColors.textSecondary,
              fontSize: 12,
            ),
          ),
        ],
      ),
    );
  }
}

class _PositionHeader extends StatelessWidget {
  const _PositionHeader();

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Row(
      children: [
        Expanded(
          child: Text(
            l10n.tradeSymbol,
            style: const TextStyle(
              color: AppColors.textSecondary,
              fontSize: 12,
            ),
          ),
        ),
        Expanded(
          child: Text(
            l10n.desktopQty,
            style: const TextStyle(
              color: AppColors.textSecondary,
              fontSize: 12,
            ),
          ),
        ),
        Expanded(
          child: Text(
            l10n.desktopMarketValue,
            style: const TextStyle(
              color: AppColors.textSecondary,
              fontSize: 12,
            ),
            textAlign: TextAlign.right,
          ),
        ),
      ],
    );
  }
}

class _OrdersPanel extends StatelessWidget {
  const _OrdersPanel({required this.ordersAsync});

  final AsyncValue<List<OrderResponse>> ordersAsync;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBg,
        borderRadius: BorderRadius.circular(5),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(
            l10n.desktopOrders,
            style: const TextStyle(
              color: AppColors.textPrimary,
              fontSize: 16,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 5),
          Row(
            children: [
              _OrderTab(label: l10n.desktopOrderTabWorking, active: true),
              const SizedBox(width: 5),
              _OrderTab(label: l10n.desktopOrderTabFilled),
              const SizedBox(width: 5),
              _OrderTab(label: l10n.desktopOrderTabCancelled),
            ],
          ),
          const SizedBox(height: 5),
          Expanded(
            child: ordersAsync.when(
              loading: () => const Center(
                child: CircularProgressIndicator(color: AppColors.accent),
              ),
              error: (_, _) => Center(
                child: Text(
                  'Unable to load orders',
                  style: const TextStyle(color: AppColors.textSecondary),
                ),
              ),
              data: (orders) {
                if (orders.isEmpty) {
                  return Center(
                    child: Text(
                      'No active orders',
                      style: const TextStyle(color: AppColors.textSecondary),
                    ),
                  );
                }
                return SingleChildScrollView(
                  child: DataTable(
                    dataRowMinHeight: 40,
                    dataRowMaxHeight: 40,
                    headingRowHeight: 40,
                    dividerThickness: 1,
                    headingRowColor: WidgetStateColor.resolveWith(
                      (states) => AppColors.secondaryBg,
                    ),
                    headingTextStyle: const TextStyle(
                      color: AppColors.textSecondary,
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                    ),
                    dataRowColor: WidgetStateColor.resolveWith(
                      (states) => AppColors.cardBg,
                    ),
                    dataTextStyle: const TextStyle(
                      color: AppColors.textPrimary,
                      fontSize: 13,
                    ),
                    columns: const [
                      DataColumn(label: Text('代码')),
                      DataColumn(label: Text('方向')),
                      DataColumn(label: Text('数量')),
                      DataColumn(label: Text('价格')),
                      DataColumn(label: Text('时间')),
                    ],
                    rows: orders
                        .map(
                          (order) => DataRow(
                            cells: [
                              DataCell(Text(order.symbol)),
                              DataCell(Text(order.side == 'BUY' ? '买入' : '卖出')),
                              DataCell(Text('${order.quantity}')),
                              DataCell(
                                Text(
                                  order.price != null
                                      ? NumberFormat.currency(
                                          symbol: '\$',
                                          decimalDigits: 2,
                                        ).format(order.price!)
                                      : '市价',
                                ),
                              ),
                              DataCell(
                                Text(
                                  order.createdAt.isNotEmpty
                                      ? order.createdAt.split('T').last
                                      : '-',
                                ),
                              ),
                            ],
                          ),
                        )
                        .toList(),
                  ),
                );
              },
            ),
          ),
          const Spacer(),
          Text(
            l10n.desktopViewAllOrders,
            style: const TextStyle(
              color: AppColors.accent,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}

class _OrderTab extends StatelessWidget {
  const _OrderTab({required this.label, this.active = false});

  final String label;
  final bool active;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 34,
      padding: const EdgeInsets.symmetric(horizontal: 14),
      decoration: BoxDecoration(
        color: active ? AppColors.accent : AppColors.secondaryBg,
        borderRadius: BorderRadius.circular(5),
      ),
      alignment: Alignment.center,
      child: Text(
        label,
        style: TextStyle(
          color: active ? AppColors.primaryBg : AppColors.textPrimary,
          fontWeight: active ? FontWeight.w600 : FontWeight.normal,
        ),
      ),
    );
  }
}

class _AccountSummaryPanel extends StatelessWidget {
  const _AccountSummaryPanel({required this.assetViewAsync});

  final AsyncValue<List<AssetViewResponse>> assetViewAsync;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final totals = assetViewAsync.maybeWhen(
      data: (assets) {
        final totalValue = assets.fold(
          0.0,
          (sum, item) => sum + item.marketValue,
        );
        final totalPnl = assets.fold(
          0.0,
          (sum, item) => sum + item.unrealizedPnl,
        );
        return {
          'net': NumberFormat.currency(
            symbol: '\$',
            decimalDigits: 0,
          ).format(totalValue),
          'cash': NumberFormat.currency(
            symbol: '\$',
            decimalDigits: 0,
          ).format(totalValue * 0.18),
          'buying': NumberFormat.currency(
            symbol: '\$',
            decimalDigits: 0,
          ).format(totalValue * 0.22),
          'daily':
              '${totalPnl >= 0 ? '+' : ''}${NumberFormat.currency(symbol: '\$', decimalDigits: 2).format(totalPnl)}',
          'ytd':
              '${totalPnl >= 0 ? '+' : ''}${NumberFormat.currency(symbol: '\$', decimalDigits: 2).format(totalPnl * 4)}',
        };
      },
      orElse: () => {
        'net': '-'.toString(),
        'cash': '-'.toString(),
        'buying': '-'.toString(),
        'daily': '-'.toString(),
        'ytd': '-'.toString(),
      },
    );

    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBg,
        borderRadius: BorderRadius.circular(5),
        border: Border.all(color: AppColors.border),
      ),
      child: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              l10n.desktopAccountSummary,
              style: const TextStyle(
                color: AppColors.textPrimary,
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 5),
            _AccountMetric(
              label: l10n.desktopNetLiquidation,
              value: totals['net']!,
            ),
            const SizedBox(height: 5),
            _AccountMetric(
              label: l10n.desktopCashBalance,
              value: totals['cash']!,
            ),
            const SizedBox(height: 5),
            _AccountMetric(
              label: l10n.desktopBuyingPower,
              value: totals['buying']!,
            ),
            const SizedBox(height: 5),
            _AccountMetric(
              label: l10n.homeDailyPnl,
              value: totals['daily']!,
              valueColor: totals['daily']!.startsWith('-')
                  ? AppColors.loss
                  : AppColors.gain,
            ),
            const SizedBox(height: 5),
            _AccountMetric(
              label: l10n.desktopYtdPnl,
              value: totals['ytd']!,
              valueColor: totals['ytd']!.startsWith('-')
                  ? AppColors.loss
                  : AppColors.gain,
            ),
          ],
        ),
      ),
    );
  }
}

class _AccountMetric extends StatelessWidget {
  const _AccountMetric({
    required this.label,
    required this.value,
    this.valueColor = AppColors.textPrimary,
  });

  final String label;
  final String value;
  final Color valueColor;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(color: AppColors.textSecondary, fontSize: 12),
        ),
        const SizedBox(height: 6),
        Text(
          value,
          style: TextStyle(color: valueColor, fontWeight: FontWeight.w600),
        ),
      ],
    );
  }
}

class _BottomStatusBar extends StatelessWidget {
  const _BottomStatusBar();

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Container(
      height: 64,
      padding: const EdgeInsets.symmetric(horizontal: 18),
      decoration: BoxDecoration(
        color: AppColors.cardBg,
        borderRadius: BorderRadius.circular(5),
        border: Border.all(color: AppColors.border),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Flexible(
            flex: 1,
            fit: FlexFit.tight,
            child: SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(
                children: [
                  const _StatusItem(
                    label: 'DOW',
                    value: '34,620',
                    diff: '+0.45%',
                  ),
                  const _StatusDivider(),
                  const _StatusItem(
                    label: 'S&P 500',
                    value: '4,215',
                    diff: '+0.32%',
                  ),
                  const _StatusDivider(),
                  const _StatusItem(
                    label: 'NASDAQ',
                    value: '13,888',
                    diff: '+0.78%',
                  ),
                  const _StatusDivider(),
                  const _StatusItem(
                    label: 'VIX',
                    value: '18.04',
                    diff: '-1.20%',
                  ),
                ],
              ),
            ),
          ),
          Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                l10n.desktopDataAsOf('09:53'),
                softWrap: false,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(
                  color: AppColors.textSecondary,
                  fontSize: 12,
                ),
              ),
              const SizedBox(width: 5),
              const Icon(Icons.link, color: AppColors.textSecondary, size: 18),
              const SizedBox(width: 8),
              Text(
                l10n.desktopConnected,
                softWrap: false,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(color: AppColors.accent, fontSize: 12),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _StatusItem extends StatelessWidget {
  const _StatusItem({
    required this.label,
    required this.value,
    required this.diff,
  });

  final String label;
  final String value;
  final String diff;

  @override
  Widget build(BuildContext context) {
    final isPositive = diff.startsWith('+');
    return Padding(
      padding: const EdgeInsets.only(right: 22),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label,
            softWrap: false,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              color: AppColors.textSecondary,
              fontSize: 12,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            value,
            softWrap: false,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              color: AppColors.textPrimary,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 2),
          Text(
            diff,
            softWrap: false,
            overflow: TextOverflow.ellipsis,
            style: TextStyle(
              color: isPositive ? AppColors.gain : AppColors.loss,
              fontSize: 12,
            ),
          ),
        ],
      ),
    );
  }
}

class _StatusDivider extends StatelessWidget {
  const _StatusDivider();

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 34,
      width: 1,
      color: AppColors.border,
      margin: const EdgeInsets.symmetric(horizontal: 12),
    );
  }
}

// ── Portfolio View ──
class _PortfolioView extends StatelessWidget {
  const _PortfolioView({required this.positionsAsync});

  final AsyncValue<List<PositionResponse>> positionsAsync;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);

    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBg,
        borderRadius: BorderRadius.circular(5),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(
            l10n.navPortfolio,
            style: const TextStyle(
              color: AppColors.textPrimary,
              fontSize: 20,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          Expanded(
            child: positionsAsync.when(
              loading: () => const Center(
                child: CircularProgressIndicator(color: AppColors.accent),
              ),
              error: (e, _) => Center(
                child: Text(
                  l10n.errorLoadFailed,
                  style: const TextStyle(color: AppColors.textSecondary),
                ),
              ),
              data: (positions) {
                if (positions.isEmpty) {
                  return Center(
                    child: Text(
                      l10n.homeNoPosions,
                      style: const TextStyle(color: AppColors.textSecondary),
                    ),
                  );
                }

                final totalValue = positions.fold<double>(
                  0.0,
                  (sum, p) => sum + p.marketValue,
                );

                return SingleChildScrollView(
                  child: Column(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: AppColors.secondaryBg,
                          borderRadius: BorderRadius.circular(5),
                        ),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                          children: [
                            Column(
                              children: [
                                Text(
                                  l10n.desktopTotalMarketValue,
                                  style: const TextStyle(
                                    color: AppColors.textSecondary,
                                    fontSize: 12,
                                  ),
                                ),
                                const SizedBox(height: 8),
                                Text(
                                  NumberFormat.currency(
                                    symbol: '\$',
                                    decimalDigits: 0,
                                  ).format(totalValue),
                                  style: const TextStyle(
                                    color: AppColors.textPrimary,
                                    fontSize: 16,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 16),
                      DataTable(
                        columns: const [
                          DataColumn(label: Text('代码')),
                          DataColumn(label: Text('数量')),
                          DataColumn(label: Text('市值')),
                          DataColumn(label: Text('损益')),
                        ],
                        rows: positions
                            .map(
                              (p) => DataRow(
                                cells: [
                                  DataCell(Text(p.symbol)),
                                  DataCell(Text('${p.quantity}')),
                                  DataCell(
                                    Text(
                                      NumberFormat.currency(
                                        symbol: '\$',
                                        decimalDigits: 0,
                                      ).format(p.marketValue),
                                    ),
                                  ),
                                  DataCell(
                                    Text(
                                      '${p.unrealizedPnlRate >= 0 ? '+' : ''}${(p.unrealizedPnlRate * 100).toStringAsFixed(2)}%',
                                      style: TextStyle(
                                        color: p.unrealizedPnlRate >= 0
                                            ? AppColors.gain
                                            : AppColors.loss,
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                            )
                            .toList(),
                      ),
                    ],
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

// ── Orders View ──
class _OrdersView extends StatelessWidget {
  const _OrdersView({required this.ordersAsync});

  final AsyncValue<List<OrderResponse>> ordersAsync;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);

    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBg,
        borderRadius: BorderRadius.circular(5),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(
            l10n.navOrders,
            style: const TextStyle(
              color: AppColors.textPrimary,
              fontSize: 20,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          Expanded(
            child: ordersAsync.when(
              loading: () => const Center(
                child: CircularProgressIndicator(color: AppColors.accent),
              ),
              error: (e, _) => Center(
                child: Text(
                  l10n.errorLoadFailed,
                  style: const TextStyle(color: AppColors.textSecondary),
                ),
              ),
              data: (orders) {
                if (orders.isEmpty) {
                  return Center(
                    child: Text(
                      '暂无订单',
                      style: const TextStyle(color: AppColors.textSecondary),
                    ),
                  );
                }

                return SingleChildScrollView(
                  child: DataTable(
                    columns: const [
                      DataColumn(label: Text('代码')),
                      DataColumn(label: Text('方向')),
                      DataColumn(label: Text('数量')),
                      DataColumn(label: Text('价格')),
                      DataColumn(label: Text('时间')),
                    ],
                    rows: orders
                        .map(
                          (order) => DataRow(
                            cells: [
                              DataCell(Text(order.symbol)),
                              DataCell(
                                Text(
                                  order.side == 'BUY' ? '买入' : '卖出',
                                  style: TextStyle(
                                    color: order.side == 'BUY'
                                        ? AppColors.gain
                                        : AppColors.loss,
                                  ),
                                ),
                              ),
                              DataCell(Text('${order.quantity}')),
                              DataCell(
                                Text(
                                  order.price != null
                                      ? NumberFormat.currency(
                                          symbol: '\$',
                                          decimalDigits: 2,
                                        ).format(order.price!)
                                      : '市价',
                                ),
                              ),
                              DataCell(
                                Text(
                                  order.createdAt.isNotEmpty
                                      ? order.createdAt.split('T').last
                                      : '-',
                                ),
                              ),
                            ],
                          ),
                        )
                        .toList(),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

// ── Trade View ──
class _TradeView extends StatelessWidget {
  const _TradeView({
    required this.selectedSymbol,
    required this.selectedAsset,
    required this.estimatedAmount,
    required this.onBuy,
    required this.onSell,
  });

  final String selectedSymbol;
  final AssetViewResponse? selectedAsset;
  final String estimatedAmount;
  final VoidCallback onBuy;
  final VoidCallback onSell;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final canTrade = selectedSymbol.isNotEmpty;

    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBg,
        borderRadius: BorderRadius.circular(5),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(
            l10n.navTrade,
            style: const TextStyle(
              color: AppColors.textPrimary,
              fontSize: 20,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          Expanded(
            child: SingleChildScrollView(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  // Quote Panel
                  if (selectedAsset != null) ...[
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: AppColors.secondaryBg,
                        borderRadius: BorderRadius.circular(5),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            selectedSymbol,
                            style: const TextStyle(
                              color: AppColors.textPrimary,
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          const SizedBox(height: 8),
                          Text(
                            NumberFormat.currency(
                              symbol: '\$',
                              decimalDigits: 2,
                            ).format(selectedAsset!.currentPrice),
                            style: const TextStyle(
                              color: AppColors.textPrimary,
                              fontSize: 24,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 16),
                  ],

                  // Trade Form
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: AppColors.secondaryBg,
                      borderRadius: BorderRadius.circular(5),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        Text(
                          l10n.desktopOrderEntry,
                          style: const TextStyle(
                            color: AppColors.textPrimary,
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 12),
                        Row(
                          children: [
                            Expanded(
                              child: ElevatedButton(
                                onPressed: canTrade ? onBuy : null,
                                style: ElevatedButton.styleFrom(
                                  backgroundColor: AppColors.gain,
                                ),
                                child: Text(l10n.desktopPreviewBuy),
                              ),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: OutlinedButton(
                                onPressed: canTrade ? onSell : null,
                                child: Text(l10n.desktopPreviewSell),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
