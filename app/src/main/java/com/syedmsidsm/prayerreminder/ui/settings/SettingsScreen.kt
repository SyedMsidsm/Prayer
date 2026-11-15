package com.syedmsidsm.prayerreminder.ui.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.ui.components.MosqueBackdrop
import com.syedmsidsm.prayerreminder.ui.components.SanctuaryBottomNavBar
import com.syedmsidsm.prayerreminder.ui.components.SanctuaryTab
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import com.syedmsidsm.prayerreminder.widgets.LargeWidgetProvider
import com.syedmsidsm.prayerreminder.widgets.MediumWidgetProvider
import com.syedmsidsm.prayerreminder.widgets.SmallWidgetProvider
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val AmberAccent = Color(0xFFFBBF24)
private val AmberTextDark = Color(0xFF020617)
private val AmberGlowText = Color(0xFFFCD34D)
private val SanctuaryDarkBg = Color(0xFF03080C)
private val SanctuaryLightBg = Color(0xFFF6F1E6)

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToJamaat: () -> Unit,
    onNavigateToRemindersVoice: () -> Unit,
    onNavigateToTimetables: () -> Unit,
    onImportNewTimetable: () -> Unit,
    onNavigateToReliability: () -> Unit = {},
    onAddWidget: () -> Unit = {}
) {
    val context = LocalContext.current
    val timetables by viewModel.timetables.collectAsState()
    val activeTimetable = timetables.firstOrNull()
    val voiceOn by viewModel.voiceEnabled.collectAsState()
    val beforeAzanOffsets by viewModel.remBeforeAzanOffsets.collectAsState()
    val beforeJamaatOffsets by viewModel.remBeforeJamaatOffsets.collectAsState()
    val azanReciter by viewModel.azanReciter.collectAsState()
    val playDua by viewModel.playDuaAfterAzan.collectAsState()

    val use24Hour by viewModel.use24Hour.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()

    val currentAppLocales = AppCompatDelegate.getApplicationLocales()
    val currentLangTag = if (!currentAppLocales.isEmpty) {
        currentAppLocales.get(0)?.language ?: "en"
    } else {
        "en"
    }

    val isDark = PrayerReminderColors.isDark
    val surfaceBg = if (isDark) SanctuaryDarkBg else SanctuaryLightBg
    val glassCardBg = if (isDark) Color(0xFF0B161E).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.94f)
    val glassBorder = if (isDark) Color.White.copy(alpha = 0.11f) else Color(0xFFE4DCC7)
    val chipUnselectedBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF3EFE4)
    val textPrimary = if (isDark) Color.White else Color(0xFF14262A)
    val textMuted = if (isDark) Color(0xFFA8A29E) else Color(0xFF5E6D70)
    val selectedGoldText = if (isDark) AmberGlowText else Color(0xFF92400E)
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl || currentLangTag in listOf("ar", "ur")

    fun pinWidget(receiverClass: Class<*>, label: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = AppWidgetManager.getInstance(context)
            if (mgr.isRequestPinAppWidgetSupported) {
                mgr.requestPinAppWidget(
                    ComponentName(context, receiverClass),
                    null,
                    null
                )
                Toast.makeText(context, context.getString(R.string.add_widget), Toast.LENGTH_SHORT).show()
                return
            }
        }
        onAddWidget()
    }

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
            // Minimalist Sanctuary Top Bar
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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.cancel),
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.settings),
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
                    .padding(top = 6.dp, bottom = 144.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val today = LocalDate.now()
                val currentTable = timetables.firstOrNull { table ->
                    val first = runCatching { LocalDate.parse(table.firstDate) }.getOrNull()
                    val last = runCatching { LocalDate.parse(table.lastDate) }.getOrNull()
                    first != null && last != null && !today.isBefore(first) && !today.isAfter(last)
                } ?: activeTimetable

                val rawPlace = currentTable?.place ?: "Bhatkal, Karnataka"
                val activePlace = when {
                    rawPlace.contains("Bhatkal", ignoreCase = true) && currentLangTag == "ar" -> "باتكال، كارناتاكا"
                    rawPlace.contains("Bhatkal", ignoreCase = true) && currentLangTag == "ur" -> "بھٹکل، کرناٹک"
                    else -> rawPlace
                }
                val activeMonth = currentTable?.let { "${it.hijriMonthName} ${it.hijriYear}" } ?: stringResource(R.string.settings_timetable_title)

                val lastDate = currentTable?.let { runCatching { LocalDate.parse(it.lastDate) }.getOrNull() }
                val daysLeft = if (lastDate != null) ChronoUnit.DAYS.between(today, lastDate).toInt().coerceAtLeast(0) else 0

                // 1. MASJID TIMETABLE SCHEDULE CARD (Bhatkal Timings & Active Table)
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.settings_timetable_title),
                                color = textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = AmberAccent.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = stringResource(R.string.settings_offline_timetable_badge),
                                color = selectedGoldText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = chipUnselectedBg,
                        border = BorderStroke(1.dp, glassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = activePlace,
                                    color = textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (daysLeft > 0) {
                                    Text(
                                        text = stringResource(R.string.days_left, daysLeft),
                                        color = selectedGoldText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$activeMonth · ${stringResource(R.string.settings_timetable_desc)}",
                                color = textMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = glassBorder)
                    Spacer(modifier = Modifier.height(4.dp))

                    SettingsNavigationRow(
                        icon = Icons.Default.Upload,
                        title = stringResource(R.string.import_new),
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        onClick = onImportNewTimetable
                    )
                    SettingsNavigationRow(
                        icon = Icons.Default.DateRange,
                        title = stringResource(R.string.all_timetables),
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        onClick = onNavigateToTimetables
                    )
                }

                // 2. MINIMALIST HOME SCREEN WIDGETS (Interactive Previews & 1-Tap Pin)
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.settings_widgets_title),
                                color = textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.settings_widgets_desc),
                        color = textMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val nightGradient = Brush.verticalGradient(
                        listOf(Color(0xFF0B1030), Color(0xFF1B1F5A), Color(0xFF3A2C78))
                    )
                    val dayGradient = Brush.verticalGradient(
                        listOf(Color(0xFF1B5FA8), Color(0xFF3F8FD8), Color(0xFF7AB9EE))
                    )
                    val duskGradient = Brush.verticalGradient(
                        listOf(Color(0xFF2A2466), Color(0xFF8A3F7A), Color(0xFFF08A4B))
                    )
                    val glassPanelBg = Color(0x80060A22)
                    val glassPanelBorder = Color(0x33FFFFFF)
                    val highlightBg = Color(0xEBFFFFFF)
                    val highlightText = Color(0xFF1A1F3C)
                    val highlightSubText = Color(0xFF3A3F5C)

                    // 1) Small (2x2) Auto-Sky Widget Preview & Pin
                    WidgetPreviewCard(
                        title = stringResource(R.string.widget_small_name),
                        description = stringResource(R.string.widget_small_desc),
                        previewContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(148.dp)
                                    .background(nightGradient, RoundedCornerShape(24.dp))
                                    .clip(RoundedCornerShape(24.dp))
                                    .padding(12.dp)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_moon),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.TopEnd)
                                )
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "NEXT · FAJR",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp
                                    )
                                    Column {
                                        Text(
                                            text = "2:14:05",
                                            color = Color.White,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "until adhan",
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 9.sp
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(glassPanelBg, RoundedCornerShape(12.dp))
                                            .border(1.dp, glassPanelBorder, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Adhan", color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp)
                                            Text("5:06 AM", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Namaz", color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp)
                                            Text("5:21 AM", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Sun, 15 Nov", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("4 Jumada II 1448", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        },
                        onPin = { pinWidget(SmallWidgetProvider::class.java, "Prayer · Small (2×2)") },
                        isDark = isDark,
                        chipBg = chipUnselectedBg,
                        border = glassBorder,
                        textPrimary = textPrimary,
                        textMuted = textMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2) Medium (4x2) Auto-Sky Widget Preview & Pin
                    WidgetPreviewCard(
                        title = stringResource(R.string.widget_medium_name),
                        description = stringResource(R.string.widget_medium_desc),
                        previewContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(142.dp)
                                    .background(dayGradient, RoundedCornerShape(24.dp))
                                    .clip(RoundedCornerShape(24.dp))
                                    .padding(12.dp)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_sun_day),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(top = 14.dp, end = 4.dp)
                                )
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Sunday, 15 Nov 2026", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("4 Jumada al-Akhirah 1448", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "Asr in 1:20:08",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val cells = listOf(
                                            Triple("Fajr", "5:06", "5:21"),
                                            Triple("Dhuhr", "12:10", "12:25"),
                                            Triple("Asr", "3:32", "3:45"),
                                            Triple("Maghrib", "5:56", "5:58"),
                                            Triple("Isha", "7:12", "7:27")
                                        )
                                        cells.forEachIndexed { idx, (pName, adhanT, namazT) ->
                                            val hi = idx == 2
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(
                                                        if (hi) highlightBg else glassPanelBg,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (hi) Color.Transparent else glassPanelBorder,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(pName, color = if (hi) highlightText else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                                Text("Adhan", color = if (hi) highlightSubText else Color.White.copy(alpha = 0.85f), fontSize = 7.sp, maxLines = 1)
                                                Text(adhanT, color = if (hi) highlightText else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                                Text("Namaz", color = if (hi) highlightSubText else Color.White.copy(alpha = 0.85f), fontSize = 7.sp, maxLines = 1)
                                                Text(namazT, color = if (hi) highlightText else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        onPin = { pinWidget(MediumWidgetProvider::class.java, "Prayer · Medium (4×2)") },
                        isDark = isDark,
                        chipBg = chipUnselectedBg,
                        border = glassBorder,
                        textPrimary = textPrimary,
                        textMuted = textMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3) Large (4x4) Auto-Sky Widget Preview & Pin
                    WidgetPreviewCard(
                        title = stringResource(R.string.widget_large_name),
                        description = stringResource(R.string.widget_large_desc),
                        previewContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(duskGradient, RoundedCornerShape(24.dp))
                                    .clip(RoundedCornerShape(24.dp))
                                    .padding(12.dp)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_moon),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(26.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(top = 18.dp, end = 6.dp)
                                )
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Sunday, 15 Nov 2026", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("4 Jumada al-Akhirah 1448", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("NEXT PRAYER", color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("Maghrib", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text("in 0:36:12", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(glassPanelBg, RoundedCornerShape(12.dp))
                                            .border(1.dp, glassPanelBorder, RoundedCornerShape(12.dp))
                                            .padding(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        val rows = listOf(
                                            Triple("Fajr", "5:06 AM", "5:21 AM"),
                                            Triple("Sunrise", "6:17 AM", "—"),
                                            Triple("Dhuhr", "12:10 PM", "12:25 PM"),
                                            Triple("Asr", "3:32 PM", "3:45 PM"),
                                            Triple("Maghrib", "5:56 PM", "5:58 PM"),
                                            Triple("Isha", "7:12 PM", "7:27 PM")
                                        )
                                        rows.forEachIndexed { idx, (rName, rAdhan, rNamaz) ->
                                            val hi = idx == 4
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (hi) highlightBg else Color.Transparent,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                            ) {
                                                Text(rName, modifier = Modifier.weight(1.3f), color = if (hi) highlightText else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                Text(rAdhan, modifier = Modifier.weight(1f), color = if (hi) highlightText else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                Text(rNamaz, modifier = Modifier.weight(1f), color = if (hi) highlightText else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        onPin = { pinWidget(LargeWidgetProvider::class.java, "Prayer · Large (4×4)") },
                        isDark = isDark,
                        chipBg = chipUnselectedBg,
                        border = glassBorder,
                        textPrimary = textPrimary,
                        textMuted = textMuted
                    )
                }

                // 3. JAMAAT TIMES CARD (Clean navigation to full Jamaat setup)
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    SettingsItemRow(
                        icon = Icons.Default.AccessTime,
                        title = stringResource(R.string.jamaat_times),
                        subtitle = stringResource(R.string.jamaat_settings_summary),
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        iconBg = chipUnselectedBg,
                        onClick = onNavigateToJamaat
                    )
                }

                // 4. AZAN RECITERS, DUA & REMINDERS VOICE CARD
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    val reciterLabel = when (azanReciter) {
                        "nurdin_hamza", "nurdin" -> stringResource(R.string.reciter_nurdin_hamza)
                        "najee_qazaz", "najee" -> stringResource(R.string.reciter_najee_qazaz)
                        else -> stringResource(R.string.reciter_ali_mala)
                    }
                    val azanListStr = beforeAzanOffsets.sortedDescending().joinToString(", ") { "${it}m" }.ifEmpty { "-" }
                    val jamListStr = beforeJamaatOffsets.sortedDescending().joinToString(", ") { "${it}m" }.ifEmpty { "-" }
                    val duaStatus = if (playDua) stringResource(R.string.dua_on) else stringResource(R.string.dua_off)
                    val voiceSummary = if (voiceOn) {
                        "$reciterLabel • $duaStatus\n${stringResource(R.string.reminders_summary_line, azanListStr, jamListStr)}"
                    } else {
                        stringResource(R.string.voice_off_line)
                    }

                    SettingsItemRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = stringResource(R.string.reminders_voice),
                        subtitle = voiceSummary,
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        iconBg = chipUnselectedBg,
                        onClick = onNavigateToRemindersVoice
                    )
                }

                // 5. TIME FORMAT, APPEARANCE, LANGUAGE & RELIABILITY
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    // Time Format Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.time_format),
                                color = textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        SanctuarySegmentedToggle(
                            options = listOf("12h" to "12h", "24h" to "24h"),
                            selectedKey = if (use24Hour) "24h" else "12h",
                            bg = if (isDark) Color.Black.copy(alpha = 0.38f) else chipUnselectedBg,
                            border = glassBorder,
                            textMuted = textMuted,
                            modifier = Modifier.width(130.dp),
                            onSelect = { viewModel.setUse24Hour(it == "24h") }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = glassBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Language Selection
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.language),
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChoiceChip(
                            label = "English",
                            isSelected = currentLangTag == "en",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                                viewModel.setAppLanguage("en")
                                viewModel.setVoiceLanguage("en")
                            }
                        )
                        ChoiceChip(
                            label = "العربية",
                            isSelected = currentLangTag == "ar",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar"))
                                viewModel.setAppLanguage("ar")
                                viewModel.setVoiceLanguage("ar")
                            }
                        )
                        ChoiceChip(
                            label = "اردو",
                            isSelected = currentLangTag == "ur",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ur"))
                                viewModel.setAppLanguage("ur")
                                viewModel.setVoiceLanguage("ur")
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = glassBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Appearance (Theme Mode)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val themeTitle = when (currentLangTag) {
                            "ar" -> "المظهر (فاتح / داكن)"
                            "ur" -> "تھیم (لائٹ / ڈارک)"
                            else -> "Appearance (Light / Dark)"
                        }
                        Text(
                            text = themeTitle,
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChoiceChip(
                            label = "☀️ ${stringResource(R.string.theme_light)}",
                            isSelected = themeMode == "light",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setThemeMode("light") }
                        )
                        ChoiceChip(
                            label = "🌙 ${stringResource(R.string.theme_dark)}",
                            isSelected = themeMode == "dark",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setThemeMode("dark") }
                        )
                        ChoiceChip(
                            label = stringResource(R.string.theme_system),
                            isSelected = themeMode == "system",
                            unselectedBg = chipUnselectedBg,
                            border = glassBorder,
                            textPrimary = textPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setThemeMode("system") }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = glassBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Font Size
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.font_size),
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val fontOptions = listOf(
                            "small" to stringResource(R.string.font_small),
                            "normal" to stringResource(R.string.font_normal),
                            "large" to stringResource(R.string.font_large),
                            "xl" to stringResource(R.string.font_xl)
                        )
                        for ((scaleKey, scaleLabel) in fontOptions) {
                            ChoiceChip(
                                label = scaleLabel,
                                isSelected = fontScale == scaleKey,
                                unselectedBg = chipUnselectedBg,
                                border = glassBorder,
                                textPrimary = textPrimary,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setFontScale(scaleKey) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = glassBorder)

                    // Reliability Settings Row
                    SettingsItemRow(
                        icon = Icons.Default.CheckCircle,
                        title = stringResource(R.string.reliability),
                        subtitle = stringResource(R.string.privacy_line),
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        iconBg = chipUnselectedBg,
                        onClick = onNavigateToReliability
                    )
                }

                // 6. ABOUT CARD
                SanctuaryGlassCard(bg = glassCardBg, border = glassBorder) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.about),
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${stringResource(R.string.app_name)} v3.0.0",
                        color = textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.privacy_line),
                        color = textMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Floating Bottom Pill Navigation
        SanctuaryBottomNavBar(
            activeTab = SanctuaryTab.SETTINGS,
            onHomeClick = onBack,
            onSettingsClick = {},
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun WidgetPreviewCard(
    title: String,
    description: String,
    previewContent: @Composable () -> Unit,
    onPin: () -> Unit,
    isDark: Boolean,
    chipBg: Color,
    border: Color,
    textPrimary: Color,
    textMuted: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = chipBg,
        border = BorderStroke(1.dp, border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        color = textMuted,
                        fontSize = 10.sp,
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onPin,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberAccent,
                        contentColor = AmberTextDark
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.pin_widget),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            previewContent()
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
        shape = RoundedCornerShape(24.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SanctuarySegmentedToggle(
    options: List<Pair<String, String>>,
    selectedKey: String,
    bg: Color,
    border: Color,
    textMuted: Color,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onSelect: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = modifier
    ) {
        Row(modifier = Modifier.padding(3.dp)) {
            for ((key, label) in options) {
                val active = key == selectedKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) AmberAccent else Color.Transparent)
                        .clickable { onSelect(key) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (active) AmberTextDark else textMuted,
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    isSelected: Boolean,
    unselectedBg: Color,
    border: Color,
    textPrimary: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) AmberAccent else unselectedBg,
        border = BorderStroke(1.dp, if (isSelected) AmberGlowText else border)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) AmberTextDark else textPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    textColor: Color,
    mutedColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 11.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AmberAccent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = mutedColor,
            modifier = Modifier.size(13.dp)
        )
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    textColor: Color,
    mutedColor: Color,
    iconBg: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 2.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(iconBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AmberAccent,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = mutedColor,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = mutedColor,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun WidgetThemeChip(
    label: String,
    swatchColor: Color,
    isSelected: Boolean,
    unselectedBg: Color,
    border: Color,
    textPrimary: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) AmberAccent else unselectedBg,
        border = BorderStroke(1.dp, if (isSelected) AmberGlowText else border)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(swatchColor)
                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSelected) AmberTextDark else textPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}
