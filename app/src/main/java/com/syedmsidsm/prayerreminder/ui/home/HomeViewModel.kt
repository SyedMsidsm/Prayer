package com.syedmsidsm.prayerreminder.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syedmsidsm.prayerreminder.data.SettingsRepository
import com.syedmsidsm.prayerreminder.data.TimetableRepository
import com.syedmsidsm.prayerreminder.domain.DayScheduleResolver
import com.syedmsidsm.prayerreminder.domain.NextPrayerResolver
import com.syedmsidsm.prayerreminder.domain.model.DaySchedule
import com.syedmsidsm.prayerreminder.domain.model.JamaatRule
import com.syedmsidsm.prayerreminder.domain.model.NextPrayer
import com.syedmsidsm.prayerreminder.domain.model.Prayer
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import com.syedmsidsm.prayerreminder.ui.format.TimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class HomeUiState(
    val isReady: Boolean = false,
    val place: String = "Bhatkal",
    val activeTimetable: Timetable? = null,
    val selectedDate: LocalDate = LocalDate.now(ZoneId.of("Asia/Kolkata")),
    val schedule: DaySchedule? = null,
    val nextPrayer: NextPrayer? = null,
    val currentTime: LocalTime = LocalTime.now(ZoneId.of("Asia/Kolkata")),
    val voiceSummary: String = "",
    val voiceEnabled: Boolean = true,
    val reminderStates: Map<Prayer, Boolean> = emptyMap(),
    val isSimulatedDemoDate: Boolean = false,
    val missedCount7d: Int = 0,
    val currentPrayerSky: Prayer = Prayer.FAJR,
    val skyOverride: String = "",
    val activeSky: Prayer = Prayer.FAJR,
    val countdownHms: String = "-00:00:00",
    val shortCountdown: String = "0m",
    val progressPct: Float = 0.15f,
    val alertModes: Map<Prayer, String> = emptyMap(),
    val use24Hour: Boolean = false
)

