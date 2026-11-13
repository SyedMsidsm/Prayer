package com.syedmsidsm.prayerreminder.ui.onboarding

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syedmsidsm.prayerreminder.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val AmberGold = Color(0xFFFBBF24)
private val DeepSanctuary = Color(0xFF03080C)
private val CardTextLight = Color(0xFFF8FAFC)
private val AmberTextSoft = Color(0xFFFCD34D)
private val MutedText = Color(0xFFA8A29E)

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

    val markScale = remember { Animatable(if (animationsEnabled) 0.6f else 0.85f) }
    val markOffsetY = remember { Animatable(if (animationsEnabled) 0f else -45f) }
    val markAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val glowAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }

    val englishAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val arabicAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val urduAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val taglineAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }

    LaunchedEffect(Unit) {
        if (!animationsEnabled) {
            delay(1000)
            onFinished()
            return@LaunchedEffect
        }

        // 0 - 0.8s: Mosque Mark & Glow scale and fade in
        launch {
            markAlpha.animateTo(1f, tween(400, easing = LinearEasing))
            glowAlpha.animateTo(0.85f, tween(600, easing = LinearEasing))
        }
        launch {
            markScale.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        }

        delay(900)

        // 0.9 - 1.5s: Mark shifts gently upward
        launch {
            markScale.animateTo(0.82f, tween(550, easing = FastOutSlowInEasing))
        }
        launch {
            markOffsetY.animateTo(-45f, tween(550, easing = FastOutSlowInEasing))
        }

        delay(250)

        // 1.15s: "PrayerReminder" appears
        launch {
            englishAlpha.animateTo(1f, tween(380))
        }

        delay(400)

        // 1.55s: Arabic appears
        launch {
            arabicAlpha.animateTo(1f, tween(320))
        }

        delay(250)

        // 1.8s: Urdu appears
        launch {
            urduAlpha.animateTo(1f, tween(320))
        }

        delay(350)

        // 2.15s: Tagline appears
        launch {
            taglineAlpha.animateTo(1f, tween(380))
        }

        delay(950)

        // Transition into Onboarding
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF071E26),
                        Color(0xFF040E14),
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
        // Ambient Radial Glow behind Mosque Mark
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(y = markOffsetY.value.dp)
                .alpha(glowAlpha.value)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AmberGold.copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Mosque Sanctuary Mark
            Image(
                painter = painterResource(R.drawable.splash_icon_static),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .offset(y = markOffsetY.value.dp)
                    .scale(markScale.value)
                    .alpha(markAlpha.value)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Brand Title: PrayerReminder
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = CardTextLight)) {
                        append("Prayer")
                    }
                    withStyle(SpanStyle(color = AmberGold)) {
                        append("Reminder")
                    }
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.alpha(englishAlpha.value)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Arabic Title
            Text(
                text = "تذكير الصلاة",
                fontSize = 22.sp,
                color = AmberTextSoft,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(arabicAlpha.value)
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Urdu Title
            Text(
                text = "نماز کی یاد دہانی",
                fontSize = 20.sp,
                color = CardTextLight.copy(alpha = 0.88f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(urduAlpha.value)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Tagline
            Text(
                text = stringResource(R.string.intro_tagline),
                fontSize = 13.sp,
                color = MutedText,
                modifier = Modifier.alpha(taglineAlpha.value)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.intro_skip),
                fontSize = 12.sp,
                color = AmberGold,
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }
    }
}
