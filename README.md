# Prayer Reminder & Timetable App 🕌

An offline, zero-internet Android application designed for accurate prayer times, azan/jamaat reminders, and home-screen Glance widgets based on local mosque timetables.

Built with **Jetpack Compose**, **Material 3**, **Jetpack Glance (Widgets)**, **ML Kit Text Recognition**, and **Kotlin Coroutines / Flow**.

---

## ✨ Features

- **🛡️ 100% Offline & Private (Zero-Internet Policy):**
  - No `android.permission.INTERNET` requested.
  - All processing (OCR, scheduling, audio playback, widget updates) occurs strictly on-device.
  - Zero analytics, trackers, or remote data transmission.

- **📸 Intelligent Timetable Scanner (On-Device OCR):**
  - Scans printed monthly mosque prayer timetables directly from camera or gallery photos using Google ML Kit.
  - Advanced **Voronoi Column Partitioning** & **Midpoint Coordinate Clustering** to accurately handle rotated, skewed, or varying-contrast photos.
  - Header row isolation, chronological 6-prayer sequential mapping (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha), and interactive review editor.

- **📱 Home Screen Glance Widgets:**
  - **Wide Widget (4×2):** Displays place name, next prayer header, big time, jamaat subtitle, live countdown chronometer, and full 5-prayer table rows with active prayer highlight.
  - **Small Widget (2×2):** Compact next prayer card with place, time, and live countdown.
  - Supports Android 12+ dynamic resizing, dark/light themes, and full RTL layout mirroring for Arabic and Urdu.

- **⏰ High-Precision Alarms & Reminders:**
  - Uses `AlarmManager.setExactAndAllowWhileIdle()` to ensure alerts fire on time even in Android Doze mode.
  - Customizable pre-azan, azan, and pre-jamaat alerts (gentle chime, full voice azan, vibration).
  - Background audio playback handled safely with foreground service notifications.

- **🌍 Multi-Language Support (i18n):**
  - English (`en`), Arabic (`ar`), and Urdu (`ur`).
  - Full bidirectional UI support with RTL layout direction and localized prayer and month names.

---

## 🛠️ Architecture & Tech Stack

- **UI:** Jetpack Compose, Material 3, Jetpack Glance (AppWidgets)
- **Language:** Kotlin 1.9+, Java 17
- **Architecture:** Clean Architecture + Unidirectional Data Flow (UDF)
  - `domain`: Parsers, Validators, Schedule & Next-Prayer Resolvers
  - `data`: Disk-persisted JSON repositories, Encrypted SharedPreferences
  - `reminders`: Exact Alarm scheduling, WorkManager daily maintenance
  - `importing`: ML Kit Text Recognition, image pre-processing
  - `widget`: Jetpack Glance widgets, RemoteViews countdown chronometer
  - `audio`: MediaPlayer & foreground voice playback service
- **Testing:** Comprehensive JUnit 4 test suites covering parsers, schedule resolvers, and i18n rules.

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34 (Android 14) / SDK 35 (Android 15)
- JDK 17

### Gradle Commands
```bash
# Run all unit tests
./gradlew test

# Build debug APK
./gradlew assembleDebug

# Build optimized release APK
./gradlew assembleRelease
```

---

## 📄 License
Open source and available under the [MIT License](LICENSE).