class HomeViewModel(
    private val timetableRepository: TimetableRepository,
    private val settingsRepository: SettingsRepository,
    private val dayScheduleResolver: DayScheduleResolver,
    private val nextPrayerResolver: NextPrayerResolver,
    private val zoneId: ZoneId = ZoneId.of("Asia/Kolkata")
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val isReady = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            timetableRepository.initialize()

            val coreFlow = combine(
                timetableRepository.timetables,
                settingsRepository.jamaatRules,
                settingsRepository.voiceEnabled,
                settingsRepository.remBeforeAzanMin,
                settingsRepository.remBeforeJamaatMin
            ) { tables, rules, voiceOn, beforeAzanMin, beforeJamaatMin ->
                FiveSettings(tables, rules, voiceOn, beforeAzanMin, beforeJamaatMin)
            }

            combine(
                coreFlow,
                settingsRepository.use24Hour,
                settingsRepository.skyOverride,
                settingsRepository.prayerAlertModes
            ) { core, use24h, skyOverride, alertModes ->
                updateStateWithRules(
                    tables = core.tables,
                    rules = core.rules,
                    voiceOn = core.voiceOn,
                    beforeAzanMin = core.beforeAzanMin,
                    beforeJamaatMin = core.beforeJamaatMin,
                    use24h = use24h,
                    skyOverride = skyOverride,
                    alertModes = alertModes
                )
            }.collectLatest {}
        }

        startCountdownLoop()
    }

    private data class FiveSettings(
        val tables: List<Timetable>,
        val rules: Map<String, JamaatRule>,
        val voiceOn: Boolean,
        val beforeAzanMin: Int,
        val beforeJamaatMin: Int
    )

    private fun resolveCurrentSkyPrayer(currentTime: LocalTime, schedule: DaySchedule): Prayer {
        val fajr = schedule.azan[Prayer.FAJR] ?: return Prayer.ISHA
        val sunrise = schedule.azan[Prayer.SUNRISE] ?: fajr.plusHours(1)
        val dhuhr = schedule.azan[Prayer.DHUHR] ?: sunrise.plusHours(6)
        val asr = schedule.azan[Prayer.ASR] ?: dhuhr.plusHours(3)
        val maghrib = schedule.azan[Prayer.MAGHRIB] ?: asr.plusHours(3)
        val isha = schedule.azan[Prayer.ISHA] ?: maghrib.plusHours(1)

        return when {
            currentTime.isBefore(fajr) -> Prayer.ISHA
            currentTime.isBefore(sunrise) -> Prayer.FAJR
            currentTime.isBefore(dhuhr) -> Prayer.SUNRISE
            currentTime.isBefore(asr) -> Prayer.DHUHR
            currentTime.isBefore(maghrib) -> Prayer.ASR
            currentTime.isBefore(isha) -> Prayer.MAGHRIB
            else -> Prayer.ISHA
        }
    }

    private fun computeProgress(
        currentTime: LocalTime,
        schedule: DaySchedule,
        next: NextPrayer?
    ): Float {
        if (next == null) return 0.15f
        val ordered = listOfNotNull(
            schedule.azan[Prayer.FAJR]?.let { Prayer.FAJR to it },
            schedule.azan[Prayer.SUNRISE]?.let { Prayer.SUNRISE to it },
            schedule.azan[Prayer.DHUHR]?.let { Prayer.DHUHR to it },
            schedule.azan[Prayer.ASR]?.let { Prayer.ASR to it },
            schedule.azan[Prayer.MAGHRIB]?.let { Prayer.MAGHRIB to it },
            schedule.azan[Prayer.ISHA]?.let { Prayer.ISHA to it }
        )
        if (ordered.isEmpty()) return 0.15f

        val nextIdx = ordered.indexOfFirst { it.first == next.prayer }.coerceAtLeast(0)
        val prevIdx = (nextIdx - 1 + ordered.size) % ordered.size
        val prevTime = ordered[prevIdx].second

        val nowSec = currentTime.toSecondOfDay()
        var targetSec = next.azanTime.toSecondOfDay()
        if (next.isTomorrow || targetSec <= nowSec) {
            targetSec += 86400
        }
        var prevSec = prevTime.toSecondOfDay()
        if (nextIdx == 0 && !next.isTomorrow) {
            prevSec -= 86400
        } else if (prevSec >= targetSec) {
            prevSec -= 86400
        }

        val total = (targetSec - prevSec).coerceAtLeast(1).toFloat()
        val elapsed = (nowSec - prevSec).coerceAtLeast(0).toFloat()
        return (elapsed / total).coerceIn(0.05f, 1.0f)
    }

    private suspend fun updateStateWithRules(
        tables: List<Timetable>,
        rules: Map<String, JamaatRule>,
        voiceOn: Boolean,
        beforeAzanMin: Int,
        beforeJamaatMin: Int,
        use24h: Boolean = false,
        skyOverride: String = "",
        alertModes: Map<Prayer, String> = emptyMap()
    ) {
        val now = ZonedDateTime.now(zoneId)
        val today = now.toLocalDate()

        val isDemo = _uiState.value.isSimulatedDemoDate
        val displayDate = if (isDemo) _uiState.value.selectedDate else today

        val activeTable = timetableRepository.getTimetableFor(displayDate)
            ?: tables.firstOrNull()

        val todaySchedule = dayScheduleResolver.resolve(displayDate, tables, rules)
        val tomorrowSchedule = dayScheduleResolver.resolve(displayDate.plusDays(1), tables, rules)

        val currentTime = if (isDemo) {
            _uiState.value.currentTime
        } else {
            now.toLocalTime()
        }

        val next = nextPrayerResolver.resolve(currentTime, todaySchedule, tomorrowSchedule)

        val remStates = mutableMapOf<Prayer, Boolean>()
        for (prayer in Prayer.entries) {
            if (prayer.hasReminders) {
                remStates[prayer] = settingsRepository.isPrayerReminderEnabled(prayer).first()
            }
        }

        val voiceSummaryText = if (voiceOn) {
            "${beforeAzanMin} min · ${beforeJamaatMin} min"
        } else {
            ""
        }

        val currentSky = resolveCurrentSkyPrayer(currentTime, todaySchedule)
        val overridePrayer = skyOverride.takeIf { it.isNotBlank() }?.let {
            runCatching { Prayer.valueOf(it) }.getOrNull()
        }
        val activeSky = overridePrayer ?: currentSky

        val dur = next?.remainingDuration ?: Duration.ZERO
        val countdownHms = TimeFormatter.formatCountdownHms(dur)
        val shortCountdown = TimeFormatter.formatShortCountdown(dur)
        val progressPct = computeProgress(currentTime, todaySchedule, next)

        val placeLabel = activeTable?.place?.ifBlank { "Bhatkal" } ?: "Bhatkal"

        _uiState.value = _uiState.value.copy(
            isReady = true,
            place = placeLabel,
            activeTimetable = activeTable,
            selectedDate = displayDate,
            schedule = todaySchedule,
            nextPrayer = next,
            currentTime = currentTime,
            voiceEnabled = voiceOn,
            voiceSummary = voiceSummaryText,
            reminderStates = remStates,
            isSimulatedDemoDate = isDemo,
            currentPrayerSky = currentSky,
            skyOverride = skyOverride,
            activeSky = activeSky,
            countdownHms = countdownHms,
            shortCountdown = shortCountdown,
            progressPct = progressPct,
            alertModes = alertModes,
            use24Hour = use24h
        )
        isReady.value = true
    }

    suspend fun loadData() {
        val tables = timetableRepository.timetables.value
        val rules = settingsRepository.jamaatRules.first()
        val voiceOn = settingsRepository.voiceEnabled.first()
        val beforeAzanMin = settingsRepository.remBeforeAzanMin.first()
        val beforeJamaatMin = settingsRepository.remBeforeJamaatMin.first()
        val use24h = settingsRepository.use24Hour.first()
        val skyOverride = settingsRepository.skyOverride.first()
        val alertModes = settingsRepository.prayerAlertModes.first()
        updateStateWithRules(tables, rules, voiceOn, beforeAzanMin, beforeJamaatMin, use24h, skyOverride, alertModes)
    }

    private fun startCountdownLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val current = _uiState.value
                val tables = timetableRepository.timetables.value
                val rules = settingsRepository.jamaatRules.first()

                val now = ZonedDateTime.now(zoneId)
                val effectiveDate = if (current.isSimulatedDemoDate) {
                    current.selectedDate
                } else {
                    now.toLocalDate()
                }

                val todaySchedule = dayScheduleResolver.resolve(effectiveDate, tables, rules)
                val tomorrowSchedule = dayScheduleResolver.resolve(effectiveDate.plusDays(1), tables, rules)

                val updatedTime = if (current.isSimulatedDemoDate) {
                    current.currentTime.plusSeconds(1)
                } else {
                    now.toLocalTime()
                }

                val activeTable = timetableRepository.getTimetableFor(effectiveDate)
                    ?: tables.firstOrNull()

                val next = nextPrayerResolver.resolve(updatedTime, todaySchedule, tomorrowSchedule)
                val currentSky = resolveCurrentSkyPrayer(updatedTime, todaySchedule)
                val overridePrayer = current.skyOverride.takeIf { it.isNotBlank() }?.let {
                    runCatching { Prayer.valueOf(it) }.getOrNull()
                }
                val activeSky = overridePrayer ?: currentSky
                val dur = next?.remainingDuration ?: Duration.ZERO

                val placeLabel = activeTable?.place?.ifBlank { "Bhatkal" } ?: "Bhatkal"

                _uiState.value = current.copy(
                    place = placeLabel,
                    selectedDate = effectiveDate,
                    activeTimetable = activeTable,
                    schedule = todaySchedule,
                    currentTime = updatedTime,
                    nextPrayer = next,
                    currentPrayerSky = currentSky,
                    activeSky = activeSky,
                    countdownHms = TimeFormatter.formatCountdownHms(dur),
                    shortCountdown = TimeFormatter.formatShortCountdown(dur),
                    progressPct = computeProgress(updatedTime, todaySchedule, next)
                )
            }
        }
    }

    fun togglePrayerReminder(prayer: Prayer) {
        viewModelScope.launch {
            settingsRepository.togglePrayerReminder(prayer)
            val updated = _uiState.value.reminderStates.toMutableMap()
            updated[prayer] = !(updated[prayer] ?: true)
            _uiState.value = _uiState.value.copy(reminderStates = updated)
        }
    }

    fun cyclePrayerAlertMode(prayer: Prayer) {
        viewModelScope.launch {
            settingsRepository.cyclePrayerAlertMode(prayer)
        }
    }

    fun cycleSkyVibe() {
        viewModelScope.launch {
            val keys = listOf("", "FAJR", "SUNRISE", "DHUHR", "ASR", "MAGHRIB", "ISHA")
            val current = settingsRepository.skyOverride.first()
            val nextIdx = (keys.indexOf(current) + 1) % keys.size
            settingsRepository.setSkyOverride(keys[nextIdx])
        }
    }

    fun setDate(date: LocalDate) {
        viewModelScope.launch {
            val tables = timetableRepository.timetables.value
            val rules = settingsRepository.jamaatRules.first()
            val todaySchedule = dayScheduleResolver.resolve(date, tables, rules)
            val tomorrowSchedule = dayScheduleResolver.resolve(date.plusDays(1), tables, rules)
            val simTime = LocalTime.of(14, 35)
            val next = nextPrayerResolver.resolve(simTime, todaySchedule, tomorrowSchedule)
            val activeTable = timetableRepository.getTimetableFor(date) ?: tables.firstOrNull()

            val placeLabel = activeTable?.place?.ifBlank { "Bhatkal" } ?: "Bhatkal"

            _uiState.value = _uiState.value.copy(
                place = placeLabel,
                selectedDate = date,
                activeTimetable = activeTable,
                schedule = todaySchedule,
                nextPrayer = next,
                currentTime = simTime,
                isSimulatedDemoDate = true
            )
        }
    }
}
