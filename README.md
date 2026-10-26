# BYCE Partner App

Android app for BYCE partner gyms: owners, managers and front-desk staff use it to run day-to-day centre operations. It is written in Kotlin with Jetpack Compose.

BYCE is an aggregator: BYCE owns pass tiers, pricing, member payments, pass pausing and gym listings. This app covers what happens at the gym (check-ins, attendance, bookings, members, staff and sessions) and shows BYCE-owned information read-only.

> **Status: UI prototype with sample data.** Every screen reads from `GymOwnerRepository`, an in-memory object filled with sample data. There is no backend, real login, or persistence yet, so **every change resets when the app restarts**. Dashboard figures and payment views are labelled "Demo data—not live" in the app. Login is a demo: the app opens on a phone-number login, but **no SMS is sent and no server checks the code**. Any valid phone number is accepted and the demo code `123456` logs in (the app labels this on screen). There are no push notifications. Booking reminders are local notifications on the staff member's own phone (see below); members are not notified.

## Features

| Area | What the app covers |
|---|---|
| Login | Opens first. Fitness photo carousel, country code + phone number, six-digit OTP screen (demo code `123456`), "Login successful" confirmation, then the dashboard. Auth logic lives behind `PhoneAuthService`; `DemoPhoneAuthService` is the only implementation. |
| Dashboard | Today's operations, KPIs, occupancy, revenue chart, expiring memberships, recent activity. Sample figures are labelled "Demo data—not live". |
| Check-in | QR/barcode scanning (CameraX + ML Kit), manual check-in (existing members only), manual override with a logged reason, first-time trial arrival alert |
| Check-in rules | Active passes check in; expired or pending passes need a logged reason; frozen passes are blocked unless staff override with a reason; unknown members, unrecognized statuses, cancelled bookings and duplicates are always blocked |
| Attendance | Today's check-ins, members currently inside, completed visits, check-out |
| Members | Member list, add member, CRM notes, activity timeline, last check-in |
| Memberships | Membership list and status, expiring soon. Pausing is handled by BYCE; the app only shows whether a pass is paused. |
| Accepted Passes | Read-only list of the BYCE passes that give entry at this gym. Tiers and pricing are managed by BYCE. |
| Bookings | Slot bookings, mark arrived (same check-in rules), cancel, change shift. Staff reminders: a local notification on this device 30 minutes before each upcoming booking (not sent to members). |
| Payments | Read-only view of member payments and pending dues, labelled as demo data. Members pay BYCE; there is no POS, payment collection, or "record paid" in this app. |
| Gym | Gym profile (contact details, address and coordinates), opening hours and open/closed status, read-only BYCE listing status, check-in QR |
| Staff | Staff roster, class sessions, assign a substitute coach |
| Other | Activity log, notifications, settings |

**Not available yet** (shown in the app as "Not available yet"): file exports, QR print/download, SMS/WhatsApp and renewal reminders, and password change.

Member-facing screens and the Flutter map demo are not part of this app. Earlier versions are preserved on the `member-app` and `flutter-map-demo` branches.

## Project structure

