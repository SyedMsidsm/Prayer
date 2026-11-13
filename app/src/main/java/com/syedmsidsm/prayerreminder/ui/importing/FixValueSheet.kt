package com.syedmsidsm.prayerreminder.ui.importing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syedmsidsm.prayerreminder.R
import com.syedmsidsm.prayerreminder.domain.ValidationFlag
import com.syedmsidsm.prayerreminder.ui.theme.PrayerReminderColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixValueSheet(
    flag: ValidationFlag,
    onUseSuggestion: (ValidationFlag) -> Unit,
    onKeepCurrent: (ValidationFlag) -> Unit,
    onSaveCustomValue: (ValidationFlag, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customText by remember { mutableStateOf(flag.suggestion ?: flag.currentValue) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PrayerReminderColors.Surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "${flag.field.replaceFirstChar { it.uppercase() }} · ${flag.date}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PrayerReminderColors.Ink
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = flag.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = PrayerReminderColors.Muted
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = customText,
                onValueChange = { customText = it },
                label = { Text(stringResource(R.string.time_format)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrayerReminderColors.Ink
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { onKeepCurrent(flag) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.import_keep, flag.currentValue))
                }

                Spacer(modifier = Modifier.width(12.dp))

                val suggestion = flag.suggestion
                if (suggestion != null) {
                    Button(
                        onClick = { onUseSuggestion(flag) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrayerReminderColors.Ground,
                            contentColor = PrayerReminderColors.Ivory
                        )
                    ) {
                        Text(stringResource(R.string.import_use, suggestion))
                    }
                } else {
                    Button(
                        onClick = { onSaveCustomValue(flag, customText) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrayerReminderColors.Ground,
                            contentColor = PrayerReminderColors.Ivory
                        )
                    ) {
                        Text(stringResource(R.string.done))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
