package com.syedmsidsm.prayerreminder.importing

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syedmsidsm.prayerreminder.data.TimetableRepository
import com.syedmsidsm.prayerreminder.domain.TimetableParser
import com.syedmsidsm.prayerreminder.domain.TimetableValidator
import com.syedmsidsm.prayerreminder.domain.ValidationFlag
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ImportUiState {
    data object Idle : ImportUiState
    data class Reading(val imageUri: Uri) : ImportUiState
    data class Review(
        val timetable: Timetable,
        val flags: List<ValidationFlag>,
        val resolvedFlagIds: Set<Pair<Int, String>> = emptySet(),
        val activeFlagForFix: ValidationFlag? = null,
        val showReplaceDialog: Boolean = false
    ) : ImportUiState {
        val unresolvedFlags: List<ValidationFlag>
            get() = flags.filter { !resolvedFlagIds.contains(Pair(it.dayIndex, it.field)) }

        val canSave: Boolean
            get() = timetable.days.isNotEmpty()
    }
    data class Saved(val timetable: Timetable) : ImportUiState
    data class Error(val message: String) : ImportUiState
}

class ImportViewModel(
    private val ocrEngine: OcrEngine,
    private val timetableParser: TimetableParser = TimetableParser(),
    private val timetableValidator: TimetableValidator = TimetableValidator(),
    private val timetableRepository: TimetableRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = ImportUiState.Idle
    }

    fun processImage(uri: Uri) {
        viewModelScope.launch {
            Log.d("ImportViewModel", "processImage started for URI: $uri")
            _uiState.value = ImportUiState.Reading(uri)
            try {
                val baseBitmap = ocrEngine.loadBitmap(uri)
                var tokens = ocrEngine.recognizeFromBitmap(baseBitmap)
                Log.d("ImportViewModel", "Recognized ${tokens.size} OCR tokens (pass 0). Parsing timetable...")
                var parseResult = timetableParser.parse(tokens)

                // If first pass didn't yield a full timetable, try 90, 270, 180 degrees
                if (parseResult == null || parseResult.timetable.days.size < 5) {
                    val testRotations = listOf(90f, 270f, 180f)
                    for (deg in testRotations) {
                        Log.d("ImportViewModel", "Attempting rotation fallback at $deg degrees...")
                        val rotated = ocrEngine.rotateBitmap(baseBitmap, deg)
                        val candidateTokens = ocrEngine.recognizeFromBitmap(rotated)
                        val candidateResult = timetableParser.parse(candidateTokens)
                        if (candidateResult != null && candidateResult.timetable.days.size > (parseResult?.timetable?.days?.size ?: 0)) {
                            Log.d("ImportViewModel", "Found better timetable with rotation $deg: ${candidateResult.timetable.days.size} days")
                            tokens = candidateTokens
                            parseResult = candidateResult
                            if (candidateResult.timetable.days.size >= 25) break
                        }
                    }
                }

                if (parseResult != null) {
                    val flags = timetableValidator.validate(parseResult.timetable)
                    Log.d("ImportViewModel", "Parsed timetable for ${parseResult.timetable.place} (${parseResult.timetable.hijriMonthName} ${parseResult.timetable.hijriYear}) with ${parseResult.timetable.days.size} days, ${flags.size} flags")
                    _uiState.value = ImportUiState.Review(
                        timetable = parseResult.timetable,
                        flags = flags
                    )
                } else {
                    Log.w("ImportViewModel", "TimetableParser.parse returned null for ${tokens.size} tokens")
                    _uiState.value = ImportUiState.Error("Could not find a prayer timetable in this image. Please check the image and try again.")
                }
            } catch (e: Exception) {
                Log.e("ImportViewModel", "Error reading image", e)
                _uiState.value = ImportUiState.Error("Error reading image: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun loadParsedTable(timetable: Timetable) {
        val flags = timetableValidator.validate(timetable)
        _uiState.value = ImportUiState.Review(
            timetable = timetable,
            flags = flags
        )
    }

    fun openFixSheet(flag: ValidationFlag) {
        val current = _uiState.value as? ImportUiState.Review ?: return
        _uiState.value = current.copy(activeFlagForFix = flag)
    }

    fun closeFixSheet() {
        val current = _uiState.value as? ImportUiState.Review ?: return
        _uiState.value = current.copy(activeFlagForFix = null)
    }

    fun resolveFlagWithSuggestion(flag: ValidationFlag) {
        val suggestion = flag.suggestion ?: return
        resolveFlagWithValue(flag, suggestion)
    }

    fun autoFixAllFlags() {
        val current = _uiState.value as? ImportUiState.Review ?: return
        val days = current.timetable.days.toMutableList()
        val newResolved = current.resolvedFlagIds.toMutableSet()

        for (flag in current.unresolvedFlags) {
            val suggestion = flag.suggestion
            val targetDay = days.getOrNull(flag.dayIndex)
            if (suggestion != null && targetDay != null) {
                val updatedTimes = targetDay.times.toMutableMap()
                if (TimetableValidator.PRAYER_FIELDS.contains(flag.field)) {
                    val formattedTime = timetableParser.cleanAndFormatTime(suggestion, flag.field) ?: suggestion
                    updatedTimes[flag.field] = formattedTime
                }
                val updatedDay = when (flag.field) {
                    "weekday" -> targetDay.copy(weekday = suggestion)
                    "hijriDay" -> targetDay.copy(hijriDay = suggestion.toIntOrNull() ?: targetDay.hijriDay)
                    "date" -> targetDay.copy(date = suggestion)
                    else -> targetDay.copy(times = updatedTimes)
                }
                days[flag.dayIndex] = updatedDay
            }
            newResolved.add(Pair(flag.dayIndex, flag.field))
        }

        val updatedTable = current.timetable.copy(days = days)
        val newFlags = timetableValidator.validate(updatedTable)
        _uiState.value = current.copy(
            timetable = updatedTable,
            flags = newFlags,
            resolvedFlagIds = newResolved,
            activeFlagForFix = null
        )
    }

    fun resolveFlagWithValue(flag: ValidationFlag, newValue: String) {
        val current = _uiState.value as? ImportUiState.Review ?: return
        val days = current.timetable.days.toMutableList()
        val targetDay = days.getOrNull(flag.dayIndex) ?: return

        val updatedTimes = targetDay.times.toMutableMap()
        if (TimetableValidator.PRAYER_FIELDS.contains(flag.field)) {
            val formattedTime = timetableParser.cleanAndFormatTime(newValue, flag.field) ?: newValue
            updatedTimes[flag.field] = formattedTime
        }

        val updatedDay = when (flag.field) {
            "weekday" -> targetDay.copy(weekday = newValue)
            "hijriDay" -> targetDay.copy(hijriDay = newValue.toIntOrNull() ?: targetDay.hijriDay)
            "date" -> targetDay.copy(date = newValue)
            else -> targetDay.copy(times = updatedTimes)
        }

        days[flag.dayIndex] = updatedDay
        val updatedTable = current.timetable.copy(days = days)
        val newFlags = timetableValidator.validate(updatedTable)
        val newResolvedIds = current.resolvedFlagIds + Pair(flag.dayIndex, flag.field)

        _uiState.value = current.copy(
            timetable = updatedTable,
            flags = newFlags,
            resolvedFlagIds = newResolvedIds,
            activeFlagForFix = null
        )
    }

    fun keepFlagValue(flag: ValidationFlag) {
        val current = _uiState.value as? ImportUiState.Review ?: return
        val newResolvedIds = current.resolvedFlagIds + Pair(flag.dayIndex, flag.field)
        _uiState.value = current.copy(
            resolvedFlagIds = newResolvedIds,
            activeFlagForFix = null
        )
    }

    fun updateMetadata(place: String, hijriMonth: Int, hijriYear: Int) {
        val current = _uiState.value as? ImportUiState.Review ?: return
        val updatedTable = current.timetable.copy(
            place = place,
            hijriMonth = hijriMonth,
            hijriYear = hijriYear
        )
        _uiState.value = current.copy(timetable = updatedTable)
    }

    fun saveTimetable(forceReplace: Boolean = false) {
        val current = _uiState.value as? ImportUiState.Review ?: return
        if (!current.canSave) return

        viewModelScope.launch {
            val existing = timetableRepository.timetables.value.firstOrNull {
                it.hijriYear == current.timetable.hijriYear && it.hijriMonth == current.timetable.hijriMonth
            }
            if (existing != null && !forceReplace) {
                _uiState.value = current.copy(showReplaceDialog = true)
                return@launch
            }

            val verifiedTable = current.timetable.copy(verified = true)
            timetableRepository.saveTimetable(verifiedTable)
            _uiState.value = ImportUiState.Saved(verifiedTable)
        }
    }

    fun dismissReplaceDialog() {
        val current = _uiState.value as? ImportUiState.Review ?: return
        _uiState.value = current.copy(showReplaceDialog = false)
    }
}
