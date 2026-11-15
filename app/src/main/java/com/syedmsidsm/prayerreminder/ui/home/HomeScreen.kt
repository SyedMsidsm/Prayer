package com.syedmsidsm.prayerreminder.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.ConfigurationCompat
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.HijriNames
import com.syedmsidsm.prayerreminder.domain.model.NextPrayer
import com.syedmsidsm.prayerreminder.domain.model.NextPrayerPhase
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.domain.model.ScheduleSource
import com.syedmsidsm.prayerreminder.ui.components.MosqueSilhouette
import com.syedmsidsm.prayerreminder.ui.components.SanctuaryBottomNavBar
import com.syedmsidsm.prayerreminder.ui.components.SanctuarySkyThemes
import com.syedmsidsm.prayerreminder.ui.components.SanctuaryTab
import com.syedmsidsm.prayerreminder.ui.format.TimeFormatter
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import java.time.format.DateTimeFormatter
import java.util.Locale

private val AmberBright = Color(0xFFFBBF24)
private val AmberSoft = Color(0xFFFCD34D)
private val AmberTint = Color(0xFFFDE68A)
private val EmeraldSoft = Color(0xFF6EE7B7)

/**
 * ONE sky for the whole screen.
 *
 * Why the old version showed a seam: the hero (mosque + countdown) had its own
 * background that ended in a solid dark "ground" edge, and the prayer list below
 * had a second gradient that started fresh. Where they met you saw a hard line
 * right behind the "Remaining" row, and the orange horizon glow sat behind the
 * list instead of behind the mosque.
 *
 * Fix: draw a single gradient behind everything, put the horizon glow where the
 * mosque actually stands, and fade the mosque's base into the sky.
 * The header and the list must both have a TRANSPARENT background.
 */
data class SkyPalette(
    val top: Color,        // zenith
    val upper: Color,      // upper sky
    val horizon: Color,    // glow at the mosque base
    val afterGlow: Color,  // glow dying out below the horizon
    val ground: Color,     // where the list sits
    val deep: Color,       // very bottom (behind nav pill)
)

// Dark-mode immersive sky palettes across all 6 prayer phases
val FajrSky = SkyPalette(
    top = Color(0xFF12102E),
    upper = Color(0xFF33235A),
    horizon = Color(0xFFD96B43),
    afterGlow = Color(0xFF3A203A),
    ground = Color(0xFF101324),
    deep = Color(0xFF060812),
)

val SunriseSky = SkyPalette(
    top = Color(0xFF182A4A),
    upper = Color(0xFF38537A),
    horizon = Color(0xFFF59E0B),
    afterGlow = Color(0xFF473028),
    ground = Color(0xFF121824),
    deep = Color(0xFF070A10),
)

val DhuhrSky = SkyPalette(
    top = Color(0xFF0C2D48),
    upper = Color(0xFF19537A),
    horizon = Color(0xFFEAB308),
    afterGlow = Color(0xFF1D3549),
    ground = Color(0xFF0C1826),
    deep = Color(0xFF050B12),
)

val AsrSky = SkyPalette(
    top = Color(0xFF1A253A),
    upper = Color(0xFF4A3D4D),
    horizon = Color(0xFFEA7B26),
    afterGlow = Color(0xFF36252B),
    ground = Color(0xFF121420),
    deep = Color(0xFF06070D),
)

val MaghribSky = SkyPalette(
    top = Color(0xFF190E2E),
    upper = Color(0xFF561D46),
    horizon = Color(0xFFEA580C),
    afterGlow = Color(0xFF38192A),
    ground = Color(0xFF120E1E),
    deep = Color(0xFF06040C),
)

val NightSky = SkyPalette(
    top = Color(0xFF0A1330),
    upper = Color(0xFF1B2A52),
    horizon = Color(0xFF8A5530),
    afterGlow = Color(0xFF2A2230),
    ground = Color(0xFF0B1220),
    deep = Color(0xFF04070D),
)

