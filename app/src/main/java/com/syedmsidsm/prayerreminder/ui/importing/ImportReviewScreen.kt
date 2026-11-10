package com.syedmsidsm.prayerreminder.ui.importing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.HijriNames
import com.syedmsidsm.prayerreminder.domain.TimetableValidator
import com.syedmsidsm.prayerreminder.domain.ValidationFlag
import com.syedmsidsm.prayerreminder.domain.model.TimetableDay
import com.syedmsidsm.prayerreminder.importing.ImportUiState
import com.syedmsidsm.prayerreminder.importing.ImportViewModel
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import java.time.LocalDate

@Composable
fun ImportReviewScreen(
    viewModel: ImportViewModel,
    onCancel: () -> Unit = {},
    onPickAnotherImage: () -> Unit = {},
    onManualEntry: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    when (val currentState = state) {
        is ImportUiState.Idle -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PrayerReminderColors.Ivory),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrayerReminderColors.Ground)
            }
        }
        is ImportUiState.Reading -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PrayerReminderColors.Ivory)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(56.dp),
                    color = PrayerReminderColors.Ground,
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.import_reading),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrayerReminderColors.Ink,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.import_offline_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrayerReminderColors.Muted,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.cancel), color = PrayerReminderColors.Ink)
                }
            }
        }
        is ImportUiState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PrayerReminderColors.Ivory)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PrayerReminderColors.FlagOutline,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.import_failed),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrayerReminderColors.Ink,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrayerReminderColors.Muted,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onPickAnotherImage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrayerReminderColors.Ground)
                ) {
                    Text(
                        stringResource(R.string.import_try_another),
                        fontWeight = FontWeight.Bold,
                        color = PrayerReminderColors.Ivory
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onManualEntry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        stringResource(R.string.import_manual),
                        color = PrayerReminderColors.Ink,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.cancel), color = PrayerReminderColors.Muted)
                }
            }
        }
        is ImportUiState.Review -> {
            ImportReviewContent(
                reviewState = currentState,
                viewModel = viewModel,
                onCancel = onCancel
            )
        }
        is ImportUiState.Saved -> {
            // Transition handled by parent container
        }
    }
}

