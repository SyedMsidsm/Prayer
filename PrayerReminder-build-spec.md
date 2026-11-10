# PrayerReminder — Complete Build Specification (v2.0)

> **Read this first (instructions for the model building this app)**
>
> 1. This document is the single source of truth. Build exactly what it describes. If something here is impossible, contradictory, or clearly wrong for the current Android version, **stop and explain it to the owner in plain language** before choosing an alternative.
> 2. The owner is **not a programmer**. Explain every step simply (what you did, why, how to test it). Give exact commands and exact clicks.
> 3. Build **phase by phase** (Section 20). Finish a phase, make sure it builds and passes its checks, report, and wait for the owner's OK.
> 4. Keep code clean and commented. Short files, clear names, no clever tricks.
> 5. Before adding any dependency, **check its latest stable version** and use that.
> 6. **Never hardcode user-visible text.** Every string goes in `strings.xml` in English, Arabic and Urdu from the start.
>
> **What changed from v1:** the app **no longer calculates prayer times and does not use location.** All times come from the **monthly azan timetable** published locally in Bhatkal, which the user imports by sharing the image to the app. New in v2: **jamaat times** (minutes after azan, or a fixed time, per prayer) and **spoken voice reminders** in Urdu, Arabic or English.

---

## Table of contents

1. Product summary
2. Users and context
3. Features (v1 scope and not-in-v1)
4. Tech stack
5. Architecture
6. The timetable (the core): format, import, checking, storage, lookup
7. Sharing a timetable with family
8. Jamaat times
9. Settings and storage
10. Reminders and scheduling
11. Voice reminders
12. Reliability: stopping the phone from killing reminders
13. Home-screen widgets
14. App screens and design
15. Languages (English, Arabic, Urdu)
16. Permissions and AndroidManifest
17. Privacy
18. Edge cases checklist
19. Testing
20. Phased build plan with acceptance checks
21. Build, signing, release and installing on phones
22. Open items to confirm with the owner
- Appendix A: strings.xml (English, Arabic, Urdu)
- Appendix B: logo, app icon and opening-screen resources
- Appendix C: the folder you were given

---

## 1. Product summary

**PrayerReminder** is a small, private Android app for a family in **Bhatkal (Karnataka, India)**. It:

- shows **today's azan times and jamaat times** from the masjid's **monthly azan timetable**, which is shared every month as an image (usually on WhatsApp);
- lets the user **import that image**. The app reads the numbers itself, on the phone, and the user checks anything that looks wrong;
- lets the user set the **jamaat time** for each prayer, either as minutes after the azan (default **15**) or as a **fixed time**;
- gives **reminders before the azan, at the azan, and before the jamaat**, each optionally **spoken aloud** in **Urdu, Arabic or English**, e.g. *"Ten minutes left for the Asr azan"*, *"Five minutes left for Asr prayer to start"*;
- has a **home-screen widget** with the next azan or jamaat and a live countdown;
- shows the **Hijri date from the timetable** (always matches local moon sighting);
- lets one person **send the checked timetable to family**, who load it with one tap;
- works fully **offline**: **no internet permission, no location, no accounts, no ads**.

**Design principle:** simple. The owner's brother mainly wants the widget. Anyone should understand the app in five seconds.

**Design reference:** the `design/` folder has images of every screen, widget, notification and the icon in all three languages, animations (some with sound), and audio samples. `design/ASSETS.md` lists everything. If an image and this spec disagree, follow the spec.

---

## 2. Users and context

| User | Notes |
|---|---|
| Owner | Follows the Bhatkal timetable. Sets things up, imports each month's table, sends it to family |
| Owner's brother (in India) | Mainly uses the widget and reminders. Receives the timetable from the owner, or imports the image himself |

- Both use **Android**. Common brands in India: Xiaomi/Redmi/POCO, Vivo, Oppo, Realme, Samsung, OnePlus. Many aggressively kill background apps (Section 12).
- Time zone in Bhatkal: `Asia/Kolkata` (UTC+5:30, a half-hour offset, so never assume whole-hour offsets).
- **The timetable runs by the Islamic month.** For example, the sample covers **Shawwal 1447 = 20 March – 18 April 2026**. A new table arrives around the start of each Hijri month.
- Times in the table are **azan times**. The table's Asr times follow the standard (Shafi'i) Asr. The app doesn't calculate or choose anything; it uses the table as given.

---

## 3. Features

### 3.1 In v1 (must have)

| # | Feature | Summary |
|---|---|---|
| F1 | Timetable import | Share the timetable **image** to the app (or pick it from the gallery). The app reads it on-device (OCR), checks every value, and shows a review screen where flagged values are fixed with one tap |
| F2 | Manual entry | Fallback editor to type or fix any value by hand |
| F3 | Family sharing | "Send to family" exports a small checked timetable file. Opening it on another phone imports it in one tap |
| F4 | Multiple tables | Keeps all imported months. Uses the right one for each date. Warns 3 days before the current table ends |
| F5 | Fallback when no table | Up to 7 days: carry the last day's times forward as **estimated**. With a year of history: use **last year's times for the same date** (estimated). Otherwise a clear "No timetable" state |
| F6 | Jamaat times | Per prayer (Fajr, Dhuhr, Asr, Maghrib, Isha) and Jumu'ah: **after azan** (1–60 min, default 15) or **fixed time** |
| F7 | Reminders | Per prayer: **before azan** (Off/5/10/15/20/30 min, default 10), **at azan** (on/off, default on), **before jamaat** (Off/2/5/10/15, default 5). Jumu'ah has its own before-azan setting (default 30) |
| F8 | Voice | Each reminder can be **spoken**: voice language Urdu/Arabic/English (default = app language, else Urdu), voice Male/Female, chime before voice (on), speak on silent (off) |
| F9 | Home | Next prayer card (azan + jamaat + countdown), today's list (azan + jamaat), bell per prayer to toggle all its reminders |
| F10 | Widgets | Small (2×2) and Wide (4×2) with live countdown to azan, then to jamaat |
| F11 | Hijri date | From the timetable's Islamic-date column |
| F12 | Three languages | English, Arabic, Urdu, chosen in the app, with full RTL |
| F13 | Reliability | Guided setup, reliability check screen, test reminder (with voice), missed-reminder detection (Section 12) |

### 3.2 Not in v1

Prayer-time calculation, location, full adhan audio, prayer tracker, Qibla, Quran/duas, dark theme for the app, Play Store, cloud anything, accounts, analytics, automatic download of timetables from the internet.

---

## 4. Tech stack

| Area | Choice | Why |
|---|---|---|
| Language | Kotlin (latest stable) | Same as the owner's other apps |
| UI | Jetpack Compose + Material 3, hosted in `AppCompatActivity` | AppCompat is needed for per-app language on older Android |
| On-device text recognition | **ML Kit Text Recognition v2, bundled Latin model** (`com.google.mlkit:text-recognition`) | Works **offline**, no Play-services model download, fast. The table's numbers and English headers are Latin script |
| Image loading | Android `ImageDecoder` (API 28+) / `BitmapFactory` fallback | No extra library |
| Storage | **JSON files** in app-private storage for timetables + **DataStore (Preferences)** for settings | Simple, inspectable, and the same JSON is the family-share format |
| JSON | `kotlinx.serialization` | Standard |
| Background | `AlarmManager.setAlarmClock` for reminders, **WorkManager** for a 12-hour self-healing job | See Section 10 |
| Voice playback | `MediaPlayer` in a short-lived **foreground service** (type `mediaPlayback`) | Reliable playback when the phone is locked |
| Widgets | **Jetpack Glance** + an embedded `AndroidRemoteViews` `Chronometer` for the live countdown | Compose-like code, battery-friendly countdown |
| Dependency injection | **Manual** `AppContainer` | No Hilt |
| Build | Gradle Kotlin DSL + version catalog | Standard |
| CI | GitHub Actions | Signed APKs |

- `minSdk = 26`, `targetSdk`/`compileSdk` = latest stable.
- Repo: `SyedMsidsm/PrayerReminder`. App ID: `com.syedmsidsm.prayerreminder`. Both to be confirmed (Section 22). The app ID can never change after first install.

---

## 5. Architecture

### 5.1 Big picture

```
┌─────────────────────────────── Android app (single module :app) ───────────────────────────────┐
│ UI (Compose)                       Widgets (Glance)           System entry points               │
│ Onboarding · Home · Settings       Small · Wide               ShareReceiverActivity (image/file)│
│ ImportReview · FixValueSheet       (read-only, tap → app)     ReminderAlarmReceiver              │
│ Timetables · JamaatSettings                                   SystemEventsReceiver               │
│ ReminderVoiceSettings · Reliability                           DailyMaintenanceWorker             │
│ ManualEntry                                                   VoicePlaybackService (FGS)         │
│        │ ViewModels                     │                                │                       │
│        ▼                                ▼                                ▼                       │
│ ┌───────────────────────────── Domain (pure Kotlin, unit-testable) ─────────────────────────────┐│
│ │ TimetableParser (OCR blocks → table)   TimetableValidator (flags + suggestions)               ││
│ │ DayScheduleResolver (date → azan, jamaat, estimated?)   NextPrayerResolver                     ││
│ │ ReminderPlanner (schedules + settings → ReminderEvents)  VoiceClipSelector                     ││
│ └───────────────────────────────────────────────────────────────────────────────────────────────┘│
│ ┌───────────────────────────── Data / platform ─────────────────────────────────────────────────┐│
│ │ TimetableRepository (JSON files)  SettingsRepository (DataStore)  OcrEngine (ML Kit wrapper)   ││
│ │ AlarmScheduler  NotificationHelper  VoicePlayer  WidgetUpdater  ReliabilityChecker              ││
│ │ FamilyShareExporter/Importer  FiredEventLog  LocaleManager  OemSettingsLauncher                 ││
│ └───────────────────────────────────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────────────────────────────────┘
```

**One refresh pipeline:** every change (timetable imported, settings, language, time/timezone change, boot, app update, alarm fired, daily job) calls **`AppRefresher.refreshAll(reason)`**. It:

1. resolves the schedules for **today and tomorrow** (Section 6.6);
2. plans the reminder events;
3. cancels the old alarms and sets the new ones;
4. updates both widgets.

### 5.2 Package structure

```
com.syedmsidsm.prayerreminder
├── PrayerReminderApp.kt / AppContainer.kt / AppRefresher.kt
├── domain/
│   ├── model/        // Prayer, Timetable, TimetableDay, DaySchedule, JamaatRule, ReminderEvent, ReminderSettings…
│   ├── TimetableParser.kt  TimetableValidator.kt  DayScheduleResolver.kt
│   ├── NextPrayerResolver.kt  ReminderPlanner.kt  VoiceClipSelector.kt  HijriNames.kt
├── data/
│   ├── TimetableRepository.kt  SettingsRepository.kt  FiredEventLog.kt
├── importing/
│   ├── OcrEngine.kt (ML Kit)  ImportViewModel.kt  FamilyShare.kt
├── platform/
│   ├── AlarmScheduler.kt  ReminderAlarmReceiver.kt  SystemEventsReceiver.kt  DailyMaintenanceWorker.kt
│   ├── NotificationHelper.kt  VoicePlaybackService.kt  VoicePlayer.kt
│   ├── ReliabilityChecker.kt  OemSettingsLauncher.kt  LocaleManager.kt
├── widget/   SmallPrayerWidget.kt  WidePrayerWidget.kt  WidgetStateBuilder.kt  WidgetUpdater.kt
└── ui/       MainActivity.kt  ShareReceiverActivity.kt  theme/  format/  onboarding/  home/  importing/
              timetables/  jamaat/  reminders/  reliability/  settings/  manualentry/
```

### 5.3 Core models

