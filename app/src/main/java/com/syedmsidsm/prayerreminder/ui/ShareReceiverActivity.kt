package com.syedmsidsm.prayerreminder.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.syedmsidsm.prayerreminder.PrayerReminderApp
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import com.syedmsidsm.prayerreminder.importing.FamilyShare
import com.syedmsidsm.prayerreminder.importing.ImportUiState
import com.syedmsidsm.prayerreminder.importing.ImportViewModel
import com.syedmsidsm.prayerreminder.importing.OcrEngine
import com.syedmsidsm.prayerreminder.ui.importing.FamilyImportDialog
import com.syedmsidsm.prayerreminder.ui.importing.ImportReviewScreen
import com.syedmsidsm.prayerreminder.ui.importing.ImportSavedScreen
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderTheme
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ShareReceiverActivity : AppCompatActivity() {

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

    private var receivedFamilyTimetable by mutableStateOf<Timetable?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIncomingIntent(intent)

        setContent {
            PrayerReminderTheme {
                val familyTable = receivedFamilyTimetable
                if (familyTable != null) {
                    FamilyImportDialog(
                        timetable = familyTable,
                        onConfirmImport = {
                            lifecycleScope.launch {
                                (application as PrayerReminderApp).container.timetableRepository.saveTimetable(familyTable)
                                Toast.makeText(this@ShareReceiverActivity, getString(R.string.saved_title), Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        },
                        onDismiss = { finish() }
                    )
                } else {
                    when (val state = importViewModel.uiState.value) {
                        is ImportUiState.Review -> {
                            ImportReviewScreen(
                                viewModel = importViewModel,
                                onCancel = { finish() }
                            )
                        }
                        is ImportUiState.Saved -> {
                            ImportSavedScreen(
                                timetable = state.timetable,
                                onSendToFamily = {
                                    val shareIntent = FamilyShare.createShareIntent(this, state.timetable)
                                    startActivity(shareIntent)
                                },
                                onDone = { finish() }
                            )
                        }
                        else -> {
                            // Empty / Reading
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val type = intent.type

        val isJsonOrPrtt = type == "application/json" ||
            type == "application/octet-stream" ||
            intent.data?.path?.endsWith(".prtt") == true

        if (isJsonOrPrtt) {
            val fileUri = if (action == Intent.ACTION_SEND) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                }
            } else {
                intent.data
            }

            if (fileUri != null) {
                tryImportFamilyFile(fileUri)
                return
            }
        }

        // Otherwise handle image
        val imageUri = when (action) {
            Intent.ACTION_SEND -> {
                if (type?.startsWith("image/") == true) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    }
                } else null
            }
            Intent.ACTION_VIEW -> intent.data
            else -> null
        }

        if (imageUri != null) {
            val cachedUri = copyToCache(imageUri)
            if (cachedUri != null) {
                importViewModel.processImage(cachedUri)
            } else {
                Toast.makeText(this, getString(R.string.import_failed), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun tryImportFamilyFile(uri: Uri) {
        lifecycleScope.launch {
            val result = runCatching {
                contentResolver.openInputStream(uri)?.use { stream ->
                    FamilyShare.parseAndValidate(stream).getOrThrow()
                } ?: throw IllegalArgumentException("Could not open stream")
            }

            result.onSuccess { table ->
                receivedFamilyTimetable = table
            }.onFailure {
                Toast.makeText(this@ShareReceiverActivity, getString(R.string.family_invalid), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun copyToCache(sourceUri: Uri): Uri? {
        return runCatching {
            val cacheDir = File(cacheDir, "imports").apply { if (!exists()) mkdirs() }
            val tempFile = File(cacheDir, "import_${System.currentTimeMillis()}.jpg")
            contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(tempFile)
        }.getOrNull()
    }
}
