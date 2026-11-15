package com.syedmsidsm.prayerreminder.ui.landing

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

private val BgDeep = Color(0xFF01090E)
private val CardFill = Color(0xFF091C24)
private val PlatformFill = Color(0xFF0B1923)
private val DomeFill = Color(0xFF122D3B)
private val DomeStroke = Color(0xFF28556B)
private val MinaretFill = Color(0xFF0E2430)
private val GoldBright = Color(0xFFFFBF22)
private val GoldBorder = Color(0xFFD4A438)
private val SlateBorder = Color(0xFF6E7A80)
private val DoorGlowWarm = Color(0xFFF5DF90)
private val StarSilver = Color(0xFFDBE0DC)
private val StarChampagne = Color(0xFFC8B978)
private val StarGoldMuted = Color(0xFFB5A66B)
private val SubtitleMuted = Color(0xFF8C9BA5)

/**
 * Landing page animation matching prayerreminder-animation_(1).mp4:
 * 1. Squircle icon card fades in while its metallic gold-and-slate border traces around the perimeter.
 * 2. The central pointed Islamic dome and base platform rise into place with a warm golden back-glow.
 * 3. Left and right minarets grow upward, crowned by gold balcony bands, conical spires, and finials.
 * 4. The central gold finial and crescent moon rise atop the dome while 4 stars pop in and twinkle.
 * 5. The arched sanctuary doorway inside the dome illuminates with a warm golden-ivory glow.
 * 6. "PrayerReminder" animates in letter-by-letter from left to right.
 * 7. "100% Offline  •  Bhatkal Timetable  •  Adhan & Jamaat" reveals sequentially below.
 * Tap anywhere to skip.
 */
