package com.syedmsidsm.prayerreminder.ui

import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.syedmsidsm.prayerreminder.PrayerReminderApp
import com.syedmsidsm.prayerreminder.ui.home.HomeScreen
import com.syedmsidsm.prayerreminder.ui.home.HomeViewModel
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderTheme

class MainActivity : AppCompatActivity() {

    private val homeViewModel: HomeViewModel by viewModels {
        val container = (application as PrayerReminderApp).container
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    container.timetableRepository,
                    container.settingsRepository,
                    container.dayScheduleResolver,
                    container.nextPrayerResolver
                ) as T
            }
        }
    }

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
            val dataReady = homeViewModel.isReady.value
            if (!animationsEnabled) !dataReady else !dataReady || (SystemClock.uptimeMillis() - start < 900)
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
                HomeScreen(viewModel = homeViewModel)
            }
        }
    }
}
