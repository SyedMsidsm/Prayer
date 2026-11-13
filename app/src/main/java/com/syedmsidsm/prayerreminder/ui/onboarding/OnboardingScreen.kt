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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.syedmsidsm.prayerreminder.reliability.ReliabilityChecker
import com.syedmsidsm.prayerreminder.ui.settings.SettingsViewModel
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (step in 1..totalSteps) {
                            val isActive = step == currentStep
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(8.dp)
                                    .width(if (isActive) 28.dp else 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isActive) PrayerReminderColors.Gold
                                        else PrayerReminderColors.Line
                                    )
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (currentStep > 1) {
                        IconButton(onClick = { currentStep-- }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = PrayerReminderColors.Ink
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                },
                actions = {
                    TextButton(onClick = onFinished) {
                        Text(
                            text = stringResource(R.string.skip),
                            color = PrayerReminderColors.Muted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrayerReminderColors.Ivory)
            )
        },
        containerColor = PrayerReminderColors.Ivory
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentStep) {
                1 -> StepWelcomeAndLanguage(
                    currentLang = currentLangTag,
                    themeMode = themeMode,
                    fontScale = fontScale,
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
                2 -> StepPrayerTimesSource(
                    activeTimetablePlace = activeTimetable?.place ?: "Bhatkal",
                    onPickImage = onPickImage,
                    onContinue = { currentStep = 3 }
                )
                3 -> StepTurnOnReminders(
                    refreshKey = refreshTrigger,
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

@Composable
private fun StepWelcomeAndLanguage(
    currentLang: String,
    themeMode: String,
    fontScale: String,
    onSelectLanguage: (String) -> Unit,
    onSelectTheme: (String) -> Unit,
    onSelectFontScale: (String) -> Unit,
    onContinue: () -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))

    Box(
        modifier = Modifier
            .size(92.dp)
            .clip(CircleShape)
            .background(PrayerReminderColors.Ground),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_icon_static),
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "Assalamu Alaikum",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = PrayerReminderColors.Ink,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = "Never miss an azan or a jamaat in Bhatkal.",
        style = MaterialTheme.typography.bodyLarge,
        color = PrayerReminderColors.Muted,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(26.dp))

    Text(
        text = stringResource(R.string.language),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = PrayerReminderColors.Ink
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val langs = listOf(
            Pair("en", "English"),
            Pair("ar", "العربية"),
            Pair("ur", "اردو")
        )
        for ((code, label) in langs) {
            val isSelected = currentLang == code
            val selectedBg = if (PrayerReminderColors.isDark) PrayerReminderColors.Gold else PrayerReminderColors.Ground
            val selectedText = if (PrayerReminderColors.isDark) PrayerReminderColors.Ground else PrayerReminderColors.CardTextLight
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clickable { onSelectLanguage(code) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) selectedBg else PrayerReminderColors.Surface,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PrayerReminderColors.Gold else PrayerReminderColors.Line
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) selectedText else PrayerReminderColors.Ink
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(22.dp))

    val themeTitle = when (currentLang) {
        "ar" -> "المظهر"
        "ur" -> "تھیم"
        else -> "Appearance"
    }
    Text(
        text = themeTitle,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = PrayerReminderColors.Ink
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val themes = listOf(
            Pair("light", if (currentLang == "ar") "☀️ فاتح" else if (currentLang == "ur") "☀️ لائٹ" else "☀️ Light"),
            Pair("dark", if (currentLang == "ar") "🌙 داكن" else if (currentLang == "ur") "🌙 ڈارک" else "🌙 Dark"),
            Pair("system", if (currentLang == "ar") "تلقائي" else if (currentLang == "ur") "سسٹم" else "System")
        )
        for ((mode, label) in themes) {
            val isSelected = themeMode == mode
            val selectedBg = if (PrayerReminderColors.isDark) PrayerReminderColors.Gold else PrayerReminderColors.Ground
            val selectedText = if (PrayerReminderColors.isDark) PrayerReminderColors.Ground else PrayerReminderColors.CardTextLight
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clickable { onSelectTheme(mode) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) selectedBg else PrayerReminderColors.Surface,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PrayerReminderColors.Gold else PrayerReminderColors.Line
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) selectedText else PrayerReminderColors.Ink
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(22.dp))

    Text(
        text = stringResource(R.string.font_size),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = PrayerReminderColors.Ink
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val fontChoices = listOf(
            "small" to stringResource(R.string.font_small),
            "normal" to stringResource(R.string.font_normal),
            "large" to stringResource(R.string.font_large),
            "xl" to stringResource(R.string.font_xl)
        )
        for ((scaleKey, scaleLabel) in fontChoices) {
            val isSelected = fontScale == scaleKey
            val selectedBg = if (PrayerReminderColors.isDark) PrayerReminderColors.Gold else PrayerReminderColors.Ground
            val selectedText = if (PrayerReminderColors.isDark) PrayerReminderColors.Ground else PrayerReminderColors.CardTextLight
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clickable { onSelectFontScale(scaleKey) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) selectedBg else PrayerReminderColors.Surface,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PrayerReminderColors.Gold else PrayerReminderColors.Line
                )
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Text(
                        text = scaleLabel,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) selectedText else PrayerReminderColors.Ink,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(28.dp))

    Button(
        onClick = onContinue,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (PrayerReminderColors.isDark) PrayerReminderColors.Gold else PrayerReminderColors.Ground,
            contentColor = if (PrayerReminderColors.isDark) PrayerReminderColors.Ground else PrayerReminderColors.CardTextLight
        )
    ) {
        Text(
            text = stringResource(R.string.continue_btn),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StepPrayerTimesSource(
    activeTimetablePlace: String,
    onPickImage: () -> Unit,
    onContinue: () -> Unit
) {
    var selectedMode by remember { mutableStateOf("builtin") }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "Your prayer times",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = PrayerReminderColors.Ink,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Pick how you want to get daily azan & jamaat times.",
        style = MaterialTheme.typography.bodyLarge,
        color = PrayerReminderColors.Muted,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Card A: Built-in Bhatkal schedule (Default Selected)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedMode = "builtin" },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface),
        border = BorderStroke(
            width = if (selectedMode == "builtin") 2.5.dp else 1.dp,
            color = if (selectedMode == "builtin") PrayerReminderColors.Gold else PrayerReminderColors.Line
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PrayerReminderColors.Ground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = PrayerReminderColors.Gold,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$activeTimetablePlace times (built-in)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrayerReminderColors.Ink
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ready now. Updates automatically every day of the year and works 100% offline.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrayerReminderColors.Muted
                )
            }
            if (selectedMode == "builtin") {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(PrayerReminderColors.Gold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = PrayerReminderColors.Ground,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Card B: Scan masjid timetable
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                selectedMode = "scan"
                onPickImage()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface),
        border = BorderStroke(
            width = if (selectedMode == "scan") 2.5.dp else 1.dp,
            color = if (selectedMode == "scan") PrayerReminderColors.Gold else PrayerReminderColors.Line
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PrayerReminderColors.Ivory),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = PrayerReminderColors.Ink,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Scan my masjid's timetable",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrayerReminderColors.Ink
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pick a photo of your monthly timetable sheet — we read and verify it on your phone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrayerReminderColors.Muted
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = onContinue,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (PrayerReminderColors.isDark) PrayerReminderColors.Gold else PrayerReminderColors.Ground,
            contentColor = if (PrayerReminderColors.isDark) PrayerReminderColors.Ground else PrayerReminderColors.CardTextLight
        )
    ) {
        Text(
            text = stringResource(R.string.continue_btn),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StepTurnOnReminders(
    refreshKey: Int,
    onAllowAllAndFinish: () -> Unit,
    onSkip: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val notifAllowed: Boolean = remember(refreshKey) { ReliabilityChecker.isNotificationsAllowed(context) }
    val exactAllowed = remember(refreshKey) { ReliabilityChecker.canScheduleExactAlarms(context) }
    val batteryUnrestricted = remember(refreshKey) { ReliabilityChecker.isIgnoringBatteryOptimizations(context) }

    Spacer(modifier = Modifier.height(12.dp))

    Box(
        modifier = Modifier
            .size(84.dp)
            .clip(CircleShape)
            .background(PrayerReminderColors.Gold.copy(alpha = 0.22f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = PrayerReminderColors.GoldText,
            modifier = Modifier.size(44.dp)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "Turn on reminders",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = PrayerReminderColors.Ink,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "We'll speak 10 min before azan and 5 min before jamaat.",
        style = MaterialTheme.typography.bodyLarge,
        color = PrayerReminderColors.Muted,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    PermissionItemCard(
        icon = Icons.Default.Notifications,
        title = "Notifications",
        isGranted = notifAllowed,
        onClick = onAllowAllAndFinish
    )

    Spacer(modifier = Modifier.height(10.dp))

    PermissionItemCard(
        icon = Icons.Default.AccessAlarm,
        title = "Exact alarms",
        isGranted = exactAllowed,
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

    PermissionItemCard(
        icon = Icons.Default.BatteryChargingFull,
        title = "Run in background",
        isGranted = batteryUnrestricted,
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

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = onAllowAllAndFinish,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (PrayerReminderColors.isDark) PrayerReminderColors.Gold else PrayerReminderColors.Ground,
            contentColor = if (PrayerReminderColors.isDark) PrayerReminderColors.Ground else PrayerReminderColors.CardTextLight
        )
    ) {
        Text(
            text = "Allow all",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(onClick = onSkip) {
        Text(
            text = "Not now",
            style = MaterialTheme.typography.titleMedium,
            color = PrayerReminderColors.Ink,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PermissionItemCard(
    icon: ImageVector,
    title: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = PrayerReminderColors.Surface,
        border = BorderStroke(1.dp, PrayerReminderColors.Line)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrayerReminderColors.Ink,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = PrayerReminderColors.Ink,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isGranted) PrayerReminderColors.OkBg else PrayerReminderColors.Ivory
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isGranted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PrayerReminderColors.OkText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = if (isGranted) "Allowed" else "Allow",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isGranted) PrayerReminderColors.OkText else PrayerReminderColors.Ink
                    )
                }
            }
        }
    }
}
