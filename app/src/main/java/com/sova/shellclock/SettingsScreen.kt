package com.sova.shellclock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    sizeMultiplier: Float,
    onSizeChanged: (Float) -> Unit,
    showSeconds: Boolean,                  // Теперь это обязательные параметры
    onShowSecondsChanged: (Boolean) -> Unit, // Без заглушек
    use24HourFormat: Boolean,
    on24HourFormatChanged: (Boolean) -> Unit
) {
    // Состояние для показа диалога сброса
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Настройки",
            fontSize = (32 * sizeMultiplier).sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Раздел: Внешний вид
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Внешний вид",
                fontSize = (20 * sizeMultiplier).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDarkTheme) "Тёмная тема" else "Светлая тема",
                    fontSize = (16 * sizeMultiplier).sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onThemeChanged(it) }
                )
            }

            Slider(
                value = sizeMultiplier,
                onValueChange = { onSizeChanged(it) },
                valueRange = 0.8f..1.5f,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Масштаб интерфейса: ${(sizeMultiplier * 100).toInt()}%",
                fontSize = (12 * sizeMultiplier).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider()

        // Раздел: Отображение времени
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Отображение времени",
                fontSize = (20 * sizeMultiplier).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Переключатель секунд
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Показывать секунды",
                        fontSize = (16 * sizeMultiplier).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Плавное обновление каждую секунду",
                        fontSize = (12 * sizeMultiplier).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = showSeconds,
                    onCheckedChange = { onShowSecondsChanged(it) }
                )
            }

            // Переключатель 24-часового формата
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "24-часовой формат",
                        fontSize = (16 * sizeMultiplier).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (use24HourFormat) "Пример: 13:00" else "Пример: 01:00 PM",
                        fontSize = (12 * sizeMultiplier).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = use24HourFormat,
                    onCheckedChange = { on24HourFormatChanged(it) }
                )
            }
        }

        HorizontalDivider()

        // Раздел: Система
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { showResetDialog = true }, // Открываем диалог при нажатии
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Сбросить настройки",
                    fontSize = (16 * sizeMultiplier).sp,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Всплывающий диалог подтверждения сброса
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(text = "Сброс настроек", fontSize = (20 * sizeMultiplier).sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = "Вы точно хотите вернуть все настройки ShellClock по умолчанию?", fontSize = (16 * sizeMultiplier).sp)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onThemeChanged(false)
                        onSizeChanged(1.0f)
                        onShowSecondsChanged(true)
                        on24HourFormatChanged(true)
                        showResetDialog = false // Закрываем диалог
                    }
                ) {
                    Text("Да", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