@Composable
fun LandingScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current

    val animationsEnabled = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        ) > 0f
    }

    val latestOnFinished = rememberUpdatedState(onFinished)
    val finishedFlag = remember { AtomicBoolean(false) }
    val finish: () -> Unit = remember {
        {
            if (finishedFlag.compareAndSet(false, true)) {
                latestOnFinished.value()
            }
        }
    }

    val initVal = if (animationsEnabled) 0f else 1f

    // 1. Squircle card & border trace
    val cardAlpha = remember { Animatable(initVal) }
    val borderProgress = remember { Animatable(initVal) }
    val ambientGlowAlpha = remember { Animatable(initVal) }

    // 2. Dome & platform
    val domeProgress = remember { Animatable(initVal) }

    // 3. Minarets & gold accents
    val leftMinaretProgress = remember { Animatable(initVal) }
    val rightMinaretProgress = remember { Animatable(initVal) }
    val minaretGoldProgress = remember { Animatable(initVal) }

    // 4. Dome finial & crescent moon
    val crescentProgress = remember { Animatable(initVal) }

    // 5. Sanctuary doorway & warm illumination
    val doorBaseAlpha = remember { Animatable(initVal) }
    val doorGlowProgress = remember { Animatable(initVal) }

    // 6. Four stars
    val starScales = remember { List(4) { Animatable(initVal) } }

    // 7. Letter-by-letter "PrayerReminder" title
    val titleText = "PrayerReminder"
    val letterProgress = remember { List(titleText.length) { Animatable(initVal) } }

    // 8. 3-part subtitle ("100% Offline", "•  Bhatkal Timetable", "•  Adhan & Jamaat")
    val subPart1Alpha = remember { Animatable(initVal) }
    val subPart2Alpha = remember { Animatable(initVal) }
    val subPart3Alpha = remember { Animatable(initVal) }

    // Subtle continuous shimmer once stars and doorway are lit
    val infinite = rememberInfiniteTransition(label = "landingShimmer")
    val starPulse by infinite.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starPulse"
    )

    LaunchedEffect(Unit) {
        if (!animationsEnabled) {
            delay(1000)
            finish()
            return@LaunchedEffect
        }

        withFrameNanos { }
        delay(120)

        // Stage 1: Squircle card & border sweep (0 - 1250 ms)
        launch { cardAlpha.animateTo(1f, tween(450, easing = LinearEasing)) }
        launch { borderProgress.animateTo(1f, tween(1250, easing = FastOutSlowInEasing)) }

        // Stage 2: Base platform & pointed dome rise (350 - 1150 ms)
        launch {
            delay(350)
            domeProgress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        }
        launch {
            delay(500)
            ambientGlowAlpha.animateTo(1f, tween(1400, easing = LinearEasing))
        }

        // Stage 3: Left & right minarets grow upward + gold spires (950 - 1950 ms)
        launch {
            delay(950)
            leftMinaretProgress.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
        }
        launch {
            delay(1150)
            rightMinaretProgress.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
        }
        launch {
            delay(1450)
            minaretGoldProgress.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }

        // Stage 4: Central finial & crescent moon (1600 - 2150 ms)
        launch {
            delay(1600)
            crescentProgress.animateTo(1f, tween(550, easing = FastOutSlowInEasing))
        }

        // Stage 5: Arched doorway appears dimly, then illuminates warmly (1900 - 3000 ms)
        launch {
            delay(1900)
            doorBaseAlpha.animateTo(1f, tween(400, easing = LinearEasing))
        }
        launch {
            delay(2350)
            doorGlowProgress.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
        }

        // Stage 6: 4 stars pop in sequentially (2050 - 2850 ms)
        val starDelays = listOf(2050L, 2250L, 2450L, 2650L)
        starScales.forEachIndexed { idx, anim ->
            launch {
                delay(starDelays[idx])
                anim.animateTo(1f, tween(360, easing = FastOutSlowInEasing))
            }
        }

        // Stage 7: "PrayerReminder" types in letter-by-letter (2750 - 3750 ms)
        letterProgress.forEachIndexed { index, anim ->
            launch {
                delay(2750L + index * 52L)
                anim.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
            }
        }

        // Stage 8: Subtitle parts fade in left-to-right (3850 - 4750 ms)
        launch {
            delay(3850)
            subPart1Alpha.animateTo(1f, tween(350, easing = LinearEasing))
        }
        launch {
            delay(4120)
            subPart2Alpha.animateTo(1f, tween(350, easing = LinearEasing))
        }
        launch {
            delay(4390)
            subPart3Alpha.animateTo(1f, tween(350, easing = LinearEasing))
        }

        // Hold complete composition briefly, then proceed
        delay(5650)
        finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .drawBehind {
                // Soft wide golden ambient halo behind the central icon
                if (ambientGlowAlpha.value > 0f) {
                    val center = Offset(size.width / 2f, size.height * 0.42f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GoldBright.copy(alpha = 0.09f * ambientGlowAlpha.value),
                                Color(0xFF0A222E).copy(alpha = 0.14f * ambientGlowAlpha.value),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension * 0.72f
                        ),
                        radius = size.minDimension * 0.72f,
                        center = center
                    )
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { finish() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
        ) {
            // Squircle Mosque Icon Canvas
            Canvas(
                modifier = Modifier.size(216.dp)
            ) {
                val s = size.minDimension
                fun u(v: Float) = v * (s / 100f)

                val cornerR = u(18.5f)
                val borderStroke = u(0.65f)

                // 1. Squircle background fill
                if (cardAlpha.value > 0f) {
                    drawRoundRect(
                        color = CardFill.copy(alpha = cardAlpha.value),
                        topLeft = Offset.Zero,
                        size = Size(s, s),
                        cornerRadius = CornerRadius(cornerR, cornerR)
                    )
                }

                // 2. Warm radial glow behind dome inside squircle
                if (domeProgress.value > 0f) {
                    val glowStrength = (0.5f * domeProgress.value + 0.5f * doorGlowProgress.value)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GoldBright.copy(alpha = 0.22f * glowStrength),
                                GoldBright.copy(alpha = 0.07f * glowStrength),
                                Color.Transparent
                            ),
                            center = Offset(u(50f), u(48f)),
                            radius = u(36f)
                        ),
                        radius = u(36f),
                        center = Offset(u(50f), u(48f))
                    )
                }

                // 3. Left & Right Minaret Shafts (growing upward from y=75.5)
                val baseLineY = u(75.5f)
                val minaretTopY = u(36.7f)
                val minaretFullH = baseLineY - minaretTopY

                fun drawMinaretShaft(cxUnit: Float, progress: Float) {
                    if (progress <= 0f) return
                    val cx = u(cxUnit)
                    val curTop = baseLineY - minaretFullH * progress
                    // Lower & main shaft
                    drawRect(
                        color = MinaretFill,
                        topLeft = Offset(cx - u(2.0f), curTop),
                        size = Size(u(4.0f), baseLineY - curTop)
                    )
                }

                drawMinaretShaft(23.0f, leftMinaretProgress.value)
                drawMinaretShaft(77.0f, rightMinaretProgress.value)

                // 4. Minaret Gold Balcony Bands, Conical Spires & Top Finials
                if (minaretGoldProgress.value > 0f) {
                    val gAlpha = minaretGoldProgress.value
                    for (cxUnit in listOf(23.0f, 77.0f)) {
                        val cx = u(cxUnit)
                        // Gold balcony ring (sherefe)
                        drawRoundRect(
                            color = GoldBright.copy(alpha = gAlpha),
                            topLeft = Offset(cx - u(2.4f), u(45.6f)),
                            size = Size(u(4.8f), u(1.45f)),
                            cornerRadius = CornerRadius(u(0.4f), u(0.4f))
                        )

                        // Gold conical pointed roof
                        val spireTopY = u(36.7f) - u(5.2f) * gAlpha
                        val roofPath = Path().apply {
                            moveTo(cx - u(1.65f), u(36.7f))
                            lineTo(cx + u(1.65f), u(36.7f))
                            lineTo(cx, spireTopY)
                            close()
                        }
                        drawPath(roofPath, GoldBright.copy(alpha = gAlpha), style = Fill)

                        // Thin gold finial rod at top of minaret
                        val rodBottom = u(30.6f)
                        val rodTop = rodBottom - u(4.8f) * gAlpha
                        drawLine(
                            color = GoldBright.copy(alpha = gAlpha),
                            start = Offset(cx, rodBottom),
                            end = Offset(cx, rodTop),
                            strokeWidth = u(0.65f),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 5. Central Pointed Islamic Dome
                if (domeProgress.value > 0f) {
                    val dProg = domeProgress.value
                    val clipTop = baseLineY - (baseLineY - u(29f)) * dProg
                    clipRect(
                        left = 0f,
                        top = clipTop,
                        right = s,
                        bottom = baseLineY + u(2f)
                    ) {
                        val domePath = Path().apply {
                            moveTo(u(29.5f), baseLineY)
                            cubicTo(
                                u(29.5f), u(54.0f),
                                u(37.5f), u(39.8f),
                                u(50.0f), u(31.0f)
                            )
                            cubicTo(
                                u(62.5f), u(39.8f),
                                u(70.5f), u(54.0f),
                                u(70.5f), baseLineY
                            )
                            close()
                        }
                        drawPath(domePath, color = DomeFill, style = Fill)
                        drawPath(
                            domePath,
                            color = DomeStroke.copy(alpha = dProg),
                            style = Stroke(width = u(0.45f))
                        )
                    }
                }

                // 6. Central Gold Finial & Crescent Moon atop Dome
                if (crescentProgress.value > 0f) {
                    val cProg = crescentProgress.value
                    val cx = u(50.0f)
                    // Vertical finial stem
                    val stemBottom = u(31.0f)
                    val stemTop = stemBottom - u(7.8f) * cProg
                    drawLine(
                        color = GoldBright.copy(alpha = cProg),
                        start = Offset(cx, stemBottom),
                        end = Offset(cx, stemTop),
                        strokeWidth = u(1.35f),
                        cap = StrokeCap.Round
                    )
                    // Small gold orb on stem
                    drawCircle(
                        color = GoldBright.copy(alpha = cProg),
                        radius = u(1.45f) * cProg,
                        center = Offset(cx, u(23.0f))
                    )
                    // Crescent moon opening to the right
                    val moonCenter = Offset(cx, u(17.6f))
                    val outerMoon = Path().apply {
                        addOval(Rect(center = moonCenter, radius = u(3.9f) * cProg))
                    }
                    val innerCutout = Path().apply {
                        addOval(
                            Rect(
                                center = Offset(cx + u(1.45f) * cProg, u(17.1f)),
                                radius = u(3.15f) * cProg
                            )
                        )
                    }
                    val crescentPath = Path.combine(PathOperation.Difference, outerMoon, innerCutout)
                    drawPath(crescentPath, color = GoldBright.copy(alpha = cProg), style = Fill)
                }

                // 7. Sanctuary Arched Doorway & Warm Golden Illumination
                if (doorBaseAlpha.value > 0f) {
                    val doorCenter = Offset(u(50.0f), u(64.5f))
                    // Radial warm glow when lit
                    if (doorGlowProgress.value > 0f) {
                        val glowAlpha = doorGlowProgress.value * starPulse
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    DoorGlowWarm.copy(alpha = 0.42f * glowAlpha),
                                    GoldBright.copy(alpha = 0.16f * glowAlpha),
                                    Color.Transparent
                                ),
                                center = doorCenter,
                                radius = u(11.0f)
                            ),
                            radius = u(11.0f),
                            center = doorCenter
                        )
                    }

                    // Arched doorway path
                    val doorLeft = u(47.5f)
                    val doorRight = u(52.5f)
                    val doorTop = u(60.2f)
                    val doorBottom = u(68.8f)
                    val archRadius = (doorRight - doorLeft) / 2f

                    val doorPath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(doorLeft, doorTop, doorRight, doorBottom),
                                topLeft = CornerRadius(archRadius, archRadius),
                                topRight = CornerRadius(archRadius, archRadius),
                                bottomLeft = CornerRadius(u(0.4f), u(0.4f)),
                                bottomRight = CornerRadius(u(0.4f), u(0.4f))
                            )
                        )
                    }

                    // Unlit dim sanctuary doorway -> warm glowing ivory-gold doorway
                    val unlitColor = Color(0xFF2B464B).copy(alpha = doorBaseAlpha.value)
                    drawPath(doorPath, color = unlitColor, style = Fill)

                    if (doorGlowProgress.value > 0f) {
                        drawPath(
                            doorPath,
                            color = DoorGlowWarm.copy(alpha = doorGlowProgress.value),
                            style = Fill
                        )
                    }
                }

                // 8. Base Platform Bar below Dome & Minarets
                if (domeProgress.value > 0f) {
                    drawRoundRect(
                        color = PlatformFill.copy(alpha = domeProgress.value),
                        topLeft = Offset(u(17.0f), baseLineY),
                        size = Size(u(66.0f), u(4.4f)),
                        cornerRadius = CornerRadius(u(0.8f), u(0.8f))
                    )
                }

                // 9. Four Twinkling Stars around the Dome
                val stars = listOf(
                    Triple(Offset(u(33.9f), u(26.8f)), u(0.85f), StarSilver),
                    Triple(Offset(u(38.1f), u(17.6f)), u(0.70f), StarSilver),
                    Triple(Offset(u(61.5f), u(16.5f)), u(0.85f), StarChampagne),
                    Triple(Offset(u(65.9f), u(23.4f)), u(1.05f), StarGoldMuted)
                )
                stars.forEachIndexed { idx, (pos, radius, color) ->
                    val scaleVal = starScales[idx].value
                    if (scaleVal > 0f) {
                        val pulse = if (idx % 2 == 0) starPulse else (1.82f - starPulse)
                        drawCircle(
                            color = color.copy(alpha = scaleVal * pulse),
                            radius = radius * scaleVal,
                            center = pos
                        )
                    }
                }

                // 10. Animated Squircle Border Trace (Gold top-left & bottom-right, Slate top-right & bottom-left)
                if (borderProgress.value > 0f) {
                    val fullBorderPath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(
                                    left = borderStroke / 2f,
                                    top = borderStroke / 2f,
                                    right = s - borderStroke / 2f,
                                    bottom = s - borderStroke / 2f
                                ),
                                cornerRadius = CornerRadius(cornerR, cornerR)
                            )
                        )
                    }
                    val measure = PathMeasure()
                    measure.setPath(fullBorderPath, false)
                    val totalLen = measure.length
                    val tracedPath = Path()
                    measure.getSegment(
                        startDistance = 0f,
                        stopDistance = totalLen * borderProgress.value,
                        destination = tracedPath,
                        startWithMoveTo = true
                    )

                    val borderBrush = Brush.linearGradient(
                        colors = listOf(
                            GoldBorder,
                            SlateBorder,
                            GoldBorder
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(s, s)
                    )
                    drawPath(
                        path = tracedPath,
                        brush = borderBrush,
                        style = Stroke(width = borderStroke, cap = StrokeCap.Round)
                    )
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            // Letter-by-letter "PrayerReminder" Title
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                titleText.forEachIndexed { index, ch ->
                    val p = letterProgress[index].value
                    Text(
                        text = ch.toString(),
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        modifier = Modifier
                            .offset(y = ((1f - p) * 12f).dp)
                            .alpha(p)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Part Subtitle: "100% Offline  •  Bhatkal Timetable  •  Adhan & Jamaat"
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "100% Offline",
                    color = SubtitleMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.alpha(subPart1Alpha.value)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(subPart2Alpha.value)
                ) {
                    Text(
                        text = "   •   ",
                        color = GoldBright,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Bhatkal Timetable",
                        color = SubtitleMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(subPart3Alpha.value)
                ) {
                    Text(
                        text = "   •   ",
                        color = GoldBright,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Adhan & Jamaat",
                        color = SubtitleMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}
