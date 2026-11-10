package com.syedmsidsm.prayerreminder.ui.timetables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun TimetablesScreen(
    timetables: List<Timetable>,
    onBack: () -> Unit,
    onImportNew: () -> Unit,
    onManualEntry: () -> Unit,
    onSendToFamily: (Timetable) -> Unit,
    onDeleteTimetable: (Timetable) -> Unit
) {
    val today = LocalDate.now()
    val activeTable = timetables.firstOrNull { table ->
        val first = runCatching { LocalDate.parse(table.firstDate) }.getOrNull()
        val last = runCatching { LocalDate.parse(table.lastDate) }.getOrNull()
        first != null && last != null && !today.isBefore(first) && !today.isAfter(last)
    } ?: timetables.firstOrNull()

    val otherTables = timetables.filter { it != activeTable }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrayerReminderColors.Ivory)
            .padding(16.dp)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cancel),
                    tint = PrayerReminderColors.Ink
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.timetables),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = PrayerReminderColors.Ink
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            // Active Timetable
            if (activeTable != null) {
                item {
                    Text(
                        text = stringResource(R.string.tt_now),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrayerReminderColors.Ink
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TimetableCard(
                        timetable = activeTable,
                        isActive = true,
                        today = today,
                        onSend = { onSendToFamily(activeTable) },
                        onDelete = { onDeleteTimetable(activeTable) }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Other Timetables
            if (otherTables.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.tt_earlier),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrayerReminderColors.Ink
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(otherTables) { table ->
                    TimetableCard(
                        timetable = table,
                        isActive = false,
                        today = today,
                        onSend = { onSendToFamily(table) },
                        onDelete = { onDeleteTimetable(table) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions
        Button(
            onClick = onImportNew,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrayerReminderColors.Ground,
                contentColor = PrayerReminderColors.Ivory
            )
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.import_new),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onManualEntry,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = PrayerReminderColors.Ink)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.import_manual),
                style = MaterialTheme.typography.bodyLarge,
                color = PrayerReminderColors.Ink
            )
        }
    }
}

@Composable
private fun TimetableCard(
    timetable: Timetable,
    isActive: Boolean,
    today: LocalDate,
    onSend: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrayerReminderColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${timetable.hijriMonthName} ${timetable.hijriYear}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrayerReminderColors.Ink
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PrayerReminderColors.OkBg
                            ) {
                                Text(
                                    text = stringResource(R.string.tt_active),
                                    color = PrayerReminderColors.OkText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${timetable.place} · ${timetable.firstDate} to ${timetable.lastDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrayerReminderColors.Muted
                    )

                    val lastDate = runCatching { LocalDate.parse(timetable.lastDate) }.getOrNull()
                    if (lastDate != null && !today.isAfter(lastDate)) {
                        val daysLeft = ChronoUnit.DAYS.between(today, lastDate).toInt()
                        Text(
                            text = stringResource(R.string.days_left, daysLeft),
                            style = MaterialTheme.typography.bodySmall,
                            color = PrayerReminderColors.GoldText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row {
                    IconButton(onClick = onSend) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.send_family),
                            tint = PrayerReminderColors.Ground
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = PrayerReminderColors.Muted
                        )
                    }
                }
            }
        }
    }
}
