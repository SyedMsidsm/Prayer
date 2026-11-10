package com.syedmsidsm.prayerreminder.ui.importing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.model.Timetable
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors

@Composable
fun ImportSavedScreen(
    timetable: Timetable,
    onSendToFamily: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrayerReminderColors.Ivory)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(PrayerReminderColors.OkBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = PrayerReminderColors.OkText,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.saved_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = PrayerReminderColors.Ink
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${timetable.place} · ${timetable.hijriMonthName} ${timetable.hijriYear}",
            style = MaterialTheme.typography.titleMedium,
            color = PrayerReminderColors.Muted
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onSendToFamily,
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
                text = stringResource(R.string.send_family),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.send_family_desc),
            style = MaterialTheme.typography.bodySmall,
            color = PrayerReminderColors.Muted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = stringResource(R.string.done),
                style = MaterialTheme.typography.titleMedium,
                color = PrayerReminderColors.Ink
            )
        }
    }
}
