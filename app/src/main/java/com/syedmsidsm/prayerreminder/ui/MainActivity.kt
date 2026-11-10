package com.syedmsidsm.prayerreminder.ui

import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderTheme

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        val start = SystemClock.uptimeMillis()

        val animatorDurationScale = Settings.Global.getFloat(
            contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )
        val animationsEnabled = animatorDurationScale > 0f

        splash.setKeepOnScreenCondition {
            if (!animationsEnabled) false else SystemClock.uptimeMillis() - start < 900
        }

        splash.setOnExitAnimationListener { provider ->
            provider.iconView.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .alpha(0f)
                .setDuration(250)
                .start()
            provider.view.animate()
                .alpha(0f)
                .setDuration(320)
                .withEndAction { provider.remove() }
                .start()
        }

        super.onCreate(savedInstanceState)

        setContent {
            PrayerReminderTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrayerReminderColors.Ivory),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = PrayerReminderColors.Ink
        )
    }
}
