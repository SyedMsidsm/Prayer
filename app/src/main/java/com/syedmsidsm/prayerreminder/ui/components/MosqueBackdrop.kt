package com.syedmsidsm.prayerreminder.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.syedmsidsm.prayerreminder.domain.model.Prayer

data class SanctuarySkyTheme(
    val id: String,
    val label: String,
    val skyGradient: List<Color>,
    val domeColor: Color,
    val domeMidColor: Color,
    val glowColor: Color,
    val starsOpacity: Float,
    val isCrescent: Boolean,
    val badgeText: String
)

object SanctuarySkyThemes {
    val FAJR = SanctuarySkyTheme(
        id = "FAJR",
        label = "Fajr",
        skyGradient = listOf(Color(0xFF18122B), Color(0xFF393053), Color(0xFFC06C84)),
        domeColor = Color(0xFF120D21),
        domeMidColor = Color(0xFF221A38),
        glowColor = Color(0x4DF4A261),
        starsOpacity = 0.45f,
        isCrescent = true,
        badgeText = "Fajr • Dawn Serenity"
    )
    val SUNRISE = SanctuarySkyTheme(
        id = "SUNRISE",
        label = "Sunrise",
        skyGradient = listOf(Color(0xFF1D3557), Color(0xFF457B9D), Color(0xFFF4A261)),
        domeColor = Color(0xFF122238),
        domeMidColor = Color(0xFF1F3654),
        glowColor = Color(0x59FBBF24),
        starsOpacity = 0.05f,
        isCrescent = false,
        badgeText = "Shuruq • First Light"
    )
    val DHUHR = SanctuarySkyTheme(
        id = "DHUHR",
        label = "Dhuhr",
        skyGradient = listOf(Color(0xFF0A3641), Color(0xFF135D66), Color(0xFFD4A373)),
        domeColor = Color(0xFF062128),
        domeMidColor = Color(0xFF0D3640),
        glowColor = Color(0x4DF59E0B),
        starsOpacity = 0.0f,
        isCrescent = false,
        badgeText = "Dhuhr • Midday Peace"
    )
    val ASR = SanctuarySkyTheme(
        id = "ASR",
        label = "Asr",
        skyGradient = listOf(Color(0xFF1B263B), Color(0xFF5C4742), Color(0xFFE07A5F)),
        domeColor = Color(0xFF111826),
        domeMidColor = Color(0xFF242938),
        glowColor = Color(0x59FB923C),
        starsOpacity = 0.05f,
        isCrescent = false,
        badgeText = "Asr • Golden Hour"
    )
    val MAGHRIB = SanctuarySkyTheme(
        id = "MAGHRIB",
        label = "Maghrib",
        skyGradient = listOf(Color(0xFF130924), Color(0xFF4A154B), Color(0xFFC85A32)),
        domeColor = Color(0xFF0C0517),
        domeMidColor = Color(0xFF210E2E),
        glowColor = Color(0x59F97316),
        starsOpacity = 0.6f,
        isCrescent = true,
        badgeText = "Maghrib • Horizon Dusk"
    )
    val ISHA = SanctuarySkyTheme(
        id = "ISHA",
        label = "Isha",
        skyGradient = listOf(Color(0xFF030712), Color(0xFF071E26), Color(0xFF0F3D3E)),
        domeColor = Color(0xFF02090D),
        domeMidColor = Color(0xFF06181F),
        glowColor = Color(0x40D4AF37),
        starsOpacity = 0.9f,
        isCrescent = true,
        badgeText = "Isha • Still Night"
    )

    fun forPrayer(prayer: Prayer): SanctuarySkyTheme = when (prayer) {
        Prayer.FAJR -> FAJR
        Prayer.SUNRISE -> SUNRISE
        Prayer.DHUHR -> DHUHR
        Prayer.ASR -> ASR
        Prayer.MAGHRIB -> MAGHRIB
        Prayer.ISHA -> ISHA
    }
}

/**
 * Full-screen atmospheric mosque silhouette backdrop matching the Sanctuary UI reference.
 * Smoothly transitions between the 6 prayer sky palettes and fades into the surface below the horizon.
 */
