import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../../../core/constants/app_colors.dart';
import '../../../l10n/app_localizations.dart';
import '../../../shared/widgets/status_badge.dart';
import '../domain/models.dart';
import '../providers/orders_provider.dart';

class OrdersScreen extends ConsumerStatefulWidget {
  const OrdersScreen({super.key});

  @override
  ConsumerState<OrdersScreen> createState() => _OrdersScreenState();
}

class _OrdersScreenState extends ConsumerState<OrdersScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  String _filter = 'ALL';

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _cancelOrder(String orderId) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppColors.cardBg,
        title: const Text(
          '注文キャンセル',
          style: TextStyle(color: AppColors.textPrimary),
        ),
        content: const Text(
          'この注文をキャンセルしますか？',
          style: TextStyle(color: AppColors.textSecondary),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text(
              'いいえ',
              style: TextStyle(color: AppColors.textSecondary),
            ),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text(
              'キャンセルする',
              style: TextStyle(color: AppColors.loss),
            ),
          ),
        ],
      ),
    );

    if (confirmed != true) return;

    try {
      await ref.read(ordersRepositoryProvider).cancelOrder(orderId);
      ref.invalidate(ordersProvider);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('注文をキャンセルしました'),
            backgroundColor: AppColors.cardBg,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('キャンセルに失敗しました'),
            backgroundColor: AppColors.loss,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.primaryBg,
      appBar: AppBar(
        backgroundColor: AppColors.primaryBg,
        title: const Text(
          '注文履歴',
          style: TextStyle(color: AppColors.textPrimary),
        ),
        bottom: TabBar(
          controller: _tabController,
          indicatorColor: AppColors.accent,
          labelColor: AppColors.accent,
          unselectedLabelColor: AppColors.textSecondary,
          tabs: const [
            Tab(text: '注文一覧'),
            Tab(text: '成約履歴'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _OrdersTab(
            filter: _filter,
            onFilterChanged: (f) => setState(() => _filter = f),
            onCancel: _cancelOrder,
          ),
          const _TradesTab(),
        ],
      ),
    );
  }
}

// ─────────────────────────────────────────
// 注文一覧タブ
// ─────────────────────────────────────────
class _OrdersTab extends ConsumerWidget {
  const _OrdersTab({
    required this.filter,
    required this.onFilterChanged,
    required this.onCancel,
  });

  final String filter;
  final ValueChanged<String> onFilterChanged;
  final ValueChanged<String> onCancel;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final ordersAsync = ref.watch(ordersProvider);
    final l10n = AppLocalizations.of(context);

    return Column(
      children: [
        // フィルタバー
        Container(
          color: AppColors.cardBg,
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
          child: Row(
            children: [
              _FilterChip(
                label: l10n.ordersFilterAll,
                value: 'ALL',
                selected: filter,
                onTap: onFilterChanged,
              ),
              const SizedBox(width: 8),
              _FilterChip(
                label: l10n.ordersFilterPending,
                value: 'PENDING',
                selected: filter,
                onTap: onFilterChanged,
              ),
              const SizedBox(width: 8),
              _FilterChip(
                label: l10n.ordersFilterFilled,
                value: 'FILLED',
                selected: filter,
                onTap: onFilterChanged,
              ),
            ],
          ),
        ),
        const Divider(color: AppColors.border, height: 1),
        Expanded(
          child: ordersAsync.when(
            loading: () => const Center(
              child: CircularProgressIndicator(color: AppColors.accent),
            ),
            error: (e, _) => Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Icon(
                    Icons.error_outline,
                    color: AppColors.loss,
                    size: 40,
                  ),
                  const SizedBox(height: 12),
                  const Text(
                    'データを取得できませんでした',
                    style: TextStyle(color: AppColors.textSecondary),
                  ),
                  const SizedBox(height: 12),
                  TextButton(
                    onPressed: () => ref.invalidate(ordersProvider),
                    child: const Text(
                      '再試行',
                      style: TextStyle(color: AppColors.accent),
                    ),
                  ),
                ],
              ),
            ),
            data: (orders) {
              final filtered = switch (filter) {
                'PENDING' =>
                  orders.where((o) => o.status == 'PENDING').toList(),
                'FILLED' => orders.where((o) => o.status == 'FILLED').toList(),
                _ => orders,
              };

              if (filtered.isEmpty) {
                return const Center(
                  child: Text(
                    '注文がありません',
                    style: TextStyle(color: AppColors.textSecondary),
                  ),
                );
              }

              return RefreshIndicator(
                color: AppColors.accent,
                onRefresh: () async => ref.invalidate(ordersProvider),
                child: ListView.separated(
                  itemCount: filtered.length,
                  separatorBuilder: (_, _) =>
                      const Divider(color: AppColors.border, height: 1),
                  itemBuilder: (_, i) =>
                      _OrderCard(order: filtered[i], onCancel: onCancel),
                ),
              );
            },
          ),
        ),
      ],
    );
  }
}

