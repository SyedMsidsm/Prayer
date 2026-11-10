package com.syedmsidsm.prayerreminder.ui.importing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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

@Composable
fun FamilyImportDialog(
    timetable: Timetable,
    onConfirmImport: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PrayerReminderColors.Surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(R.string.family_import_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PrayerReminderColors.Ink
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrayerReminderColors.OkBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PrayerReminderColors.OkText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.family_checked),
                            color = PrayerReminderColors.OkText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "${timetable.place} · ${timetable.hijriMonthName} ${timetable.hijriYear}",
                    style = MaterialTheme.typography.titleLarge,
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
        },
        confirmButton = {
            Button(
                onClick = onConfirmImport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrayerReminderColors.Ground,
                    contentColor = PrayerReminderColors.Ivory
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.import_btn),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = PrayerReminderColors.Ink
                )
            }
        }
    )
}
