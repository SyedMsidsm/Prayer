package com.syedmsidsm.prayerreminder.ui.landing

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.PathParser
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LandingScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val animatorDurationScale = Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1.0f
    )
    val animationsEnabled = animatorDurationScale > 0f

    // Crescent animation states
    val crescentRotation = remember { Animatable(if (animationsEnabled) -55f else 0f) }
    val crescentScale = remember { Animatable(if (animationsEnabled) 0.55f else 1f) }
    val crescentAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }

    // 5 prayer dots animation states (scale from 0 to 1 with overshoot)
    val dot0Scale = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val dot1Scale = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val dot2Scale = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val dot3Scale = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val dot4Scale = remember { Animatable(if (animationsEnabled) 0f else 1f) }

    // Pulse ring animation states
    val ringRadius = remember { Animatable(3.9f) }
    val ringAlpha = remember { Animatable(0f) }

    // Text & Mosque animation states
    val textAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val textOffsetY = remember { Animatable(if (animationsEnabled) 20f else 0f) }
    val arabicAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val urduAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val taglineAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val mosqueAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }

    // Crescent SVG path cached
    val crescentPath = remember {
        PathParser.createPathFromPathData(
            "M65.33,24.2 A30,30 0 1,0 65.33,75.8 A26,26 0 1,1 65.33,24.2 Z"
        ).asComposePath()
    }

    LaunchedEffect(Unit) {
        if (!animationsEnabled) {
            delay(600)
            onFinished()
            return@LaunchedEffect
        }

        // 0. Mosque silhouette gently fades in
        launch {
            mosqueAlpha.animateTo(1f, tween(600, easing = LinearEasing))
        }

        // 1. Crescent sweeps in: 0 - 450ms
        launch {
            crescentAlpha.animateTo(1f, tween(350, easing = LinearEasing))
        }
        launch {
            crescentRotation.animateTo(0f, tween(450, easing = FastOutSlowInEasing))
        }
        launch {
            crescentScale.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        }

        // 2. Five dots pop in sequentially: 350ms - 890ms
        delay(350)
        launch {
            dot0Scale.animateTo(1f, tween(220, easing = FastOutSlowInEasing))
        }

        delay(80)
        launch {
            dot1Scale.animateTo(1f, tween(220, easing = FastOutSlowInEasing))
        }

        delay(80)
        launch {
            dot2Scale.animateTo(1f, tween(220, easing = FastOutSlowInEasing))
        }

        delay(80)
        launch {
            dot3Scale.animateTo(1f, tween(220, easing = FastOutSlowInEasing))
        }

        delay(80)
        launch {
            dot4Scale.animateTo(1f, tween(220, easing = FastOutSlowInEasing))
        }

        // 3. Pulse ring ripples out from center gold dot (Asr)
        delay(100)
        launch {
            ringAlpha.snapTo(0.85f)
            ringAlpha.animateTo(0f, tween(350, easing = LinearEasing))
        }
        launch {
            ringRadius.animateTo(14f, tween(350, easing = FastOutSlowInEasing))
        }

        // 4. "PrayerReminder" typography fades and slides up
        delay(60)
        launch {
            textAlpha.animateTo(1f, tween(350))
            textOffsetY.animateTo(0f, tween(350, easing = FastOutSlowInEasing))
        }

        // 5. Arabic subtitle
        delay(150)
        launch {
            arabicAlpha.animateTo(1f, tween(300))
        }

        // 6. Urdu subtitle
        delay(120)
        launch {
            urduAlpha.animateTo(1f, tween(300))
        }

        // 7. Tagline
        delay(120)
        launch {
            taglineAlpha.animateTo(1f, tween(300))
        }

        // 8. Total presentation delay, then transition to Home
        delay(400)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrayerReminderColors.Ground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onFinished() }
    ) {
        // Subtle mosque silhouette at bottom
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
                .alpha(mosqueAlpha.value * 0.08f)
        ) {
            drawMosqueSilhouette(PrayerReminderColors.Gold)
        }

        // Main content in center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
        ) {
            // Animated Brand Vector Mark
            Canvas(
                modifier = Modifier.size(140.dp)
            ) {
                val scale = size.minDimension / 100f
                withTransform({
                    scale(scale, scale, Offset.Zero)
                    translate(left = 7.3f, top = 0f)
                    rotate(degrees = -18f, pivot = Offset(42.7f, 50.0f))
                }) {
                    // Crescent path with rotation & scale animation
                    withTransform({
                        rotate(degrees = crescentRotation.value, pivot = Offset(42.7f, 50.0f))
                        scale(
                            scaleX = crescentScale.value,
                            scaleY = crescentScale.value,
                            pivot = Offset(42.7f, 50.0f)
                        )
                    }) {
                        drawPath(
                            path = crescentPath,
                            color = PrayerReminderColors.Gold.copy(alpha = crescentAlpha.value),
                            style = Fill
                        )
                    }

                    // Gold pulse ring expanding from dot 2
                    if (ringAlpha.value > 0f) {
                        drawCircle(
                            color = PrayerReminderColors.Gold.copy(alpha = ringAlpha.value),
                            radius = ringRadius.value,
                            center = Offset(43.5f, 50.0f),
                            style = Stroke(width = 1.2f)
                        )
                    }

                    // 5 Prayer Dots (bottom to top: Fajr -> Isha)
                    if (dot0Scale.value > 0f) {
                        drawCircle(
                            color = Color(0xFFF7F4EC),
                            radius = 2.8f * dot0Scale.value,
                            center = Offset(52.75f, 66.02f)
                        )
                    }

                    if (dot1Scale.value > 0f) {
                        drawCircle(
                            color = Color(0xFFF7F4EC),
                            radius = 2.8f * dot1Scale.value,
                            center = Offset(45.98f, 59.25f)
                        )
                    }

                    if (dot2Scale.value > 0f) {
                        drawCircle(
                            color = PrayerReminderColors.Gold,
                            radius = 3.9f * dot2Scale.value,
                            center = Offset(43.5f, 50.0f)
                        )
                    }

                    if (dot3Scale.value > 0f) {
                        drawCircle(
                            color = Color(0xFFF7F4EC),
                            radius = 2.8f * dot3Scale.value,
                            center = Offset(45.98f, 40.75f)
                        )
                    }

                    if (dot4Scale.value > 0f) {
                        drawCircle(
                            color = Color(0xFFF7F4EC),
                            radius = 2.8f * dot4Scale.value,
                            center = Offset(52.75f, 33.98f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // App Brand Name: "Prayer" in Ivory, "Reminder" in Gold
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = PrayerReminderColors.CardTextLight)) {
                        append("Prayer")
                    }
                    withStyle(SpanStyle(color = PrayerReminderColors.Gold)) {
                        append("Reminder")
                    }
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .offset(y = textOffsetY.value.dp)
                    .alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Arabic Title
            Text(
                text = "تذكير الصلاة",
                fontSize = 20.sp,
                color = PrayerReminderColors.CardTextLight,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(arabicAlpha.value)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Urdu Title
            Text(
                text = "نماز کی یاد دہانی",
                fontSize = 18.sp,
                color = PrayerReminderColors.CardTextLight,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(urduAlpha.value)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline
            Text(
                text = stringResource(R.string.intro_tagline),
                fontSize = 13.sp,
                color = PrayerReminderColors.OnDarkMuted,
                modifier = Modifier.alpha(taglineAlpha.value)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tap to enter indicator
            Text(
                text = stringResource(R.string.intro_skip),
                fontSize = 11.sp,
                color = PrayerReminderColors.Gold.copy(alpha = 0.8f),
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMosqueSilhouette(color: Color) {
    val w = size.width
    val h = size.height
    val cx = w / 2f

    // Central dome
    val domePath = Path().apply {
        moveTo(cx - w * 0.22f, h)
        cubicTo(
            cx - w * 0.22f, h * 0.45f,
            cx - w * 0.08f, h * 0.25f,
            cx, h * 0.22f
        )
        cubicTo(
            cx + w * 0.08f, h * 0.25f,
            cx + w * 0.22f, h * 0.45f,
            cx + w * 0.22f, h
        )
        close()
    }
    drawPath(domePath, color, style = Fill)

    // Spire above dome
    drawLine(
        color = color,
        start = Offset(cx, h * 0.22f),
        end = Offset(cx, h * 0.14f),
        strokeWidth = w * 0.005f
    )

    // Left minaret
    val lmX = cx - w * 0.30f
    val minaretWidth = w * 0.028f
    val leftMinaretPath = Path().apply {
        moveTo(lmX - minaretWidth, h)
        lineTo(lmX - minaretWidth, h * 0.40f)
        lineTo(lmX - minaretWidth * 0.6f, h * 0.36f)
        lineTo(lmX, h * 0.28f)
        lineTo(lmX + minaretWidth * 0.6f, h * 0.36f)
        lineTo(lmX + minaretWidth, h * 0.40f)
        lineTo(lmX + minaretWidth, h)
        close()
    }
    drawPath(leftMinaretPath, color, style = Fill)

    // Right minaret
    val rmX = cx + w * 0.30f
    val rightMinaretPath = Path().apply {
        moveTo(rmX - minaretWidth, h)
        lineTo(rmX - minaretWidth, h * 0.40f)
        lineTo(rmX - minaretWidth * 0.6f, h * 0.36f)
        lineTo(rmX, h * 0.28f)
        lineTo(rmX + minaretWidth * 0.6f, h * 0.36f)
        lineTo(rmX + minaretWidth, h * 0.40f)
        lineTo(rmX + minaretWidth, h)
        close()
    }
    drawPath(rightMinaretPath, color, style = Fill)
}
