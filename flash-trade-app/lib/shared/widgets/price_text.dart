import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../core/constants/app_colors.dart';

class PriceText extends StatelessWidget {
  const PriceText({
    super.key,
    required this.value,
    this.decimals = 2,
    this.showSign = false,
    this.style,
    this.suffix,
  });

  final double value;
  final int decimals;
  final bool showSign;
  final TextStyle? style;
  final String? suffix;

  @override
  Widget build(BuildContext context) {
    final isPositive = value >= 0;
    final color = value == 0
        ? AppColors.textSecondary
        : isPositive
        ? AppColors.gain
        : AppColors.loss;
    final sign = showSign ? (isPositive ? '+' : '') : '';
    final fmt = NumberFormat.decimalPatternDigits(decimalDigits: decimals);
    final text = '$sign${fmt.format(value)}${suffix ?? ''}';

    return Text(
      text,
      style: (style ?? const TextStyle(fontSize: 14)).copyWith(
        color: color,
        fontFeatures: const [FontFeature.tabularFigures()],
      ),
    );
  }
}