@Composable
fun MosqueBackdrop(
    activeSky: Prayer,
    surfaceFadeColor: Color,
    modifier: Modifier = Modifier
) {
    val theme = SanctuarySkyThemes.forPrayer(activeSky)
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val animSpec = tween<Color>(durationMillis = 800)

    val topSky by animateColorAsState(theme.skyGradient[0], animationSpec = animSpec, label = "skyTop")
    val midSky by animateColorAsState(theme.skyGradient[1], animationSpec = animSpec, label = "skyMid")
    val lowSky by animateColorAsState(theme.skyGradient[2], animationSpec = animSpec, label = "skyLow")
    val domeColor by animateColorAsState(theme.domeColor, animationSpec = animSpec, label = "dome")
    val domeMidColor by animateColorAsState(theme.domeMidColor, animationSpec = animSpec, label = "domeMid")
    val glowColor by animateColorAsState(theme.glowColor, animationSpec = animSpec, label = "glow")

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        // Mosque horizon spans vertically across the upper ~360dp zone proportionally
        val horizonBottom = (h * 0.44f).coerceIn(280f, 920f)
        val mosqueTop = horizonBottom * 0.28f

        // 1. Dynamic Sky Gradient over the upper sky/horizon zone + solid sanctuary surface below
        drawRect(
            brush = Brush.verticalGradient(
                0.0f to topSky,
                0.24f to midSky,
                0.42f to lowSky,
                0.56f to surfaceFadeColor,
                1.0f to surfaceFadeColor
            ),
            size = size
        )

        // 2. Soft Radial Sanctuary Glow behind Main Dome
        val glowCenter = Offset(w * 0.5f, mosqueTop + (horizonBottom - mosqueTop) * 0.48f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowColor, Color.Transparent),
                center = glowCenter,
                radius = w * 0.48f
            ),
            radius = w * 0.48f,
            center = glowCenter
        )

        // 3. Twinkling Starfield for Night/Dawn
        if (theme.starsOpacity > 0.01f) {
            val stars = listOf(
                Triple(0.11f, 0.06f, 2.2f),
                Triple(0.26f, 0.11f, 1.6f),
                Triple(0.41f, 0.05f, 2.6f),
                Triple(0.60f, 0.09f, 1.9f),
                Triple(0.77f, 0.055f, 2.3f),
                Triple(0.89f, 0.12f, 1.8f),
                Triple(0.18f, 0.16f, 1.4f),
                Triple(0.52f, 0.14f, 1.5f)
            )
            stars.forEachIndexed { idx, (rx, ry, r) ->
                val starColor = if (idx % 2 == 0) Color(0xFFFDE68A) else Color.White
                drawCircle(
                    color = starColor.copy(alpha = theme.starsOpacity * 0.85f),
                    radius = r * (w / 420f).coerceAtLeast(0.8f),
                    center = Offset(w * rx, horizonBottom * ry * 2.1f)
                )
            }
        }

        // 4. Celestial Crescent or Glowing Sun
        val celX = if (isRtl) w * 0.16f else w * 0.84f
        val celCenter = Offset(celX, horizonBottom * 0.21f)
        val celRadius = w * 0.036f
        if (theme.isCrescent) {
            // Subtle halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x45FBBF24), Color.Transparent),
                    center = celCenter,
                    radius = celRadius * 2.4f
                ),
                radius = celRadius * 2.4f,
                center = celCenter
            )
            drawCircle(
                color = Color(0xFFFDE68A).copy(alpha = 0.92f),
                radius = celRadius,
                center = celCenter
            )
            val cutoutOffsetX = celRadius * 0.36f
            drawCircle(
                color = topSky,
                radius = celRadius * 0.86f,
                center = Offset(celCenter.x + cutoutOffsetX, celCenter.y - celRadius * 0.24f)
            )
        } else {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x99FBBF24), Color(0x00FBBF24)),
                    center = celCenter,
                    radius = celRadius * 2.8f
                ),
                radius = celRadius * 2.8f,
                center = celCenter
            )
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFEF3C7), Color(0xFFFCD34D))
                ),
                radius = celRadius * 0.95f,
                center = celCenter
            )
        }

        // Helper coordinates matching the SVG viewBox (420 x 260) mapped to [0..w, mosqueTop..horizonBottom]
        val scaleX = w / 420f
        val scaleY = (horizonBottom - mosqueTop) / 260f
        fun sx(x: Float) = x * scaleX
        fun sy(y: Float) = mosqueTop + y * scaleY

        // 5. Distant Minarets & Domes (Back Horizon Layer)
        val midFill = domeMidColor.copy(alpha = 0.6f)
        // Left back minaret
        drawRect(
            color = midFill,
            topLeft = Offset(sx(28f), sy(92f)),
            size = Size(sx(8f), (sy(197f) - sy(92f)))
        )
        drawPath(
            path = Path().apply {
                moveTo(sx(28f), sy(92f))
                lineTo(sx(32f), sy(72f))
                lineTo(sx(36f), sy(92f))
                close()
            },
            color = midFill
        )
        // Right back minaret
        drawRect(
            color = midFill,
            topLeft = Offset(sx(384f), sy(92f)),
            size = Size(sx(8f), (sy(197f) - sy(92f)))
        )
        drawPath(
            path = Path().apply {
                moveTo(sx(384f), sy(92f))
                lineTo(sx(388f), sy(72f))
                lineTo(sx(392f), sy(92f))
                close()
            },
            color = midFill
        )
        // Distant back domes
        drawPath(
            path = Path().apply {
                moveTo(sx(52f), sy(185f))
                quadraticTo(sx(85f), sy(135f), sx(118f), sy(185f))
                close()
            },
            color = midFill
        )
        drawPath(
            path = Path().apply {
                moveTo(sx(302f), sy(185f))
                quadraticTo(sx(335f), sy(135f), sx(368f), sy(185f))
                close()
            },
            color = midFill
        )

        // 6. Foreground Grand Mosque Silhouette
        val goldFinial = Color(0xFFFBBF24)

        // Left Minaret
        drawRect(color = domeColor, topLeft = Offset(sx(68f), sy(52f)), size = Size(sx(13f), sy(212f) - sy(52f)))
        drawRoundRect(
            color = domeColor,
            topLeft = Offset(sx(65f), sy(80f)),
            size = Size(sx(19f), 3.5f * scaleY),
            cornerRadius = CornerRadius(1.5f * scaleX, 1.5f * scaleY)
        )
        drawPath(
            path = Path().apply {
                moveTo(sx(68f), sy(52f))
                quadraticTo(sx(74.5f), sy(28f), sx(81f), sy(52f))
                close()
            },
            color = domeColor
        )
        drawLine(
            color = goldFinial,
            start = Offset(sx(74.5f), sy(18f)),
            end = Offset(sx(74.5f), sy(30f)),
            strokeWidth = (1.6f * scaleX).coerceAtLeast(2f),
            cap = StrokeCap.Round
        )

        // Right Minaret
        drawRect(color = domeColor, topLeft = Offset(sx(339f), sy(52f)), size = Size(sx(13f), sy(212f) - sy(52f)))
        drawRoundRect(
            color = domeColor,
            topLeft = Offset(sx(336f), sy(80f)),
            size = Size(sx(19f), 3.5f * scaleY),
            cornerRadius = CornerRadius(1.5f * scaleX, 1.5f * scaleY)
        )
        drawPath(
            path = Path().apply {
                moveTo(sx(339f), sy(52f))
                quadraticTo(sx(345.5f), sy(28f), sx(352f), sy(52f))
                close()
            },
            color = domeColor
        )
        drawLine(
            color = goldFinial,
            start = Offset(sx(345.5f), sy(18f)),
            end = Offset(sx(345.5f), sy(30f)),
            strokeWidth = (1.6f * scaleX).coerceAtLeast(2f),
            cap = StrokeCap.Round
        )

        // Central Grand Dome
        drawPath(
            path = Path().apply {
                moveTo(sx(140f), sy(168f))
                cubicTo(sx(140f), sy(106f), sx(185f), sy(75f), sx(210f), sy(56f))
                cubicTo(sx(235f), sy(75f), sx(280f), sy(106f), sx(280f), sy(168f))
                close()
            },
            color = domeColor,
            style = Fill
        )
        drawLine(
            color = goldFinial,
            start = Offset(sx(210f), sy(38f)),
            end = Offset(sx(210f), sy(57f)),
            strokeWidth = (2.0f * scaleX).coerceAtLeast(2.5f),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = goldFinial,
            radius = (2.4f * scaleX).coerceAtLeast(3f),
            center = Offset(sx(210f), sy(46f))
        )

        // Flanking Domes
        drawPath(
            path = Path().apply {
                moveTo(sx(94f), sy(175f))
                cubicTo(sx(94f), sy(140f), sx(120f), sy(124f), sx(135f), sy(113f))
                cubicTo(sx(150f), sy(124f), sx(174f), sy(140f), sx(174f), sy(175f))
                close()
            },
            color = domeColor
        )
        drawPath(
            path = Path().apply {
                moveTo(sx(246f), sy(175f))
                cubicTo(sx(246f), sy(140f), sx(270f), sy(124f), sx(285f), sy(113f))
                cubicTo(sx(300f), sy(124f), sx(326f), sy(140f), sx(326f), sy(175f))
                close()
            },
            color = domeColor
        )

        // Sanctuary Wall
        drawRect(
            color = domeColor,
            topLeft = Offset(0f, sy(165f)),
            size = Size(w, sy(260f) - sy(165f))
        )

        // 7. Subtle Glowing Arched Sanctuary Windows
        val windowBrush = Brush.verticalGradient(
            colors = listOf(Color(0xD9FDE68A), Color(0x33D97706)),
            startY = sy(142f),
            endY = sy(196f)
        )
        listOf(190f to 152f, 206f to 150f, 222f to 152f).forEach { (wx, wy) ->
            drawPath(
                path = Path().apply {
                    moveTo(sx(wx), sy(wy))
                    quadraticTo(sx(wx + 4f), sy(wy - 8f), sx(wx + 8f), sy(wy))
                    lineTo(sx(wx + 8f), sy(160f))
                    lineTo(sx(wx), sy(160f))
                    close()
                },
                brush = windowBrush
            )
        }
        drawPath(
            path = Path().apply {
                moveTo(sx(200f), sy(176f))
                quadraticTo(sx(210f), sy(162f), sx(220f), sy(176f))
                lineTo(sx(220f), sy(196f))
                lineTo(sx(200f), sy(196f))
                close()
            },
            brush = windowBrush,
            alpha = 0.7f
        )

        // 8. Smooth Bottom Fade into Sanctuary Surface
        drawRect(
            brush = Brush.verticalGradient(
                0.0f to surfaceFadeColor.copy(alpha = 0f),
                0.65f to surfaceFadeColor.copy(alpha = 0.92f),
                1.0f to surfaceFadeColor,
                startY = sy(130f),
                endY = sy(260f)
            ),
            topLeft = Offset(0f, sy(130f)),
            size = Size(w, sy(260f) - sy(130f))
        )
    }
}
