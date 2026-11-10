# PrayerReminder — start here

This folder has everything planned for the app: the instructions for the model that builds it, the full plan, all pictures, videos, voice sounds, the logo, and ready-made Android files.

## What's inside

| Folder / file | What it is |
|---|---|
| **PROMPT.md** | The message to give the building model. Copy everything below its line |
| **PrayerReminder-build-spec.md** | The full plan: how every part of the app works |
| design/images/ | 81 pictures: every screen, widget, notification and the logo (English, Arabic, Urdu) |
| design/videos/ | 14 videos + a GIF. 05, 06, 07 have the real voice reminders with sound. 10 and 11 are the opening animations |
| design/audio/samples/ | Listen to how reminders sound (Urdu, Arabic, English, male and female) |
| design/audio/voice-pack-preview/ | All 180 spoken sentences (preview voices) |
| design/logo/ | The logo as SVG files |
| android-resources/ | Ready-made Android files for the icon and the animated opening screen |
| test-fixtures/ | Your Bhatkal timetable image + the exact times the app must read from it |
| tools/ | The voice sentence list used to make the clips |

## It comes in two zips

- **PrayerReminder-part1-plan-and-design.zip**: this guide, the prompt, the plan, Android files, timetable fixture, voice sounds, and pictures 01–05.
- **PrayerReminder-part2-media.zip**: pictures 06–07 (notifications, widgets) and all videos.

Unzip **both into the same place**. They fill the same `PrayerReminder` folder. If the model only accepts one upload at a time, give it part 1 first, then part 2.

## How to start the build

1. Unzip both zips into the same place on your computer.
2. Open the folder with the building model (for example, Claude Code: open a terminal in this folder and start it there).
3. Open **PROMPT.md**, copy everything below the line, and paste it as your first message.
4. The model will read everything and then ask you some questions. Have these answers ready:
   - Does your masjid start the jamaat **15 minutes after the azan for every prayer**? Or are some different, or at a fixed time (e.g. Isha 8:15 PM, Jumu'ah 1:15 PM)?
   - Voice: **AI voice** (made under your own free Azure account) or **recorded by a family member**? Male, female or both?
   - Default voice language: **Urdu**?
   - Repo name `SyedMsidsm/PrayerReminder` and app ID `com.syedmsidsm.prayerreminder` — OK?
   - Who imports the timetable each month and sends it to family?
   - Someone who can check the Arabic and Urdu wording?
5. The model builds in phases and stops after each one to show you how to try it. Say "OK, next phase" when you're happy.

## When the app is built: testing

Try these on **both phones** for about 3 days:

1. Fresh install → setup in Urdu → import this month's timetable image → fix any yellow boxes → save.
2. Add the widget to the home screen.
3. "Send test notification", lock the phone, and check you see it **and hear the voice**.
4. Real prayers: the voice **10 min before azan**, the **azan** reminder, and the voice **5 min before jamaat** all arrive on time.
5. Put the phone on silent: you should get a vibration only.
6. Restart the phone: reminders still arrive.
7. "Send to family" → your brother opens the file → his times match yours.
8. Leave the phone overnight: the Fajr reminders arrive.

## Reporting changes back

For each problem or change, write:

- **Phone:** brand and model (e.g. Redmi Note 12)
- **Where:** which screen or feature (e.g. widget, Asr reminder, import)
- **What happened:** what you saw or heard (a screenshot or screen recording helps a lot)
- **What you wanted instead:** what it should do

Then bring that list back to this chat and we'll plan the changes together.
