package com.syedmsidsm.prayerreminder.ui.reliability

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.reliability.OemSettingsLauncher
import com.syedmsidsm.prayerreminder.reliability.ReliabilityChecker
import com.syedmsidsm.prayerreminder.ui.settings.SettingsViewModel
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReliabilityScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToTimetables: () -> Unit
) {
    val context = LocalContext.current
    val timetables by viewModel.timetables.collectAsState()
    val activeTimetable = timetables.firstOrNull()
    val lastTestResult by viewModel.lastTestResult.collectAsState()

    var testCountdownSeconds by remember { mutableIntStateOf(0) }
    var isWaitingForTest by remember { mutableStateOf(false) }

    LaunchedEffect(isWaitingForTest) {
        if (isWaitingForTest) {
            testCountdownSeconds = 60
            while (testCountdownSeconds > 0) {
                delay(1000)
                testCountdownSeconds--
            }
            isWaitingForTest = false
        }
    }

    val notifAllowed = ReliabilityChecker.isNotificationsAllowed(context)
    val channelsEnabled = ReliabilityChecker.areChannelsEnabled(context)
    val exactAlarmsAllowed = ReliabilityChecker.canScheduleExactAlarms(context)
    val batteryOptOff = ReliabilityChecker.isIgnoringBatteryOptimizations(context)

    val oemGuidance = remember { OemSettingsLauncher.getGuidance() }
    val daysLeft = if (activeTimetable != null) {
        val last = runCatching { LocalDate.parse(activeTimetable.lastDate) }.getOrNull()
        if (last != null) ChronoUnit.DAYS.between(LocalDate.now(), last).toInt().coerceAtLeast(0) else 0
    } else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reliability),
                        color = PrayerReminderColors.Ink,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancel),
                            tint = PrayerReminderColors.Ink
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Test Reminder Card (Prominent at top)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Ground)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = PrayerReminderColors.Gold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.send_test),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrayerReminderColors.Ivory
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.onb_test),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrayerReminderColors.OnDarkMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isWaitingForTest) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PrayerReminderColors.WarnBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Test arriving in $testCountdownSeconds seconds...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrayerReminderColors.WarnText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.test_wait),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrayerReminderColors.WarnText
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.scheduleTestReminder()
                                isWaitingForTest = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrayerReminderColors.Gold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.send_test),
                                color = PrayerReminderColors.Ink,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (lastTestResult.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = lastTestResult,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = PrayerReminderColors.Gold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Checklist Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "System Permissions Checklist",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrayerReminderColors.Ink
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Notifications
                    ChecklistItemRow(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.reliability_notifications),
                        isOk = notifAllowed,
                        onFix = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(color = PrayerReminderColors.Line, modifier = Modifier.padding(vertical = 8.dp))

                    // 2. Exact Alarms
                    ChecklistItemRow(
                        icon = Icons.Default.Alarm,
                        title = stringResource(R.string.reliability_alarms),
                        isOk = exactAlarmsAllowed,
                        onFix = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        }
                    )

                    HorizontalDivider(color = PrayerReminderColors.Line, modifier = Modifier.padding(vertical = 8.dp))

                    // 3. Battery Optimization
                    ChecklistItemRow(
                        icon = Icons.Default.BatteryAlert,
                        title = stringResource(R.string.reliability_battery),
                        isOk = batteryOptOff,
                        onFix = {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(color = PrayerReminderColors.Line, modifier = Modifier.padding(vertical = 8.dp))

                    // 4. Timetable
                    ChecklistItemRow(
                        icon = Icons.Default.DateRange,
                        title = "${stringResource(R.string.reliability_timetable)} (${daysLeft}d left)",
                        isOk = activeTimetable != null && daysLeft > 0,
                        onFix = onNavigateToTimetables
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Manufacturer Specific Guidance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = PrayerReminderColors.GoldText,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${oemGuidance.brandName} Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrayerReminderColors.Ink
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.onb_phone_specific),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrayerReminderColors.Muted
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    for (step in oemGuidance.steps) {
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = PrayerReminderColors.Ink,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { OemSettingsLauncher.openSettings(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrayerReminderColors.Ground),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text(
                            text = "Open phone settings",
                            color = PrayerReminderColors.Ivory,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .clickable {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dontkillmyapp.com"))
                                context.startActivity(browserIntent)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.dontkillmyapp_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = PrayerReminderColors.GoldText,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = PrayerReminderColors.GoldText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChecklistItemRow(
    icon: ImageVector,
    title: String,
    isOk: Boolean,
    onFix: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrayerReminderColors.Ink,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = PrayerReminderColors.Ink
            )
        }

        if (isOk) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = PrayerReminderColors.OkBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PrayerReminderColors.OkText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "OK",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrayerReminderColors.OkText
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onFix() },
                color = PrayerReminderColors.WarnBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = PrayerReminderColors.WarnText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Fix",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrayerReminderColors.WarnText
                    )
                }
            }
        }
    }
}
