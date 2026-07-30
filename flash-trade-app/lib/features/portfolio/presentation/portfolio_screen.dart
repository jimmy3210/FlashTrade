import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import '../../../core/constants/app_colors.dart';
import '../../../l10n/app_localizations.dart';
import '../../../shared/widgets/price_text.dart';
import '../../trade/providers/trade_provider.dart';
import '../providers/portfolio_provider.dart';

class PortfolioScreen extends ConsumerWidget {
  const PortfolioScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final positionsAsync = ref.watch(positionsProvider);

    return Scaffold(
      backgroundColor: AppColors.primaryBg,
      appBar: AppBar(
        backgroundColor: AppColors.primaryBg,
        title: Text(
          l10n.navPortfolio,
          style: const TextStyle(color: AppColors.textPrimary),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh, color: AppColors.textSecondary),
            onPressed: () => ref.invalidate(positionsProvider),
          ),
        ],
      ),
      body: positionsAsync.when(
        loading: () => const Center(
          child: CircularProgressIndicator(color: AppColors.accent),
        ),
        error: (e, _) => Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.error_outline, color: AppColors.loss, size: 48),
              const SizedBox(height: 16),
              Text(
                l10n.errorLoadFailed,
                style: const TextStyle(color: AppColors.textSecondary),
              ),
              const SizedBox(height: 12),
              ElevatedButton(
                onPressed: () => ref.invalidate(positionsProvider),
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.accent,
                ),
                child: Text(l10n.errorRetry),
              ),
            ],
          ),
        ),
        data: (positions) {
          if (positions.isEmpty) {
            return const Center(
              child: Text(
                'ポジションがありません',
                style: TextStyle(color: AppColors.textSecondary, fontSize: 15),
              ),
            );
          }

          final totalMarketValue = positions.fold(
            0.0,
            (s, p) => s + p.marketValue,
          );
          final totalUnrealizedPnl = positions.fold(
            0.0,
            (s, p) => s + p.unrealizedPnl,
          );
          final fmtCcy = NumberFormat.currency(symbol: '¥', decimalDigits: 0);

          return Column(
            children: [
              // ── サマリー行 ──
              Container(
                padding: const EdgeInsets.symmetric(vertical: 14),
                color: AppColors.cardBg,
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    _Summary(
                      label: '総評価額',
                      value: fmtCcy.format(totalMarketValue),
                      valueColor: AppColors.textPrimary,
                    ),
                    Container(width: 1, height: 36, color: AppColors.border),
                    _Summary(
                      label: '評価損益',
                      value:
                          (totalUnrealizedPnl >= 0 ? '+' : '') +
                          fmtCcy.format(totalUnrealizedPnl),
                      valueColor: totalUnrealizedPnl >= 0
                          ? AppColors.gain
                          : AppColors.loss,
                    ),
                    Container(width: 1, height: 36, color: AppColors.border),
                    _Summary(
                      label: '銘柄数',
                      value: '${positions.length}',
                      valueColor: AppColors.textPrimary,
                    ),
                  ],
                ),
              ),
              const Divider(color: AppColors.border, height: 1),

              // ── ポジションリスト ──
              Expanded(
                child: ListView.separated(
                  itemCount: positions.length,
                  separatorBuilder: (_, _) =>
                      const Divider(color: AppColors.border, height: 1),
                  itemBuilder: (context, i) {
                    final p = positions[i];
                    return Material(
                      color: AppColors.cardBg,
                      child: InkWell(
                        onTap: () {
                          ref.read(selectedSymbolProvider.notifier).state =
                              p.symbol;
                          context.go('/home/trade');
                        },
                        child: Padding(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 16,
                            vertical: 12,
                          ),
                          child: Row(
                            children: [
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      children: [
                                        Text(
                                          p.symbol,
                                          style: const TextStyle(
                                            color: AppColors.textPrimary,
                                            fontWeight: FontWeight.bold,
                                            fontSize: 16,
                                          ),
                                        ),
                                        const SizedBox(width: 8),
                                        Text(
                                          '${p.quantity}株',
                                          style: const TextStyle(
                                            color: AppColors.textSecondary,
                                            fontSize: 13,
                                          ),
                                        ),
                                      ],
                                    ),
                                    const SizedBox(height: 3),
                                    Row(
                                      children: [
                                        Text(
                                          '平均 ¥${NumberFormat('0.00').format(p.averageCost)}',
                                          style: const TextStyle(
                                            color: AppColors.textSecondary,
                                            fontSize: 12,
                                          ),
                                        ),
                                        if (p.brokerName.isNotEmpty) ...[
                                          const SizedBox(width: 8),
                                          Container(
                                            padding: const EdgeInsets.symmetric(
                                              horizontal: 6,
                                              vertical: 1,
                                            ),
                                            decoration: BoxDecoration(
                                              color: AppColors.accent.withAlpha(
                                                25,
                                              ),
                                              borderRadius:
                                                  BorderRadius.circular(4),
                                              border: Border.all(
                                                color: AppColors.accent
                                                    .withAlpha(60),
                                              ),
                                            ),
                                            child: Text(
                                              p.brokerName,
                                              style: const TextStyle(
                                                color: AppColors.accent,
                                                fontSize: 10,
                                                fontWeight: FontWeight.w600,
                                              ),
                                            ),
                                          ),
                                        ],
                                      ],
                                    ),
                                  ],
                                ),
                              ),
                              Column(
                                crossAxisAlignment: CrossAxisAlignment.end,
                                children: [
                                  Text(
                                    '¥${NumberFormat('#,##0.00').format(p.marketValue)}',
                                    style: const TextStyle(
                                      color: AppColors.textPrimary,
                                      fontSize: 15,
                                      fontWeight: FontWeight.w600,
                                      fontFeatures: [
                                        FontFeature.tabularFigures(),
                                      ],
                                    ),
                                  ),
                                  const SizedBox(height: 2),
                                  PriceText(
                                    value: p.unrealizedPnl,
                                    showSign: true,
                                    style: const TextStyle(fontSize: 12),
                                  ),
                                ],
                              ),
                              const SizedBox(width: 4),
                              const Icon(
                                Icons.chevron_right,
                                color: AppColors.textSecondary,
                                size: 18,
                              ),
                            ],
                          ),
                        ),
                      ),
                    );
                  },
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _Summary extends StatelessWidget {
  const _Summary({
    required this.label,
    required this.value,
    required this.valueColor,
  });

  final String label;
  final String value;
  final Color valueColor;

  @override
  Widget build(BuildContext context) => Column(
    children: [
      Text(
        label,
        style: const TextStyle(color: AppColors.textSecondary, fontSize: 12),
      ),
      const SizedBox(height: 4),
      Text(
        value,
        style: TextStyle(
          color: valueColor,
          fontSize: 15,
          fontWeight: FontWeight.bold,
          fontFeatures: const [FontFeature.tabularFigures()],
        ),
      ),
    ],
  );
}