```kotlin
enum class Prayer { FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA }       // SUNRISE: shown only, no jamaat, no reminders

@Serializable data class TimetableDay(
    val date: String,                 // ISO "2026-03-24"
    val weekday: String,              // "Tue" as printed (used for checking)
    val hijriDay: Int,                // 5
    val times: Map<String, String>    // "fajr" -> "05:18" … 24-hour local time
)

@Serializable data class Timetable(
    val schemaVersion: Int = 1,
    val kind: String = "prayerreminder.timetable",
    val place: String,                // "Bhatkal"
    val hijriMonth: Int,              // 1..12 (10 = Shawwal)
    val hijriMonthName: String,       // "Shawwal" (English, for files/logs)
    val hijriYear: Int,               // 1447
    val firstDate: String, val lastDate: String,
    val timeZone: String = "Asia/Kolkata",
    val source: String,               // "image" | "family-file" | "manual"
    val verified: Boolean,            // true once a person reviewed and saved it
    val importedAt: Long,
    val days: List<TimetableDay>
)

data class DaySchedule(
    val date: LocalDate,
    val hijri: HijriDate?,                    // null when unknown (estimated days)
    val azan: Map<Prayer, LocalTime>,         // includes SUNRISE
    val jamaat: Map<Prayer, LocalTime>,       // no SUNRISE
    val isFriday: Boolean,                    // Dhuhr shown as Jumu'ah
    val source: ScheduleSource                // TABLE | ESTIMATED_CARRY_FORWARD | ESTIMATED_LAST_YEAR | NONE
)

enum class ReminderKind { BEFORE_AZAN, AT_AZAN, BEFORE_JAMAAT, TEST, TABLE_EXPIRING, MIDNIGHT }
data class ReminderEvent(val prayer: Prayer?, val kind: ReminderKind, val fireAt: ZonedDateTime,
                         val minutes: Int?, val isJumuah: Boolean, val requestCode: Int)
```

`requestCode = (epochDay % 100_000) * 100 + prayerOrdinal * 10 + kindOrdinal`, which is unique per day, prayer and kind.

---

## 6. The timetable (the core)

### 6.1 What the source looks like

See `design/reference/bhatkal-timetable-shawwal-1447.jpg` (also in `test-fixtures/`). It's a tall image with:

- a header: `BHATKAL AZAN TIMIING` (sic) with Urdu text alongside, and a month banner `MAR - APR 2026` with the Hijri month in Urdu (`شوال المکرم ۱۴۴۷`);
- a table with columns **Is. Date | Date | Day | Fajr | Sunrise | Dhuhr | Asr | Maghrib | Isha**;
- **Is. Date**: Islamic day 1–29/30 (the first row reads `S1` — "S" for Shawwal);
- **Date**: Gregorian day of month, with a month letter where the month starts (`M 20` = March 20, `A 1` = April 1);
- **Day**: Mon…Sun;
- **times**: 12-hour **without AM/PM** (e.g. `5:21`, `12:45`, `4:01`, `7:50`);
- Friday rows highlighted;
- a photo background behind the table, which makes OCR harder.

`test-fixtures/bhatkal-shawwal-1447.expected.json` is the **exact expected result** for that image (30 days, 24-hour times). Use it as the golden test.

### 6.2 Getting the image into the app

- **Share target:** `ShareReceiverActivity` with intent filters for `ACTION_SEND` with `image/*`. This is what appears when the user taps Share on the image in WhatsApp or the gallery.
- **In-app:** "Import new timetable" opens the **Android photo picker** (`PickVisualMedia`, no storage permission needed).
- **Family file:** `ACTION_SEND`/`ACTION_VIEW` for `application/json` and the `.prtt` extension (Section 7), plus "Import a file" in-app via `ACTION_OPEN_DOCUMENT`.
- Copy the incoming image into app cache right away (the shared URI may expire).

### 6.3 Reading it (OCR → table) — `OcrEngine` + `TimetableParser`

1. **Decode** the bitmap. If its shortest side is under 1000 px, upscale ×2 (bilinear). Keep EXIF orientation.
2. **Run ML Kit** text recognition (Latin, bundled). Collect every **element** (word) with its text and bounding box.
3. **Find the header row:** fuzzy-match (case-insensitive, edit distance ≤ 2) the tokens `Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha` (and `Date`, `Day`). Their x-centers define the **column centers**. The `Is. Date` and `Date` columns are to the left of `Day`.
   - If the header isn't found, infer columns by clustering x-centers of all `\d{1,2}[:.]\d{2}` tokens into 6 clusters (k-means on x), ordered left to right.
4. **Group into rows:** take all tokens below the header. Cluster by y-center with tolerance = 0.6 × median token height. Sort rows top to bottom.
5. **Assign tokens to columns** by nearest column center (reject if farther than 0.6 × median column gap).
6. **Parse cells:**
   - time cells: regex `^(\d{1,2})[:.;,](\d{2})$` (OCR often reads `:` as `.` or `;`). Common digit confusions to repair *only when needed to pass the checks*: `O→0`, `l/I→1`, `S→5`, `B→8`;
   - day cells: `Mon|Tue|Wed|Thu|Fri|Sat|Sun` (fuzzy);
   - date cells: optional month letter + number (`M 20`, `A 1`, `20`);
   - Is. date: optional letter + number (`S1`, `2`…).
7. **Add AM/PM** (the table has none):
   - Fajr, Sunrise → AM;
   - Dhuhr → `11:xx` = AM, `12:xx` = PM (noon), `1:xx`/`2:xx` = PM;
   - Asr, Maghrib, Isha → PM.
   Store as 24-hour `HH:mm`.
8. **Gregorian dates:** read the banner (`MAR - APR 2026` → start month and year). The first row's date number (`20`) with the first month gives the start date. Each following row is +1 day. **Cross-check** with the Day column (weekday must match) and with month letters (`A 1` must be April 1). If the banner can't be read, ask the user for the start date on the review screen, pre-filled with the day after the previous table ends.
9. **Hijri month and year:** the Urdu banner isn't reliably readable with the Latin model. **Propose** the month after the latest saved table (e.g. after Shawwal 1447 → Dhul Qa'dah 1447). If no table exists, use the month implied by the first letter in the `Is. Date` column (`S` → Shawwal or Safar; `R` → Rabi/Rajab/Ramadan…) and the Hijri calendar from `HijrahDate` for the start date. **Always show it on the review screen for the user to confirm** (month + year pickers).
10. **Place name:** read the text before `AZAN` in the header (`BHATKAL` → "Bhatkal"), title case. The user can edit it on the review screen.

### 6.4 Checking it (`TimetableValidator`)

Produce a list of **flags** (`dayIndex`, `field`, `reason`, `suggestion`). Rules:

| Rule | Flag when |
|---|---|
| Missing | Any cell empty or unparseable |
| Order within a day | Not strictly increasing: Fajr < Sunrise < Dhuhr < Asr < Maghrib < Isha |
| Smooth day to day | A value differs by **> 3 min** from the median of its neighbours (2 before + 2 after) in the same column |
| Weekday | Day text doesn't match the computed date's weekday |
| Sequence | Is. Date not 1…N consecutive. Dates not consecutive |
| Length | Fewer than 29 or more than 30 rows |
| Plausible range | Fajr 3:30–6:30, Sunrise 5:00–7:30, Dhuhr 11:30–13:30, Asr 14:30–17:30, Maghrib 17:00–20:00, Isha 18:00–21:30 (24h) |

**Suggestion** for a flagged time = linear interpolation of the nearest valid neighbours, rounded to the minute. Example (in the design images): Asr on Tue 24 Mar read as `3:39` while neighbours are `4:00` and `3:59` → suggest `3:59`.

**Save is disabled while any flag is unresolved.** Resolving a flag means either "Use <suggestion>", typing a value, or "Keep" (an explicit override, logged).

### 6.5 Storage (`TimetableRepository`)

