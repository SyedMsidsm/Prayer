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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syedmsidsm.prayerreminder.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val AmberGold = Color(0xFFFBBF24)
private val DeepSanctuary = Color(0xFF03080C)
private val CardBackground = Color(0xFF07141C)
private val CardBorderGold = Color(0xFFFBBF24).copy(alpha = 0.55f)
private val MutedText = Color(0xFF94A3B8)

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

    val badgeScale = remember { Animatable(if (animationsEnabled) 0.7f else 1f) }
    val badgeAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val glowAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }

    val contentOffsetY = remember { Animatable(if (animationsEnabled) 0f else -30f) }
    val titleAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val taglineAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }

    LaunchedEffect(Unit) {
        if (!animationsEnabled) {
            delay(1200)
            onFinished()
            return@LaunchedEffect
        }

        // 0 - 0.8s: Badge & Ambient Glow scale and illuminate
        launch {
            badgeAlpha.animateTo(1f, tween(500, easing = LinearEasing))
            glowAlpha.animateTo(0.9f, tween(700, easing = LinearEasing))
        }
        launch {
            badgeScale.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        }

        delay(950)

        // 0.95 - 1.4s: Shift slightly up to make room for Title & Tagline
        launch {
            contentOffsetY.animateTo(-30f, tween(550, easing = FastOutSlowInEasing))
        }

        delay(200)

        // 1.15s: "PrayerReminder" appears
        launch {
            titleAlpha.animateTo(1f, tween(400))
        }

        delay(350)

        // 1.5s: Tagline appears: "100% Offline • Bhatkal Timetable • Adhan & Jamaat"
        launch {
            taglineAlpha.animateTo(1f, tween(450))
        }

        delay(1400)

        // Transition smoothly to Onboarding / Home
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF061A22),
                        Color(0xFF030D13),
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
        // Horizontal Ambient Radial Glow behind the Badge
        Box(
            modifier = Modifier
                .size(340.dp)
                .offset(y = contentOffsetY.value.dp)
                .alpha(glowAlpha.value)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AmberGold.copy(alpha = 0.25f),
                            Color(0xFF0E3242).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = contentOffsetY.value.dp)
                .padding(24.dp)
        ) {
            // Squircle App Icon Badge as in user's video animation
            Surface(
                modifier = Modifier
                    .size(160.dp)
                    .scale(badgeScale.value)
                    .alpha(badgeAlpha.value),
                shape = RoundedCornerShape(38.dp),
                color = CardBackground,
                border = BorderStroke(1.8.dp, CardBorderGold),
                shadowElevation = 18.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.splash_icon_static),
                        contentDescription = null,
                        modifier = Modifier.size(126.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Brand Title: PrayerReminder
            Text(
                text = "PrayerReminder",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.3).sp,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Exact Tagline from video
            Row(
                modifier = Modifier.alpha(taglineAlpha.value),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "100% Offline",
                    fontSize = 12.sp,
                    color = MutedText,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = AmberGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bhatkal Timetable",
                    fontSize = 12.sp,
                    color = MutedText,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = AmberGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Adhan & Jamaat",
                    fontSize = 12.sp,
                    color = MutedText,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.intro_skip),
                fontSize = 11.sp,
                color = AmberGold.copy(alpha = 0.75f),
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }
    }
}
