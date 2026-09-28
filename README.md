# Partime Map Explorer

A sleek, minimal Flutter mobile application dedicated to exploring and testing an interactive dark vector map powered by **MapLibre** and **OpenFreeMap**.

---

## Features

- **Full-Screen Dark Map**: Uses the official [OpenFreeMap Dark Style](https://tiles.openfreemap.org/styles/dark) loaded directly via vector tile style URL.
- **Initial Location**: Automatically centers on **Kerala, India** (`10.8505° N, 76.2711° E`) at zoom level `8.0`.
- **Interactive Gestures**: Smooth pinch-to-zoom, panning, rotation, and tilting.
- **Dedicated Map Controls**: Minimalist floating dark controls for:
  - **Zoom In (+)**
  - **Zoom Out (-)**
  - **My Location** (requests runtime GPS permission, displays current position indicator, and animates camera to user's location)
- **Top Search Bar**: Clean, floating search bar tailored for location queries.
- **Pure Focus**: No auth, no backend, no databases, no job pins, or cluttered tabs.

---

## Architecture & Project Structure

```
lib/
├── main.dart                      # App entry point & system UI overlay styling
├── app.dart                       # MaterialApp with dark theme configuration
└── map/
    ├── map_screen.dart            # Full-screen MapLibreMap & UI overlays
    ├── map_controller.dart        # Controller for camera, zoom, and GPS tracking
    └── widgets/
        ├── map_controls.dart      # Zoom in/out floating buttons
        ├── location_button.dart   # "My Location" GPS button with loading state
        └── search_bar.dart        # Minimal floating place search bar
```

---

## How It Works

```
Flutter App (MapScreen)
        ↓
    MapLibre (maplibre_gl)
        ↓
   OpenFreeMap (https://tiles.openfreemap.org/styles/dark)
        ↓
Interactive Dark Vector Map
```

---

## Running the Application

### Prerequisites
- [Flutter SDK](https://docs.flutter.dev/get-started/install) (3.10.0 or higher)
- Android Studio / Xcode / Chrome for testing

### Steps
1. Fetch dependencies:
   ```bash
   flutter pub get
   ```

2. Run the application:
   ```bash
   flutter run
   ```

### Permissions Configured
- **Android (`AndroidManifest.xml`)**:
  - `ACCESS_FINE_LOCATION`
  - `ACCESS_COARSE_LOCATION`
  - `INTERNET`
  - `minSdkVersion`: 21 (required by MapLibre)
- **iOS (`Info.plist`)**:
  - `NSLocationWhenInUseUsageDescription`
  - `NSLocationAlwaysAndWhenInUseUsageDescription`
