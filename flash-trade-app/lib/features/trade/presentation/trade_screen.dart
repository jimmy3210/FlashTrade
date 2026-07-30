import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/constants/app_colors.dart';
import '../../../shared/widgets/loading_overlay.dart';
import '../../../l10n/app_localizations.dart';
import '../../orders/providers/orders_provider.dart';
import '../providers/trade_provider.dart';

class TradeScreen extends ConsumerStatefulWidget {
  const TradeScreen({super.key});

  @override
  ConsumerState<TradeScreen> createState() => _TradeScreenState();
}

class _TradeScreenState extends ConsumerState<TradeScreen> {
  final _symbolController = TextEditingController();
  final _quantityController = TextEditingController();
  final _priceController = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final symbol = ref.read(selectedSymbolProvider);
      if (symbol.isNotEmpty) {
        _symbolController.text = symbol;
        ref.read(tradeFormProvider.notifier).setSymbol(symbol);
        ref.read(selectedSymbolProvider.notifier).state = '';
      }
    });
  }

  @override
  void dispose() {
    _symbolController.dispose();
    _quantityController.dispose();
    _priceController.dispose();
    super.dispose();
  }

  Future<void> _confirm() async {
    if (!_formKey.currentState!.validate()) return;
    final form = ref.read(tradeFormProvider);
    final l10n = AppLocalizations.of(context);

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppColors.cardBg,
        title: Text(
          l10n.tradeConfirmTitle(
            form.side == 'BUY' ? l10n.tradeBuy : l10n.tradeSell,
          ),
          style: const TextStyle(color: AppColors.textPrimary),
        ),
        content: Text(
          form.orderType == 'LIMIT'
              ? l10n.tradeConfirmBodyLimit(
                  form.side == 'BUY' ? l10n.tradeBuy : l10n.tradeSell,
                  form.symbol,
                  form.quantity,
                  form.price,
                )
              : l10n.tradeConfirmBodyMarket(
                  form.side == 'BUY' ? l10n.tradeBuy : l10n.tradeSell,
                  form.symbol,
                  form.quantity,
                ),
          style: const TextStyle(color: AppColors.textSecondary, height: 1.5),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: Text(
              l10n.tradeCancel,
              style: const TextStyle(color: AppColors.textSecondary),
            ),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(ctx, true),
            style: ElevatedButton.styleFrom(
              backgroundColor: form.side == 'BUY'
                  ? AppColors.gain
                  : AppColors.loss,
            ),
            child: Text(
              form.side == 'BUY' ? l10n.tradeSubmitBuy : l10n.tradeSubmitSell,
            ),
          ),
        ],
      ),
    );

    if (confirmed != true) return;

    final success = await ref.read(tradeFormProvider.notifier).submitOrder();
    if (!mounted) return;

    if (success) {
      _symbolController.clear();
      _quantityController.clear();
      _priceController.clear();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(l10n.tradeSuccess),
          backgroundColor: AppColors.gain,
        ),
      );
      ref.invalidate(ordersProvider);
      context.go('/home/orders');
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            ref.read(tradeFormProvider).errorMessage ?? l10n.tradeFailed,
          ),
          backgroundColor: AppColors.loss,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final form = ref.watch(tradeFormProvider);

    return Scaffold(
      backgroundColor: AppColors.primaryBg,
      appBar: AppBar(
        backgroundColor: AppColors.primaryBg,
        title: Text(
          l10n.tradeTitle,
          style: const TextStyle(color: AppColors.textPrimary),
        ),
      ),
      body: LoadingOverlay(
        isLoading: form.isLoading,
        child: SafeArea(
          child: Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 520),
              child: SingleChildScrollView(
                padding: const EdgeInsets.all(20),
                child: Form(
                  key: _formKey,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      // ── 銘柄入力 ──
                      _SectionLabel(l10n.tradeSymbol),
                      const SizedBox(height: 6),
                      TextFormField(
                        controller: _symbolController,
                        style: const TextStyle(
                          color: AppColors.textPrimary,
                          fontSize: 18,
                        ),
                        textCapitalization: TextCapitalization.characters,
                        decoration: _inputDecoration(l10n.tradeSymbolHint),
                        inputFormatters: [
                          FilteringTextInputFormatter.allow(
                            RegExp(r'[A-Za-z]'),
                          ),
                          LengthLimitingTextInputFormatter(5),
                        ],
                        onChanged: (v) =>
                            ref.read(tradeFormProvider.notifier).setSymbol(v),
                        validator: (v) {
                          if (v == null || v.isEmpty) {
                            return l10n.tradeValidSymbolEmpty;
                          }
                          if (!RegExp(r'^[A-Z]{1,5}$').hasMatch(v)) {
                            return l10n.tradeValidSymbolFormat;
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 20),

                      // ── 売買方向 ──
                      _SectionLabel(l10n.tradeSide),
                      const SizedBox(height: 8),
                      _SegmentedRow(
                        options: const ['BUY', 'SELL'],
                        labels: [l10n.tradeBuy, l10n.tradeSell],
                        selected: form.side,
                        activeColors: const [AppColors.gain, AppColors.loss],
                        onChanged: (v) =>
                            ref.read(tradeFormProvider.notifier).setSide(v),
                      ),
                      const SizedBox(height: 20),

                      // ── 注文種別 ──
                      _SectionLabel(l10n.tradeOrderType),
                      const SizedBox(height: 8),
                      _SegmentedRow(
                        options: const ['MARKET', 'LIMIT'],
                        labels: [l10n.tradeMarket, l10n.tradeLimit],
                        selected: form.orderType,
                        activeColors: const [
                          AppColors.accent,
                          AppColors.accent,
                        ],
                        onChanged: (v) => ref
                            .read(tradeFormProvider.notifier)
                            .setOrderType(v),
                      ),
                      const SizedBox(height: 20),

                      // ── 数量 ──
                      _SectionLabel(l10n.tradeQuantity),
                      const SizedBox(height: 6),
                      TextFormField(
                        controller: _quantityController,
                        style: const TextStyle(color: AppColors.textPrimary),
                        keyboardType: TextInputType.number,
                        inputFormatters: [
                          FilteringTextInputFormatter.digitsOnly,
                        ],
                        decoration: _inputDecoration(l10n.tradeQuantityHint),
                        onChanged: (v) =>
                            ref.read(tradeFormProvider.notifier).setQuantity(v),
                        validator: (v) {
                          if (v == null || v.isEmpty) {
                            return l10n.tradeValidQtyEmpty;
                          }
                          final n = int.tryParse(v);
                          if (n == null || n < 1) {
                            return l10n.tradeValidQtyMin;
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 20),

                      // ── 価格（LIMIT時のみ） ──
                      AnimatedOpacity(
                        opacity: form.orderType == 'LIMIT' ? 1 : 0.4,
                        duration: const Duration(milliseconds: 200),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: [
                            _SectionLabel(l10n.tradePrice),
                            const SizedBox(height: 6),
                            TextFormField(
                              controller: _priceController,
                              style: const TextStyle(
                                color: AppColors.textPrimary,
                              ),
                              keyboardType:
                                  const TextInputType.numberWithOptions(
                                    decimal: true,
                                  ),
                              enabled: form.orderType == 'LIMIT',
                              decoration: _inputDecoration(l10n.tradePriceHint),
                              onChanged: (v) => ref
                                  .read(tradeFormProvider.notifier)
                                  .setPrice(v),
                              validator: form.orderType == 'LIMIT'
                                  ? (v) {
                                      if (v == null || v.isEmpty) {
                                        return l10n.tradeValidPriceEmpty;
                                      }
                                      final d = double.tryParse(v);
                                      if (d == null || d <= 0) {
                                        return l10n.tradeValidPriceMin;
                                      }
                                      return null;
                                    }
                                  : null,
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 36),

                      // ── 発注ボタン ──
                      SizedBox(
                        height: 54,
                        child: ElevatedButton(
                          onPressed: form.isLoading ? null : _confirm,
                          style: ElevatedButton.styleFrom(
                            backgroundColor: form.side == 'BUY'
                                ? AppColors.gain
                                : AppColors.loss,
                            foregroundColor: AppColors.textPrimary,
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(10),
                            ),
                          ),
                          child: Text(
                            form.side == 'BUY'
                                ? l10n.tradeSubmitBuy
                                : l10n.tradeSubmitSell,
                            style: const TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  InputDecoration _inputDecoration(String hint) => InputDecoration(
    hintText: hint,
    hintStyle: const TextStyle(color: AppColors.textSecondary),
    filled: true,
    fillColor: AppColors.cardBg,
    border: OutlineInputBorder(
      borderRadius: BorderRadius.circular(8),
      borderSide: const BorderSide(color: AppColors.border),
    ),
    enabledBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(8),
      borderSide: const BorderSide(color: AppColors.border),
    ),
    focusedBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(8),
      borderSide: const BorderSide(color: AppColors.accent),
    ),
    disabledBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(8),
      borderSide: BorderSide(color: AppColors.border.withAlpha(100)),
    ),
    errorBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(8),
      borderSide: const BorderSide(color: AppColors.loss),
    ),
    focusedErrorBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(8),
      borderSide: const BorderSide(color: AppColors.loss),
    ),
  );
}

class _SectionLabel extends StatelessWidget {
  const _SectionLabel(this.text);
  final String text;

  @override
  Widget build(BuildContext context) => Text(
    text,
    style: const TextStyle(
      color: AppColors.textSecondary,
      fontSize: 13,
      fontWeight: FontWeight.w600,
    ),
  );
}

class _SegmentedRow extends StatelessWidget {
  const _SegmentedRow({
    required this.options,
    required this.labels,
    required this.selected,
    required this.activeColors,
    required this.onChanged,
  });

  final List<String> options;
  final List<String> labels;
  final String selected;
  final List<Color> activeColors;
  final ValueChanged<String> onChanged;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: List.generate(options.length, (i) {
        final isSelected = selected == options[i];
        final color = activeColors[i];
        return Expanded(
          child: GestureDetector(
            onTap: () => onChanged(options[i]),
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 150),
              padding: const EdgeInsets.symmetric(vertical: 12),
              margin: EdgeInsets.only(left: i == 0 ? 0 : 6),
              decoration: BoxDecoration(
                color: isSelected ? color.withAlpha(50) : AppColors.cardBg,
                borderRadius: BorderRadius.circular(8),
                border: Border.all(
                  color: isSelected ? color : AppColors.border,
                  width: isSelected ? 1.5 : 1,
                ),
              ),
              child: Center(
                child: Text(
                  labels[i],
                  style: TextStyle(
                    color: isSelected ? color : AppColors.textSecondary,
                    fontWeight: isSelected
                        ? FontWeight.bold
                        : FontWeight.normal,
                  ),
                ),
              ),
            ),
          ),
        );
      }),
    );
  }
}
