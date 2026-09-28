import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:maplibre_gl/maplibre_gl.dart';

class MapScreenController extends ChangeNotifier {
  // OpenFreeMap dark style URL
  static const String openFreeMapDarkStyleUrl =
      'https://tiles.openfreemap.org/styles/dark';

  // Kerala, India coordinates
  static const LatLng keralaLocation = LatLng(10.8505, 76.2711);
  static const double initialZoomLevel = 8.0;

  MapLibreMapController? _mapController;
  bool _isLocating = false;
  bool _locationPermissionGranted = false;

  MapLibreMapController? get mapController => _mapController;
  bool get isLocating => _isLocating;
  bool get locationPermissionGranted => _locationPermissionGranted;

  void onMapCreated(MapLibreMapController controller) {
    _mapController = controller;
    notifyListeners();
  }

  /// Zoom in by 1 level
  Future<void> zoomIn() async {
    if (_mapController == null) return;
    await _mapController!.animateCamera(
      CameraUpdate.zoomIn(),
    );
  }

  /// Zoom out by 1 level
  Future<void> zoomOut() async {
    if (_mapController == null) return;
    await _mapController!.animateCamera(
      CameraUpdate.zoomOut(),
    );
  }

  /// Center back on Kerala, India
  Future<void> resetToKerala() async {
    if (_mapController == null) return;
    await _mapController!.animateCamera(
      CameraUpdate.newCameraPosition(
        const CameraPosition(
          target: keralaLocation,
          zoom: initialZoomLevel,
        ),
      ),
    );
  }

  /// Request permission and navigate smoothly to the user's current GPS location
  Future<void> moveToCurrentLocation({BuildContext? context}) async {
    if (_mapController == null || _isLocating) return;

    _isLocating = true;
    notifyListeners();

    try {
      // Check if location services are enabled on device
      final serviceEnabled = await Geolocator.isLocationServiceEnabled();
      if (!serviceEnabled) {
        if (context != null && context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Location services are disabled. Please enable GPS.'),
              backgroundColor: Color(0xFF262626),
            ),
          );
        }
        _isLocating = false;
        notifyListeners();
        return;
      }

      // Check and request location permissions
      var permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
        if (permission == LocationPermission.denied) {
          if (context != null && context.mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                content: Text('Location permission was denied.'),
                backgroundColor: Color(0xFF262626),
              ),
            );
          }
          _isLocating = false;
          notifyListeners();
          return;
        }
      }

      if (permission == LocationPermission.deniedForever) {
        if (context != null && context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text(
                'Location permissions are permanently denied. Please enable them in system settings.',
              ),
              backgroundColor: Color(0xFF262626),
            ),
          );
        }
        _isLocating = false;
        notifyListeners();
        return;
      }

      _locationPermissionGranted = true;
      notifyListeners();

      // Retrieve high-accuracy current location
      final position = await Geolocator.getCurrentPosition(
        locationSettings: const LocationSettings(
          accuracy: LocationAccuracy.high,
          timeLimit: Duration(seconds: 10),
        ),
      );

      final userLatLng = LatLng(position.latitude, position.longitude);

      await _mapController!.animateCamera(
        CameraUpdate.newCameraPosition(
          CameraPosition(
            target: userLatLng,
            zoom: 15.0,
          ),
        ),
      );
    } catch (e) {
      if (context != null && context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to get current location: $e'),
            backgroundColor: const Color(0xFF262626),
          ),
        );
      }
    } finally {
      _isLocating = false;
      notifyListeners();
    }
  }

  @override
  void dispose() {
    _mapController = null;
    super.dispose();
  }
}
