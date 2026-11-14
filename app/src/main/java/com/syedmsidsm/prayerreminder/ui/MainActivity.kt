package com.syedmsidsm.prayerreminder.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.syedmsidsm.prayerreminder.reminders.AppRefresher
import com.syedmsidsm.prayerreminder.ui.home.HomeScreen
import com.syedmsidsm.prayerreminder.ui.home.HomeViewModel
import com.syedmsidsm.prayerreminder.ui.importing.ImportReviewScreen
import com.syedmsidsm.prayerreminder.ui.importing.ImportSavedScreen
import com.syedmsidsm.prayerreminder.ui.manualentry.ManualEntryScreen
import com.syedmsidsm.prayerreminder.ui.onboarding.BrandIntroScreen
import com.syedmsidsm.prayerreminder.ui.onboarding.OnboardingScreen
import com.syedmsidsm.prayerreminder.ui.reliability.ReliabilityScreen
import com.syedmsidsm.prayerreminder.ui.settings.JamaatSettingsScreen
import com.syedmsidsm.prayerreminder.ui.settings.RemindersVoiceSettingsScreen
import com.syedmsidsm.prayerreminder.ui.settings.SettingsScreen
import com.syedmsidsm.prayerreminder.ui.settings.SettingsViewModel
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderTheme
import com.syedmsidsm.prayerreminder.ui.timetables.TimetablesScreen
import com.syedmsidsm.prayerreminder.widget.WidgetUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    SETTINGS,
    JAMAAT_SETTINGS,
    REMINDERS_VOICE_SETTINGS,
    TIMETABLES,
    MANUAL_ENTRY,
    IMPORT_REVIEW,
    IMPORT_SAVED,
    RELIABILITY,
    BRAND_INTRO,
    ONBOARDING
}

class MainActivity : AppCompatActivity() {

    private val currentIntentFlow = MutableStateFlow<Intent?>(null)

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
                    context = applicationContext,
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

        // Eagerly initialize ViewModels so state is ready immediately on cold start or widget click
        homeViewModel
        settingsViewModel

        // Dismiss system splash immediately - brand landing MP4 animation plays directly in BrandIntroScreen
        splash.setKeepOnScreenCondition { false }
        splash.setOnExitAnimationListener { provider ->
            provider.remove()
        }

        super.onCreate(savedInstanceState)
        currentIntentFlow.value = intent

        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val fontScale by settingsViewModel.fontScale.collectAsState()
            val appLanguage by settingsViewModel.appLanguage.collectAsState()
            val isRtl = appLanguage in listOf("ar", "ur")
            val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                PrayerReminderTheme(themeMode = themeMode, fontScale = fontScale) {
                    MainAppNavigation()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntentFlow.value = intent
    }

    override fun onResume() {
        super.onResume()
        WidgetUpdater.updateAll(applicationContext)
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP) {
            if (com.syedmsidsm.prayerreminder.audio.VoicePlaybackService.isRunning || settingsViewModel.audioPlayer.isPlaying.value) {
                com.syedmsidsm.prayerreminder.audio.VoicePlaybackService.stop(this)
                settingsViewModel.stopSample()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    @Composable
    private fun MainAppNavigation() {
        val isOnboardingDone by settingsViewModel.isOnboardingDone.collectAsState()
        var currentScreen by remember {
            mutableStateOf(Screen.BRAND_INTRO)
        }

        LaunchedEffect(isOnboardingDone) {
            if (isOnboardingDone == false && currentScreen == Screen.HOME) {
                currentScreen = Screen.BRAND_INTRO
            }
        }

        val importState by importViewModel.uiState.collectAsState()
        val timetables by (application as PrayerReminderApp).container.timetableRepository.timetables.collectAsState()
        val activeIntent by currentIntentFlow.collectAsState()

        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                if (uri != null) {
                    importViewModel.processImage(uri)
                    currentScreen = Screen.IMPORT_REVIEW
                }
            }
        )

        LaunchedEffect(activeIntent) {
            val intentToProcess = activeIntent ?: return@LaunchedEffect
            val pinType = intentToProcess.getStringExtra("pin_widget")
            if (pinType != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(this@MainActivity)
                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    val receiverClass = when (pinType) {
                        "small" -> com.syedmsidsm.prayerreminder.widget.SmallPrayerWidgetReceiver::class.java
                        "strip" -> com.syedmsidsm.prayerreminder.widget.StripPrayerWidgetReceiver::class.java
                        else -> com.syedmsidsm.prayerreminder.widget.WidePrayerWidgetReceiver::class.java
                    }
                    appWidgetManager.requestPinAppWidget(
                        android.content.ComponentName(this@MainActivity, receiverClass),
                        null,
                        null
                    )
                }
            }
            val incomingUri: Uri? = intentToProcess.data ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intentToProcess.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intentToProcess.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            if (incomingUri != null) {
                val pathLower = incomingUri.toString().lowercase()
                val mime = contentResolver.getType(incomingUri) ?: intentToProcess.type ?: ""
                if (mime.startsWith("image/") || pathLower.endsWith(".jpg") || pathLower.endsWith(".jpeg") || pathLower.endsWith(".png")) {
                    importViewModel.processImage(incomingUri)
                    currentScreen = Screen.IMPORT_REVIEW
                } else {
                    runCatching {
                        contentResolver.openInputStream(incomingUri)?.use { stream ->
                            FamilyShare.parseAndValidate(stream).getOrThrow()
                        } ?: error("Unable to open stream")
                    }.onSuccess { table ->
                        importViewModel.loadParsedTable(table)
                        currentScreen = Screen.IMPORT_REVIEW
                    }
                }
            }
        }

