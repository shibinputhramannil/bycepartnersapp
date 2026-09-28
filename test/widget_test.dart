import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:partime_map_explorer/map/map_controller.dart';
import 'package:partime_map_explorer/map/widgets/location_button.dart';
import 'package:partime_map_explorer/map/widgets/map_controls.dart';
import 'package:partime_map_explorer/map/widgets/search_bar.dart';

void main() {
  test('MapScreenController initial values are correct', () {
    final controller = MapScreenController();
    expect(controller.isLocating, isFalse);
    expect(controller.locationPermissionGranted, isFalse);
    expect(
      MapScreenController.openFreeMapDarkStyleUrl,
      'https://tiles.openfreemap.org/styles/dark',
    );
    expect(MapScreenController.keralaLocation.latitude, closeTo(10.8505, 0.0001));
    expect(MapScreenController.keralaLocation.longitude, closeTo(76.2711, 0.0001));
    expect(MapScreenController.initialZoomLevel, 8.0);
  });

  testWidgets('MapControls renders zoom in and zoom out buttons', (WidgetTester tester) async {
    bool zoomedIn = false;
    bool zoomedOut = false;

    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: MapControls(
            onZoomIn: () => zoomedIn = true,
            onZoomOut: () => zoomedOut = true,
          ),
        ),
      ),
    );

    expect(find.byIcon(Icons.add), findsOneWidget);
    expect(find.byIcon(Icons.remove), findsOneWidget);

    await tester.tap(find.byIcon(Icons.add));
    expect(zoomedIn, isTrue);

    await tester.tap(find.byIcon(Icons.remove));
    expect(zoomedOut, isTrue);
  });

  testWidgets('LocationButton renders and responds to tap', (WidgetTester tester) async {
    bool tapped = false;

    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: LocationButton(
            onPressed: () => tapped = true,
          ),
        ),
      ),
    );

    expect(find.byType(LocationButton), findsOneWidget);
    await tester.tap(find.byType(LocationButton));
    expect(tapped, isTrue);
  });

  testWidgets('MapSearchBar enters text and triggers callbacks', (WidgetTester tester) async {
    String query = '';

    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: MapSearchBar(
            onChanged: (val) => query = val,
          ),
        ),
      ),
    );

    expect(find.byType(MapSearchBar), findsOneWidget);
    await tester.enterText(find.byType(TextField), 'Kochi');
    expect(query, 'Kochi');
  });
}
