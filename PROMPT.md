# Prompt: build the PrayerReminder Android app

Copy everything below the line into the model (for example Claude Code), together with this whole folder.

---

You are building **PrayerReminder**, a small private Android app for my family in **Bhatkal, India**. Every month our masjid shares an **azan timetable image** (on WhatsApp) with the times for the whole Islamic month. The app:

- **imports that image**: it reads the numbers on the phone and lets me fix anything it got wrong;
- shows today's **azan** and **jamaat** times (jamaat = minutes after azan, default 15, or a fixed time, adjustable per prayer);
- gives **reminders before the azan, at the azan, and before the jamaat**. They can be **spoken aloud in Urdu, Arabic or English**, e.g. "Ten minutes left for the Asr azan", "Five minutes left for Asr prayer to start";
- has a **home-screen widget**;
- lets me **send the checked timetable to my brother**, who loads it with one tap.

It does **not** calculate prayer times and does **not** use location or the internet.

## What you have in this folder

(It arrives as two zips, part 1 and part 2, that unzip into this same `PrayerReminder` folder. If a file mentioned below is missing, ask me for part 2.)

- **`PrayerReminder-build-spec.md`**: the complete specification. **It is the source of truth. Read all of it before writing any code.**
- **`design/images/`**: every screen, widget, notification and the icon, in English, Arabic and Urdu.
- **`design/videos/`**: animations of the flows. Videos 05–07 have the **real voice audio** so you can hear how reminders should sound.
- **`design/audio/`**: listenable samples, the full **preview voice pack** (180 clips + `phrases.json`) and the chime. Read spec Section 11.3 about replacing the preview clips with properly licensed ones before release.
- **`design/reference/`** and **`test-fixtures/`**: the real Bhatkal timetable image and the **exact expected result** of reading it (`bhatkal-shawwal-1447.expected.json`). Use it as the golden test for the importer.
- **`android-resources/`**: ready-made Android `res/` files for the **app icon** and the **animated opening screen** (spec 14.10). Copy them in, don't redraw them.
- **`design/logo/`**: the logo as SVG files.
- **`tools/make_voice_clips.py`**: the exact voice sentences and file names.
- **`design/ASSETS.md`**: a list of every file and what it shows.

If an image and the spec disagree, **follow the spec** and tell me.

## About me

I'm **not a programmer**. So:

- Explain what you're doing in simple words.
- Whenever I need to do something (install a tool, click something, test on my phone), give me **exact step-by-step instructions**.
- If you hit a problem or need a decision, **stop and ask me** with clear options and your recommendation.
- Don't assume I can fix code myself.

## How to work

1. **First**, read the spec, look at the images, watch or listen to the videos and audio. Then ask me the questions in **spec Section 22 (Open items)** before starting.
2. Build **phase by phase** exactly as in **spec Section 20**. After each phase:
   - make sure it builds and its checks and tests pass;
   - commit to GitHub with a clear message;
   - tell me in plain language what now works and **how to try it on my phone**;
   - **wait for my OK** before the next phase.
3. Check the **latest stable version** of every library before adding it.
4. Keep the code clean, simple and well commented. Follow the package structure in the spec.

## Priorities (in this order)

1. **Reminders and the voice must arrive on time**, even on Xiaomi, Vivo, Oppo, Realme and Samsung phones (spec Sections 10–12).
2. **The times must exactly match the imported timetable.** The importer must catch misread numbers (spec Section 6).
3. **Simple to use.** My brother mainly wants the widget.
4. Looks matching the design images.

## Hard rules

- No `INTERNET` permission, no location, no storage permission, no ads, analytics, accounts or servers. Everything runs on the phone.
- No hardcoded text: every string in `strings.xml` in **English, Arabic and Urdu** from the start, with full right-to-left support, including the widget.
- Don't calculate prayer times. Times come only from imported timetables (with the estimated fallbacks in spec 6.6).
- Don't build features marked "Not in v1".
- Never commit the signing keystore or passwords. Remind me to back up the keystore in two places.
- Don't use Hilt or other dependency-injection frameworks. Use the simple `AppContainer`.

Start now: read everything, then ask me the Section 22 questions.
