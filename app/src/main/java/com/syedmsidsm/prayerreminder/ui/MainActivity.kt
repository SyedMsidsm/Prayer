package com.syedmsidsm.prayerreminder.ui

import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.syedmsidsm.prayerreminder.PrayerReminderApp
import com.syedmsidsm.prayerreminder.audio.AudioSamplePlayer
import com.syedmsidsm.prayerreminder.importing.FamilyShare
import com.syedmsidsm.prayerreminder.importing.ImportUiState
import com.syedmsidsm.prayerreminder.importing.ImportViewModel
import com.syedmsidsm.prayerreminder.importing.OcrEngine
import com.syedmsidsm.prayerreminder.ui.home.HomeScreen
import com.syedmsidsm.prayerreminder.ui.home.HomeViewModel
import com.syedmsidsm.prayerreminder.ui.importing.ImportReviewScreen
import com.syedmsidsm.prayerreminder.ui.importing.ImportSavedScreen
import com.syedmsidsm.prayerreminder.ui.manualentry.ManualEntryScreen
import com.syedmsidsm.prayerreminder.ui.settings.JamaatSettingsScreen
import com.syedmsidsm.prayerreminder.ui.settings.RemindersVoiceSettingsScreen
import com.syedmsidsm.prayerreminder.ui.settings.SettingsScreen
import com.syedmsidsm.prayerreminder.ui.settings.SettingsViewModel
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderTheme
import com.syedmsidsm.prayerreminder.ui.timetables.TimetablesScreen
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    SETTINGS,
    JAMAAT_SETTINGS,
    REMINDERS_VOICE_SETTINGS,
    TIMETABLES,
    MANUAL_ENTRY,
    IMPORT_REVIEW,
    IMPORT_SAVED
}

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

    private val settingsViewModel: SettingsViewModel by viewModels {
        val container = (application as PrayerReminderApp).container
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    settingsRepository = container.settingsRepository,
                    timetableRepository = container.timetableRepository,
                    dayScheduleResolver = container.dayScheduleResolver,
                    audioPlayer = AudioSamplePlayer(applicationContext)
                ) as T
            }
        }
    }

    private val importViewModel: ImportViewModel by viewModels {
        val container = (application as PrayerReminderApp).container
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ImportViewModel(
                    ocrEngine = OcrEngine(applicationContext),
                    timetableRepository = container.timetableRepository
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
                MainAppNavigation()
            }
        }
    }

    @Composable
    private fun MainAppNavigation() {
        var currentScreen by remember { mutableStateOf(Screen.HOME) }
        val importState by importViewModel.uiState.collectAsState()
        val timetables by (application as PrayerReminderApp).container.timetableRepository.timetables.collectAsState()

        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                if (uri != null) {
                    importViewModel.processImage(uri)
                    currentScreen = Screen.IMPORT_REVIEW
                }
            }
        )

        when {
            importState is ImportUiState.Saved && currentScreen == Screen.IMPORT_REVIEW -> {
                currentScreen = Screen.IMPORT_SAVED
            }
        }

        when (currentScreen) {
            Screen.HOME -> {
                HomeScreen(
                    viewModel = homeViewModel,
                    onSettingsClick = { currentScreen = Screen.SETTINGS }
                )
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { currentScreen = Screen.HOME },
                    onNavigateToJamaat = { currentScreen = Screen.JAMAAT_SETTINGS },
                    onNavigateToRemindersVoice = { currentScreen = Screen.REMINDERS_VOICE_SETTINGS },
                    onNavigateToTimetables = { currentScreen = Screen.TIMETABLES },
                    onImportNewTimetable = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onSendCurrentToFamily = { table ->
                        val shareIntent = FamilyShare.createShareIntent(this@MainActivity, table)
                        startActivity(shareIntent)
                    }
                )
            }
            Screen.JAMAAT_SETTINGS -> {
                JamaatSettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { currentScreen = Screen.SETTINGS }
                )
            }
            Screen.REMINDERS_VOICE_SETTINGS -> {
                RemindersVoiceSettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { currentScreen = Screen.SETTINGS }
                )
            }
            Screen.TIMETABLES -> {
                TimetablesScreen(
                    timetables = timetables,
                    onBack = { currentScreen = Screen.SETTINGS },
                    onImportNew = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onManualEntry = { currentScreen = Screen.MANUAL_ENTRY },
                    onSendToFamily = { table ->
                        val shareIntent = FamilyShare.createShareIntent(this@MainActivity, table)
                        startActivity(shareIntent)
                    },
                    onDeleteTimetable = { table ->
                        homeViewModel.viewModelScope.launch {
                            (application as PrayerReminderApp).container.timetableRepository.deleteTimetable(
                                table.hijriYear,
                                table.hijriMonth
                            )
                        }
                    }
                )
            }
            Screen.MANUAL_ENTRY -> {
                ManualEntryScreen(
                    onBack = { currentScreen = Screen.TIMETABLES },
                    onStartEditing = { table ->
                        importViewModel.loadParsedTable(table)
                        currentScreen = Screen.IMPORT_REVIEW
                    }
                )
            }
            Screen.IMPORT_REVIEW -> {
                ImportReviewScreen(
                    viewModel = importViewModel,
                    onCancel = { currentScreen = Screen.TIMETABLES }
                )
            }
            Screen.IMPORT_SAVED -> {
                val savedTable = (importState as? ImportUiState.Saved)?.timetable
                if (savedTable != null) {
                    ImportSavedScreen(
                        timetable = savedTable,
                        onSendToFamily = {
                            val shareIntent = FamilyShare.createShareIntent(this@MainActivity, savedTable)
                            startActivity(shareIntent)
                        },
                        onDone = {
                            currentScreen = Screen.HOME
                        }
                    )
                } else {
                    currentScreen = Screen.HOME
                }
            }
        }
    }
}