// ─────────────────────────────────────────
// 成約履歴タブ
// ─────────────────────────────────────────
class _TradesTab extends ConsumerWidget {
  const _TradesTab();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tradesAsync = ref.watch(tradesProvider);

    return tradesAsync.when(
      loading: () => const Center(
        child: CircularProgressIndicator(color: AppColors.accent),
      ),
      error: (e, _) => Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.error_outline, color: AppColors.loss, size: 40),
            const SizedBox(height: 12),
            const Text(
              'データを取得できませんでした',
              style: TextStyle(color: AppColors.textSecondary),
            ),
            const SizedBox(height: 12),
            TextButton(
              onPressed: () => ref.invalidate(tradesProvider),
              child: const Text(
                '再試行',
                style: TextStyle(color: AppColors.accent),
              ),
            ),
          ],
        ),
      ),
      data: (trades) {
        if (trades.isEmpty) {
          return const Center(
            child: Text(
              '成約履歴がありません',
              style: TextStyle(color: AppColors.textSecondary),
            ),
          );
        }
        return RefreshIndicator(
          color: AppColors.accent,
          onRefresh: () async => ref.invalidate(tradesProvider),
          child: ListView.separated(
            itemCount: trades.length,
            separatorBuilder: (_, _) =>
                const Divider(color: AppColors.border, height: 1),
            itemBuilder: (_, i) => _TradeCard(trade: trades[i]),
          ),
        );
      },
    );
  }
}

// ─────────────────────────────────────────
// 注文カード
// ─────────────────────────────────────────
class _OrderCard extends StatelessWidget {
  const _OrderCard({required this.order, required this.onCancel});
  final OrderResponse order;
  final ValueChanged<String> onCancel;

