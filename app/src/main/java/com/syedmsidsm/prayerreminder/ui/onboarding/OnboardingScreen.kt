package com.syedmsidsm.prayerreminder.ui.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.model.JamaatRule
import com.syedmsidsm.prayerreminder.reliability.OemSettingsLauncher
import com.syedmsidsm.prayerreminder.reliability.ReliabilityChecker
import com.syedmsidsm.prayerreminder.ui.settings.SettingsViewModel
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: SettingsViewModel,
    onPickImage: () -> Unit,
    onImportFile: () -> Unit,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 11

    val timetables by viewModel.timetables.collectAsState()
    val activeTimetable = timetables.firstOrNull()

    val currentAppLocales = AppCompatDelegate.getApplicationLocales()
    val currentLangTag = if (!currentAppLocales.isEmpty) {
        currentAppLocales.get(0)?.language ?: "en"
    } else {
        "en"
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { currentStep = 7 }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Step $currentStep of $totalSteps",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrayerReminderColors.Muted,
                        fontWeight = FontWeight.Medium
                    )
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
                    }
                },
                actions = {
                    if (currentStep in listOf(3, 6, 7, 8, 9, 10)) {
                        TextButton(onClick = {
                            if (currentStep < totalSteps) currentStep++ else onFinished()
                        }) {
                            Text(
                                text = stringResource(R.string.skip),
                                color = PrayerReminderColors.Muted,
                                fontWeight = FontWeight.Medium
                            )
                        }
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentStep) {
                1 -> StepLanguage(
                    currentLang = currentLangTag,
                    onSelectLanguage = { lang ->
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
                        viewModel.setAppLanguage(lang)
                        viewModel.setVoiceLanguage(lang)
                    },
                    onContinue = { currentStep = 2 }
                )
                2 -> StepWelcome(onContinue = { currentStep = 3 })
                3 -> StepTimetable(
                    activeTimetable = activeTimetable,
                    onPickImage = onPickImage,
                    onImportFile = onImportFile,
                    onContinue = { currentStep = 4 }
                )
                4 -> StepJamaat(
                    viewModel = viewModel,
                    onContinue = { currentStep = 5 }
                )
                5 -> StepVoiceReminders(
                    viewModel = viewModel,
                    onContinue = { currentStep = 6 }
                )
                6 -> StepNotifications(
                    context = context,
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            currentStep = 7
                        }
                    },
                    onContinue = { currentStep = 7 }
                )
                7 -> StepExactAlarms(
                    context = context,
                    onContinue = { currentStep = 8 }
                )
                8 -> StepBattery(
                    context = context,
                    onContinue = { currentStep = 9 }
                )
                9 -> StepPhoneSpecific(
                    context = context,
                    onContinue = { currentStep = 10 }
                )
                10 -> StepTestReminder(
                    viewModel = viewModel,
                    onContinue = { currentStep = 11 }
                )
                11 -> StepWidget(
                    context = context,
                    onDone = onFinished
                )
            }
        }
    }
}

@Composable
private fun StepLanguage(
    currentLang: String,
    onSelectLanguage: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = painterResource(R.drawable.splash_icon_static),
            contentDescription = null,
            modifier = Modifier.size(90.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.language),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrayerReminderColors.Ink
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val options = listOf(Pair("English", "en"), Pair("العربية", "ar"), Pair("اردو", "ur"))
            for ((label, code) in options) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clickable { onSelectLanguage(code) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (currentLang == code) PrayerReminderColors.Ground else PrayerReminderColors.Surface,
                    border = BorderStroke(1.dp, if (currentLang == code) PrayerReminderColors.Ground else PrayerReminderColors.Line)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            color = if (currentLang == code) PrayerReminderColors.Ivory else PrayerReminderColors.Ink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
        PrimaryButton(text = stringResource(R.string.continue_btn), onClick = onContinue)
    }
}

@Composable
private fun StepWelcome(onContinue: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            painter = painterResource(R.drawable.splash_icon_static),
            contentDescription = null,
            modifier = Modifier.size(110.dp)
        )
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = PrayerReminderColors.Ink
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.onb_welcome),
            style = MaterialTheme.typography.bodyLarge,
            color = PrayerReminderColors.Muted,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
        PrimaryButton(text = stringResource(R.string.continue_btn), onClick = onContinue)
    }
}

