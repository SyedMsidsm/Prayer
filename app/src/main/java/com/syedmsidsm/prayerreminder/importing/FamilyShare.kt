package com.syedmsidsm.prayerreminder.importing

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.syedmsidsm.prayerreminder.domain.TimetableValidator
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream

object FamilyShare {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    /**
     * Exports a checked timetable as a .prtt (UTF-8 JSON) file into the shared cache directory.
     * File name format: PrayerReminder-<Place>-<Month>-<Year>.prtt
     */
    fun exportToCache(context: Context, timetable: Timetable): File {
        val sharedDir = File(context.cacheDir, "shared").apply { if (!exists()) mkdirs() }
        val safePlace = timetable.place.replace(Regex("""[^a-zA-Z0-9]"""), "")
        val safeMonth = timetable.hijriMonthName.replace(Regex("""[^a-zA-Z0-9]"""), "")
        val filename = "PrayerReminder-$safePlace-$safeMonth-${timetable.hijriYear}.prtt"
        val file = File(sharedDir, filename)

        val verifiedTable = timetable.copy(
            verified = true,
            source = "family-file"
        )
        file.writeText(json.encodeToString(Timetable.serializer(), verifiedTable))
        return file
    }

    /**
     * Creates an ACTION_SEND Intent to share the .prtt file via WhatsApp or any sharing target.
     */
    fun createShareIntent(context: Context, timetable: Timetable): Intent {
        val file = exportToCache(context, timetable)
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.files",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(intent, "Send to family")
    }

    /**
     * Parses and validates a received family timetable stream.
     * Ensures schema version, kind, required fields, and passes TimetableValidator checks.
     */
    fun parseAndValidate(inputStream: InputStream): Result<Timetable> {
        return runCatching {
            val content = inputStream.bufferedReader().use { it.readText() }
            val table = json.decodeFromString<Timetable>(content)

            // Validate schema
            require(table.kind == "prayerreminder.timetable") {
                "Invalid timetable kind: ${table.kind}"
            }
            require(table.schemaVersion == 1) {
                "Unsupported schema version: ${table.schemaVersion}"
            }
            require(table.place.isNotBlank()) {
                "Missing place name"
            }
            require(table.days.size in 29..30) {
                "Invalid day count: ${table.days.size}"
            }

            // Run validator checks
            val validator = TimetableValidator()
            val flags = validator.validate(table)
            require(flags.isEmpty()) {
                "Timetable contains validation errors: ${flags.size} flags"
            }

            table
        }
    }
}