  @override
  Widget build(BuildContext context) {
    final isBuy = order.side == 'BUY';
    final sideColor = isBuy ? AppColors.gain : AppColors.loss;
    final fmt = NumberFormat('#,##0.00');

    return Container(
      color: AppColors.cardBg,
      padding: const EdgeInsets.all(16),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // 売買インジケーター
          Container(
            width: 4,
            height: 60,
            decoration: BoxDecoration(
              color: sideColor,
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Text(
                      order.symbol,
                      style: const TextStyle(
                        color: AppColors.textPrimary,
                        fontWeight: FontWeight.bold,
                        fontSize: 16,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Text(
                      isBuy ? '買' : '売',
                      style: TextStyle(
                        color: sideColor,
                        fontWeight: FontWeight.w600,
                        fontSize: 14,
                      ),
                    ),
                    const SizedBox(width: 8),
                    OrderTypeBadge(order.orderType),
                    const Spacer(),
                    StatusBadge(order.status),
                  ],
                ),
                const SizedBox(height: 6),
                Row(
                  children: [
                    Text(
                      '${order.quantity}株',
                      style: const TextStyle(
                        color: AppColors.textPrimary,
                        fontSize: 14,
                      ),
                    ),
                    const SizedBox(width: 8),
                    if (order.price != null)
                      Text(
                        '¥${fmt.format(order.price)}',
                        style: const TextStyle(
                          color: AppColors.textPrimary,
                          fontSize: 14,
                        ),
                      ),
                  ],
                ),
                const SizedBox(height: 4),
                Text(
                  order.createdAt
                      .replaceFirst('T', ' ')
                      .substring(
                        0,
                        order.createdAt.length > 19
                            ? 19
                            : order.createdAt.length,
                      ),
                  style: const TextStyle(
                    color: AppColors.textSecondary,
                    fontSize: 11,
                  ),
                ),
              ],
            ),
          ),
          if (order.status == 'PENDING') ...[
            const SizedBox(width: 8),
            SizedBox(
              height: 36,
              child: OutlinedButton(
                onPressed: () => onCancel(order.id),
                style: OutlinedButton.styleFrom(
                  foregroundColor: AppColors.loss,
                  side: const BorderSide(color: AppColors.loss),
                  padding: const EdgeInsets.symmetric(horizontal: 12),
                ),
                child: const Text('取消', style: TextStyle(fontSize: 12)),
              ),
            ),
          ],
        ],
      ),
    );
  }
}

// ─────────────────────────────────────────
// 成約カード
// ─────────────────────────────────────────
class _TradeCard extends StatelessWidget {
  const _TradeCard({required this.trade});
  final TradeResponse trade;

  @override
  Widget build(BuildContext context) {
    final isBuy = trade.side == 'BUY';
    final sideColor = isBuy ? AppColors.gain : AppColors.loss;
    final fmt = NumberFormat('#,##0.00');

    return Container(
      color: AppColors.cardBg,
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Container(
            width: 4,
            height: 50,
            decoration: BoxDecoration(
              color: sideColor,
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Text(
                      trade.symbol,
                      style: const TextStyle(
                        color: AppColors.textPrimary,
                        fontWeight: FontWeight.bold,
                        fontSize: 16,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Text(
                      isBuy ? '買' : '売',
                      style: TextStyle(
                        color: sideColor,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                Row(
                  children: [
                    Text(
                      '${trade.quantity}株 @ ¥${fmt.format(trade.executedPrice)}',
                      style: const TextStyle(
                        color: AppColors.textPrimary,
                        fontSize: 14,
                      ),
                    ),
                    const Spacer(),
                    Text(
                      '¥${fmt.format(trade.quantity * trade.executedPrice)}',
                      style: const TextStyle(
                        color: AppColors.textSecondary,
                        fontSize: 13,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 3),
                Text(
                  trade.executedAt
                      .replaceFirst('T', ' ')
                      .substring(
                        0,
                        trade.executedAt.length > 19
                            ? 19
                            : trade.executedAt.length,
                      ),
                  style: const TextStyle(
                    color: AppColors.textSecondary,
                    fontSize: 11,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _FilterChip extends StatelessWidget {
  const _FilterChip({
    required this.label,
    required this.value,
    required this.selected,
    required this.onTap,
  });

  final String label;
  final String value;
  final String selected;
  final ValueChanged<String> onTap;

  @override
  Widget build(BuildContext context) {
    final isSelected = selected == value;
    return GestureDetector(
      onTap: () => onTap(value),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
        decoration: BoxDecoration(
          color: isSelected
              ? AppColors.accent.withAlpha(40)
              : AppColors.secondaryBg,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: isSelected ? AppColors.accent : AppColors.border,
          ),
        ),
        child: Text(
          label,
          style: TextStyle(
            color: isSelected ? AppColors.accent : AppColors.textSecondary,
            fontSize: 13,
            fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
          ),
        ),
      ),
    );
  }
}