// Light-mode immersive sky palettes (atmospheric hero dome + warm parchment ground for the prayer list)
val FajrLightSky = SkyPalette(
    top = Color(0xFF2A2152),
    upper = Color(0xFF63407A),
    horizon = Color(0xFFF08A5D),
    afterGlow = Color(0xFFE6C8B4),
    ground = Color(0xFFF6F1E6),
    deep = Color(0xFFECE3CE),
)

val SunriseLightSky = SkyPalette(
    top = Color(0xFF1E3F66),
    upper = Color(0xFF4A789C),
    horizon = Color(0xFFFBBF24),
    afterGlow = Color(0xFFEED6B3),
    ground = Color(0xFFF6F1E6),
    deep = Color(0xFFECE3CE),
)

val DhuhrLightSky = SkyPalette(
    top = Color(0xFF144266),
    upper = Color(0xFF2B6FA3),
    horizon = Color(0xFFF5C451),
    afterGlow = Color(0xFFE6D8BE),
    ground = Color(0xFFF6F1E6),
    deep = Color(0xFFECE3CE),
)

val AsrLightSky = SkyPalette(
    top = Color(0xFF243754),
    upper = Color(0xFF6E5359),
    horizon = Color(0xFFF59E0B),
    afterGlow = Color(0xFFEAD0B3),
    ground = Color(0xFFF6F1E6),
    deep = Color(0xFFECE3CE),
)

val MaghribLightSky = SkyPalette(
    top = Color(0xFF2B1644),
    upper = Color(0xFF7A2852),
    horizon = Color(0xFFF97316),
    afterGlow = Color(0xFFE5C1AE),
    ground = Color(0xFFF6F1E6),
    deep = Color(0xFFECE3CE),
)

val NightLightSky = SkyPalette(
    top = Color(0xFF132046),
    upper = Color(0xFF283D6E),
    horizon = Color(0xFFC27D44),
    afterGlow = Color(0xFFDCCBB8),
    ground = Color(0xFFF6F1E6),
    deep = Color(0xFFECE3CE),
)

fun skyForPrayer(prayer: Prayer, isDark: Boolean): SkyPalette = if (isDark) {
    when (prayer) {
        Prayer.FAJR -> FajrSky
        Prayer.SUNRISE -> SunriseSky
        Prayer.DHUHR -> DhuhrSky
        Prayer.ASR -> AsrSky
        Prayer.MAGHRIB -> MaghribSky
        Prayer.ISHA -> NightSky
    }
} else {
    when (prayer) {
        Prayer.FAJR -> FajrLightSky
        Prayer.SUNRISE -> SunriseLightSky
        Prayer.DHUHR -> DhuhrLightSky
        Prayer.ASR -> AsrLightSky
        Prayer.MAGHRIB -> MaghribLightSky
        Prayer.ISHA -> NightLightSky
    }
}

@Composable
fun PrayerSkyBackground(
    modifier: Modifier = Modifier,
    sky: SkyPalette = NightSky,
    horizonFraction: Float = 0.22f, // where the mosque base sits, as a fraction of screen height
    content: @Composable BoxScope.() -> Unit,
) {
    val top by animateColorAsState(sky.top, tween(750), label = "skyTop")
    val upper by animateColorAsState(sky.upper, tween(750), label = "skyUpper")
    val horizon by animateColorAsState(sky.horizon, tween(750), label = "skyHorizon")
    val afterGlow by animateColorAsState(sky.afterGlow, tween(750), label = "skyAfterGlow")
    val ground by animateColorAsState(sky.ground, tween(750), label = "skyGround")
    val deep by animateColorAsState(sky.deep, tween(750), label = "skyDeep")

    Box(
        modifier
            .fillMaxSize()
            .background(deep)
            .drawBehind {
                val h = size.height
                val f = horizonFraction.coerceIn(0.05f, 0.45f)
                val hy = h * f

                fun at(x: Float) = x.coerceIn(0f, 0.99f)

                // 1) single continuous vertical gradient
                drawRect(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to top,
                            at(f * 0.60f) to upper,
                            at(f * 0.95f) to horizon,
                            at(f * 1.30f) to afterGlow,
                            at(f * 2.10f) to ground,
                            1f to deep,
                        )
                    )
                )

                // 2) soft glow centred on the dome so the light seems to come from behind it
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(horizon.copy(alpha = 0.44f), Color.Transparent),
                        center = Offset(size.width / 2f, hy),
                        radius = size.width * 0.85f,
                    )
                )
            },
        content = content,
    )
}