@Composable
private fun StepTimetable(
    activeTimetable: com.syedmsidsm.prayerreminder.domain.model.Timetable?,
    onPickImage: () -> Unit,
    onImportFile: () -> Unit,
    onContinue: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.timetable),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrayerReminderColors.Ink
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onb_timetable),
            style = MaterialTheme.typography.bodyMedium,
            color = PrayerReminderColors.Muted,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (activeTimetable != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.OkBg)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PrayerReminderColors.OkText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${activeTimetable.place} · ${activeTimetable.hijriMonthName} ${activeTimetable.hijriYear}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrayerReminderColors.OkText
                        )
                        Text(
                            text = "${activeTimetable.days.size} days loaded",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrayerReminderColors.OkText
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        Button(
            onClick = onPickImage,
            colors = ButtonDefaults.buttonColors(containerColor = PrayerReminderColors.Ground),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(stringResource(R.string.onb_choose_image), fontWeight = FontWeight.Bold, color = PrayerReminderColors.Ivory)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onImportFile,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            border = BorderStroke(1.dp, PrayerReminderColors.Line)
        ) {
            Text(stringResource(R.string.onb_family_file), color = PrayerReminderColors.Ink, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(28.dp))
        PrimaryButton(text = stringResource(R.string.continue_btn), onClick = onContinue)
    }
}

@Composable
private fun StepJamaat(
    viewModel: SettingsViewModel,
    onContinue: () -> Unit
) {
    var minutes by remember { mutableIntStateOf(15) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.jamaat_times),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrayerReminderColors.Ink
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onb_jamaat),
            style = MaterialTheme.typography.bodyMedium,
            color = PrayerReminderColors.Muted,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$minutes minutes after azan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrayerReminderColors.Ink
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrayerReminderColors.Ivory)
                            .clickable { if (minutes > 1) minutes-- },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = null, tint = PrayerReminderColors.Ink)
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrayerReminderColors.Ivory)
                            .clickable { if (minutes < 60) minutes++ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = PrayerReminderColors.Ink)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Today: Asr azan 3:59 PM → jamaat 4:14 PM",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrayerReminderColors.GoldText,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
        PrimaryButton(
            text = stringResource(R.string.continue_btn),
            onClick = {
                val keys = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA", "JUMUAH")
                for (k in keys) viewModel.setJamaatRule(k, JamaatRule.AfterAzan(minutes))
                onContinue()
            }
        )
    }
}

@Composable
private fun StepVoiceReminders(
    viewModel: SettingsViewModel,
    onContinue: () -> Unit
) {
    val voiceLang by viewModel.voiceLanguage.collectAsState()
    val isPlaying by viewModel.audioPlayer.isPlaying.collectAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.voice_enabled),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrayerReminderColors.Ink
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onb_voice),
            style = MaterialTheme.typography.bodyMedium,
            color = PrayerReminderColors.Muted,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val langs = listOf(Pair("اردو", "ur"), Pair("العربية", "ar"), Pair("English", "en"))
            for ((label, code) in langs) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clickable { viewModel.setVoiceLanguage(code) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (voiceLang == code) PrayerReminderColors.Ground else PrayerReminderColors.Surface,
                    border = BorderStroke(1.dp, if (voiceLang == code) PrayerReminderColors.Ground else PrayerReminderColors.Line)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            color = if (voiceLang == code) PrayerReminderColors.Ivory else PrayerReminderColors.Ink,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = { if (isPlaying) viewModel.stopSample() else viewModel.playSample("before_azan", 10) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(46.dp),
            border = BorderStroke(1.dp, PrayerReminderColors.Line)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = PrayerReminderColors.GoldText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.play_sample), color = PrayerReminderColors.Ink, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(36.dp))
        PrimaryButton(text = stringResource(R.string.continue_btn), onClick = onContinue)
    }
}