- One file per month: `files/timetables/<hijriYear>-<hijriMonth 2 digits>.json` (the `Timetable` JSON above), plus an in-memory index built at startup.
- Importing a month that already exists → ask "Replace the existing Shawwal 1447?"
- If two tables overlap in dates, the **newer import wins** for overlapping dates (warn on review).
- Keep all tables (they're tiny). The Timetables screen allows deleting one.

### 6.6 Resolving a day (`DayScheduleResolver`)

For a date **D** (device time zone):

1. If a saved table contains D → `TABLE`.
2. Else if a table ended ≤ **7 days** before D → copy the last day's times → `ESTIMATED_CARRY_FORWARD`.
3. Else if a table exists containing a date within ±3 days of **D minus 1 year** (or 354 days, the Hijri year) → use the closest such day's times → `ESTIMATED_LAST_YEAR`. Solar times repeat within about a minute year to year.
4. Else → `NONE` (no times). Home shows "No timetable for today. Import the new timetable." No reminders are scheduled. The widget shows the setup message.

Estimated days: Home shows the yellow banner, the widget shows a small "est." mark, and notifications add "(estimated)". The Hijri date is unknown on estimated days unless the new table is imported.

Then apply **jamaat rules** (Section 8) and mark **Friday**.

### 6.7 "New timetable needed" reminder

At 9:00 AM on each of the **last 3 days** of the newest table (if no later table exists), post a notification: *"New timetable needed — Shawwal ends in 3 days. Share the new month's timetable to PrayerReminder."* Tapping it opens the import screen. Also shown as a card on the Timetables screen.

### 6.8 Manual entry (`ManualEntryScreen`)

The same grid as the review screen, editable. "New blank month" asks for the place, Hijri month/year and start date, and how many days (29/30), then lets the user type all times (numeric keypad, auto-advance). Same validator. Used when OCR fails completely.

---

## 7. Sharing a timetable with family

- After saving (and from the Timetables screen): **"Send to family"** → writes `PrayerReminder-<Place>-<Month>-<Year>.prtt` (UTF-8 JSON, exactly the `Timetable` schema with `verified: true` and `source` set to the sender's source) to cache. Shares it via `ACTION_SEND` with a `FileProvider` URI, MIME `application/json`.
- Receiving: tapping the file in WhatsApp offers PrayerReminder (intent filters for `application/json`, `application/octet-stream` with path pattern `.*\\.prtt`, and `ACTION_SEND`). The app shows **"Import timetable?"** with place, month, dates, days and "checked by sender" → **Import**.
- **Validate** on receive (schema version, required fields, the same checks). If it fails, refuse with a friendly message. Never execute or trust anything else in the file.
- The brother can also just receive the **image** and import it himself (Section 6).

---

## 8. Jamaat times

- Rules per prayer: **FAJR, DHUHR, ASR, MAGHRIB, ISHA, JUMUAH** (Jumu'ah replaces Dhuhr on Fridays).
  ```kotlin
  sealed interface JamaatRule {
      data class AfterAzan(val minutes: Int) : JamaatRule   // 1..60, default 15
      data class Fixed(val time: LocalTime) : JamaatRule    // same time every day
  }
  ```
- **Default: AfterAzan(15) for all**, as the owner asked.
- Fixed time sanity: if a fixed jamaat time is **earlier than or equal to** that day's azan (e.g. fixed Isha 8:15 PM but azan 8:20 PM in another month), use azan + 15 for that day and show a small warning icon on that prayer in Home and in Jamaat settings ("Fixed time is before azan today").
- Shown everywhere: Home card and list, widget, notifications, reminders.
- Changing any rule → `refreshAll("jamaat")`.

---

## 9. Settings and storage

DataStore Preferences (`settings`). Keys and defaults:

| Key | Type | Default | Meaning |
|---|---|---|---|
| `onboarding_done` | Boolean | false | |
| `app_language` | String | "" (system) | "en" / "ar" / "ur" |
| `jamaat_<P>_mode` | String | "after" | "after" / "fixed", P ∈ FAJR, DHUHR, ASR, MAGHRIB, ISHA, JUMUAH |
| `jamaat_<P>_minutes` | Int | 15 | 1–60 |
| `jamaat_<P>_fixed` | String | "" | "HH:mm" |
| `rem_before_azan_min` | Int | 10 | 0 (Off), 5, 10, 15, 20, 30 |
| `rem_jumuah_before_azan_min` | Int | 30 | 0, 5, 10, 15, 20, 30 |
| `rem_at_azan` | Boolean | true | |
| `rem_before_jamaat_min` | Int | 5 | 0, 2, 5, 10, 15 |
| `rem_<P>_<KIND>` | Boolean | true | Per-prayer matrix: KIND ∈ BEFORE_AZAN, AT_AZAN, BEFORE_JAMAAT |
| `voice_enabled` | Boolean | true | |
| `voice_language` | String | app language, or "ur" if system/English unknown | "ur" / "ar" / "en" |
| `voice_gender` | String | "male" | "male" / "female" |
| `voice_chime` | Boolean | true | |
| `voice_on_silent` | Boolean | false | |
| `scheduled_codes`, `scheduled_events`, `fired_log`, `missed_count_7d` | … | | For alarm management and missed detection (Section 12) |
| `oem_step_confirmed` | Boolean | false | User tapped "I've done it" |
| `last_test_result` | String | "" | |

The **bell on Home** toggles all three `rem_<P>_*` for that prayer (off if any is on; on restores all three).

---

## 10. Reminders and scheduling

### 10.1 What gets scheduled

`ReminderPlanner.plan(now, today, tomorrow, settings)` → events for **now → now + 36 h**. For each prayer FAJR, DHUHR (or JUMUAH on Friday), ASR, MAGHRIB, ISHA on days with source ≠ NONE:

| Kind | Fires at | Condition |
|---|---|---|
| BEFORE_AZAN | azan − N min (N = `rem_before_azan_min`, or the Jumu'ah value on Friday Dhuhr) | N > 0 and matrix on |
| AT_AZAN | azan | `rem_at_azan` and matrix on |
| BEFORE_JAMAAT | jamaat − M min (M = `rem_before_jamaat_min`) | M > 0, matrix on, and **fire time > azan time** (otherwise skip it) |

Plus `TABLE_EXPIRING` (Section 6.7), `MIDNIGHT` (00:00:30, refresh only) and `TEST`.

- Drop events in the past (≤ now + 5 s).
- If two events for the same prayer are < 60 s apart, keep only the later one.

### 10.2 Alarm API

- Use **`AlarmManager.setAlarmClock(AlarmClockInfo(fireAt, showIntent), pi)`** for **all reminder events**. It's exact, isn't throttled in Doze (reminders here can be only a few minutes apart), and lets the app start the voice foreground service. Side effect: a small alarm icon in the status bar. Explain this in onboarding (it's in the design).
- If exact alarms aren't allowed (`canScheduleExactAlarms()` false on 12+), fall back to `setAndAllowWhileIdle` and show a red item in the Reliability screen plus a Home banner.
- MIDNIGHT and TABLE_EXPIRING can use `setExactAndAllowWhileIdle`.
- `PendingIntent` flags: `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE`. Cancel all previous codes before scheduling the new set. Save the codes and events.

### 10.3 When an alarm fires (`ReminderAlarmReceiver`)

```text
goAsync(); coroutine with 8 s timeout:
  load settings + today's schedule (re-check this reminder is still enabled and still correct)
  post notification (built NOW in the current app language)
  if voice should play (Section 11.4): start VoicePlaybackService with clip ids (chime + sentence)
  if kind == AT_AZAN: cancel that prayer's BEFORE_AZAN notification
  if kind == BEFORE_JAMAAT: cancel that prayer's AT_AZAN notification
  FiredEventLog.record(code, now)
  AppRefresher.refreshAll("alarm")
finish()
```

### 10.4 Other triggers (`SystemEventsReceiver`)

`BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`, `TIME_SET`, `TIMEZONE_CHANGED`, `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` → `refreshAll(reason)`.

### 10.5 Notifications

| Channel | Importance | Sound | Use |
|---|---|---|---|
| `reminders_voice` | HIGH | **none** (the app plays chime + voice itself), vibration on | Reminders when voice is enabled |
| `reminders_sound` | HIGH | default notification sound | Reminders when voice is disabled |
| `general` | DEFAULT | default | New timetable needed, missed reminders |

Content (English; all three languages in Appendix A):

| Kind | Title | Text |
|---|---|---|
| BEFORE_AZAN | "Asr azan in 10 minutes" | "Azan 3:59 PM · Jamaat 4:14 PM" |
| AT_AZAN | "Asr azan — 3:59 PM" | "Jamaat 4:14 PM" |
| BEFORE_JAMAAT | "Asr prayer starts in 5 minutes" | "Jamaat 4:14 PM" |
| Jumu'ah BEFORE_AZAN | "Jumu'ah azan in 30 minutes" | "Azan 12:43 PM · Jamaat 12:58 PM" |

- Small icon: monochrome crescent. Notification ID = request code. Category `CATEGORY_REMINDER`. Tap opens Home.
- Estimated days: append " (estimated)" to the text.
- When the language changes, recreate the channels with the same IDs and new names.

### 10.6 Daily self-healing job

WorkManager `PeriodicWorkRequest` every 12 h, unique name `daily_maintenance`, runs `refreshAll("worker")` and the missed-reminder check.

---

## 11. Voice reminders

### 11.1 Approach (decided)

**Pre-recorded sentence clips bundled in the app**, not the phone's text-to-speech engine. The phone's built-in speech often has **no Urdu voice** (especially on Vivo, Oppo and Xiaomi), sounds robotic, and mispronounces *azan*/*jamaat*. Clips work offline on every phone and always use correct grammar (Arabic changes the form of "2 minutes", "5 minutes", "10 minutes" and so on).

### 11.2 Sentences

Per language, per prayer (fajr, dhuhr, asr, maghrib, isha, jumuah):

| Kind | Minutes | English | Urdu | Arabic |
|---|---|---|---|---|
| before_azan | 5, 10, 15, 20, 30 | "Ten minutes left for the Asr azan." | "عصر کی اذان میں دس منٹ باقی ہیں" | "بقيت عشر دقائق على أذان العصر" |
| at_azan | — | "It is time for the Asr azan." | "عصر کی اذان کا وقت ہو گیا ہے" | "حان الآن موعد أذان العصر" |
| before_jamaat | 2, 5, 10, 15 | "Five minutes left for Asr prayer to start." | "عصر کی نماز شروع ہونے میں پانچ منٹ باقی ہیں" | "بقيت خمس دقائق على إقامة صلاة العصر" |

That's **60 clips per language per voice**: 6 prayers × (5 + 1 + 4). The complete phrase list is in `design/audio/voice-pack-preview/phrases.json`. English text spells names the way they're said in India (Zohar, Asar, Jumma) so the voice pronounces them naturally. The screen text uses the normal spellings.

### 11.3 The clips you were given — and the final clips

- `design/audio/voice-pack-preview/{ur,ar,en}/` has **all 180 male clips** (MP3, 24 kHz) and `design/audio/samples/` has listenable samples, including female voices. They were generated with Microsoft neural voices (`ur-IN-SalmanNeural`, `ar-SA-HamedNeural`, `en-IN-PrabhatNeural`; female `ur-IN-GulNeural`, `ar-SA-ZariyahNeural`, `en-IN-NeerjaNeural`) **as a preview**.
- **For the release build:** regenerate the clips with **Azure AI Speech under the owner's own Azure account** (the free tier is more than enough for about 360 short sentences), using the same voices, or **record a real person**. Then the app's audio is properly licensed. `tools/make_voice_clips.py` has the exact sentences and file names. Adapt it to the Azure Speech SDK/REST API, or use it as the recording script. The preview clips may be used **during development**.
- Process for the app:
  - convert to **Ogg Opus, mono, 32 kbps**;
  - trim silence (keep 100 ms at each end);
  - normalize loudness to −16 LUFS;
  - name them `res/raw/v_<lang>_<gender>_<kind>_<prayer>[_<minutes>].ogg`, e.g. `v_ur_male_before_azan_asr_10.ogg`;
  - add the chime `res/raw/chime.ogg` (from `design/audio/chime.wav`).
  Total about 5–6 MB for both genders.
- `VoiceClipSelector(language, gender, kind, prayer, minutes)` returns the resource id. The minute options are exactly the clip set, so there's always a clip.

### 11.4 Playback rules (`VoicePlayer` / `VoicePlaybackService`)

- Play only if `voice_enabled`. Otherwise use the `reminders_sound` channel.
- **Ringer mode:**
  - NORMAL → play;
  - SILENT or VIBRATE → **don't play**, just vibrate, unless `voice_on_silent` is on. Then play with `USAGE_ALARM` so it's heard.
- **In a call** (`AudioManager.mode` is IN_CALL or IN_COMMUNICATION) → don't play.
- Request **transient audio focus with ducking**. Music lowers, then resumes.
- Sequence: chime (if on) → 250 ms pause → sentence. Audio attributes `USAGE_NOTIFICATION_EVENT` (or `USAGE_ALARM` when overriding silent), `CONTENT_TYPE_SPEECH`.
- Volume follows the chosen stream. No in-app volume control in v1.
- Run in a **foreground service** (`foregroundServiceType="mediaPlayback"`) started from the alarm receiver. Starting it from an alarm-clock alarm is allowed. It shows a minimal "Playing reminder" notification (or reuses the reminder notification), stops itself after playback or after 15 s max, and releases the player.
- If two reminders overlap, queue them, never overlap.
- "Play sample" buttons in Settings and onboarding use the same player (no service needed in the foreground).

---

## 12. Reliability: stopping the phone from killing reminders

### 12.1 Layers

1. Exact alarm-clock alarms (Section 10.2).
2. Battery optimization exemption (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`).
3. Manufacturer autostart / no-restriction settings (guided, 12.3).
4. Self-rescheduling: every fired alarm reschedules.
5. Boot/time/update receivers.
6. 12-hour WorkManager safety net.
7. Missed-reminder detection: on each refresh, any scheduled event with `fireAt < now − 2 min` not in `fired_log` counts as missed (7-day rolling count). Home shows a banner.
8. Test reminder (with voice) proves the whole path.

### 12.2 Reliability checklist (`ReliabilityScreen`, also used by onboarding)

| Check | Detect | Fix |
|---|---|---|
| Notifications allowed | `areNotificationsEnabled()` + `POST_NOTIFICATIONS` on 33+ | Request permission or open settings |
| Channels enabled | importance ≠ NONE | Open channel settings |
| Alarms & reminders (exact alarms) | `canScheduleExactAlarms()` on 31+ | `ACTION_REQUEST_SCHEDULE_EXACT_ALARM` |
| Battery optimization off | `isIgnoringBatteryOptimizations()` | `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` |
| Manufacturer setting | Can't detect → "Please check" | `OemSettingsLauncher` + "I've done it" |
| Timetable | Days left in the current table, estimated or none | Import |
| Missed reminders (7 days) | `missed_count_7d` | Explain + re-check |

Re-check on `ON_RESUME`.

### 12.3 Manufacturer guidance (`OemSettingsLauncher`)

Detect from `Build.MANUFACTURER`. Show 2–4 short steps and try these screens in order (wrap each in try/catch; fall back to the app details page):

| Manufacturer | Steps | Screens to try |
|---|---|---|
| xiaomi / redmi / poco | Autostart on. Battery saver → No restrictions. Lock in Recents | `com.miui.securitycenter / com.miui.permcenter.autostart.AutoStartManagementActivity` |
| samsung | Battery → Unrestricted. Not in Sleeping / Deep sleeping apps | `com.samsung.android.lool / com.samsung.android.sm.battery.ui.BatteryActivity` |
| oppo / realme / oneplus | Allow Auto launch. Allow background activity | `com.coloros.safecenter / com.coloros.safecenter.permission.startup.StartupAppListActivity`, `com.oplus.battery / com.oplus.powermanager.fuelgaue.PowerUsageModelActivity` |
| vivo / iqoo | Allow background power consumption / Autostart | `com.vivo.permissionmanager / com.vivo.permissionmanager.activity.BgStartUpManagerActivity`, `com.iqoo.secure / com.iqoo.secure.ui.phoneoptimize.BgStartUpManager` |
| huawei / honor | App launch → Manage manually, all on | `com.huawei.systemmanager / com.huawei.systemmanager.startupmgr.ui.StartupNavigateActivity` |

Add the text line "More help: dontkillmyapp.com" (opened in the user's browser via `ACTION_VIEW`; the app itself has no internet).

### 12.4 Test reminder

"Send test notification" schedules a TEST alarm **60 s** ahead through the real path (alarm-clock → receiver → notification → voice). Message: *"Test reminder — if you can see and hear this, reminders work."* The voice plays the selected language's `before_azan_asr_10` clip. Tell the user to **lock the phone** while waiting. Store the result ("Test passed at 2:41 PM (voice played)").

---

## 13. Home-screen widgets

Dark (`#0F2A2E`), 24–28 dp corners, ivory/gold text, system font.

### 13.1 Small (2×2)
- Place ("Bhatkal")
- **Before azan:** prayer name (gold); azan time (large); "Jamaat 4:14 PM"; ⏱ countdown to azan.
- **Between azan and jamaat:** "Asr · Jamaat" (gold); jamaat time (large); "Azan ✓ 3:59 PM"; ⏱ countdown to jamaat.

### 13.2 Wide (4×2)
- Left: place; "Next · Asr" (or "Jamaat · Asr"); big time; the other time; ⏱ countdown; Hijri date.
- Right: header row "Azan | Jamaat" and the five prayers (no Sunrise) with azan and jamaat columns. Past rows muted. The next row is highlighted.

### 13.3 Behaviour
- **Phases per prayer:** until azan → count down to azan; azan → jamaat → count down to jamaat; after jamaat → next prayer. Phase changes happen at the AT_AZAN and jamaat moments. Schedule a silent `PHASE` alarm at each jamaat time (refresh only, no notification) so the widget switches even if BEFORE_JAMAAT is off.
- Countdown: `AndroidRemoteViews` with a `Chronometer` counting down (`setChronometerCountDown(true)`). Fallback if that causes trouble: text "in 1h 24m" + updates at phase changes and every 15 min (tell the owner).
- Friday: Dhuhr shows as Jumu'ah.
- Estimated day: a small "est." after the place.
- No table (NONE): "Import the new timetable" + tap opens the import.
- Tap anywhere → app. Update triggers: every `refreshAll`, the midnight alarm, and phase alarms.
- RTL: build rows explicitly per the **app's** language (the launcher may use the phone's language): reversed order + end alignment for ar/ur. Test with the phone in English and the app in Arabic/Urdu.
- "Add widget" button uses `requestPinAppWidget()`.

---

## 14. App screens and design

### 14.1 Design tokens

| Token | Hex | Use |
|---|---|---|
| ground | `#0F2A2E` | Next card, widgets, selected controls, icon background |
| ink | `#14262A` | Main text |
| ivory | `#F7F4EC` | App background; text on dark |
| surface | `#FFFFFF` | Cards |
| gold | `#D9AE5C` | Accent on dark |
| goldText | `#8A6420` | Gold text on light (Hijri date) |
| muted | `#5E6D70` | Secondary text |
| onDarkMuted | `#C8D2CF` | Secondary on dark |
| pastOnDark | `#9FB0AD` | Past rows in widget |
| nextRowBg / nextRowBgDark | `#F5ECD7` / `#23403F` | Highlighted next row |
| tag | `#EBD9AE` / `#6B4A12` | "Next" tag |
| warnBg / warnText | `#FBEFD5` / `#5C3D00` | Banners, flagged items |
| flag | `#FCD98A` + outline `#E0A526` | Flagged cell on review |
| ok | `#E3F1E8` / `#1F7A4D` | Success |
| line | `#EDE8DC` | Dividers |

Fonts (bundled, OFL): **Plus Jakarta Sans** (English), **Noto Naskh Arabic** (Arabic), **Noto Nastaliq Urdu** (Urdu, line height ~1.8×). Western digits everywhere in v1. Touch targets ≥ 48 dp. Light theme only.

### 14.2 Onboarding (11 steps; images `02-onboarding/*`, video `02-…`)

On the very first launch, the brand intro (14.10) plays first, then:


1. **Language** — English / العربية / اردو (switches immediately).
2. **Welcome** — logo + "Your masjid's azan and jamaat times, with reminders and a widget."
3. **Your timetable** — "Choose timetable image" (photo picker) or "I got a timetable file from family". Runs the import flow (Section 14.4). Must end with a saved table to continue (or "Enter manually").
4. **Jamaat time** — one stepper "15 minutes after azan" applied to all prayers, with today's example ("Asr azan 3:59 PM → jamaat 4:14 PM"). Note that each prayer and fixed times can be set later.
5. **Voice reminders** — voice language segmented control + "Play sample"; shows the defaults (10 min before azan · 5 min before jamaat).
6. **Notifications** — request `POST_NOTIFICATIONS` (33+).
7. **Alarms & reminders** — only if `canScheduleExactAlarms()` is false. Explain the status-bar alarm icon.
8. **Battery** — ignore battery optimization.
9. **Phone-specific** — only for brands in 12.3.
10. **Test** — test reminder with voice.
11. **Widget** — pin request + "Done".

Permission steps can be skipped. Skipped items show in Reliability and as Home banners.

### 14.3 Home (`03-home/*`)

- **Header:** mosque icon + **place** ("Bhatkal"); sub-line "Timetable: Shawwal 1447 · until 18 Apr" (or "Shawwal 1447 timetable ended 18 Apr" when estimated). Settings button.
- **Banners** (when relevant): estimated times / no timetable / missed reminders / notifications off.
- **Date block:** Gregorian (e.g. "Tuesday, 24 March") + Hijri from the table ("5 Shawwal 1447 AH").
- **Next card:**
  - before azan: "Next prayer" · name · **Azan 3:59 PM** (large) and Jamaat 4:14 PM · "Azan in 1 hour 24 minutes";
  - between azan and jamaat: "Now" · name · Azan ✓ 3:59 PM (small) and **Jamaat 4:14 PM** (large) · "Jamaat in 11 minutes";
  - bottom line: speaker icon + "Voice: 10 min before azan · 5 min before jamaat" (or "Voice off").
- **Today list:** header "Today | Azan | Jamaat". Six rows (Sunrise has "—" for jamaat and no bell). Past rows muted. Next row highlighted. Bell toggles that prayer's reminders.
- After Isha's jamaat: the next prayer is tomorrow's Fajr ("Next prayer · Tomorrow").
- Countdown text updates every 30 s while visible.

### 14.4 Import flow (`04-timetable-import/*`, video `03-…`)

1. **Share** the image to PrayerReminder (or pick it).
2. **Reading** screen: the image with a scanning line + progress, "Reading the timetable… On your phone, no internet needed".
3. **Check the timetable:**
   - summary card (place · Hijri month/year · date range · days) with an edit button for place/month/year/start date;
   - banner "1 value looks wrong. Tap the yellow box to fix it." or a green "All 180 times look right";
   - the full grid (Is. | Date | Fajr | Sunr. | Dhuhr | Asr | Magh. | Isha) with Friday rows tinted and flagged cells yellow;
   - footnote about AM/PM;
   - **Save** (disabled while flags remain).
4. **Fix sheet:** the cell's prayer and date, an explanation ("The day before is 4:00 and the day after is 3:59. 3:39 is about 20 minutes off"), a large editable value, **Keep 3:39** / **Use 3:59**.
5. **Saved:** "Timetable saved" + **Send to family** + Done.
6. **Receiving a family file** (example in Urdu): "Import timetable?" card with place/month/dates/"checked by sender" → Import.

### 14.5 Timetables screen

Now (active, days left) · Next (not imported yet, needed by date, Import) · Earlier months · "Import new timetable" · "Send current to family". Long-press a month → Delete / Send / View.

### 14.6 Settings (`05-settings/settings-*`)

Language · Timetable (current + Import new + Send to family + All timetables) · Jamaat times → screen · Reminders & voice → screen · Reliability check + Send test notification · Widget · About (version, licences, "No internet, no location, no accounts. Everything stays on your phone.").

### 14.7 Jamaat times screen

One card row per prayer + Jumu'ah:
- name;
- "Today: azan X → jamaat Y";
- segmented **After azan | Fixed time**;
- a ± stepper ("15 min") or a time chip ("8:15 PM").

Plus "Reset all to 15 minutes".

### 14.8 Reminders & voice screen

- Voice language: اردو / العربية / English.
- Voice: Male / Female.
- Card:
  - Before azan: chips Off/5/10/15/20/30 + ▶ sample;
  - At azan: switch;
  - Before jamaat: chips Off/2/5/10/15 + ▶ sample;
  - Jumu'ah before azan: chips.
- Matrix "Which reminders for each prayer" (Before azan / At azan / Before jamaat × Fajr…Jumu'ah).
- Switches: Speak when phone is on silent (off) · Chime before the voice (on) · Voice on/off (master).

### 14.9 Reliability screen

Checklist (12.2), manufacturer steps, test result + "Send test notification".

### 14.10 Opening screen (every launch) and first-launch intro

**Every launch: animated opening screen** (video `design/videos/10-opening-screen-every-launch.mp4`, phone-only `12-…`).

- Use the **Android SplashScreen API** (`androidx.core:core-splashscreen`) with the ready-made resources in `android-resources/`:
  - `Theme.PrayerReminder.Starting` is the `MainActivity` theme;
  - background `#0F2A2E`;
  - icon `@drawable/avd_splash_icon`, animation duration 1000 ms.
- The animation, as it plays inside the AVD:

| Time | What happens |
|---|---|
| 0–450 ms | Crescent sweeps in: rotates −55° → 0°, scales 0.55 → 1, fades in (0–350 ms) |
| 350–890 ms | The five dots pop in one after another, bottom to top (each 220 ms, 80 ms apart, overshoot) |
| 750–1000 ms | A thin gold ring pulses out from the gold centre dot (next prayer) and fades |

- Kotlin in `MainActivity.onCreate` (before `super.onCreate`):
  ```kotlin
  val splash = installSplashScreen()
  val start = SystemClock.uptimeMillis()
  // keep the opening screen until data is loaded AND the 1-second animation has played
  splash.setKeepOnScreenCondition { !viewModel.isReady.value || SystemClock.uptimeMillis() - start < 900 }
  splash.setOnExitAnimationListener { p ->
      p.iconView.animate().scaleX(0.85f).scaleY(0.85f).alpha(0f).setDuration(250).start()
      p.view.animate().alpha(0f).setDuration(320).withEndAction { p.remove() }.start()
  }
  ```
  Home then slides up slightly (40 dp → 0, 400 ms, ease-out) as in the video.
- **Android 8–11:** the compat library shows the **static** icon (`splash_icon_static`) on the same background, which is expected.
- If the user has turned off animations (`Settings.Global.ANIMATOR_DURATION_SCALE == 0`), don't wait the 900 ms.

**First launch only: brand intro** (video `11-first-launch-intro.mp4`, phone-only `13-…`)

- A Compose screen shown once, before onboarding step 1, when `onboarding_done == false`. Total about 3.6 s, then it slides into the language step. **Tap anywhere to skip.**

| Time | What happens |
|---|---|
| 0–1.0 s | The same mark animation, drawn in Compose `Canvas` (crescent path + 5 circles; same timings and easing) |
| 1.0–1.6 s | Mark rises about 110 dp and scales to 0.75 |
| 1.3 s | "PrayerReminder" fades and slides up (Prayer ivory, Reminder gold) |
| 1.8 s | "تذكير الصلاة" (Noto Naskh Arabic) |
| 2.1 s | "نماز کی یاد دہانی" (Noto Nastaliq Urdu) |
| 2.6 s | Tagline "Azan & jamaat times · voice reminders · widget" + "Tap to skip" |
| 3.6–4.1 s | Slides left into onboarding step 1 |

- Respect the system's animation setting: if animations are off, show the final frame for 1 s.

---

## 15. Languages (English, Arabic, Urdu)

- `values/`, `values-ar/`, `values-ur/`; `locales_config.xml`; `AppCompatDelegate.setApplicationLocales()`; `AppLocalesMetadataHolderService` with `autoStoreLocales=true`.
- `supportsRtl="true"`; use start/end only; auto-mirror directional icons.
- No concatenated sentences. Use format args and **plurals** (Arabic has six forms).
- Times: `h:mm` + localized AM/PM marker (EN AM/PM, AR ص/م, UR AM/PM) or 24h if the system uses 24h. In Urdu, wrap times in an LTR isolate so "3:59 PM" doesn't flip to "PM 3:59".
- Dates: `DateTimeFormatter.ofPattern("EEEE, d MMMM", locale).withDecimalStyle(DecimalStyle.STANDARD)`.
- Hijri: day from the table + month name from `R.array.hijri_months` + year + suffix.
- **Voice language is separate from app language** (default: same as app language).
- After a language change: recreate notification channels, update widgets.

---

## 16. Permissions and AndroidManifest

| Permission | Why |
|---|---|
| `POST_NOTIFICATIONS` | Reminders (runtime on 33+) |
| `SCHEDULE_EXACT_ALARM` (`maxSdkVersion="32"`) | Exact alarms on Android 12 |
| `USE_EXACT_ALARM` | Exact alarms on 13+ without a user prompt. Allowed for apps whose core job is timed reminders. Fine for sideloading. **If ever published on Play, revisit** (Play restricts it) |
| `RECEIVE_BOOT_COMPLETED` | Restore alarms after restart |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Ask to skip battery optimization (revisit for Play) |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Voice playback service |
| `VIBRATE` | Vibrate-only reminders |
| `WAKE_LOCK` | Added by WorkManager |

**Not used:** INTERNET, any location, any storage/media permission (the photo picker and share intents need none).

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" android:maxSdkVersion="32"/>
    <uses-permission android:name="android.permission.USE_EXACT_ALARM"/>
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"/>
    <uses-permission android:name="android.permission.VIBRATE"/>

    <application android:name=".PrayerReminderApp" android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher" android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true" android:localeConfig="@xml/locales_config"
        android:allowBackup="false" android:theme="@style/Theme.PrayerReminder">

        <activity android:name=".ui.MainActivity" android:exported="true" android:launchMode="singleTop">
            <intent-filter><action android:name="android.intent.action.MAIN"/><category android:name="android.intent.category.LAUNCHER"/></intent-filter>
        </activity>

        <!-- Receives the timetable image or a family .prtt file -->
        <activity android:name=".ui.ShareReceiverActivity" android:exported="true" android:theme="@style/Theme.PrayerReminder">
            <intent-filter><action android:name="android.intent.action.SEND"/><category android:name="android.intent.category.DEFAULT"/><data android:mimeType="image/*"/></intent-filter>
            <intent-filter><action android:name="android.intent.action.SEND"/><category android:name="android.intent.category.DEFAULT"/><data android:mimeType="application/json"/></intent-filter>
            <intent-filter><action android:name="android.intent.action.VIEW"/><category android:name="android.intent.category.DEFAULT"/><data android:mimeType="application/json"/></intent-filter>
            <intent-filter><action android:name="android.intent.action.VIEW"/><category android:name="android.intent.category.DEFAULT"/><category android:name="android.intent.category.BROWSABLE"/>
                <data android:scheme="content"/><data android:mimeType="application/octet-stream"/><data android:pathPattern=".*\\.prtt"/></intent-filter>
        </activity>

        <provider android:name="androidx.core.content.FileProvider" android:authorities="${applicationId}.files"
            android:exported="false" android:grantUriPermissions="true">
            <meta-data android:name="android.support.FILE_PROVIDER_PATHS" android:resource="@xml/file_paths"/>
        </provider>

        <receiver android:name=".platform.ReminderAlarmReceiver" android:exported="false"/>
        <receiver android:name=".platform.SystemEventsReceiver" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED"/>
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED"/>
                <action android:name="android.intent.action.TIME_SET"/>
                <action android:name="android.intent.action.TIMEZONE_CHANGED"/>
                <action android:name="android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"/>
            </intent-filter>
        </receiver>

        <service android:name=".platform.VoicePlaybackService" android:exported="false"
            android:foregroundServiceType="mediaPlayback"/>

        <receiver android:name=".widget.SmallPrayerWidgetReceiver" android:exported="true" android:label="@string/widget_small_label">
            <intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE"/></intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/widget_small_info"/>
        </receiver>
        <receiver android:name=".widget.WidePrayerWidgetReceiver" android:exported="true" android:label="@string/widget_wide_label">
            <intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE"/></intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/widget_wide_info"/>
        </receiver>

        <service android:name="androidx.appcompat.app.AppLocalesMetadataHolderService" android:enabled="false" android:exported="false">
            <meta-data android:name="autoStoreLocales" android:value="true"/>
        </service>
    </application>
</manifest>
```

(The `.prtt` VIEW filter is best effort. Some apps hand files over as `application/octet-stream` without a usable path. The in-app "Import a file" button is the reliable fallback.)

---

## 17. Privacy

No internet permission. No location. No analytics, ads, crash SDKs or accounts. Timetables and settings stay in app-private storage. `allowBackup="false"`. Shared family files contain only the timetable (place, dates, times).

---

## 18. Edge cases checklist

- [ ] OCR misreads (`:` as `.`, `5`↔`S`, `1`↔`l`) → repaired or flagged.
- [ ] OCR misses a whole row → flagged as missing. The user can type it.
- [ ] Image is a screenshot, a cropped table or rotated → still parsed. If the header isn't found, column clustering is used.
- [ ] Image isn't a timetable → "Couldn't find a timetable in this image" + Try another / Enter manually.
- [ ] Table spans two Gregorian months (Mar → Apr) and a year change (Dec → Jan).
- [ ] 29- and 30-day months.
- [ ] Hijri month guess wrong → user changes it on review.
- [ ] Same month imported twice → replace prompt.
- [ ] Table ended, no new one: days 1–7 estimated (carry forward), then last-year data, then NONE (no reminders, clear message).
- [ ] Friday: Jumu'ah label, Jumu'ah before-azan minutes, Jumu'ah jamaat rule.
- [ ] Fixed jamaat time earlier than azan → azan + 15 for that day + warning icon.
- [ ] Before-jamaat time not after azan → skipped.
- [ ] Reminders < 60 s apart → merged.
- [ ] Phone on silent/vibrate → vibration only (unless override). In a call → no voice.
- [ ] Music playing → ducked, then resumed.
- [ ] Two voice reminders overlap → queued.
- [ ] Language switch → UI, widget, next notifications, channel names; voice language independent.
- [ ] Restart, app update, time/timezone change → alarms restored.
- [ ] Exact alarms revoked → inexact fallback + red check + banner.
- [ ] Midnight → widget and Home roll over. Phase changes at azan and jamaat.
- [ ] Family file invalid or edited → refused with a message.
- [ ] Phone in another time zone → times shown as local clock times from the table (the table is for Bhatkal). Show a small note on Home if the device zone ≠ `Asia/Kolkata`: "Times are for Bhatkal".

---

## 19. Testing

### 19.1 Unit tests (JVM)

- **Parser golden test:** feed recorded OCR output (save ML Kit's blocks for `test-fixtures/bhatkal-timetable-shawwal-1447.jpg` as JSON once, in an instrumented run) into `TimetableParser`. The result must equal `test-fixtures/bhatkal-shawwal-1447.expected.json` exactly.
- **Parser robustness:** the same blocks with injected noise (swap `:`→`.`, `5`→`S`, drop a token, shift a row by ±4 px, delete a row) → correct repairs and flags.
- **Validator:** each rule in 6.4 triggers. Suggestion = interpolation (e.g. 3:39 → 3:59).
- **AM/PM inference** for every column, including Dhuhr 11:xx/12:xx/1:xx.
- **DayScheduleResolver:** in table, carry-forward days 1–7, last-year fallback, NONE.
- **Jamaat rules:** after-azan, fixed, fixed-before-azan fallback, Jumu'ah.
- **ReminderPlanner:** counts, Friday, skip rules, merging, matrix toggles, past events dropped, request codes unique.
- **VoiceClipSelector:** every combination of language × gender × kind × prayer × minutes maps to an existing resource (test against `R.raw` names).
- **Formatting:** times (12h/24h, Urdu LTR isolate), durations with plurals (Arabic two/few/many), Hijri.

### 19.2 Instrumented tests

- ML Kit on the fixture image → parsed table equals the expected JSON, or differs only in cells that are flagged.
- Share intent with the fixture image opens the review screen.
- Family file round trip: export → import on a clean install → identical.

### 19.3 Manual checklist for the owner (write it in plain language)

1. Fresh install → onboarding in Urdu → import the real timetable image → fix any yellow cells → save → widget added → test reminder arrives **with voice** while the phone is locked.
2. Wait for a real prayer: heads-up + voice 10 min before azan, azan reminder, "5 minutes left" voice before jamaat, all on time.
3. Phone on silent → vibration only, no voice.
4. Restart the phone → the next reminder still arrives.
5. Send to family → the brother imports the file → his times match.
6. Leave the phone overnight → the Fajr reminders arrive.
7. Run 2 and 6 on **both phones for 3 days** before release.

---

## 20. Phased build plan with acceptance checks

| Phase | Build | Accept when |
|---|---|---|
| 0. Setup | Repo, Gradle, Compose theme + fonts, 3 languages' `app_name`, icon + animated opening screen from `android-resources/` (14.10), CI | App installs with the correct name and icon. Opening it plays the 1-second animation. CI is green |
| 1. Data + Home | Timetable model, repository, resolver, jamaat rules, **load the fixture JSON as a built-in demo**, Home + countdown | Home shows 24 Mar 2026 correctly (azan + jamaat), Hijri from the table. Unit tests pass |
| 2. Import | Share target, photo picker, ML Kit OCR, parser, validator, review/fix screens, save, Timetables screen, manual entry | The real image imports. A deliberately bad value gets flagged and fixed. Golden test passes |
| 3. Family share | Export `.prtt`, receive, validate | File sent on WhatsApp imports in one tap on another phone |
| 4. Settings | Jamaat screen, Reminders & voice screen, Settings | Changing rules updates Home immediately |
| 5. Reminders + voice | Planner, alarm-clock scheduling, receivers, notifications, channels, voice service + clips (preview pack), worker, missed detection, table-expiring notice | All three reminders arrive on time with correct voice, language and gender. Silent mode respected. Survives restart |
| 6. Reliability + onboarding | Reliability screen, OEM steps, test reminder, first-launch intro (14.10) + onboarding (11 steps) | Fresh install walks through setup. Test passes with the phone locked |
| 7. Widgets | Small + Wide, phases, countdown, midnight/phase alarms, RTL | The widget counts to azan, then to jamaat, then moves on. Correct in all 3 languages |
| 8. Languages + polish | All strings (Appendix A as a start), RTL review, channel renaming | Nothing untranslated. Native-speaker review done |
| 9. Release | Final licensed voice clips (Azure or recorded), R8 release build, signing via CI, 3-day test on both phones | Owner and brother get every reminder for 3 days. `v1.0.0` released |

---

## 21. Build, signing, release and installing on phones

- **One release keystore**, backed up in two places. Stored as GitHub Actions secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`), never committed. Losing it means both phones must uninstall and reinstall.
- `versionCode` +1 for every APK given to anyone. `versionName` 1.0.0, 1.0.1, …
- CI: debug build + unit tests on every push. A tag `v*` produces a signed release APK attached to a GitHub Release. Test the R8 release build (add keep rules for ML Kit/serialization if needed).
- **Install:** send the APK → open → allow "Install unknown apps" once → Install.
- **Google developer verification:** enforcement started in a few countries in late 2026 and is planned **globally from 2027 (including India)**. Before then, sideloading works as now. To prepare, register the app under a free **limited-distribution (hobbyist) developer account** in the Android Developer Console. Otherwise installs will need the slower "advanced flow" or a USB/ADB install. Remind the owner as 2027 approaches.

---

## 22. Open items to confirm with the owner

1. Repo `SyedMsidsm/PrayerReminder` and app ID `com.syedmsidsm.prayerreminder` — OK?
2. Does the masjid really use **15 minutes for every prayer**, or are some different (e.g. Maghrib shorter, Fajr longer) or **fixed** (e.g. Isha 8:15 PM, Jumu'ah 1:15 PM)? Set the defaults to match.
3. **Voice:** AI voice (regenerated with Azure under the owner's account) or a **recorded family member**? Male, female or both?
4. Default **voice language**: Urdu?
5. Who imports each month (owner, then "Send to family")?
6. A fluent reviewer for the Arabic and Urdu text, and for the voice sentences.
7. Does the masjid also publish timetables for other places, or is it only ever Bhatkal?

---

## Appendix A — strings.xml (starting translations)

Keys shared by all three files. Translations must be reviewed by a fluent speaker.

### `values/strings.xml` (English)

```xml
<resources>
    <string name="app_name">PrayerReminder</string>
    <string name="prayer_fajr">Fajr</string>
    <string name="prayer_sunrise">Sunrise</string>
    <string name="prayer_dhuhr">Dhuhr</string>
    <string name="prayer_asr">Asr</string>
    <string name="prayer_maghrib">Maghrib</string>
    <string name="prayer_isha">Isha</string>
    <string name="prayer_jumuah">Jumu\'ah</string>
    <string name="am_marker">AM</string>
    <string name="pm_marker">PM</string>

    <string name="azan">Azan</string>
    <string name="jamaat">Jamaat</string>
    <string name="next_prayer">Next prayer</string>
    <string name="now_prayer">Now</string>
    <string name="next_tag">Next</string>
    <string name="today">Today</string>
    <string name="tomorrow">Tomorrow</string>
    <string name="azan_in">Azan in %1$s</string>
    <string name="jamaat_in">Jamaat in %1$s</string>
    <string name="duration_join">%1$s %2$s</string>
    <plurals name="hours"><item quantity="one">%d hour</item><item quantity="other">%d hours</item></plurals>
    <plurals name="minutes"><item quantity="one">%d minute</item><item quantity="other">%d minutes</item></plurals>
    <string name="min_short">%d min</string>
    <string name="hijri_suffix">AH</string>
    <string name="timetable_line">Timetable: %1$s · until %2$s</string>
    <string name="timetable_ended_line">%1$s timetable ended %2$s</string>
    <string name="voice_line">Voice: %1$s before azan · %2$s before jamaat</string>
    <string name="voice_off_line">Voice off</string>
    <string name="estimated_suffix">(estimated)</string>
    <string name="times_for_place">Times are for %1$s</string>

    <string name="banner_estimated">Estimated times — import the new timetable</string>
    <string name="banner_no_timetable">No timetable for today. Import the new timetable.</string>
    <string name="banner_missed">Some reminders didn\'t arrive. Tap to fix.</string>
    <string name="banner_notifications_off">Notifications are off. Tap to fix.</string>

    <string name="notif_before_azan">%1$s azan in %2$s</string>
    <string name="notif_at_azan">%1$s azan — %2$s</string>
    <string name="notif_before_jamaat">%1$s prayer starts in %2$s</string>
    <string name="notif_azan_jamaat_text">Azan %1$s · Jamaat %2$s</string>
    <string name="notif_jamaat_text">Jamaat %1$s</string>
    <string name="notif_new_timetable_title">New timetable needed</string>
    <string name="notif_new_timetable_text">%1$s ends in %2$s. Share the new month\'s timetable to PrayerReminder.</string>
    <string name="notif_test">Test reminder — if you can see and hear this, reminders work.</string>
    <string name="notif_playing">Playing reminder</string>
    <string name="channel_voice">Reminders (voice)</string>
    <string name="channel_sound">Reminders (sound)</string>
    <string name="channel_general">General</string>

    <string name="import_title">Import timetable</string>
    <string name="import_reading">Reading the timetable…</string>
    <string name="import_offline_note">On your phone, no internet needed</string>
    <string name="import_check_title">Check the timetable</string>
    <string name="import_flags">%1$d value(s) look wrong. Tap the yellow box to fix it.</string>
    <string name="import_all_good">All %1$d times look right</string>
    <string name="import_ampm_note">AM/PM is added automatically: Fajr and Sunrise are morning; Dhuhr is midday; the rest are afternoon/evening.</string>
    <string name="import_save">Save timetable</string>
    <string name="import_keep">Keep %1$s</string>
    <string name="import_use">Use %1$s</string>
    <string name="import_neighbours">The day before is %1$s and the day after is %2$s.</string>
    <string name="import_failed">Couldn\'t find a timetable in this image.</string>
    <string name="import_try_another">Try another image</string>
    <string name="import_manual">Enter manually</string>
    <string name="import_replace">Replace the existing %1$s?</string>
    <string name="saved_title">Timetable saved</string>
    <string name="send_family">Send to family</string>
    <string name="send_family_desc">They tap the file and it\'s loaded. No checking needed on their side.</string>
    <string name="family_import_title">Import timetable?</string>
    <string name="family_checked">Checked by the sender</string>
    <string name="family_invalid">This file isn\'t a valid PrayerReminder timetable.</string>
    <string name="import_btn">Import</string>
    <string name="cancel">Cancel</string>
    <string name="done">Done</string>

    <string name="settings">Settings</string>
    <string name="language">Language</string>
    <string name="timetable">Timetable</string>
    <string name="timetables">Timetables</string>
    <string name="import_new">Import new timetable</string>
    <string name="import_file">Import a file</string>
    <string name="all_timetables">All timetables</string>
    <string name="days_left">%1$d days left</string>
    <string name="tt_now">Now</string>
    <string name="tt_next">Next</string>
    <string name="tt_earlier">Earlier</string>
    <string name="tt_active">Active</string>
    <string name="tt_not_imported">Not imported yet · needed by %1$s</string>

    <string name="jamaat_times">Jamaat times</string>
    <string name="jamaat_intro">Set when each prayer starts after its azan. Most masjids start 15 minutes after the azan.</string>
    <string name="jamaat_after">After azan</string>
    <string name="jamaat_fixed">Fixed time</string>
    <string name="jamaat_today">Today: azan %1$s → jamaat %2$s</string>
    <string name="jamaat_fixed_note">Fixed: same time every day, whatever the azan time.</string>
    <string name="jamaat_fixed_before_azan">Fixed time is before azan today — using 15 minutes after azan.</string>
    <string name="jamaat_reset">Reset all to 15 minutes</string>
    <string name="jamaat_after_value">%1$d min after azan</string>

    <string name="reminders_voice">Reminders &amp; voice</string>
    <string name="voice_language">Voice language</string>
    <string name="voice">Voice</string>
    <string name="voice_male">Male</string>
    <string name="voice_female">Female</string>
    <string name="before_azan">Before azan</string>
    <string name="at_azan">At azan</string>
    <string name="before_jamaat">Before jamaat</string>
    <string name="jumuah_before_azan">Jumu\'ah: before azan</string>
    <string name="off">Off</string>
    <string name="which_reminders">Which reminders for each prayer</string>
    <string name="speak_on_silent">Speak when phone is on silent</string>
    <string name="speak_on_silent_desc">Off: on silent or vibrate you get a vibration only.</string>
    <string name="chime">Chime before the voice</string>
    <string name="voice_enabled">Spoken reminders</string>
    <string name="play_sample">Play sample</string>

    <string name="reliability">Reminder reliability check</string>
    <string name="send_test">Send test notification</string>
    <string name="test_wait">Lock your phone now. The test arrives in 1 minute.</string>
    <string name="test_passed">Test passed at %1$s</string>
    <string name="add_widget">Add widget to home screen</string>
    <string name="widget">Widget</string>
    <string name="about">About</string>
    <string name="privacy_line">No internet, no location, no accounts. Everything stays on your phone.</string>
    <string name="widget_small_label">Next prayer</string>
    <string name="widget_wide_label">Prayer times</string>
    <string name="widget_import">Import the new timetable</string>
    <string name="estimated_short">est.</string>

    <string name="onb_welcome">Your masjid\'s azan and jamaat times, with reminders and a widget.</string>
    <string name="onb_timetable">Share the monthly azan timetable image to PrayerReminder (from WhatsApp or your gallery). The app reads it for you.</string>
    <string name="onb_choose_image">Choose timetable image</string>
    <string name="onb_family_file">I got a timetable file from family</string>
    <string name="onb_jamaat">How many minutes after the azan does the prayer start at your masjid?</string>
    <string name="onb_voice">A voice tells you when the azan and the prayer are close.</string>
    <string name="onb_notifications">Allow notifications so you get your prayer reminders.</string>
    <string name="onb_alarms">Allow this so reminders arrive on the exact minute. You may see a small alarm icon in the status bar — that\'s PrayerReminder.</string>
    <string name="onb_battery">Let PrayerReminder run in the background so your phone doesn\'t stop the reminders.</string>
    <string name="onb_phone_specific">Your phone needs one more setting:</string>
    <string name="onb_test">Send a test reminder, then lock your phone. In 1 minute you should see it and hear the voice.</string>
    <string name="onb_widget">See the next azan and jamaat right on your home screen.</string>
    <string name="continue_btn">Continue</string>
    <string name="skip">Skip</string>

    <string-array name="hijri_months">
        <item>Muharram</item><item>Safar</item><item>Rabi al-Awwal</item><item>Rabi al-Thani</item>
        <item>Jumada al-Ula</item><item>Jumada al-Akhirah</item><item>Rajab</item><item>Sha\'ban</item>
        <item>Ramadan</item><item>Shawwal</item><item>Dhul Qa\'dah</item><item>Dhul Hijjah</item>
    </string-array>
</resources>
```

### `values-ar/strings.xml` (Arabic)

```xml
<resources>
    <string name="app_name">تذكير الصلاة</string>
    <string name="prayer_fajr">الفجر</string>
    <string name="prayer_sunrise">الشروق</string>
    <string name="prayer_dhuhr">الظهر</string>
    <string name="prayer_asr">العصر</string>
    <string name="prayer_maghrib">المغرب</string>
    <string name="prayer_isha">العشاء</string>
    <string name="prayer_jumuah">الجمعة</string>
    <string name="am_marker">ص</string>
    <string name="pm_marker">م</string>

    <string name="azan">الأذان</string>
    <string name="jamaat">الإقامة</string>
    <string name="next_prayer">الصلاة القادمة</string>
    <string name="now_prayer">الآن</string>
    <string name="next_tag">التالية</string>
    <string name="today">اليوم</string>
    <string name="tomorrow">غدًا</string>
    <string name="azan_in">الأذان بعد %1$s</string>
    <string name="jamaat_in">الإقامة بعد %1$s</string>
    <string name="duration_join">%1$s و%2$s</string>
    <plurals name="hours"><item quantity="zero">%d ساعة</item><item quantity="one">ساعة</item><item quantity="two">ساعتين</item><item quantity="few">%d ساعات</item><item quantity="many">%d ساعة</item><item quantity="other">%d ساعة</item></plurals>
    <plurals name="minutes"><item quantity="zero">%d دقيقة</item><item quantity="one">دقيقة</item><item quantity="two">دقيقتين</item><item quantity="few">%d دقائق</item><item quantity="many">%d دقيقة</item><item quantity="other">%d دقيقة</item></plurals>
    <string name="min_short">%d د</string>
    <string name="hijri_suffix">هـ</string>
    <string name="timetable_line">جدول %1$s · حتى %2$s</string>
    <string name="timetable_ended_line">انتهى جدول %1$s في %2$s</string>
    <string name="voice_line">الصوت: قبل الأذان بـ%1$s · قبل الإقامة بـ%2$s</string>
    <string name="voice_off_line">الصوت متوقف</string>
    <string name="estimated_suffix">(تقديري)</string>
    <string name="times_for_place">الأوقات لمدينة %1$s</string>

    <string name="banner_estimated">أوقات تقديرية — استورد الجدول الجديد</string>
    <string name="banner_no_timetable">لا يوجد جدول لليوم. استورد الجدول الجديد.</string>
    <string name="banner_missed">لم تصل بعض التنبيهات. اضغط للإصلاح.</string>
    <string name="banner_notifications_off">الإشعارات متوقفة. اضغط للإصلاح.</string>

    <string name="notif_before_azan">أذان %1$s بعد %2$s</string>
    <string name="notif_at_azan">أذان %1$s — %2$s</string>
    <string name="notif_before_jamaat">تبدأ صلاة %1$s بعد %2$s</string>
    <string name="notif_azan_jamaat_text">الأذان %1$s · الإقامة %2$s</string>
    <string name="notif_jamaat_text">الإقامة %1$s</string>
    <string name="notif_new_timetable_title">مطلوب جدول جديد</string>
    <string name="notif_new_timetable_text">ينتهي %1$s بعد %2$s. شارك جدول الشهر الجديد مع التطبيق.</string>
    <string name="notif_test">تنبيه تجريبي — إذا رأيته وسمعته فالتذكيرات تعمل.</string>
    <string name="notif_playing">جارٍ تشغيل التذكير</string>
    <string name="channel_voice">التذكيرات (صوت)</string>
    <string name="channel_sound">التذكيرات (نغمة)</string>
    <string name="channel_general">عام</string>

    <string name="import_title">استيراد الجدول</string>
    <string name="import_reading">جارٍ قراءة الجدول…</string>
    <string name="import_offline_note">على هاتفك، بدون إنترنت</string>
    <string name="import_check_title">راجع الجدول</string>
    <string name="import_flags">%1$d قيمة تبدو خاطئة. اضغط على المربع الأصفر لإصلاحها.</string>
    <string name="import_all_good">كل الأوقات (%1$d) تبدو صحيحة</string>
    <string name="import_ampm_note">يُضاف ص/م تلقائيًا: الفجر والشروق صباحًا، والظهر وقت الظهيرة، والباقي بعد الظهر والمساء.</string>
    <string name="import_save">حفظ الجدول</string>
    <string name="import_keep">إبقاء %1$s</string>
    <string name="import_use">استخدام %1$s</string>
    <string name="import_neighbours">اليوم السابق %1$s واليوم التالي %2$s.</string>
    <string name="import_failed">لم نجد جدولًا في هذه الصورة.</string>
    <string name="import_try_another">جرّب صورة أخرى</string>
    <string name="import_manual">إدخال يدوي</string>
    <string name="import_replace">استبدال جدول %1$s الحالي؟</string>
    <string name="saved_title">تم حفظ الجدول</string>
    <string name="send_family">إرسال إلى العائلة</string>
    <string name="send_family_desc">يضغطون على الملف فيُحمَّل، دون الحاجة إلى المراجعة.</string>
    <string name="family_import_title">استيراد الجدول؟</string>
    <string name="family_checked">راجعه المرسل</string>
    <string name="family_invalid">هذا الملف ليس جدولًا صالحًا للتطبيق.</string>
    <string name="import_btn">استيراد</string>
    <string name="cancel">إلغاء</string>
    <string name="done">تم</string>

    <string name="settings">الإعدادات</string>
    <string name="language">اللغة</string>
    <string name="timetable">جدول الأوقات</string>
    <string name="timetables">الجداول</string>
    <string name="import_new">استيراد جدول جديد</string>
    <string name="import_file">استيراد ملف</string>
    <string name="all_timetables">كل الجداول</string>
    <string name="days_left">بقي %1$d يومًا</string>
    <string name="tt_now">الحالي</string>
    <string name="tt_next">القادم</string>
    <string name="tt_earlier">السابقة</string>
    <string name="tt_active">نشط</string>
    <string name="tt_not_imported">لم يُستورد بعد · مطلوب قبل %1$s</string>

    <string name="jamaat_times">أوقات الإقامة</string>
    <string name="jamaat_intro">حدّد متى تبدأ كل صلاة بعد أذانها. تبدأ معظم المساجد بعد الأذان بـ15 دقيقة.</string>
    <string name="jamaat_after">بعد الأذان</string>
    <string name="jamaat_fixed">وقت ثابت</string>
    <string name="jamaat_today">اليوم: الأذان %1$s ← الإقامة %2$s</string>
    <string name="jamaat_fixed_note">ثابت: نفس الوقت كل يوم مهما كان وقت الأذان.</string>
    <string name="jamaat_fixed_before_azan">الوقت الثابت قبل الأذان اليوم — سيُستخدم 15 دقيقة بعد الأذان.</string>
    <string name="jamaat_reset">إعادة الكل إلى 15 دقيقة</string>
    <string name="jamaat_after_value">بعد الأذان بـ%1$d دقيقة</string>

    <string name="reminders_voice">التذكيرات والصوت</string>
    <string name="voice_language">لغة الصوت</string>
    <string name="voice">الصوت</string>
    <string name="voice_male">رجل</string>
    <string name="voice_female">امرأة</string>
    <string name="before_azan">قبل الأذان</string>
    <string name="at_azan">عند الأذان</string>
    <string name="before_jamaat">قبل الإقامة</string>
    <string name="jumuah_before_azan">الجمعة: قبل الأذان</string>
    <string name="off">إيقاف</string>
    <string name="which_reminders">التذكيرات لكل صلاة</string>
    <string name="speak_on_silent">التحدث والهاتف صامت</string>
    <string name="speak_on_silent_desc">عند الإيقاف: في الوضع الصامت أو الاهتزاز تصلك اهتزازة فقط.</string>
    <string name="chime">نغمة قبل الصوت</string>
    <string name="voice_enabled">التذكيرات الصوتية</string>
    <string name="play_sample">تشغيل عينة</string>

    <string name="reliability">فحص موثوقية التنبيهات</string>
    <string name="send_test">إرسال تنبيه تجريبي</string>
    <string name="test_wait">اقفل هاتفك الآن. سيصل التنبيه خلال دقيقة.</string>
    <string name="test_passed">نجح الاختبار الساعة %1$s</string>
    <string name="add_widget">إضافة الأداة إلى الشاشة الرئيسية</string>
    <string name="widget">الأداة</string>
    <string name="about">حول التطبيق</string>
    <string name="privacy_line">بلا إنترنت، بلا موقع، بلا حسابات. كل شيء يبقى على هاتفك.</string>
    <string name="widget_small_label">الصلاة القادمة</string>
    <string name="widget_wide_label">أوقات الصلاة</string>
    <string name="widget_import">استورد الجدول الجديد</string>
    <string name="estimated_short">تقديري</string>

    <string name="onb_welcome">أوقات الأذان والإقامة في مسجدك، مع تذكيرات وأداة.</string>
    <string name="onb_timetable">شارك صورة جدول الأذان الشهري مع التطبيق (من واتساب أو المعرض)، وسيقرؤها لك.</string>
    <string name="onb_choose_image">اختر صورة الجدول</string>
    <string name="onb_family_file">وصلني ملف جدول من العائلة</string>
    <string name="onb_jamaat">كم دقيقة بعد الأذان تبدأ الصلاة في مسجدك؟</string>
    <string name="onb_voice">صوت يخبرك عند اقتراب الأذان والصلاة.</string>
    <string name="onb_notifications">اسمح بالإشعارات لتصلك تذكيرات الصلاة.</string>
    <string name="onb_alarms">اسمح بهذا لتصل التذكيرات في موعدها تمامًا. قد ترى أيقونة منبه صغيرة في شريط الحالة — إنها من التطبيق.</string>
    <string name="onb_battery">اسمح للتطبيق بالعمل في الخلفية حتى لا يوقف هاتفك التذكيرات.</string>
    <string name="onb_phone_specific">يحتاج هاتفك إلى إعداد إضافي واحد:</string>
    <string name="onb_test">أرسل تنبيهًا تجريبيًا ثم اقفل هاتفك. خلال دقيقة يجب أن تراه وتسمع الصوت.</string>
    <string name="onb_widget">شاهد الأذان والإقامة القادمين على شاشتك الرئيسية.</string>
    <string name="continue_btn">متابعة</string>
    <string name="skip">تخطٍّ</string>

    <string-array name="hijri_months">
        <item>محرم</item><item>صفر</item><item>ربيع الأول</item><item>ربيع الآخر</item>
        <item>جمادى الأولى</item><item>جمادى الآخرة</item><item>رجب</item><item>شعبان</item>
        <item>رمضان</item><item>شوال</item><item>ذو القعدة</item><item>ذو الحجة</item>
    </string-array>
</resources>
```

### `values-ur/strings.xml` (Urdu)

```xml
<resources>
    <string name="app_name">نماز کی یاد دہانی</string>
    <string name="prayer_fajr">فجر</string>
    <string name="prayer_sunrise">طلوعِ آفتاب</string>
    <string name="prayer_dhuhr">ظہر</string>
    <string name="prayer_asr">عصر</string>
    <string name="prayer_maghrib">مغرب</string>
    <string name="prayer_isha">عشاء</string>
    <string name="prayer_jumuah">جمعہ</string>
    <string name="am_marker">AM</string>
    <string name="pm_marker">PM</string>

    <string name="azan">اذان</string>
    <string name="jamaat">جماعت</string>
    <string name="next_prayer">اگلی نماز</string>
    <string name="now_prayer">ابھی</string>
    <string name="next_tag">اگلی</string>
    <string name="today">آج</string>
    <string name="tomorrow">کل</string>
    <string name="azan_in">اذان %1$s میں</string>
    <string name="jamaat_in">جماعت %1$s میں</string>
    <string name="duration_join">%1$s %2$s</string>
    <plurals name="hours"><item quantity="one">%d گھنٹہ</item><item quantity="other">%d گھنٹے</item></plurals>
    <plurals name="minutes"><item quantity="one">%d منٹ</item><item quantity="other">%d منٹ</item></plurals>
    <string name="min_short">%d منٹ</string>
    <string name="hijri_suffix">ہجری</string>
    <string name="timetable_line">نظام الاوقات: %1$s · %2$s تک</string>
    <string name="timetable_ended_line">%1$s کا نظام الاوقات %2$s کو ختم ہوا</string>
    <string name="voice_line">آواز: اذان سے %1$s پہلے · جماعت سے %2$s پہلے</string>
    <string name="voice_off_line">آواز بند</string>
    <string name="estimated_suffix">(اندازاً)</string>
    <string name="times_for_place">اوقات %1$s کے ہیں</string>

    <string name="banner_estimated">اندازاً اوقات — نیا نظام الاوقات شامل کریں</string>
    <string name="banner_no_timetable">آج کا نظام الاوقات نہیں ہے۔ نیا نظام الاوقات شامل کریں۔</string>
    <string name="banner_missed">کچھ یاد دہانیاں نہیں پہنچیں۔ ٹھیک کرنے کے لیے ٹیپ کریں۔</string>
    <string name="banner_notifications_off">نوٹیفکیشن بند ہیں۔ ٹھیک کرنے کے لیے ٹیپ کریں۔</string>

    <string name="notif_before_azan">%1$s کی اذان %2$s میں</string>
    <string name="notif_at_azan">%1$s کی اذان — %2$s</string>
    <string name="notif_before_jamaat">%1$s کی نماز %2$s میں شروع ہوگی</string>
    <string name="notif_azan_jamaat_text">اذان %1$s · جماعت %2$s</string>
    <string name="notif_jamaat_text">جماعت %1$s</string>
    <string name="notif_new_timetable_title">نیا نظام الاوقات چاہیے</string>
    <string name="notif_new_timetable_text">%1$s %2$s میں ختم ہو رہا ہے۔ نئے مہینے کا نظام الاوقات ایپ میں شیئر کریں۔</string>
    <string name="notif_test">آزمائشی یاد دہانی — اگر یہ نظر آئے اور سنائی دے تو یاد دہانیاں کام کر رہی ہیں۔</string>
    <string name="notif_playing">یاد دہانی چل رہی ہے</string>
    <string name="channel_voice">یاد دہانیاں (آواز)</string>
    <string name="channel_sound">یاد دہانیاں (ٹون)</string>
    <string name="channel_general">عمومی</string>

    <string name="import_title">نظام الاوقات شامل کریں</string>
    <string name="import_reading">نظام الاوقات پڑھا جا رہا ہے…</string>
    <string name="import_offline_note">آپ کے فون پر، انٹرنیٹ کی ضرورت نہیں</string>
    <string name="import_check_title">نظام الاوقات جانچیں</string>
    <string name="import_flags">%1$d قدر غلط لگ رہی ہے۔ ٹھیک کرنے کے لیے پیلے خانے پر ٹیپ کریں۔</string>
    <string name="import_all_good">تمام %1$d اوقات درست لگ رہے ہیں</string>
    <string name="import_ampm_note">AM/PM خود لگ جاتا ہے: فجر اور طلوع صبح، ظہر دوپہر، باقی سہ پہر اور شام۔</string>
    <string name="import_save">محفوظ کریں</string>
    <string name="import_keep">%1$s رکھیں</string>
    <string name="import_use">%1$s استعمال کریں</string>
    <string name="import_neighbours">پچھلے دن %1$s اور اگلے دن %2$s ہے۔</string>
    <string name="import_failed">اس تصویر میں نظام الاوقات نہیں ملا۔</string>
    <string name="import_try_another">دوسری تصویر آزمائیں</string>
    <string name="import_manual">خود درج کریں</string>
    <string name="import_replace">موجودہ %1$s بدل دیں؟</string>
    <string name="saved_title">نظام الاوقات محفوظ ہو گیا</string>
    <string name="send_family">گھر والوں کو بھیجیں</string>
    <string name="send_family_desc">وہ فائل پر ٹیپ کریں اور یہ شامل ہو جائے گا، جانچنے کی ضرورت نہیں۔</string>
    <string name="family_import_title">نظام الاوقات شامل کریں؟</string>
    <string name="family_checked">بھیجنے والے نے جانچ لیا ہے</string>
    <string name="family_invalid">یہ فائل درست نظام الاوقات نہیں ہے۔</string>
    <string name="import_btn">شامل کریں</string>
    <string name="cancel">منسوخ</string>
    <string name="done">ہو گیا</string>

    <string name="settings">ترتیبات</string>
    <string name="language">زبان</string>
    <string name="timetable">نظام الاوقات</string>
    <string name="timetables">نظام الاوقات کی فہرست</string>
    <string name="import_new">نیا نظام الاوقات شامل کریں</string>
    <string name="import_file">فائل سے شامل کریں</string>
    <string name="all_timetables">تمام نظام الاوقات</string>
    <string name="days_left">%1$d دن باقی</string>
    <string name="tt_now">موجودہ</string>
    <string name="tt_next">اگلا</string>
    <string name="tt_earlier">پچھلے</string>
    <string name="tt_active">فعال</string>
    <string name="tt_not_imported">ابھی شامل نہیں · %1$s تک چاہیے</string>

    <string name="jamaat_times">جماعت کے اوقات</string>
    <string name="jamaat_intro">ہر نماز اذان کے کتنی دیر بعد شروع ہوتی ہے، یہ طے کریں۔ اکثر مساجد میں اذان کے 15 منٹ بعد جماعت ہوتی ہے۔</string>
    <string name="jamaat_after">اذان کے بعد</string>
    <string name="jamaat_fixed">مقررہ وقت</string>
    <string name="jamaat_today">آج: اذان %1$s ← جماعت %2$s</string>
    <string name="jamaat_fixed_note">مقررہ: ہر دن ایک ہی وقت، اذان چاہے جب ہو۔</string>
    <string name="jamaat_fixed_before_azan">آج مقررہ وقت اذان سے پہلے ہے — اذان کے 15 منٹ بعد استعمال ہوگا۔</string>
    <string name="jamaat_reset">سب کو 15 منٹ پر واپس کریں</string>
    <string name="jamaat_after_value">اذان کے %1$d منٹ بعد</string>

    <string name="reminders_voice">یاد دہانیاں اور آواز</string>
    <string name="voice_language">آواز کی زبان</string>
    <string name="voice">آواز</string>
    <string name="voice_male">مرد</string>
    <string name="voice_female">خاتون</string>
    <string name="before_azan">اذان سے پہلے</string>
    <string name="at_azan">اذان کے وقت</string>
    <string name="before_jamaat">جماعت سے پہلے</string>
    <string name="jumuah_before_azan">جمعہ: اذان سے پہلے</string>
    <string name="off">بند</string>
    <string name="which_reminders">ہر نماز کے لیے کون سی یاد دہانیاں</string>
    <string name="speak_on_silent">فون سائلنٹ ہو تب بھی بولے</string>
    <string name="speak_on_silent_desc">بند ہو تو سائلنٹ یا وائبریٹ پر صرف وائبریشن ہوگی۔</string>
    <string name="chime">آواز سے پہلے ٹون</string>
    <string name="voice_enabled">آواز والی یاد دہانیاں</string>
    <string name="play_sample">سن کر دیکھیں</string>

    <string name="reliability">یاد دہانی کی جانچ</string>
    <string name="send_test">آزمائشی نوٹیفکیشن بھیجیں</string>
    <string name="test_wait">اب اپنا فون لاک کریں۔ آزمائش ایک منٹ میں آئے گی۔</string>
    <string name="test_passed">جانچ کامیاب، وقت: %1$s</string>
    <string name="add_widget">ہوم اسکرین پر وجٹ لگائیں</string>
    <string name="widget">وجٹ</string>
    <string name="about">ایپ کے بارے میں</string>
    <string name="privacy_line">نہ انٹرنیٹ، نہ مقام، نہ اکاؤنٹ۔ سب کچھ آپ کے فون میں رہتا ہے۔</string>
    <string name="widget_small_label">اگلی نماز</string>
    <string name="widget_wide_label">نماز کے اوقات</string>
    <string name="widget_import">نیا نظام الاوقات شامل کریں</string>
    <string name="estimated_short">اندازاً</string>

    <string name="onb_welcome">آپ کی مسجد کے اذان اور جماعت کے اوقات، یاد دہانیوں اور وجٹ کے ساتھ۔</string>
    <string name="onb_timetable">ماہانہ اذان کے نظام الاوقات کی تصویر ایپ میں شیئر کریں (واٹس ایپ یا گیلری سے)، ایپ خود پڑھ لے گی۔</string>
    <string name="onb_choose_image">نظام الاوقات کی تصویر منتخب کریں</string>
    <string name="onb_family_file">مجھے گھر والوں سے فائل ملی ہے</string>
    <string name="onb_jamaat">آپ کی مسجد میں اذان کے کتنے منٹ بعد جماعت ہوتی ہے؟</string>
    <string name="onb_voice">ایک آواز بتاتی ہے جب اذان اور نماز قریب ہوں۔</string>
    <string name="onb_notifications">نوٹیفکیشن کی اجازت دیں تاکہ آپ کو نماز کی یاد دہانیاں ملیں۔</string>
    <string name="onb_alarms">اس کی اجازت دیں تاکہ یاد دہانیاں بالکل وقت پر آئیں۔ اسٹیٹس بار میں الارم کا چھوٹا نشان نظر آ سکتا ہے — وہ اسی ایپ کا ہے۔</string>
    <string name="onb_battery">ایپ کو پس منظر میں چلنے دیں تاکہ فون یاد دہانیاں بند نہ کرے۔</string>
    <string name="onb_phone_specific">آپ کے فون میں ایک اور ترتیب ضروری ہے:</string>
    <string name="onb_test">آزمائشی یاد دہانی بھیجیں اور فون لاک کریں۔ ایک منٹ میں یہ نظر آنی اور آواز سنائی دینی چاہیے۔</string>
    <string name="onb_widget">اگلی اذان اور جماعت اپنی ہوم اسکرین پر دیکھیں۔</string>
    <string name="continue_btn">جاری رکھیں</string>
    <string name="skip">چھوڑیں</string>

    <string-array name="hijri_months">
        <item>محرم</item><item>صفر</item><item>ربیع الاول</item><item>ربیع الثانی</item>
        <item>جمادی الاول</item><item>جمادی الثانی</item><item>رجب</item><item>شعبان</item>
        <item>رمضان</item><item>شوال</item><item>ذوالقعدہ</item><item>ذوالحجہ</item>
    </string-array>
</resources>
```

---

## Appendix B — Logo, app icon and opening-screen resources

**The refined mark:** a gold crescent with **five dots following its inner curve** (like prayer beads, on an arc concentric with the inner edge). The larger **gold centre dot is the next prayer**. The whole mark is tilted −18°. Colours: crescent and centre dot `#D9AE5C`, other dots `#F7F4EC`, on `#0F2A2E`. On light backgrounds: crescent `#B8862F`, dots `#0F2A2E`.

Geometry (100 × 100 units):
- crescent path `M65.33,24.2 A30,30 0 1,0 65.33,75.8 A26,26 0 1,1 65.33,24.2 Z`;
- dots at (52.75, 66.02) r 2.8 · (45.98, 59.25) r 2.8 · **(43.5, 50) r 3.9 gold** · (45.98, 40.75) r 2.8 · (52.75, 33.98) r 2.8;
- the whole mark is wrapped in translate (7.3, 0) + rotate −18° about (42.7, 50).

**Ready-made files. Use these, don't redraw:**
- `android-resources/`: adaptive icon (foreground + monochrome), notification icon, **animated splash icon `avd_splash_icon.xml`**, static splash icon, splash themes, brand colours. Copy them into `app/src/main/res/`.
- `design/logo/*.svg`: the mark (on dark, on light, white, ink) and the app icon (rounded square, round), for any other use.
- `design/images/01-brand/`: PNG renders (512/1024), wordmarks, the logo sheet (with animation frames).

## Appendix C — The folder you were given

```
PrayerReminder/
├── PROMPT.md                         ← the instructions to start with
├── PrayerReminder-build-spec.md      ← this file
├── design/
│   ├── ASSETS.md                     ← list of every image, video and sound
│   ├── images/                       ← every screen, widget, notification, icon (3 languages)
│   ├── logo/                         ← SVG logo files
│   ├── videos/                       ← animations; 05–07 include the real voice audio; 10–14 opening screen & logo
│   ├── audio/
│   │   ├── samples/                  ← listen: how reminders sound (Urdu/Arabic/English, male + female)
│   │   ├── voice-pack-preview/       ← all 180 preview clips + phrases.json (dev only, see 11.3)
│   │   └── chime.wav
│   └── reference/bhatkal-timetable-shawwal-1447.jpg
├── android-resources/               ← drop-in res/ files: icons, animated splash icon, splash themes
├── test-fixtures/
│   ├── bhatkal-timetable-shawwal-1447.jpg
│   └── bhatkal-shawwal-1447.expected.json   ← golden expected parse result
└── tools/make_voice_clips.py         ← sentence list + file naming used for the voice clips
```

*End of specification.*
