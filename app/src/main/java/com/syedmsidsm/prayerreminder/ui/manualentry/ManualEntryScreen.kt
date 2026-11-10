package com.syedmsidsm.prayerreminder.ui.manualentry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.HijriNames
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import com.syedmsidsm.prayerreminder.domain.model.TimetableDay
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors
import java.time.LocalDate

@Composable
fun ManualEntryScreen(
    onBack: () -> Unit,
    onStartEditing: (Timetable) -> Unit
) {
    var place by remember { mutableStateOf("Bhatkal") }
    var hijriMonth by remember { mutableIntStateOf(10) } // Shawwal
    var hijriYear by remember { mutableIntStateOf(1447) }
    var startDateText by remember { mutableStateOf("2026-03-20") }
    var dayCount by remember { mutableIntStateOf(30) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrayerReminderColors.Ivory)
            .padding(16.dp)
    ) {
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
                text = stringResource(R.string.import_manual),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = PrayerReminderColors.Ink
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = place,
            onValueChange = { place = it },
            label = { Text("Place Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = hijriMonth.toString(),
                onValueChange = { it.toIntOrNull()?.let { m -> if (m in 1..12) hijriMonth = m } },
                label = { Text("Hijri Month (1-12)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedTextField(
                value = hijriYear.toString(),
                onValueChange = { it.toIntOrNull()?.let { y -> hijriYear = y } },
                label = { Text("Hijri Year") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = startDateText,
            onValueChange = { startDateText = it },
            label = { Text("Start Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { dayCount = 29 },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (dayCount == 29) PrayerReminderColors.Ground else PrayerReminderColors.Surface,
                    contentColor = if (dayCount == 29) PrayerReminderColors.Ivory else PrayerReminderColors.Ink
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("29 days")
            }
            Button(
                onClick = { dayCount = 30 },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (dayCount == 30) PrayerReminderColors.Ground else PrayerReminderColors.Surface,
                    contentColor = if (dayCount == 30) PrayerReminderColors.Ivory else PrayerReminderColors.Ink
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("30 days")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val startDate = runCatching { LocalDate.parse(startDateText) }.getOrElse { LocalDate.now() }
                val days = mutableListOf<TimetableDay>()
                var d = startDate
                for (i in 1..dayCount) {
                    days.add(
                        TimetableDay(
                            date = d.toString(),
                            weekday = d.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                            hijriDay = i,
                            times = emptyMap()
                        )
                    )
                    d = d.plusDays(1)
                }
                val monthName = HijriNames.getMonthName(hijriMonth, "en")
                val table = Timetable(
                    place = place.ifBlank { "Bhatkal" },
                    hijriMonth = hijriMonth,
                    hijriMonthName = monthName,
                    hijriYear = hijriYear,
                    firstDate = days.first().date,
                    lastDate = days.last().date,
                    source = "manual",
                    verified = false,
                    days = days
                )
                onStartEditing(table)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrayerReminderColors.Ground,
                contentColor = PrayerReminderColors.Ivory
            )
        ) {
            Text(
                text = stringResource(R.string.continue_btn),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