@Composable
private fun StepNotifications(
    context: Context,
    onRequestPermission: () -> Unit,
    onContinue: () -> Unit
) {
    val isAllowed = ReliabilityChecker.isNotificationsAllowed(context)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = PrayerReminderColors.GoldText,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrayerReminderColors.Ink
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onb_notifications),
            style = MaterialTheme.typography.bodyMedium,
            color = PrayerReminderColors.Muted,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(36.dp))

        if (isAllowed) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PrayerReminderColors.OkBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✓ Notifications allowed",
                    color = PrayerReminderColors.OkText,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(text = stringResource(R.string.continue_btn), onClick = onContinue)
        } else {
            PrimaryButton(text = "Allow notifications", onClick = onRequestPermission)
        }
    }
}

@Composable
private fun StepExactAlarms(
    context: Context,
    onContinue: () -> Unit
) {
    val canSchedule = ReliabilityChecker.canScheduleExactAlarms(context)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = PrayerReminderColors.GoldText,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Exact Timing",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrayerReminderColors.Ink
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onb_alarms),
            style = MaterialTheme.typography.bodyMedium,
            color = PrayerReminderColors.Muted,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(36.dp))

        if (!canSchedule && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrayerReminderColors.Ground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Allow exact alarms", color = PrayerReminderColors.Ivory, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        PrimaryButton(text = stringResource(R.string.continue_btn), onClick = onContinue)
    }
}

@Composable
private fun StepBattery(
    context: Context,
    onContinue: () -> Unit
) {
    val isIgnoring = ReliabilityChecker.isIgnoringBatteryOptimizations(context)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Battery Optimization",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrayerReminderColors.Ink
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onb_battery),
            style = MaterialTheme.typography.bodyMedium,
            color = PrayerReminderColors.Muted,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(36.dp))

        if (!isIgnoring) {
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrayerReminderColors.Ground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Turn off battery optimization", color = PrayerReminderColors.Ivory, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        PrimaryButton(text = stringResource(R.string.continue_btn), onClick = onContinue)
    }
}

@Composable
private fun StepPhoneSpecific(
    context: Context,
    onContinue: () -> Unit
) {
    val guidance = remember { OemSettingsLauncher.getGuidance() }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${guidance.brandName} Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrayerReminderColors.Ink
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onb_phone_specific),
            style = MaterialTheme.typography.bodyMedium,
            color = PrayerReminderColors.Muted
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                for (step in guidance.steps) {
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = PrayerReminderColors.Ink,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { OemSettingsLauncher.openSettings(context) },
            colors = ButtonDefaults.buttonColors(containerColor = PrayerReminderColors.Ground),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Open phone settings", color = PrayerReminderColors.Ivory, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
        PrimaryButton(text = stringResource(R.string.reliability_i_done_it), onClick = onContinue)
    }
}

@Composable
private fun StepTestReminder(
    viewModel: SettingsViewModel,
    onContinue: () -> Unit
) {
    var countdown by remember { mutableIntStateOf(0) }
    var waiting by remember { mutableStateOf(false) }

    LaunchedEffect(waiting) {
        if (waiting) {
            countdown = 60
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            waiting = false
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Test Reminder",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrayerReminderColors.Ink
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onb_test),
            style = MaterialTheme.typography.bodyMedium,
            color = PrayerReminderColors.Muted,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(28.dp))

        if (waiting) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrayerReminderColors.WarnBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Test arrives in $countdown s", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrayerReminderColors.WarnText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(stringResource(R.string.test_wait), style = MaterialTheme.typography.bodySmall, color = PrayerReminderColors.WarnText)
                }
            }
        } else {
            Button(
                onClick = {
                    viewModel.scheduleTestReminder()
                    waiting = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrayerReminderColors.Gold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(stringResource(R.string.send_test), color = PrayerReminderColors.Ink, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        PrimaryButton(text = stringResource(R.string.continue_btn), onClick = onContinue)
    }
}

@Composable
private fun StepWidget(
    context: Context,
    onDone: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(20.dp))
        Icon(
            imageVector = Icons.Default.Widgets,
            contentDescription = null,
            tint = PrayerReminderColors.GoldText,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.widget),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrayerReminderColors.Ink
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onb_widget),
            style = MaterialTheme.typography.bodyMedium,
            color = PrayerReminderColors.Muted,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(36.dp))
        PrimaryButton(text = stringResource(R.string.done), onClick = onDone)
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = PrayerReminderColors.Ground),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        Text(text = text, color = PrayerReminderColors.Ivory, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