        LaunchedEffect(importState) {
            if (importState is ImportUiState.Saved && currentScreen == Screen.IMPORT_REVIEW) {
                AppRefresher.refreshAll(applicationContext, "timetable_imported")
                WidgetUpdater.updateAll(applicationContext)
                currentScreen = Screen.IMPORT_SAVED
            }
        }

        BackHandler(enabled = currentScreen != Screen.HOME) {
            currentScreen = when (currentScreen) {
                Screen.JAMAAT_SETTINGS,
                Screen.REMINDERS_VOICE_SETTINGS,
                Screen.TIMETABLES,
                Screen.RELIABILITY -> Screen.SETTINGS

                Screen.MANUAL_ENTRY -> Screen.TIMETABLES

                Screen.IMPORT_REVIEW -> {
                    importViewModel.resetState()
                    if (settingsViewModel.isOnboardingDone.value == false) {
                        Screen.ONBOARDING
                    } else {
                        Screen.TIMETABLES
                    }
                }

                Screen.IMPORT_SAVED -> Screen.HOME
                Screen.SETTINGS -> Screen.HOME
                Screen.ONBOARDING, Screen.BRAND_INTRO -> Screen.HOME
                Screen.HOME -> Screen.HOME
                else -> Screen.HOME
            }
        }

        when (currentScreen) {
            Screen.BRAND_INTRO -> {
                BrandIntroScreen(
                    onFinished = {
                        currentScreen = if (isOnboardingDone == false) Screen.ONBOARDING else Screen.HOME
                    }
                )
            }
            Screen.ONBOARDING -> {
                OnboardingScreen(
                    viewModel = settingsViewModel,
                    onPickImage = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onFinished = {
                        settingsViewModel.setOnboardingDone(true)
                        WidgetUpdater.updateAll(applicationContext)
                        currentScreen = Screen.HOME
                    }
                )
            }
            Screen.HOME -> {
                HomeScreen(
                    viewModel = homeViewModel,
                    onSettingsClick = { currentScreen = Screen.SETTINGS },
                    onToggleTheme = {
                        val nextMode = if ( com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors.isDark) "light" else "dark"
                        settingsViewModel.setThemeMode(nextMode)
                    }
                )
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { currentScreen = Screen.HOME },
                    onNavigateToJamaat = { currentScreen = Screen.JAMAAT_SETTINGS },
                    onNavigateToRemindersVoice = { currentScreen = Screen.REMINDERS_VOICE_SETTINGS },
                    onNavigateToTimetables = { currentScreen = Screen.TIMETABLES },
                    onNavigateToReliability = { currentScreen = Screen.RELIABILITY },
                    onImportNewTimetable = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onAddWidget = {
                        val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(this@MainActivity)
                        val provider = android.content.ComponentName(
                            this@MainActivity,
                            com.syedmsidsm.prayerreminder.widget.WidePrayerWidgetReceiver::class.java
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
                            appWidgetManager.requestPinAppWidget(provider, null, null)
                        }
                    }
                )
            }
            Screen.RELIABILITY -> {
                ReliabilityScreen(
                    viewModel = settingsViewModel,
                    onBack = { currentScreen = Screen.SETTINGS },
                    onNavigateToTimetables = { currentScreen = Screen.TIMETABLES }
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
                    onDeleteTimetable = { table ->
                        homeViewModel.viewModelScope.launch {
                            (application as PrayerReminderApp).container.timetableRepository.deleteTimetable(
                                table.hijriYear,
                                table.hijriMonth
                            )
                            AppRefresher.refreshAll(applicationContext, "timetable_deleted")
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
                    onCancel = {
                        importViewModel.resetState()
                        currentScreen = if (settingsViewModel.isOnboardingDone.value == false) {
                            Screen.ONBOARDING
                        } else {
                            Screen.TIMETABLES
                        }
                    },
                    onPickAnotherImage = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onManualEntry = {
                        currentScreen = Screen.MANUAL_ENTRY
                    }
                )
            }
            Screen.IMPORT_SAVED -> {
                val savedTable = (importState as? ImportUiState.Saved)?.timetable
                if (savedTable != null) {
                    ImportSavedScreen(
                        timetable = savedTable,
                        onDone = {
                            importViewModel.resetState()
                            if (settingsViewModel.isOnboardingDone.value == false) {
                                settingsViewModel.setOnboardingDone(true)
                            }
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
