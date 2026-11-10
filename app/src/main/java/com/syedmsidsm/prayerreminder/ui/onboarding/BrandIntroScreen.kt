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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
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
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    val markScale = remember { Animatable(if (animationsEnabled) 0.55f else 0.75f) }
    val markOffsetY = remember { Animatable(if (animationsEnabled) 0f else -60f) }
    val markAlpha = remember { Animatable(if (animationsEnabled) 0f else 1f) }

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

        // 0 - 1.0s: Mark scales in and fades in
        launch {
            markAlpha.animateTo(1f, tween(350, easing = LinearEasing))
        }
        launch {
            markScale.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
        }

        delay(1000)

        // 1.0 - 1.6s: Mark rises and scales to 0.75
        launch {
            markScale.animateTo(0.75f, tween(600, easing = FastOutSlowInEasing))
        }
        launch {
            markOffsetY.animateTo(-60f, tween(600, easing = FastOutSlowInEasing))
        }

        delay(300)

        // 1.3s: "PrayerReminder" appears
        launch {
            englishAlpha.animateTo(1f, tween(400))
        }

        delay(500)

        // 1.8s: Arabic appears
        launch {
            arabicAlpha.animateTo(1f, tween(300))
        }

        delay(300)

        // 2.1s: Urdu appears
        launch {
            urduAlpha.animateTo(1f, tween(300))
        }

        delay(500)

        // 2.6s: Tagline appears
        launch {
            taglineAlpha.animateTo(1f, tween(400))
        }

        delay(1000)

        // 3.6s: Finish intro and slide into Onboarding
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrayerReminderColors.Ground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onFinished() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Brand Mark
            Image(
                painter = painterResource(R.drawable.splash_icon_static),
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .offset(y = markOffsetY.value.dp)
                    .scale(markScale.value)
                    .alpha(markAlpha.value)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 1.3s: "PrayerReminder"
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
                modifier = Modifier.alpha(englishAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 1.8s: Arabic Title
            Text(
                text = "تذكير الصلاة",
                fontSize = 22.sp,
                color = PrayerReminderColors.CardTextLight,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(arabicAlpha.value)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 2.1s: Urdu Title
            Text(
                text = "نماز کی یاد دہانی",
                fontSize = 20.sp,
                color = PrayerReminderColors.CardTextLight,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(urduAlpha.value)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2.6s: Tagline
            Text(
                text = stringResource(R.string.intro_tagline),
                fontSize = 14.sp,
                color = PrayerReminderColors.OnDarkMuted,
                modifier = Modifier.alpha(taglineAlpha.value)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.intro_skip),
                fontSize = 12.sp,
                color = PrayerReminderColors.Gold,
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }
    }
}
