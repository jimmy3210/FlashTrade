import 'package:flutter/material.dart';
import '../../core/constants/app_colors.dart';

class LoadingOverlay extends StatelessWidget {
  const LoadingOverlay({
    super.key,
    required this.isLoading,
    required this.child,
  });

  final bool isLoading;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        child,
        if (isLoading)
          const ColoredBox(
            color: Color(0x88000000),
            child: SizedBox.expand(
              child: Center(
                child: CircularProgressIndicator(color: AppColors.accent),
              ),
            ),
          ),
      ],
    );
  }
}
