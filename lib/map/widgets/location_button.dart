import 'package:flutter/material.dart';

class LocationButton extends StatelessWidget {
  final VoidCallback onPressed;
  final bool isLocating;
  final bool hasPermission;

  const LocationButton({
    super.key,
    required this.onPressed,
    this.isLocating = false,
    this.hasPermission = false,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: const Color(0xDD1E1E1E),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: hasPermission ? const Color(0x553B82F6) : const Color(0x33FFFFFF),
          width: 0.8,
        ),
        boxShadow: const [
          BoxShadow(
            color: Colors.black45,
            blurRadius: 10,
            offset: Offset(0, 4),
          ),
        ],
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: isLocating ? null : onPressed,
          borderRadius: BorderRadius.circular(12),
          child: Tooltip(
            message: 'My Location',
            child: SizedBox(
              width: 44,
              height: 44,
              child: Center(
                child: isLocating
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(
                          strokeWidth: 2.2,
                          color: Color(0xFF60A5FA),
                        ),
                      )
                    : Icon(
                        hasPermission ? Icons.my_location : Icons.location_searching,
                        color: hasPermission ? const Color(0xFF60A5FA) : Colors.white,
                        size: 22,
                      ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
