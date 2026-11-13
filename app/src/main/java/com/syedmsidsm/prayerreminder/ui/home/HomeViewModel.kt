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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
    val missedCount7d: Int = 0
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

            combine(
                timetableRepository.timetables,
                settingsRepository.jamaatRules,
                settingsRepository.voiceEnabled,
                settingsRepository.remBeforeAzanMin,
                settingsRepository.remBeforeJamaatMin
            ) { tables, rules, voiceOn, beforeAzanMin, beforeJamaatMin ->
                FiveSettings(tables, rules, voiceOn, beforeAzanMin, beforeJamaatMin)
            }.collectLatest { (tables, rules, voiceOn, beforeAzanMin, beforeJamaatMin) ->
                updateStateWithRules(tables, rules, voiceOn, beforeAzanMin, beforeJamaatMin)
            }
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

    private suspend fun updateStateWithRules(
        tables: List<Timetable>,
        rules: Map<String, JamaatRule>,
        voiceOn: Boolean,
        beforeAzanMin: Int,
        beforeJamaatMin: Int
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

        _uiState.value = _uiState.value.copy(
            isReady = true,
            place = activeTable?.place ?: "Bhatkal",
            activeTimetable = activeTable,
            selectedDate = displayDate,
            schedule = todaySchedule,
            nextPrayer = next,
            currentTime = currentTime,
            voiceEnabled = voiceOn,
            voiceSummary = voiceSummaryText,
            reminderStates = remStates,
            isSimulatedDemoDate = isDemo
        )
        isReady.value = true
    }

    suspend fun loadData() {
        val tables = timetableRepository.timetables.value
        val rules = settingsRepository.jamaatRules.first()
        val voiceOn = settingsRepository.voiceEnabled.first()
        val beforeAzanMin = settingsRepository.remBeforeAzanMin.first()
        val beforeJamaatMin = settingsRepository.remBeforeJamaatMin.first()
        updateStateWithRules(tables, rules, voiceOn, beforeAzanMin, beforeJamaatMin)
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

                // Automatically re-resolve schedule if date rolled over at midnight
                val todaySchedule = if (!current.isSimulatedDemoDate && effectiveDate != current.selectedDate) {
                    dayScheduleResolver.resolve(effectiveDate, tables, rules)
                } else {
                    current.schedule ?: dayScheduleResolver.resolve(effectiveDate, tables, rules)
                }

                val tomorrowSchedule = dayScheduleResolver.resolve(
                    effectiveDate.plusDays(1),
                    tables,
                    rules
                )

                val updatedTime = if (current.isSimulatedDemoDate) {
                    current.currentTime.plusSeconds(1)
                } else {
                    now.toLocalTime()
                }

                val activeTable = timetableRepository.getTimetableFor(effectiveDate)
                    ?: tables.firstOrNull()

                val next = nextPrayerResolver.resolve(updatedTime, todaySchedule, tomorrowSchedule)
                _uiState.value = current.copy(
                    selectedDate = effectiveDate,
                    activeTimetable = activeTable,
                    schedule = todaySchedule,
                    currentTime = updatedTime,
                    nextPrayer = next
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

    fun setDate(date: LocalDate) {
        viewModelScope.launch {
            val tables = timetableRepository.timetables.value
            val rules = settingsRepository.jamaatRules.first()
            val todaySchedule = dayScheduleResolver.resolve(date, tables, rules)
            val tomorrowSchedule = dayScheduleResolver.resolve(date.plusDays(1), tables, rules)
            val simTime = LocalTime.of(14, 35)
            val next = nextPrayerResolver.resolve(simTime, todaySchedule, tomorrowSchedule)
            val activeTable = timetableRepository.getTimetableFor(date) ?: tables.firstOrNull()

            _uiState.value = _uiState.value.copy(
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
