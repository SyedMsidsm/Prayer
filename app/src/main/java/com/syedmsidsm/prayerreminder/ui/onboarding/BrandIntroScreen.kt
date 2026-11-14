package com.syedmsidsm.prayerreminder.ui.onboarding

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syedmsidsm.prayerreminder.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val AmberGold = Color(0xFFFBBF24)
private val DeepSanctuary = Color(0xFF03080C)
private val CardBackground = Color(0xFF081923)
private val CardBorderGold = Color(0xFFFBBF24)
private val MutedText = Color(0xFF94A3B8)

/**
 * Brand Landing Animation Screen.
 * Full-bleed vertical portrait experience (no letterboxing / no horizontal cutoffs).
 * Features smooth expanding golden aura, the official golden squircle mosque badge,
 * brand title, and tagline with instant tap-to-skip.
 */
@Composable
fun BrandIntroScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val animatorDurationScale = Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1.0f
    )
    val animationsEnabled = animatorDurationScale > 0f

    val badgeScale = remember { Animatable(if (animationsEnabled) 0.65f else 1f) }
    val badgeAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val glowScale = remember { Animatable(if (animationsEnabled) 0.5f else 1.2f) }
    val glowAlpha = remember { Animatable(if (animationsEnabled) 0f else 0.85f) }

    val contentOffsetY = remember { Animatable(if (animationsEnabled) 0f else -26f) }
    val titleAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val taglineAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }

    LaunchedEffect(animationsEnabled) {
        if (!animationsEnabled) {
            delay(800)
            onFinished()
            return@LaunchedEffect
        }

        // 0.0s - 0.7s: Soft golden ambient glow and badge fade in & scale gracefully
        launch {
            badgeAlpha.animateTo(1f, tween(500, easing = LinearEasing))
            glowAlpha.animateTo(0.85f, tween(600, easing = LinearEasing))
        }
        launch {
            badgeScale.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
        }
        launch {
            glowScale.animateTo(1.25f, tween(1100, easing = FastOutSlowInEasing))
        }

        delay(900)

        // 0.9s - 1.3s: Shift slightly upward to welcome Title & Tagline
        launch {
            contentOffsetY.animateTo(-26f, tween(500, easing = FastOutSlowInEasing))
        }

        delay(180)

        // 1.1s: "PrayerReminder" appears
        launch {
            titleAlpha.animateTo(1f, tween(400))
        }

        delay(250)

        // 1.35s: Tagline appears
        launch {
            taglineAlpha.animateTo(1f, tween(400))
        }

        delay(1200)

        // Smooth transition into app
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF061A22),
                        Color(0xFF030E15),
                        DeepSanctuary
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onFinished() },
        contentAlignment = Alignment.Center
    ) {
        // Full bleed soft golden-teal ambient radial glow behind the badge
        Box(
            modifier = Modifier
                .size(420.dp)
                .offset(y = contentOffsetY.value.dp)
                .scale(glowScale.value)
                .alpha(glowAlpha.value)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AmberGold.copy(alpha = 0.28f),
                            Color(0xFF14384A).copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = contentOffsetY.value.dp)
                .padding(horizontal = 24.dp)
        ) {
            // Official Golden Squircle Mosque Badge
            Surface(
                modifier = Modifier
                    .size(164.dp)
                    .scale(badgeScale.value)
                    .alpha(badgeAlpha.value),
                shape = RoundedCornerShape(38.dp),
                color = CardBackground,
                border = BorderStroke(1.8.dp, CardBorderGold),
                shadowElevation = 20.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.splash_icon_static),
                        contentDescription = null,
                        modifier = Modifier.size(136.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Brand Title: PrayerReminder
            Text(
                text = "PrayerReminder",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.4).sp,
                maxLines = 1,
                softWrap = false,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tagline
            Row(
                modifier = Modifier.alpha(taglineAlpha.value),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "100% Offline",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AmberGold
                )
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = MutedText.copy(alpha = 0.6f)
                )
                Text(
                    text = "Bhatkal Timetable",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = MutedText.copy(alpha = 0.6f)
                )
                Text(
                    text = "Adhan & Jamaat",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AmberGold
                )
            }
        }
    }
}
