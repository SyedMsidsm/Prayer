package com.syedmsidsm.prayerreminder.widgets

import android.content.Context
import android.content.res.Configuration
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.syedmsidsm.prayerreminder.AppContainer
import com.syedmsidsm.prayerreminder.PrayerReminderApp
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.util.Locale

/** Widgets follow the language chosen INSIDE the app (English by default, Arabic when selected). */
object WidgetLocale {
    fun wrap(context: Context): Context {
        val rawLang = resolveLanguage(context)
        val lang = if (rawLang.startsWith("ar", ignoreCase = true)) "ar" else "en"
        val locale = Locale.forLanguageTag(lang)
        val cfg = Configuration(context.resources.configuration)
        cfg.setLocale(locale)
        cfg.setLayoutDirection(locale)
        return context.createConfigurationContext(cfg)
    }

    private fun resolveLanguage(context: Context): String {
        val prefsLang = context
            .getSharedPreferences(WidgetConfig.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(WidgetConfig.KEY_LANGUAGE, null)
            ?.takeIf { it.isNotBlank() }

        if (prefsLang != null) return prefsLang

        val app = context.applicationContext as? PrayerReminderApp
        val container = app?.container ?: AppContainer(context.applicationContext)
        val dsLang = runBlocking {
            container.settingsRepository.appLanguage.firstOrNull()
        }?.takeIf { it.isNotBlank() }

        if (dsLang != null) return dsLang

        val appLocales = AppCompatDelegate.getApplicationLocales()
        if (!appLocales.isEmpty) {
            return appLocales.get(0)?.language ?: "en"
        }
        return "en"
    }

    fun is24Hour(context: Context): Boolean {
        val app = context.applicationContext as? PrayerReminderApp
        val container = app?.container ?: AppContainer(context.applicationContext)
        val pref24 = runBlocking {
            container.settingsRepository.use24Hour.firstOrNull()
        }
        return (pref24 == true) || DateFormat.is24HourFormat(context)
    }

    fun isRtl(locale: Locale): Boolean =
        TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL
}
