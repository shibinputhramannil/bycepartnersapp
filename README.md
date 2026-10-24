# BYCE Partner App

Android app for BYCE partner gyms: owners, managers and front-desk staff use it to run day-to-day centre operations. It is written in Kotlin with Jetpack Compose.

BYCE is an aggregator: BYCE owns pass tiers, pricing, member payments, pass pausing and gym listings. This app covers what happens at the gym (check-ins, attendance, bookings, members, staff and sessions) and shows BYCE-owned information read-only.

> **Status: UI prototype with sample data.** Every screen reads from `GymOwnerRepository`, an in-memory object filled with sample data. There is no backend, real login, or persistence yet, so **every change resets when the app restarts**. Dashboard figures and payment views are labelled "Demo data—not live" in the app. Login is not real: any phone number with a password of 6 or more characters gets in.

## Features

| Area | What the app covers |
|---|---|
| Dashboard | Today's operations, KPIs, occupancy, revenue chart, expiring memberships, recent activity. Sample figures are labelled "Demo data—not live". |
| Check-in | QR/barcode scanning (CameraX + ML Kit), manual check-in (existing members only), manual override with a logged reason, first-time trial arrival alert |
| Check-in rules | Active passes check in; expired or pending passes need a logged reason; frozen passes are blocked unless staff override with a reason; unknown members, unrecognized statuses, cancelled bookings and duplicates are always blocked |
| Attendance | Today's check-ins, members currently inside, completed visits, check-out |
| Members | Member list, add member, CRM notes, activity timeline, last check-in |
| Memberships | Membership list and status, expiring soon. Pausing is handled by BYCE; the app only shows whether a pass is paused. |
| Accepted Passes | Read-only list of the BYCE passes that give entry at this gym. Tiers and pricing are managed by BYCE. |
| Bookings | Slot bookings, mark arrived (same check-in rules), cancel, open a Gmail draft to remind a member |
| Payments | Read-only view of member payments and pending dues, labelled as demo data. Members pay BYCE; there is no POS, payment collection, or "record paid" in this app. |
| Gym | Gym profile, opening hours and open/closed status, read-only BYCE listing status, location details, check-in QR |
| Staff | Staff roster, class sessions, assign a substitute coach |
| Other | Activity log, notifications, settings |

**Not available yet** (shown in the app as "Not available yet"): file exports, QR print/download, SMS/WhatsApp and renewal reminders, password change, and a map view.

Member-facing screens and the Flutter map demo are not part of this app. Earlier versions are preserved on the `member-app` and `flutter-map-demo` branches.

## Project structure

```
app/src/main/java/com/example/
├── MainActivity.kt          # Entry point: login screen and owner portal
├── data/
│   ├── models/ByceModels.kt # Data models, member statuses, check-in rule types
│   └── services/GymOwnerRepository.kt  # In-memory sample data and check-in rules (no backend yet)
└── ui/
    ├── owner/               # Partner portal screens
    ├── auth/                # Login / sign-up UI (not connected to a backend)
    ├── components/          # Shared Compose components
    └── theme/               # Colours, typography, theme
```

## Requirements

- Android Studio with the bundled JDK (JDK 17 or newer)
- Android SDK 36
- Min SDK 24

## Building

The debug build is signed with `debug.keystore` in the project root. That file is gitignored, so a fresh clone needs one:

```
keytool -genkeypair -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
```

Then build and run the unit tests:

```
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

### Release signing

Release builds read the upload key from environment variables:

| Variable | Purpose |
|---|---|
| `KEYSTORE_PATH` | Path to the upload keystore (defaults to `my-upload-key.jks` in the project root) |
| `STORE_PASSWORD` | Keystore password |
| `KEY_PASSWORD` | Password for the `upload` key alias |

Never commit keystores or passwords.

## Tests

Unit tests run on the JVM; the UI tests use Robolectric and need no device.

| Test | Covers |
|---|---|
| `CheckInRulesTest` | Check-in rules for each member status, bookings, overrides and duplicates |
| `ScannerPayloadTest` | Scanned codes only match real members or bookings |
| `AggregatorScopeTest`, `AggregatorScopeUiTest` | Accepted passes, read-only payments and listing status, demo-data labels, no freeze/POS tools |
| `PartnerScopeTest`, `PartnerCleanupUiTest` | Menu routes and badges, removed prototype features stay removed |
| `ListRefreshUiTest` | Customer list updates after adding a member |
| `ChartSafetyTest` | Charts handle zero and empty values |
| `GymTabRoutingTest`, `AssignCoachTest` | Gym tabs; substitute coach selection |
| `OwnerDashboardScreenshotTest` | Renders the owner dashboard |
| `ExampleUnitTest`, `ExampleRobolectricTest` | Template tests |
| `app/src/androidTest/.../ExampleInstrumentedTest.kt` | Instrumented test (needs a device or emulator) |

Camera scanning, the flip-camera and torch buttons, and the screens' appearance have not been verified on a device.

## Release configuration

- Release builds use R8 code shrinking and resource shrinking. Test release builds on a device after changes, and add keep rules to `app/proguard-rules.pro` if something breaks.
- The app requests no network permissions until backend networking is added. ML Kit's analytics library merges `INTERNET` in, so the app manifest removes it explicitly; restore both when adding a backend.
- Backup and device-to-device transfer are disabled, and cleartext traffic is off.
- Keystores, passwords and signing property files are gitignored. Never commit them.

## Before release

These still need to be done before the app can ship:

- Final application ID and code namespace (currently `com.aistudio.bycepartner.kxmpzq` and `com.example`). The ID cannot be changed after the first Play Store upload, and it should be based on a domain BYCE owns.
- Real authentication with staff roles
- Backend API and local persistence in place of the sample repository, then remove the demo-data labels
- Verify the R8 release build on a device
