import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import '../../../core/constants/app_colors.dart';
import '../../../l10n/app_localizations.dart';
import '../../../shared/widgets/price_text.dart';
import '../../../shared/widgets/stock_card.dart';
import '../../auth/presentation/logout_action.dart';
import '../../portfolio/domain/models.dart';
import '../../portfolio/providers/portfolio_provider.dart';
import '../domain/models.dart';
import '../providers/home_provider.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final assetViewAsync = ref.watch(assetViewProvider);
    final dailyPnlAsync = ref.watch(dailyPnlProvider);

    return Scaffold(
      backgroundColor: AppColors.primaryBg,
      appBar: AppBar(
        backgroundColor: AppColors.primaryBg,
        titleSpacing: 0,
        title: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 34,
              height: 34,
              padding: const EdgeInsets.all(6),
              decoration: BoxDecoration(
                color: AppColors.cardBg,
                borderRadius: BorderRadius.circular(10),
                border: Border.all(color: AppColors.border),
              ),
              child: Image.asset(
                'assets/icons/ft_icon.png',
                fit: BoxFit.contain,
              ),
            ),
            const SizedBox(width: 10),
            const Text(
              'FlashTrade',
              style: TextStyle(
                color: AppColors.textPrimary,
                fontWeight: FontWeight.bold,
                fontSize: 20,
              ),
            ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.notifications_outlined),
            onPressed: () {},
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: AppLocalizations.of(context).homeLogout,
            onPressed: () async {
              await logoutAndNavigate(context, ref);
            },
          ),
        ],
      ),
      body: RefreshIndicator(
        color: AppColors.accent,
        onRefresh: () async {
          ref.invalidate(assetViewProvider);
          ref.invalidate(dailyPnlProvider);
        },
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // ── 資産サマリーカード ──
            assetViewAsync.when(
              loading: () => const _LoadingCard(),
              error: (e, _) =>
                  _ErrorCard(onRetry: () => ref.invalidate(assetViewProvider)),
              data: (assets) => _AssetSummaryCard(assets: assets),
            ),
            const SizedBox(height: 16),

            // ── ポジション Top5 ──
            const _SectionHeader(title: 'ポジション（上位5件）'),
            const SizedBox(height: 8),
            assetViewAsync.when(
              loading: () => const _LoadingCard(height: 200),
              error: (_, _) => const SizedBox.shrink(),
              data: (assets) {
                final sorted = [...assets]
                  ..sort((a, b) => b.unrealizedPnl.compareTo(a.unrealizedPnl));
                final top5 = sorted.take(5).toList();
                if (top5.isEmpty) {
                  return const Center(
                    child: Padding(
                      padding: EdgeInsets.all(24),
                      child: Text(
                        'ポジションがありません',
                        style: TextStyle(color: AppColors.textSecondary),
                      ),
                    ),
                  );
                }
                return Container(
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(color: AppColors.border),
                    color: AppColors.cardBg,
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withAlpha(60),
                        blurRadius: 12,
                        offset: const Offset(0, 6),
                      ),
                    ],
                  ),
                  clipBehavior: Clip.antiAlias,
                  child: Column(
                    children: top5
                        .map(
                          (a) => StockCard(
                            symbol: a.symbol,
                            currentPrice: a.currentPrice,
                            changeRate: a.unrealizedPnlRate,
                            subtitle: '${a.quantity}株',
                            onTap: () => context.go('/home/portfolio'),
                          ),
                        )
                        .toList(),
                  ),
                );
              },
            ),
            const SizedBox(height: 16),

            // ── 日次損益グラフ ──
            const _SectionHeader(title: '日次損益（過去履歴）'),
            const SizedBox(height: 8),
            dailyPnlAsync.when(
              loading: () => const _LoadingCard(height: 180),
              error: (e, _) =>
                  _ErrorCard(onRetry: () => ref.invalidate(dailyPnlProvider)),
              data: (pnlList) => _DailyPnlChart(pnlList: pnlList),
            ),
            const SizedBox(height: 24),
          ],
        ),
      ),
    );
  }
}

// ─────────────────────────────────────────
// 資産サマリーカード
// ─────────────────────────────────────────
class _AssetSummaryCard extends StatelessWidget {
  const _AssetSummaryCard({required this.assets});
  final List<AssetViewResponse> assets;

