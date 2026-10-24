# BYCE Partner App

Android app for BYCE partner gyms: owners, managers and front-desk staff use it to run day-to-day centre operations. It is written in Kotlin with Jetpack Compose.

> **Status: UI prototype.** Every screen reads from `GymOwnerRepository`, an in-memory object with sample data. There is no backend, login, or persistence yet; all changes are lost when the app restarts. Several integrations shown in the UI (reminders, exports, payments) are simulated.

## Features

| Area | What the owner portal covers |
|---|---|
| Dashboard | Today's check-ins, bookings, members inside, expiring memberships, pending dues |
| Attendance | Check-in list, manual check-in/out, manual override with reason, first-time trial arrival alert |
| QR scanner | CameraX + ML Kit barcode/QR scanning for member check-in |
| Members | Member list, add member, CRM notes, activity timeline |
| Memberships & plans | Membership list, expiring soon, freeze/defer, plan management |
| Bookings | Slot bookings, mark arrived, cancel, reminder email via the Gmail app |
| Payments | Payment ledger, pending dues, POS invoices |
| Gym | Gym profile, opening hours, check-in QR |
| Staff | Staff roster, class sessions |
| Other | Reports, activity log, notifications, settings |

The app also still contains member-facing screens (`ui/home/`), reachable from the owner drawer via "Member App Mode". They are scheduled to move out of this app.

## Project structure

```
app/src/main/java/com/example/
├── MainActivity.kt          # Entry point and top-level screen switching
├── data/
│   ├── models/ByceModels.kt # Data models
│   └── services/GymOwnerRepository.kt  # In-memory sample data (no backend yet)
└── ui/
    ├── owner/               # Partner/owner portal screens
    ├── auth/                # Login / sign-up UI (not connected to a backend)
    ├── onboarding/          # Onboarding pager (currently unreachable)
    ├── home/                # Member-facing screens (to be separated)
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

| Test | Type |
|---|---|
| `app/src/test/.../ExampleUnitTest.kt` | JVM unit test (template) |
| `app/src/test/.../ExampleRobolectricTest.kt` | Robolectric resource test |
| `app/src/test/.../GreetingScreenshotTest.kt` | Roborazzi screenshot of the onboarding screen |
| `app/src/androidTest/.../ExampleInstrumentedTest.kt` | Instrumented test (needs a device or emulator) |

## Before release

These still need to be done before the app can ship:

- Final application ID (currently `com.aistudio.bycepartner.kxmpzq`; it cannot be changed after the first Play Store upload)
- Real authentication with staff roles
- Backend API and local persistence in place of the sample repository
- Turn off cleartext traffic and configure backup rules
- Enable minification and remove unused dependencies