/**
 * Apply to your mosque silhouette. Its lower part fades to transparent, so the
 * sky shows through and there is no solid "ground" edge any more.
 * Make the silhouette extend ~40-60dp below the horizon so the fade has room.
 */
fun Modifier.fadeOutBottom(startFraction: Float = 0.60f): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(
                startFraction to Color.Black,
                1f to Color.Transparent,
            ),
            blendMode = BlendMode.DstIn,
        )
    }

/**
 * Prayer-row card that reads the same everywhere on the screen.
 * Same translucent fill on every row, so rows no longer look lighter or darker
 * depending on what gradient happens to be behind them.
 */
fun Modifier.glassCard(
    active: Boolean = false,
    isDark: Boolean = true,
    shape: Shape = RoundedCornerShape(22.dp),
): Modifier {
    val gold = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
    val bgColor = when {
        active && isDark -> gold.copy(alpha = 0.12f)
        active && !isDark -> Color(0xFFFFFBEB).copy(alpha = 0.92f)
        isDark -> Color.White.copy(alpha = 0.06f)
        else -> Color.White.copy(alpha = 0.78f)
    }
    val borderColor = when {
        active && isDark -> gold.copy(alpha = 0.85f)
        active && !isDark -> gold.copy(alpha = 0.75f)
        isDark -> Color.White.copy(alpha = 0.10f)
        else -> Color(0xFFD6CEBF)
    }
    return this
        .clip(shape)
        .background(bgColor)
        .border(
            width = 1.dp,
            color = borderColor,
            shape = shape,
        )
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSettingsClick: () -> Unit = {},
    onNavigateToTimetables: () -> Unit = {},
    onToggleTheme: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val currentLocale = ConfigurationCompat.getLocales(configuration).get(0) ?: Locale.getDefault()
    val languageCode = currentLocale.language ?: "en"

    val amMarker = stringResource(R.string.am_marker)
    val pmMarker = stringResource(R.string.pm_marker)

    val isDark = PrayerReminderColors.isDark
    val cardTextPrimary = if (isDark) Color.White else Color(0xFF1C1917)
    val cardTextSecondary = if (isDark) Color(0xFFA8A29E) else Color(0xFF57534E)
    val next = uiState.nextPrayer
    val schedule = uiState.schedule
    val currentSkyPalette = skyForPrayer(uiState.activeSky, isDark)

    PrayerSkyBackground(sky = currentSkyPalette) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 104.dp)
        ) {
            // Header: NO background of its own
            Box(modifier = Modifier.fillMaxWidth()) {
                MosqueSilhouette(
                    modifier = Modifier
                        .matchParentSize()
                        .fadeOutBottom(),
                    activeSky = uiState.activeSky
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(bottom = 12.dp)
                ) {
                    // MINIMALIST HEADER
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Place & Hijri / Timetable Subtitle
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigateToTimetables() }
                                .padding(end = 8.dp, top = 2.dp, bottom = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = AmberBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.place,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            val hijri = uiState.schedule?.hijri
                            val subtitleText = if (hijri != null) {
                                val hijriMonthName = HijriNames.getMonthName(hijri.month, languageCode)
                                "${hijri.day} $hijriMonthName ${hijri.year}"
                            } else {
                                uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM", currentLocale))
                            }
                            Text(
                                text = subtitleText,
                                color = Color(0xFFD6D3D1).copy(alpha = 0.88f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 22.dp, top = 2.dp)
                            )
                        }

                        // Right: Sky Vibe Switcher & Theme Toggle Button ONLY
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sky Vibe Switcher Pill
                            val activeSkyName = SanctuarySkyThemes.forPrayer(uiState.activeSky).label
                            val skyLabel = if (uiState.skyOverride.isBlank()) {
                                "${stringResource(R.string.auto_sky)} • $activeSkyName"
                            } else {
                                activeSkyName
                            }
                            val skyPillIcon = if (uiState.skyOverride.isBlank()) {
                                Icons.Default.AutoAwesome
                            } else {
                                getPrayerIcon(uiState.activeSky)
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.Black.copy(alpha = 0.38f),
                                border = BorderStroke(
                                    1.dp,
                                    if (uiState.skyOverride.isNotBlank()) AmberBright.copy(alpha = 0.65f)
                                    else Color.White.copy(alpha = 0.18f)
                                ),
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable { viewModel.cycleSkyVibe() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = skyPillIcon,
                                        contentDescription = "Sky Vibe",
                                        tint = AmberBright,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = skyLabel,
                                        color = AmberTint,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Theme toggle button
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.38f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                IconButton(onClick = onToggleTheme) {
                                    Icon(
                                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Theme",
                                        tint = AmberBright,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // CountdownBlock
                    if (next != null) {
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            HeroNextPrayerSection(
                                next = next,
                                countdownHms = uiState.countdownHms,
                                progressPct = uiState.progressPct,
                                use24Hour = uiState.use24Hour,
                                amMarker = amMarker,
                                pmMarker = pmMarker,
                                hasWarning = schedule?.fixedTimeBeforeAzanWarnings?.contains(next.prayer) == true
                            )
                        }
                    }
                }
            }

            // List: NO background of its own
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Status / Warning Banners
                if (schedule != null && schedule.source.isEstimated) {
                    Spacer(modifier = Modifier.height(10.dp))
                    BannerPill(
                        text = stringResource(R.string.banner_estimated),
                        bg = PrayerReminderColors.WarnBg,
                        textColor = PrayerReminderColors.WarnText
                    )
                } else if (schedule != null && schedule.source == ScheduleSource.NONE) {
                    Spacer(modifier = Modifier.height(10.dp))
                    BannerPill(
                        text = stringResource(R.string.banner_no_timetable),
                        bg = PrayerReminderColors.WarnBg,
                        textColor = PrayerReminderColors.WarnText
                    )
                }

                if (uiState.missedCount7d > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    BannerPill(
                        text = stringResource(R.string.banner_missed),
                        bg = PrayerReminderColors.WarnBg,
                        textColor = PrayerReminderColors.WarnText
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // DAILY PRAYER LIST
                if (schedule != null && schedule.source != ScheduleSource.NONE) {
                    val prayers = listOf(
                        Prayer.FAJR,
                        Prayer.SUNRISE,
                        Prayer.DHUHR,
                        Prayer.ASR,
                        Prayer.MAGHRIB,
                        Prayer.ISHA
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (prayer in prayers) {
                            val azan = schedule.azan[prayer] ?: continue
                            val jamaat = schedule.jamaat[prayer]
                            val isNext = next?.prayer == prayer && !next.isTomorrow
                            val alertMode = uiState.alertModes[prayer] ?: if (prayer.hasReminders) {
                                if (uiState.reminderStates[prayer] != false) "sound" else "muted"
                            } else {
                                "muted"
                            }

                            SanctuaryPrayerRow(
                                modifier = Modifier.glassCard(active = isNext, isDark = isDark),
                                prayer = prayer,
                                isFriday = schedule.isFriday,
                                azanTime = azan,
                                jamaatTime = jamaat,
                                isNext = isNext,
                                isDark = isDark,
                                use24Hour = uiState.use24Hour,
                                amMarker = amMarker,
                                pmMarker = pmMarker,
                                alertMode = alertMode,
                                cardTextPrimary = cardTextPrimary,
                                cardTextSecondary = cardTextSecondary,
                                hasWarning = schedule.fixedTimeBeforeAzanWarnings.contains(prayer),
                                onToggleAlert = { viewModel.cyclePrayerAlertMode(prayer) }
                            )
                        }
                    }
                }
            }
        }

        // 3. FLOATING BOTTOM PILL NAVIGATION (HOME & SETTINGS)
        SanctuaryBottomNavBar(
            activeTab = SanctuaryTab.HOME,
            onHomeClick = {},
            onSettingsClick = onSettingsClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun HeroNextPrayerSection(
    next: NextPrayer,
    countdownHms: String,
    progressPct: Float,
    use24Hour: Boolean,
    amMarker: String,
    pmMarker: String,
    hasWarning: Boolean
) {
    val prayerLabel = if (next.isJumuah) {
        stringResource(R.string.prayer_jumuah)
    } else {
        getPrayerName(next.prayer)
    }
    val arabicLabel = getPrayerArabic(next.prayer)
    val phaseTag = if (next.phase == NextPrayerPhase.BEFORE_AZAN) {
        stringResource(R.string.up_next)
    } else {
        stringResource(R.string.now_prayer)
    }

    val targetTime = if (next.phase == NextPrayerPhase.BEFORE_AZAN) next.azanTime else next.jamaatTime
    val (timeDigits, suffix) = TimeFormatter.formatTimeParts(targetTime, use24Hour, amMarker, pmMarker)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pill: "Up Next • الفجر"
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.Black.copy(alpha = 0.42f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
        ) {
            Text(
                text = "$phaseTag • $arabicLabel",
                color = AmberTint,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large Hero Prayer Name + Monospace Time
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$prayerLabel ",
                color = Color.White,
                fontSize = 33.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = timeDigits,
                color = AmberSoft,
                fontSize = 33.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light
            )
            if (suffix.isNotEmpty()) {
                Text(
                    text = " $suffix",
                    color = Color(0xFFD6D3D1),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Iqamah & Azan compact line
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val azanFormatted = TimeFormatter.formatTime(next.azanTime, use24Hour, amMarker, pmMarker)
            val jamaatFormatted = TimeFormatter.formatTime(next.jamaatTime, use24Hour, amMarker, pmMarker)
            Text(
                text = "${stringResource(R.string.azan)} $azanFormatted  •  ${stringResource(R.string.jamaat)} $jamaatFormatted",
                color = Color(0xFFE7E5E4).copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            if (hasWarning) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = stringResource(R.string.jamaat_fixed_before_azan),
                    tint = AmberBright,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Countdown & Progress Bar
        Column(
            modifier = Modifier.widthIn(max = 250.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.remaining),
                    color = Color(0xFFD6D3D1),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = countdownHms,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Progress Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.16f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressPct.coerceIn(0.05f, 1.0f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(AmberSoft, Color(0xFFF59E0B))
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun SanctuaryPrayerRow(
    modifier: Modifier = Modifier,
    prayer: Prayer,
    isFriday: Boolean,
    azanTime: java.time.LocalTime,
    jamaatTime: java.time.LocalTime?,
    isNext: Boolean,
    isDark: Boolean,
    use24Hour: Boolean,
    amMarker: String,
    pmMarker: String,
    alertMode: String,
    cardTextPrimary: Color,
    cardTextSecondary: Color,
    hasWarning: Boolean,
    onToggleAlert: () -> Unit
) {
    val prayerName = if (prayer == Prayer.DHUHR && isFriday) {
        stringResource(R.string.prayer_jumuah)
    } else {
        getPrayerName(prayer)
    }
    val prayerIcon = getPrayerIcon(prayer)
    val accentGold = if (isDark) AmberSoft else Color(0xFFB45309)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 58.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Icon Box + Name + Iqamah Subtitle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isNext -> AmberBright
                            isDark -> Color.White.copy(alpha = 0.09f)
                            else -> Color(0xFFECE3CE).copy(alpha = 0.85f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = prayerIcon,
                    contentDescription = null,
                    tint = when {
                        isNext -> Color(0xFF020617)
                        isDark -> Color(0xFFD6D3D1)
                        else -> Color(0xFF57534E)
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = prayerName,
                        color = if (isNext) cardTextPrimary else cardTextPrimary.copy(alpha = 0.92f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    if (isNext) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = AmberBright.copy(alpha = if (isDark) 0.22f else 0.26f)
                        ) {
                            Text(
                                text = stringResource(R.string.next_tag).uppercase(Locale.US),
                                color = accentGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (jamaatTime != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${stringResource(R.string.jamaat)} ${TimeFormatter.formatTime(jamaatTime, use24Hour, amMarker, pmMarker)}",
                            color = cardTextSecondary,
                            fontSize = 12.sp
                        )
                        if (hasWarning) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = stringResource(R.string.jamaat_fixed_before_azan),
                                tint = if (isDark) AmberBright else Color(0xFFD97706),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Right: Monospace Adhan Time + One-Tap Alert Mode Toggle Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = TimeFormatter.formatTime(azanTime, use24Hour, amMarker, pmMarker),
                color = if (isNext) accentGold else cardTextPrimary,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )

            if (prayer.hasReminders) {
                val (btnBg, btnBorder, btnTint, btnIcon) = when (alertMode) {
                    "sound" -> AlertBtnStyle(
                        bg = AmberBright.copy(alpha = if (isDark) 0.16f else 0.20f),
                        border = AmberBright.copy(alpha = if (isDark) 0.45f else 0.65f),
                        tint = accentGold,
                        icon = Icons.AutoMirrored.Filled.VolumeUp
                    )
                    "vibrate" -> AlertBtnStyle(
                        bg = Color(0xFF34D399).copy(alpha = if (isDark) 0.16f else 0.18f),
                        border = Color(0xFF34D399).copy(alpha = if (isDark) 0.45f else 0.60f),
                        tint = if (isDark) EmeraldSoft else Color(0xFF047857),
                        icon = Icons.Default.Vibration
                    )
                    else -> AlertBtnStyle(
                        bg = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFECE3CE).copy(alpha = 0.65f),
                        border = if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFD6CEBF),
                        tint = cardTextSecondary,
                        icon = Icons.Default.NotificationsOff
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(btnBg)
                        .border(1.dp, btnBorder, RoundedCornerShape(12.dp))
                        .clickable { onToggleAlert() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = btnIcon,
                        contentDescription = "Toggle $prayerName alert ($alertMode)",
                        tint = btnTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color(0xFFECE3CE).copy(alpha = 0.45f))
                        .border(
                            1.dp,
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFD6CEBF),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = cardTextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private data class AlertBtnStyle(
    val bg: Color,
    val border: Color,
    val tint: Color,
    val icon: ImageVector
)

@Composable
private fun BannerPill(text: String, bg: Color, textColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = bg
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(12.dp)
        )
    }
}

private fun getPrayerIcon(prayer: Prayer): ImageVector = when (prayer) {
    Prayer.FAJR -> Icons.Default.WbTwilight
    Prayer.SUNRISE -> Icons.Default.WbSunny
    Prayer.DHUHR -> Icons.Default.Cloud
    Prayer.ASR -> Icons.Default.WbSunny
    Prayer.MAGHRIB -> Icons.Default.NightsStay
    Prayer.ISHA -> Icons.Default.Bedtime
}

private fun getPrayerArabic(prayer: Prayer): String = when (prayer) {
    Prayer.FAJR -> "الفجر"
    Prayer.SUNRISE -> "الشروق"
    Prayer.DHUHR -> "الظهر"
    Prayer.ASR -> "العصر"
    Prayer.MAGHRIB -> "المغرب"
    Prayer.ISHA -> "العشاء"
}

@Composable
fun getPrayerName(prayer: Prayer): String {
    return when (prayer) {
        Prayer.FAJR -> stringResource(R.string.prayer_fajr)
        Prayer.SUNRISE -> stringResource(R.string.prayer_sunrise)
        Prayer.DHUHR -> stringResource(R.string.prayer_dhuhr)
        Prayer.ASR -> stringResource(R.string.prayer_asr)
        Prayer.MAGHRIB -> stringResource(R.string.prayer_maghrib)
        Prayer.ISHA -> stringResource(R.string.prayer_isha)
    }
}
