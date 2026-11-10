# PrayerReminder: design assets (v2: timetable, jamaat, voice)

Sample data in the pictures comes from the **real Bhatkal timetable for Shawwal 1447** (20 Mar – 18 Apr 2026). "Today" is **Tuesday 24 March 2026 (5 Shawwal)**. Jamaat = azan + 15 min, except **Isha set to a fixed 8:15 PM** to show the fixed-time option. Screens are 390×844 dp exported at 2×. Widgets are 3× on transparent backgrounds.

## images/01-brand
**logo-sheet.png** (refined logo, icon variants, small sizes, the 1-second opening animation frame by frame, palette) · wordmark-on-dark.png · wordmark-on-light-transparent.png · mark-on-light-1024-transparent.png · icon-and-colors.png · feature-graphic-1024x500.png · ic_launcher-512.png · ic_launcher_round-512.png · ic_launcher_foreground-432-transparent.png · ic_launcher_monochrome-432-white.png

## logo (SVG)
prayerreminder-mark-on-dark.svg · prayerreminder-mark-on-light.svg · prayerreminder-mark-white.svg · prayerreminder-mark-ink.svg · prayerreminder-app-icon.svg · prayerreminder-app-icon-round.svg

## images/02-onboarding (spec 14.2)
onboarding-01 … 11: language, welcome, **timetable**, **jamaat time**, **voice reminders**, notifications, alarms & reminders, battery, phone-specific (Xiaomi), test, widget. Extras: 03a (timetable step before importing), 10b (test waiting).

## images/03-home (spec 14.3)
| File | Shows |
|---|---|
| home-before-azan-{en,ar,ur} | Next prayer Asr: azan 3:59 PM, jamaat 4:14 PM, countdown to azan |
| home-between-azan-and-jamaat-{en,ar,ur} | After the azan: counting down to jamaat |
| home-friday-jumuah-{en,ar,ur} | Friday 27 March: Dhuhr shown as Jumu'ah |
| home-after-isha-en | Next prayer is tomorrow's Fajr |
| home-estimated-times-new-timetable-needed-{en,ur} | 20 April: table ended, estimated times + banner |
| home-missed-reminders-banner-en | Missed reminders banner |

## images/04-timetable-import (spec 6, 7, 14.4–14.5)
import-01-share-image-to-app · import-02-reading · import-03-check-one-value-flagged (full 30-day grid, one misread value 3:39 in yellow) · import-04-fix-the-value · import-05-check-all-good · import-06-saved-send-to-family · import-07-brother-receives-family-file-ur · timetables-list

## images/05-settings (spec 14.6–14.9)
settings-{en,ar,ur} · jamaat-times-en (after azan / fixed time per prayer) · reminders-and-voice-en (voice language, male/female, minute choices, per-prayer matrix, silent/chime) · reliability-check-en

## images/06-notifications (spec 10.5)
For each language: 1-before-azan (3:49) · 2-at-azan (3:59) · 3-before-jamaat (4:09) · jumuah-before-azan. Plus new-timetable-needed-{en,ur}. The speaker label shows the voice is playing.

## images/07-widgets (spec 13)
widgets-overview-{en,ar,ur} (before azan · between azan and jamaat · Friday), individual small/wide widgets per language, Friday versions, and before-jamaat versions.

## videos (1920×1080, 30 fps, MP4)
| File | Shows |
|---|---|
| 01-logo-intro.mp4 | Logo build-up |
| 02-onboarding-walkthrough.mp4 | All 11 setup steps |
| 03-import-timetable-check-and-share.mp4 | Share image → app reads it → flagged value → fix → save → send to family |
| 04-language-switch-en-ar-ur.mp4 | Home in English → Arabic → Urdu |
| 05-reminders-with-voice-urdu.mp4 | **With sound.** Lock screen: 10 min before azan → at azan → 5 min before jamaat, with chime + Urdu voice |
| 06-reminders-with-voice-arabic.mp4 | Same, Arabic voice |
| 07-reminders-with-voice-english.mp4 | Same, English voice |
| 08-widget-azan-then-jamaat-countdown.mp4 | Widget counts to azan, then to jamaat, then moves to the next prayer |
| 09-home-bell-toggle.mp4 | Bell turns a prayer's reminders off and on |
| 10-opening-screen-every-launch.mp4 | Tap the icon → it grows into the opening screen → 1-second logo animation → Home |
| 11-first-launch-intro.mp4 | First launch only: logo animation → name in English, Arabic, Urdu → into setup |
| 12-opening-screen-phone-only.mp4 | Same as 10, phone screen only (780×1688) for exact timing |
| 13-first-launch-intro-phone-only.mp4 | Same as 11, phone screen only |
| 14-logo-animation-vertical-1080x1920.mp4 | Logo animation in portrait, e.g. for a WhatsApp status |
| opening-screen.gif | Small looping preview of the opening screen |

## audio
| Path | What |
|---|---|
| samples/how-it-sounds-{urdu,arabic,english}-asr.mp3 | The three Asr reminders in a row, as the app would play them (chime + voice) |
| samples/how-it-sounds-{urdu,arabic,english}-jumuah.mp3 | Jumu'ah: 30 min before azan + 10 min before jamaat |
| samples/female-voice-{urdu,arabic,english}.mp3 | The same sentence in the female voice, to compare |
| samples/female_*_before_azan_asr_10.mp3 | Raw female clips |
| samples/chime.mp3 and chime.wav (in audio/) | The two-note chime played before the voice |
| voice-pack-preview/{ur,ar,en}/*.mp3 | All 180 male sentence clips (60 per language) + phrases.json. **Preview only**; see spec 11.3 for release clips |

## reference
bhatkal-timetable-shawwal-1447.jpg: the real timetable image the importer must read.
