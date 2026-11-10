# Android resources for PrayerReminder (logo + opening screen)

Drop these into `app/src/main/res/` (same folder names). All files are well-formed XML generated from the same geometry as `design/logo/*.svg`.

| File | What it is |
|---|---|
| drawable/ic_launcher_foreground.xml | Adaptive app icon foreground (mark inside the 66dp safe zone) |
| drawable/ic_launcher_monochrome.xml | Monochrome layer for Android 13+ themed icons |
| mipmap-anydpi-v26/ic_launcher(_round).xml | Adaptive icon definitions (background = `@color/ic_launcher_background` #0F2A2E) |
| drawable/ic_stat_prayer.xml | Notification small icon (white) |
| drawable/avd_splash_icon.xml | **Animated** opening-screen icon (Android 12+). 1000 ms: crescent sweeps in 0–450 ms, dots pop in 350–890 ms (80 ms apart), gold ring pulses 750–1000 ms |
| drawable/splash_icon_static.xml | Static icon used on Android 8–11 (the compat splash can't animate) |
| values/colors_brand.xml | Brand colours |
| values/themes_splash.xml, values-v31/themes_splash.xml | `Theme.PrayerReminder.Starting` — set it as the theme of `MainActivity` in the manifest |

See spec section 14.10 for the Kotlin side (`installSplashScreen()`, keep-on-screen rule, exit animation) and the first-launch intro.
