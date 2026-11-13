package com.syedmsidsm.prayerreminder.ui.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.reliability.ReliabilityChecker
import com.syedmsidsm.prayerreminder.ui.components.MosqueBackdrop
import com.syedmsidsm.prayerreminder.ui.settings.SettingsViewModel
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors

private val AmberAccent = Color(0xFFFBBF24)
private val AmberTextDark = Color(0xFF020617)
private val AmberGlowText = Color(0xFFFCD34D)
private val SanctuaryDarkBg = Color(0xFF03080C)
private val SanctuaryLightBg = Color(0xFFF6F1E6)

@Composable
fun OnboardingScreen(
    viewModel: SettingsViewModel,
    onPickImage: () -> Unit,
    onImportFile: () -> Unit = {},
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by rememberSaveable { mutableIntStateOf(1) }
    val totalSteps = 3

    BackHandler(enabled = currentStep > 1) {
        currentStep--
    }

    val timetables by viewModel.timetables.collectAsState()
    val activeTimetable = timetables.firstOrNull()
    val themeMode by viewModel.themeMode.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()

    val currentAppLocales = AppCompatDelegate.getApplicationLocales()
    val currentLangTag = if (!currentAppLocales.isEmpty) {
        currentAppLocales.get(0)?.language ?: "en"
    } else {
        "en"
    }

    var refreshTrigger by remember { mutableIntStateOf(0) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            refreshTrigger++
            onFinished()
        }
    )

    val isDark = PrayerReminderColors.isDark
    val surfaceBg = if (isDark) SanctuaryDarkBg else SanctuaryLightBg
    val textPrimary = if (isDark) Color.White else Color(0xFF14262A)
    val textMuted = if (isDark) Color(0xFFA8A29E) else Color(0xFF5E6D70)
    val glassCardBg = if (isDark) Color(0xFF0B161E).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.94f)
    val glassBorder = if (isDark) Color.White.copy(alpha = 0.11f) else Color(0xFFE4DCC7)
    val chipUnselectedBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF3EFE4)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceBg)
    ) {
        MosqueBackdrop(
            activeSky = Prayer.ISHA,
            surfaceFadeColor = surfaceBg
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Sanctuary Step Indicator Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.38f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(onClick = { currentStep-- }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.size(38.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (step in 1..totalSteps) {
                        val isActive = step == currentStep
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(6.dp)
                                .width(if (isActive) 26.dp else 7.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (isActive) AmberAccent else Color.White.copy(alpha = 0.25f))
                        )
                    }
                }

                TextButton(onClick = onFinished) {
                    Text(
                        text = stringResource(R.string.skip),
                        color = AmberGlowText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (currentStep) {
                    1 -> StepWelcomeAndLanguageSanctuary(
                        currentLang = currentLangTag,
                        themeMode = themeMode,
                        fontScale = fontScale,
                        glassCardBg = glassCardBg,
                        glassBorder = glassBorder,
                        chipUnselectedBg = chipUnselectedBg,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        onSelectLanguage = { lang ->
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
                            viewModel.setAppLanguage(lang)
                            viewModel.setVoiceLanguage(lang)
                        },
                        onSelectTheme = { mode -> viewModel.setThemeMode(mode) },
                        onSelectFontScale = { scale -> viewModel.setFontScale(scale) },
                        onContinue = {
                            viewModel.setAppLanguage(currentLangTag)
                            currentStep = 2
                        }
                    )
                    2 -> StepPrayerTimesSourceSanctuary(
                        activeTimetablePlace = activeTimetable?.place ?: "Bhatkal",
                        glassCardBg = glassCardBg,
                        glassBorder = glassBorder,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        onPickImage = onPickImage,
                        onContinue = { currentStep = 3 }
                    )
                    3 -> StepTurnOnRemindersSanctuary(
                        refreshKey = refreshTrigger,
                        glassCardBg = glassCardBg,
                        glassBorder = glassBorder,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        onAllowAllAndFinish = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !ReliabilityChecker.isNotificationsAllowed(context)
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onFinished()
                            }
                        },
                        onSkip = onFinished,
                        onRefresh = { refreshTrigger++ }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepWelcomeAndLanguageSanctuary(
    currentLang: String,
    themeMode: String,
    fontScale: String,
    glassCardBg: Color,
    glassBorder: Color,
    chipUnselectedBg: Color,
    textPrimary: Color,
    textMuted: Color,
    onSelectLanguage: (String) -> Unit,
    onSelectTheme: (String) -> Unit,
    onSelectFontScale: (String) -> Unit,
    onContinue: () -> Unit
) {
    Spacer(modifier = Modifier.height(6.dp))

    // Mosque Emblem
    Image(
        painter = painterResource(id = R.drawable.splash_icon_static),
        contentDescription = null,
        modifier = Modifier.size(80.dp)
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = "Assalamu Alaikum",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Never miss an azan or a jamaat in Bhatkal.",
        fontSize = 14.sp,
        color = textMuted,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = glassCardBg,
        border = BorderStroke(1.dp, glassBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language
            Column {
                Text(
                    text = stringResource(R.string.language),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val langs = listOf(
                        "en" to "English",
                        "ar" to "العربية",
                        "ur" to "اردو"
                    )
                    for ((code, label) in langs) {
                        val selected = currentLang == code
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) AmberAccent else chipUnselectedBg,
                            border = BorderStroke(1.dp, if (selected) AmberGlowText else glassBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectLanguage(code) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    color = if (selected) AmberTextDark else textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Theme Mode
            Column {
                val themeTitle = when (currentLang) {
                    "ar" -> "المظهر"
                    "ur" -> "تھیم"
                    else -> "Appearance"
                }
                Text(
                    text = themeTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themes = listOf(
                        "light" to if (currentLang == "ar") "☀️ فاتح" else if (currentLang == "ur") "☀️ لائٹ" else "☀️ Light",
                        "dark" to if (currentLang == "ar") "🌙 داكن" else if (currentLang == "ur") "🌙 ڈارک" else "🌙 Dark",
                        "system" to if (currentLang == "ar") "تلقائي" else if (currentLang == "ur") "سسٹم" else "System"
                    )
                    for ((mode, label) in themes) {
                        val selected = themeMode == mode
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) AmberAccent else chipUnselectedBg,
                            border = BorderStroke(1.dp, if (selected) AmberGlowText else glassBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectTheme(mode) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    color = if (selected) AmberTextDark else textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Font Scale
            Column {
                Text(
                    text = stringResource(R.string.font_size),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val fontChoices = listOf(
                        "small" to stringResource(R.string.font_small),
                        "normal" to stringResource(R.string.font_normal),
                        "large" to stringResource(R.string.font_large),
                        "xl" to stringResource(R.string.font_xl)
                    )
                    for ((scaleKey, scaleLabel) in fontChoices) {
                        val selected = fontScale == scaleKey
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) AmberAccent else chipUnselectedBg,
                            border = BorderStroke(1.dp, if (selected) AmberGlowText else glassBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectFontScale(scaleKey) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = scaleLabel,
                                    color = if (selected) AmberTextDark else textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onContinue,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AmberAccent,
            contentColor = AmberTextDark
        )
    ) {
        Text(
            text = stringResource(R.string.continue_btn),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StepPrayerTimesSourceSanctuary(
    activeTimetablePlace: String,
    glassCardBg: Color,
    glassBorder: Color,
    textPrimary: Color,
    textMuted: Color,
    onPickImage: () -> Unit,
    onContinue: () -> Unit
) {
    var selectedMode by remember { mutableStateOf("builtin") }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "Your prayer times",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = "Choose how to get daily azan & jamaat times.",
        fontSize = 14.sp,
        color = textMuted,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Card A: Built-in Bhatkal schedule
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { selectedMode = "builtin" },
        shape = RoundedCornerShape(22.dp),
        color = glassCardBg,
        border = BorderStroke(
            1.5.dp,
            if (selectedMode == "builtin") AmberAccent else glassBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selectedMode == "builtin") AmberAccent else Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (selectedMode == "builtin") AmberTextDark else AmberAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$activeTimetablePlace times (built-in)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ready right now. Updates automatically every day and works 100% offline.",
                    fontSize = 13.sp,
                    color = textMuted
                )
            }
            if (selectedMode == "builtin") {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AmberAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Card B: Scan masjid timetable
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable {
                selectedMode = "scan"
                onPickImage()
            },
        shape = RoundedCornerShape(22.dp),
        color = glassCardBg,
        border = BorderStroke(
            1.5.dp,
            if (selectedMode == "scan") AmberAccent else glassBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selectedMode == "scan") AmberAccent else Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = if (selectedMode == "scan") AmberTextDark else AmberAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Scan my masjid's timetable",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select a photo of your monthly timetable — our local scanner reads and verifies it.",
                    fontSize = 13.sp,
                    color = textMuted
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(28.dp))

    Button(
        onClick = onContinue,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AmberAccent,
            contentColor = AmberTextDark
        )
    ) {
        Text(
            text = stringResource(R.string.continue_btn),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StepTurnOnRemindersSanctuary(
    refreshKey: Int,
    glassCardBg: Color,
    glassBorder: Color,
    textPrimary: Color,
    textMuted: Color,
    onAllowAllAndFinish: () -> Unit,
    onSkip: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val notifAllowed: Boolean = remember(refreshKey) { ReliabilityChecker.isNotificationsAllowed(context) }
    val exactAllowed = remember(refreshKey) { ReliabilityChecker.canScheduleExactAlarms(context) }
    val batteryUnrestricted = remember(refreshKey) { ReliabilityChecker.isIgnoringBatteryOptimizations(context) }

    Spacer(modifier = Modifier.height(10.dp))

    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(AmberAccent.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = AmberAccent,
            modifier = Modifier.size(38.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Turn on reminders",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = "We'll announce before azan and before jamaat.",
        fontSize = 14.sp,
        color = textMuted,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(22.dp))

    SanctuaryPermissionItem(
        icon = Icons.Default.Notifications,
        title = stringResource(R.string.reliability_notifications),
        isGranted = notifAllowed,
        glassCardBg = glassCardBg,
        glassBorder = glassBorder,
        textPrimary = textPrimary,
        onClick = onAllowAllAndFinish
    )

    Spacer(modifier = Modifier.height(10.dp))

    SanctuaryPermissionItem(
        icon = Icons.Default.AccessAlarm,
        title = stringResource(R.string.reliability_alarms),
        isGranted = exactAllowed,
        glassCardBg = glassCardBg,
        glassBorder = glassBorder,
        textPrimary = textPrimary,
        onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                runCatching {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            }
            onRefresh()
        }
    )

    Spacer(modifier = Modifier.height(10.dp))

    SanctuaryPermissionItem(
        icon = Icons.Default.BatteryChargingFull,
        title = stringResource(R.string.reliability_battery),
        isGranted = batteryUnrestricted,
        glassCardBg = glassCardBg,
        glassBorder = glassBorder,
        textPrimary = textPrimary,
        onClick = {
            runCatching {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
            onRefresh()
        }
    )

    Spacer(modifier = Modifier.height(26.dp))

    Button(
        onClick = onAllowAllAndFinish,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AmberAccent,
            contentColor = AmberTextDark
        )
    ) {
        Text(
            text = "Allow all",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(onClick = onSkip) {
        Text(
            text = "Not now",
            fontSize = 14.sp,
            color = textMuted,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SanctuaryPermissionItem(
    icon: ImageVector,
    title: String,
    isGranted: Boolean,
    glassCardBg: Color,
    glassBorder: Color,
    textPrimary: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = glassCardBg,
        border = BorderStroke(1.dp, glassBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AmberAccent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isGranted) Color(0xFF10B981).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isGranted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = if (isGranted) "Allowed" else "Allow",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGranted) Color(0xFF34D399) else textPrimary
                    )
                }
            }
        }
    }
}