@Composable
private fun ImportReviewContent(
    reviewState: ImportUiState.Review,
    viewModel: ImportViewModel,
    onCancel: () -> Unit
) {
    val timetable = reviewState.timetable
    val unresolvedFlags = reviewState.unresolvedFlags
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrayerReminderColors.Ivory)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.import_check_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = PrayerReminderColors.Ink
            )
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel), color = PrayerReminderColors.Muted)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${timetable.place} · ${timetable.hijriMonthName} ${timetable.hijriYear}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrayerReminderColors.Ink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${timetable.firstDate} to ${timetable.lastDate} · ${timetable.days.size} days",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrayerReminderColors.Muted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Banner
        if (unresolvedFlags.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = PrayerReminderColors.WarnBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.import_flags, unresolvedFlags.size),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = PrayerReminderColors.WarnText,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.autoFixAllFlags() }) {
                        Text(
                            text = "Auto-fix all",
                            color = PrayerReminderColors.Ground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = PrayerReminderColors.OkBg
            ) {
                val totalTimes = timetable.days.size * 6
                Text(
                    text = stringResource(R.string.import_all_good, totalTimes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = PrayerReminderColors.OkText,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Timetable Grid (Horizontal and Vertical scrollable)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(PrayerReminderColors.Surface, RoundedCornerShape(12.dp))
                .border(1.dp, PrayerReminderColors.Line, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
            ) {
                // Table Column Headers
                Row(
                    modifier = Modifier
                        .background(PrayerReminderColors.Ground)
                        .padding(vertical = 10.dp)
                ) {
                    TableHeaderCell("Is.", 40.dp)
                    TableHeaderCell("Date", 50.dp)
                    TableHeaderCell("Day", 45.dp)
                    TableHeaderCell("Fajr", 62.dp)
                    TableHeaderCell("Sunr.", 62.dp)
                    TableHeaderCell("Dhuhr", 62.dp)
                    TableHeaderCell("Asr", 62.dp)
                    TableHeaderCell("Magh.", 62.dp)
                    TableHeaderCell("Isha", 62.dp)
                }

                // Table Rows
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(timetable.days) { index, day ->
                        val isFriday = day.weekday.equals("Fri", ignoreCase = true)
                        val rowBg = if (isFriday) PrayerReminderColors.NextRowBg else PrayerReminderColors.Surface

                        Row(
                            modifier = Modifier
                                .background(rowBg)
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell("${day.hijriDay}", 40.dp)
                            val dayOfMonth = runCatching { LocalDate.parse(day.date).dayOfMonth }.getOrDefault(day.date)
                            TableCell("$dayOfMonth", 50.dp)
                            TableCell(day.weekday, 45.dp)

                            for (field in TimetableValidator.PRAYER_FIELDS) {
                                val flag = unresolvedFlags.firstOrNull { it.dayIndex == index && it.field == field }
                                TableTimeCell(
                                    value = day.times[field] ?: "",
                                    flag = flag,
                                    width = 62.dp,
                                    onCellClick = { if (flag != null) viewModel.openFixSheet(flag) }
                                )
                            }
                        }
                        HorizontalDivider(color = PrayerReminderColors.Line, thickness = 0.5.dp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.import_ampm_note),
            style = MaterialTheme.typography.bodySmall,
            color = PrayerReminderColors.Muted,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Save Button
        Button(
            onClick = { viewModel.saveTimetable() },
            enabled = reviewState.canSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrayerReminderColors.Ground,
                contentColor = PrayerReminderColors.Ivory,
                disabledContainerColor = PrayerReminderColors.Line,
                disabledContentColor = PrayerReminderColors.Muted
            )
        ) {
            Text(
                text = stringResource(R.string.import_save),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Fix Sheet Bottom Sheet
    val activeFlag = reviewState.activeFlagForFix
    if (activeFlag != null) {
        FixValueSheet(
            flag = activeFlag,
            onUseSuggestion = { viewModel.resolveFlagWithSuggestion(it) },
            onKeepCurrent = { viewModel.keepFlagValue(it) },
            onSaveCustomValue = { f, v -> viewModel.resolveFlagWithValue(f, v) },
            onDismiss = { viewModel.closeFixSheet() }
        )
    }

    // Replace confirmation dialog
    if (reviewState.showReplaceDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissReplaceDialog() },
            title = { Text(stringResource(R.string.import_replace, "${timetable.hijriMonthName} ${timetable.hijriYear}")) },
            confirmButton = {
                TextButton(onClick = { viewModel.saveTimetable(forceReplace = true) }) {
                    Text(stringResource(R.string.import_save), color = PrayerReminderColors.Ground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissReplaceDialog() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun TableHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        color = PrayerReminderColors.Ivory,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(width)
    )
}

@Composable
private fun TableCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        color = PrayerReminderColors.Ink,
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(width)
    )
}

@Composable
private fun TableTimeCell(
    value: String,
    flag: ValidationFlag?,
    width: androidx.compose.ui.unit.Dp,
    onCellClick: () -> Unit
) {
    val isFlagged = flag != null
    val bg = if (isFlagged) PrayerReminderColors.FlagBg else androidx.compose.ui.graphics.Color.Transparent
    val borderModifier = if (isFlagged) {
        Modifier.border(1.5.dp, PrayerReminderColors.FlagOutline, RoundedCornerShape(4.dp))
    } else Modifier

    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .background(bg, RoundedCornerShape(4.dp))
            .then(borderModifier)
            .clickable(enabled = isFlagged) { onCellClick() }
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isFlagged) FontWeight.Bold else FontWeight.Normal,
            color = if (isFlagged) PrayerReminderColors.Ink else PrayerReminderColors.Ink,
            textAlign = TextAlign.Center
        )
    }
}