```
app/src/main/java/com/example/
├── MainActivity.kt          # Entry point: login screen and owner portal
├── data/
│   ├── models/ByceModels.kt # Data models, member statuses, check-in rule types
│   └── services/
│       ├── GymOwnerRepository.kt  # In-memory sample data and check-in rules (no backend yet)
│       └── PhoneAuthService.kt    # Phone validation, OTP auth interface and demo implementation
└── ui/
    ├── owner/               # Partner portal screens
    ├── auth/                # Phone + OTP login UI (demo; not connected to a backend)
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
| `PhoneAuthTest` | Phone number validation, OTP format, demo code verification |
| `LoginFlowUiTest`, `AppStartupUiTest` | Login screen contents, phone errors, OTP screen and back navigation, wrong/correct demo code, app opens on login and reaches the dashboard |
| `BookingReminderPlannerTest` | Reminder time (30 minutes before start), no reminder for cancelled, completed or past bookings |
| `BookingReminderSchedulerTest` | Alarms scheduled, replaced on date/time/shift change, cancelled on cancel/complete; notification channel and permission check |
| `BookingRemindersUiTest` | Bookings screen has no Gmail reminders, says reminders are for staff on this device, asks for notification permission |
| `OwnerDashboardScreenshotTest`, `LoginScreenshotTest` | Renders the owner dashboard and the login/OTP screens |
| `ExampleUnitTest`, `ExampleRobolectricTest` | Template tests |
| `app/src/androidTest/.../ExampleInstrumentedTest.kt` | Instrumented test (needs a device or emulator) |

Camera scanning, the flip-camera and torch buttons, and the screens' appearance have not been verified on a device.

## Booking reminders

Staff get a local notification on their own phone 30 minutes before each upcoming booking. It is not a push notification and nothing is sent to members.

- `BookingReminderPlanner` decides the reminder time. Cancelled, completed and past bookings get none, and neither does a booking whose reminder time has already passed.
- `BookingReminderScheduler` sets an `AlarmManager` alarm for each booking. `MainActivity` installs it at launch, and `GymOwnerRepository.onBookingsChanged` re-syncs it whenever a booking is cancelled, completed, moved to another shift, or rescheduled (`rescheduleBooking`; there is no reschedule screen yet).
- `BookingReminderReceiver` posts the notification on the "Booking reminders" channel. Member names are hidden on the lock screen.
- Android 13+ asks for notification permission from the Bookings screen. Without it, or with notifications turned off, no reminders appear and the screen says so.
- Reminders are exact on Android 11 and older. On Android 12+ the app doesn't request the exact-alarm permission, so Android may deliver a reminder a few minutes late.

**Limits until bookings are stored on a backend:** the bookings are in-memory sample data, so reminders only cover those sample bookings, and they are rebuilt from sample data on every launch. Alarms are cleared when the phone restarts and come back the next time the app opens. Don't rely on these reminders for real bookings yet.

## Release configuration

- Release builds use R8 code shrinking and resource shrinking. Test release builds on a device after changes, and add keep rules to `app/proguard-rules.pro` if something breaks.
- `POST_NOTIFICATIONS` is requested for local booking reminders only.
- The app requests no network permissions until backend networking is added. ML Kit's analytics library merges `INTERNET` in, so the app manifest removes it explicitly; restore both when adding a backend.
- Backup and device-to-device transfer are disabled, and cleartext traffic is off.
- Keystores, passwords and signing property files are gitignored. Never commit them.

## Before release

These still need to be done before the app can ship:

- Final application ID and code namespace (currently `com.aistudio.bycepartner.kxmpzq` and `com.example`). The ID cannot be changed after the first Play Store upload, and it should be based on a domain BYCE owns.
- Real authentication with staff roles: implement `PhoneAuthService` against an SMS/OTP backend and pass it to `AuthScreen`
- Backend API and local persistence in place of the sample repository, then remove the demo-data labels
- Verify the R8 release build on a device

## Image credits

The login carousel photos are CC0 (public domain dedication) images from Wikimedia Commons, resized for the app:

| File | Source | Author |
|---|---|---|
| `drawable-nodpi/login_slide_barbell.jpg` | [Man lifting a heavy barbell](https://commons.wikimedia.org/wiki/File:Man_lifting_a_heavy_barbell.jpg) | Binyamin Mellish |
| `drawable-nodpi/login_slide_gym_floor.jpg` | [Fitness Center Equipment](https://commons.wikimedia.org/wiki/File:Fitness_Center_Equipment.jpg) | Teddyhtsai |
| `drawable-nodpi/login_slide_dumbbells.jpg` | [Pair of 8kg dumbbells (Unsplash)](https://commons.wikimedia.org/wiki/File:Pair_of_8kg_dumbbells_(Unsplash).jpg) | Cyril Saulnier |