  @override
  Widget build(BuildContext context) {
    final totalMarketValue = assets.fold(0.0, (s, a) => s + a.marketValue);
    final totalUnrealizedPnl = assets.fold(0.0, (s, a) => s + a.unrealizedPnl);
    final fmtCcy = NumberFormat.currency(symbol: '¥', decimalDigits: 0);
    final pnlSign = totalUnrealizedPnl >= 0 ? '+' : '';

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.cardBg,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.border),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withAlpha(70),
            blurRadius: 16,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '総資産評価額',
            style: TextStyle(color: AppColors.textSecondary, fontSize: 13),
          ),
          const SizedBox(height: 4),
          Text(
            fmtCcy.format(totalMarketValue),
            style: const TextStyle(
              color: AppColors.textPrimary,
              fontSize: 28,
              fontWeight: FontWeight.bold,
              fontFeatures: [FontFeature.tabularFigures()],
            ),
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              const Text(
                '本日評価損益:',
                style: TextStyle(color: AppColors.textSecondary, fontSize: 13),
              ),
              const SizedBox(width: 8),
              PriceText(
                value: totalUnrealizedPnl,
                showSign: true,
                style: const TextStyle(fontSize: 14),
              ),
              const SizedBox(width: 4),
              Text(
                '($pnlSign${totalMarketValue > 0 ? (totalUnrealizedPnl / totalMarketValue * 100).toStringAsFixed(2) : '0.00'}%)',
                style: TextStyle(
                  color: totalUnrealizedPnl >= 0
                      ? AppColors.gain
                      : AppColors.loss,
                  fontSize: 13,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

// ─────────────────────────────────────────
// 日次損益棒グラフ
// ─────────────────────────────────────────
class _DailyPnlChart extends StatelessWidget {
  const _DailyPnlChart({required this.pnlList});
  final List<DailyPnlResponse> pnlList;

  @override
  Widget build(BuildContext context) {
    if (pnlList.isEmpty) {
      return const Center(
        child: Padding(
          padding: EdgeInsets.all(24),
          child: Text(
            '損益データがありません',
            style: TextStyle(color: AppColors.textSecondary),
          ),
        ),
      );
    }

    final recent = pnlList.length > 7
        ? pnlList.sublist(pnlList.length - 7)
        : pnlList;
    final maxAbs = recent
        .map((e) => e.totalPnl.abs())
        .fold(0.0, (a, b) => a > b ? a : b);
    final yMax = maxAbs == 0 ? 1000.0 : maxAbs * 1.3;

    return Container(
      height: 180,
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.cardBg,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.border),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withAlpha(70),
            blurRadius: 14,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: BarChart(
        BarChartData(
          alignment: BarChartAlignment.spaceAround,
          maxY: yMax,
          minY: -yMax,
          barGroups: recent.asMap().entries.map((entry) {
            final i = entry.key;
            final pnl = entry.value.totalPnl;
            return BarChartGroupData(
              x: i,
              barRods: [
                BarChartRodData(
                  toY: pnl,
                  color: pnl >= 0 ? AppColors.gain : AppColors.loss,
                  width: 14,
                  borderRadius: BorderRadius.circular(3),
                ),
              ],
            );
          }).toList(),
          titlesData: FlTitlesData(
            leftTitles: const AxisTitles(
              sideTitles: SideTitles(showTitles: false),
            ),
            rightTitles: const AxisTitles(
              sideTitles: SideTitles(showTitles: false),
            ),
            topTitles: const AxisTitles(
              sideTitles: SideTitles(showTitles: false),
            ),
            bottomTitles: AxisTitles(
              sideTitles: SideTitles(
                showTitles: true,
                getTitlesWidget: (value, meta) {
                  final idx = value.toInt();
                  if (idx < 0 || idx >= recent.length) {
                    return const SizedBox.shrink();
                  }
                  final date = recent[idx].date;
                  final label = date.length >= 5 ? date.substring(5) : date;
                  return Padding(
                    padding: const EdgeInsets.only(top: 4),
                    child: Text(
                      label,
                      style: const TextStyle(
                        color: AppColors.textSecondary,
                        fontSize: 10,
                      ),
                    ),
                  );
                },
              ),
            ),
          ),
          borderData: FlBorderData(show: false),
          gridData: FlGridData(
            show: true,
            drawHorizontalLine: true,
            drawVerticalLine: false,
            getDrawingHorizontalLine: (_) =>
                const FlLine(color: AppColors.border, strokeWidth: 0.5),
          ),
        ),
      ),
    );
  }
}

class _SectionHeader extends StatelessWidget {
  const _SectionHeader({required this.title});
  final String title;

  @override
  Widget build(BuildContext context) => Text(
    title,
    style: const TextStyle(
      color: AppColors.textSecondary,
      fontSize: 13,
      fontWeight: FontWeight.w600,
    ),
  );
}

class _LoadingCard extends StatelessWidget {
  const _LoadingCard({this.height = 120});
  final double height;

  @override
  Widget build(BuildContext context) => Container(
    height: height,
    decoration: BoxDecoration(
      color: AppColors.cardBg,
      borderRadius: BorderRadius.circular(12),
    ),
    child: const Center(
      child: CircularProgressIndicator(color: AppColors.accent),
    ),
  );
}

class _ErrorCard extends StatelessWidget {
  const _ErrorCard({required this.onRetry});
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.all(16),
    decoration: BoxDecoration(
      color: AppColors.cardBg,
      borderRadius: BorderRadius.circular(12),
      border: Border.all(color: AppColors.border),
    ),
    child: Column(
      children: [
        const Icon(Icons.error_outline, color: AppColors.loss, size: 32),
        const SizedBox(height: 8),
        const Text(
          'データを取得できませんでした',
          style: TextStyle(color: AppColors.textSecondary, fontSize: 13),
        ),
        const SizedBox(height: 8),
        TextButton(
          onPressed: onRetry,
          child: const Text('再試行', style: TextStyle(color: AppColors.accent)),
        ),
      ],
    ),
  );
}
