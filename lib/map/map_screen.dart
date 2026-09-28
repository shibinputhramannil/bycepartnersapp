import 'dart:math';
import 'package:flutter/material.dart';
import 'package:maplibre_gl/maplibre_gl.dart';
import 'map_controller.dart';
import 'widgets/location_button.dart';
import 'widgets/map_controls.dart';
import 'widgets/search_bar.dart';

class MapScreen extends StatefulWidget {
  const MapScreen({super.key});

  @override
  State<MapScreen> createState() => _MapScreenState();
}

class _MapScreenState extends State<MapScreen> {
  late final MapScreenController _controller;
  bool _isMapReady = false;

  @override
  void initState() {
    super.initState();
    _controller = MapScreenController();
    _controller.addListener(_onControllerUpdate);
  }

  void _onControllerUpdate() {
    if (mounted) {
      setState(() {});
    }
  }

  @override
  void dispose() {
    _controller.removeListener(_onControllerUpdate);
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF141414),
      body: Stack(
        fit: StackFit.expand,
        children: [
          // Full-screen MapLibre interactive map with OpenFreeMap Dark style
          MapLibreMap(
            styleString: MapScreenController.openFreeMapDarkStyleUrl,
            initialCameraPosition: const CameraPosition(
              target: MapScreenController.keralaLocation,
              zoom: MapScreenController.initialZoomLevel,
            ),
            myLocationEnabled: _controller.locationPermissionGranted,
            myLocationTrackingMode: _controller.locationPermissionGranted
                ? MyLocationTrackingMode.tracking
                : MyLocationTrackingMode.none,
            myLocationRenderMode: MyLocationRenderMode.normal,
            rotateGesturesEnabled: true,
            scrollGesturesEnabled: true,
            zoomGesturesEnabled: true,
            tiltGesturesEnabled: true,
            compassEnabled: true,
            compassViewPosition: CompassViewPosition.topRight,
            compassViewMargins: const Point(16, 120),
            attributionButtonPosition: AttributionButtonPosition.bottomLeft,
            attributionButtonMargins: const Point(16, 16),
            onMapCreated: (mapController) {
              _controller.onMapCreated(mapController);
            },
            onStyleLoadedCallback: () {
              setState(() {
                _isMapReady = true;
              });
            },
          ),

          // Map loading placeholder
          if (!_isMapReady)
            Container(
              color: const Color(0xFF141414),
              child: const Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    SizedBox(
                      width: 28,
                      height: 28,
                      child: CircularProgressIndicator(
                        strokeWidth: 2.5,
                        color: Color(0xFF60A5FA),
                      ),
                    ),
                    SizedBox(height: 16),
                    Text(
                      'Loading OpenFreeMap Dark...',
                      style: TextStyle(
                        color: Color(0xFFAAAAAA),
                        fontSize: 13,
                        fontWeight: FontWeight.w400,
                        letterSpacing: 0.2,
                      ),
                    ),
                  ],
                ),
              ),
            ),

          // Top Search Bar
          SafeArea(
            child: Align(
              alignment: Alignment.topCenter,
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                child: MapSearchBar(
                  onSubmitted: (query) {
                    if (query.trim().isEmpty) return;
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content: Text('Search for "$query" (UI preview)'),
                        duration: const Duration(seconds: 2),
                        backgroundColor: const Color(0xFF262626),
                      ),
                    );
                  },
                ),
              ),
            ),
          ),

          // Map Controls & "My Location" Button (Bottom Right)
          SafeArea(
            child: Align(
              alignment: Alignment.bottomRight,
              child: Padding(
                padding: const EdgeInsets.only(right: 16, bottom: 24),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    // "My Location" Button
                    LocationButton(
                      isLocating: _controller.isLocating,
                      hasPermission: _controller.locationPermissionGranted,
                      onPressed: () => _controller.moveToCurrentLocation(context: context),
                    ),
                    const SizedBox(height: 12),

                    // Zoom In & Zoom Out Controls
                    MapControls(
                      onZoomIn: _controller.zoomIn,
                      onZoomOut: _controller.zoomOut,
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
