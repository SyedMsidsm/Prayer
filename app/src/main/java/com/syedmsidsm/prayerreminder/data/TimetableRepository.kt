package com.syedmsidsm.prayerreminder.data

import android.content.Context
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate

class TimetableRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    private val timetablesDir: File
        get() = File(context.filesDir, "timetables").apply { if (!exists()) mkdirs() }

    private val _timetables = MutableStateFlow<List<Timetable>>(emptyList())
    val timetables: StateFlow<List<Timetable>> = _timetables.asStateFlow()

    init {
        ensureBuiltInTablesLoaded()
        loadAllFromDisk()
    }

    suspend fun initialize() = withContext(ioDispatcher) {
        ensureBuiltInTablesLoaded()
        loadAllFromDisk()
    }

    private fun ensureBuiltInTablesLoaded() {
        val builtInAssets = listOf("default_timetable.json", "bhatkal_1448_06.json")
        for (assetName in builtInAssets) {
            runCatching {
                context.assets.open(assetName).bufferedReader().use { reader ->
                    val content = reader.readText()
                    val table = json.decodeFromString<Timetable>(content)
                    val filename = "${table.hijriYear}-${table.hijriMonth.toString().padStart(2, '0')}.json"
                    val targetFile = File(timetablesDir, filename)
                    if (!targetFile.exists()) {
                        targetFile.writeText(json.encodeToString(Timetable.serializer(), table))
                    }
                }
            }
        }
    }

    private fun loadAllFromDisk() {
        val files = timetablesDir.listFiles { _, name -> name.endsWith(".json") } ?: emptyArray()
        val loaded = mutableListOf<Timetable>()
        for (file in files) {
            runCatching {
                val content = file.readText()
                val table = json.decodeFromString<Timetable>(content)
                loaded.add(table)
            }
        }
        // Sort descending by first date (newest first)
        _timetables.value = loaded.sortedByDescending { it.firstDate }
    }

    suspend fun saveTimetable(timetable: Timetable) = withContext(ioDispatcher) {
        saveTimetableSync(timetable)
    }

    private fun saveTimetableSync(timetable: Timetable) {
        val filename = "${timetable.hijriYear}-${timetable.hijriMonth.toString().padStart(2, '0')}.json"
        val file = File(timetablesDir, filename)
        file.writeText(json.encodeToString(Timetable.serializer(), timetable))
        loadAllFromDisk()
    }

    suspend fun deleteTimetable(hijriYear: Int, hijriMonth: Int): Boolean = withContext(ioDispatcher) {
        val filename = "$hijriYear-${hijriMonth.toString().padStart(2, '0')}.json"
        val file = File(timetablesDir, filename)
        val deleted = if (file.exists()) file.delete() else false
        if (deleted) {
            loadAllFromDisk()
        }
        deleted
    }

    fun getTimetableFor(date: LocalDate): Timetable? {
        val dateIso = date.toString()
        return _timetables.value.firstOrNull { table ->
            table.days.any { it.date == dateIso }
        }
    }
}
