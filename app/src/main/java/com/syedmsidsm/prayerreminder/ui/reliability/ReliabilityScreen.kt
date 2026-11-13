package com.syedmsidsm.prayerreminder.ui.reliability

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.reliability.OemSettingsLauncher
import com.syedmsidsm.prayerreminder.reliability.ReliabilityChecker
import com.syedmsidsm.prayerreminder.ui.components.MosqueBackdrop
import com.syedmsidsm.prayerreminder.ui.settings.SettingsViewModel
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val AmberAccent = Color(0xFFFBBF24)
private val AmberTextDark = Color(0xFF020617)
private val AmberGlowText = Color(0xFFFCD34D)
private val SanctuaryDarkBg = Color(0xFF03080C)
private val SanctuaryLightBg = Color(0xFFF6F1E6)

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
    val exactAlarmsAllowed = ReliabilityChecker.canScheduleExactAlarms(context)
    val batteryOptOff = ReliabilityChecker.isIgnoringBatteryOptimizations(context)

    val oemGuidance = remember { OemSettingsLauncher.getGuidance() }
    val daysLeft = if (activeTimetable != null) {
        val last = runCatching { LocalDate.parse(activeTimetable.lastDate) }.getOrNull()
        if (last != null) ChronoUnit.DAYS.between(LocalDate.now(), last).toInt().coerceAtLeast(0) else 0
    } else 0

    val isDark = PrayerReminderColors.isDark
    val surfaceBg = if (isDark) SanctuaryDarkBg else SanctuaryLightBg
    val glassCardBg = if (isDark) Color(0xFF0B161E).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.94f)
    val glassBorder = if (isDark) Color.White.copy(alpha = 0.11f) else Color(0xFFE4DCC7)
    val chipUnselectedBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF3EFE4)
    val textPrimary = if (isDark) Color.White else Color(0xFF14262A)
    val textMuted = if (isDark) Color(0xFFA8A29E) else Color(0xFF5E6D70)
    val selectedGoldText = if (isDark) AmberGlowText else Color(0xFF92400E)

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
            // Sanctuary Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.38f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancel),
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.reliability),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Test Reminder Card (Prominent at top)
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.send_test),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.onb_test),
                        fontSize = 12.sp,
                        color = textMuted,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isWaitingForTest) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AmberAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Test arriving in $testCountdownSeconds seconds...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = selectedGoldText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.test_wait),
                                    fontSize = 11.sp,
                                    color = textMuted
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.scheduleTestReminder()
                                isWaitingForTest = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberAccent,
                                contentColor = AmberTextDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.send_test),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (lastTestResult.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = lastTestResult,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = AmberGlowText
                        )
                    }
                }

                // 2. System Permissions Checklist Card
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    Text(
                        text = "System Permissions Checklist",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Notifications
                    SanctuaryChecklistItemRow(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.reliability_notifications),
                        isOk = notifAllowed,
                        textPrimary = textPrimary,
                        border = glassBorder,
                        onFix = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(color = glassBorder, modifier = Modifier.padding(vertical = 10.dp))

                    // 2. Exact Alarms
                    SanctuaryChecklistItemRow(
                        icon = Icons.Default.Alarm,
                        title = stringResource(R.string.reliability_alarms),
                        isOk = exactAlarmsAllowed,
                        textPrimary = textPrimary,
                        border = glassBorder,
                        onFix = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        }
                    )

                    HorizontalDivider(color = glassBorder, modifier = Modifier.padding(vertical = 10.dp))

                    // 3. Battery Optimization
                    SanctuaryChecklistItemRow(
                        icon = Icons.Default.BatteryAlert,
                        title = stringResource(R.string.reliability_battery),
                        isOk = batteryOptOff,
                        textPrimary = textPrimary,
                        border = glassBorder,
                        onFix = {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(color = glassBorder, modifier = Modifier.padding(vertical = 10.dp))

                    // 4. Timetable
                    SanctuaryChecklistItemRow(
                        icon = Icons.Default.DateRange,
                        title = "${stringResource(R.string.reliability_timetable)} (${daysLeft}d left)",
                        isOk = activeTimetable != null && daysLeft > 0,
                        textPrimary = textPrimary,
                        border = glassBorder,
                        onFix = onNavigateToTimetables
                    )
                }

                // 3. Manufacturer Specific Guidance Card
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${oemGuidance.brandName} Settings",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.onb_phone_specific),
                        fontSize = 12.sp,
                        color = textMuted,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    for (step in oemGuidance.steps) {
                        Text(
                            text = "• $step",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { OemSettingsLauncher.openSettings(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = chipUnselectedBg,
                            contentColor = textPrimary
                        ),
                        border = BorderStroke(1.dp, glassBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text(
                            text = "Open phone settings",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dontkillmyapp.com"))
                                context.startActivity(browserIntent)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.dontkillmyapp_note),
                            fontSize = 11.sp,
                            color = selectedGoldText,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = selectedGoldText,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SanctuaryChecklistItemRow(
    icon: ImageVector,
    title: String,
    isOk: Boolean,
    textPrimary: Color,
    border: Color,
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
                tint = AmberAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimary
            )
        }

        if (isOk) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF10B981).copy(alpha = 0.18f),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "OK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6EE7B7)
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onFix() },
                shape = RoundedCornerShape(8.dp),
                color = AmberAccent.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, AmberAccent)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Fix",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberGlowText
                    )
                }
            }
        }
    }
}

@Composable
private fun SanctuaryGlassCard(
    bg: Color,
    border: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
