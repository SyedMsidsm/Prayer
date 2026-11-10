package com.syedmsidsm.prayerreminder.reliability

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.syedmsidsm.prayerreminder.notifications.NotificationHelper

data class OemGuidance(
    val brandName: String,
    val steps: List<String>,
    val isKnownBrand: Boolean
)

object OemSettingsLauncher {

    fun getGuidance(): OemGuidance = getGuidanceForManufacturer(Build.MANUFACTURER)

    fun getGuidanceForManufacturer(manufacturer: String): OemGuidance {
        val m = manufacturer.lowercase()
        return when {
            m.contains("xiaomi") || m.contains("redmi") || m.contains("poco") -> {
                OemGuidance(
                    brandName = "Xiaomi / Redmi / POCO",
                    steps = listOf(
                        "1. Autostart: Turn ON for PrayerReminder",
                        "2. Battery saver: Choose 'No restrictions'",
                        "3. Lock PrayerReminder in Recent Apps"
                    ),
                    isKnownBrand = true
                )
            }
            m.contains("samsung") -> {
                OemGuidance(
                    brandName = "Samsung",
                    steps = listOf(
                        "1. App info -> Battery -> Choose 'Unrestricted'",
                        "2. Ensure PrayerReminder is not in Sleeping or Deep sleeping apps list"
                    ),
                    isKnownBrand = true
                )
            }
            m.contains("oppo") || m.contains("realme") || m.contains("oneplus") -> {
                OemGuidance(
                    brandName = "OPPO / Realme / OnePlus",
                    steps = listOf(
                        "1. App info -> Battery: Allow background activity",
                        "2. Auto launch: Allow PrayerReminder to start automatically"
                    ),
                    isKnownBrand = true
                )
            }
            m.contains("vivo") || m.contains("iqoo") -> {
                OemGuidance(
                    brandName = "Vivo / iQOO",
                    steps = listOf(
                        "1. Settings -> Battery -> High background power consumption: Allow",
                        "2. Autostart: Allow PrayerReminder to run in background"
                    ),
                    isKnownBrand = true
                )
            }
            m.contains("huawei") || m.contains("honor") -> {
                OemGuidance(
                    brandName = "Huawei / Honor",
                    steps = listOf(
                        "1. Battery -> App launch: Manage manually",
                        "2. Turn Auto-launch, Secondary launch, and Run in background ON"
                    ),
                    isKnownBrand = true
                )
            }
            else -> {
                OemGuidance(
                    brandName = manufacturer.replaceFirstChar { it.uppercase() },
                    steps = listOf(
                        "1. Settings -> Apps -> PrayerReminder",
                        "2. Battery -> Set to Unrestricted or Don't optimize"
                    ),
                    isKnownBrand = false
                )
            }
        }
    }

    fun openSettings(context: Context) {
        val m = Build.MANUFACTURER.lowercase()
        val candidateIntents = mutableListOf<Intent>()

        when {
            m.contains("xiaomi") || m.contains("redmi") || m.contains("poco") -> {
                candidateIntents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity"
                        )
                    )
                )
            }
            m.contains("samsung") -> {
                candidateIntents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.samsung.android.lool",
                            "com.samsung.android.sm.battery.ui.BatteryActivity"
                        )
                    )
                )
            }
            m.contains("oppo") || m.contains("realme") || m.contains("oneplus") -> {
                candidateIntents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.coloros.safecenter",
                            "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                        )
                    )
                )
                candidateIntents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.oplus.battery",
                            "com.oplus.powermanager.fuelgaue.PowerUsageModelActivity"
                        )
                    )
                )
            }
            m.contains("vivo") || m.contains("iqoo") -> {
                candidateIntents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.vivo.permissionmanager",
                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                        )
                    )
                )
                candidateIntents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.iqoo.secure",
                            "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                        )
                    )
                )
            }
            m.contains("huawei") || m.contains("honor") -> {
                candidateIntents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.huawei.systemmanager",
                            "com.huawei.systemmanager.startupmgr.ui.StartupNavigateActivity"
                        )
                    )
                )
            }
        }

        // Standard App details fallback
        val fallback = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        candidateIntents.add(fallback)

        for (intent in candidateIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (_: Exception) {}
        }
    }
}

object ReliabilityChecker {

    fun isNotificationsAllowed(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun areChannelsEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val voiceChan = nm.getNotificationChannel(NotificationHelper.CHANNEL_VOICE)
            val soundChan = nm.getNotificationChannel(NotificationHelper.CHANNEL_SOUND)
            if (voiceChan != null && voiceChan.importance == NotificationManager.IMPORTANCE_NONE) return false
            if (soundChan != null && soundChan.importance == NotificationManager.IMPORTANCE_NONE) return false
        }
        return true
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }
}
