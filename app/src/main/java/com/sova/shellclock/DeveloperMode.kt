package com.sova.shellclock

import android.media.RingtoneManager
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeveloperModeScreen(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = settings.sizeMultiplier
    val context = LocalContext.current

    var showBorders by remember { mutableStateOf(false) }
    var logClicks by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (showBorders) Modifier.border(2.dp, MaterialTheme.colorScheme.error) else Modifier
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 40.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                }
                Text(
                    text = "Для разработчика",
                    fontSize = (28 * scale).sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Внимание! Эти настройки предназначены только для отладки. Будьте осторожны.",
                                fontSize = (14 * scale).sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                item { SectionHeader("Диагностика", scale) }
                item { InfoRow("Версия ОС", android.os.Build.VERSION.RELEASE, scale) }
                item { InfoRow("Модель устройства", android.os.Build.MODEL, scale) }
                item { InfoRow("Плотность экрана", "${context.resources.displayMetrics.density} dpi", scale) }
                item { InfoRow("Версия приложения", BuildConfig.VERSION_NAME, scale) }

                item { SectionHeader("Тесты и отладка", scale) }
                item {
                    SettingsSwitchRow(
                        title = "Показывать границы (Border)",
                        subtitle = "Включает отрисовку красной рамки вокруг экрана",
                        checked = showBorders,
                        scale = scale,
                        onCheckedChange = { showBorders = it }
                    )
                }
                item {
                    SettingsSwitchRow(
                        title = "Логировать нажатия",
                        subtitle = "Показывает Toast при нажатии на кнопки",
                        checked = logClicks,
                        scale = scale,
                        onCheckedChange = { logClicks = it }
                    )
                }
                item {
                    Button(
                        onClick = {
                            if (logClicks) Toast.makeText(context, "Тест звука запущен", Toast.LENGTH_SHORT).show()
                            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                            val ringtone = RingtoneManager.getRingtone(context, notification)
                            ringtone.play()
                            Toast.makeText(context, "Играет системный будильник", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Тест звука будильника", fontSize = (15 * scale).sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }

                item { SectionHeader("Опасная зона", scale) }
                item {
                    Button(
                        onClick = {
                            onSettingsChange(AppSettings.DEFAULT)
                            Toast.makeText(context, "Все настройки сброшены!", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Сбросить ВСЁ и выйти", color = MaterialTheme.colorScheme.onError)
                    }
                }
                item {
                    Button(
                        onClick = {
                            onSettingsChange(settings.copy(isDeveloperMode = false))
                            Toast.makeText(context, "Режим разработчика выключен", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Выключить режим разработчика", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, scale: Float) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = (15 * scale).sp)
        Text(value, fontSize = (15 * scale).sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionHeader(title: String, scale: Float) {
    Text(
        title,
        fontSize = (16 * scale).sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String, subtitle: String, checked: Boolean, scale: Float, onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = (15 * scale).sp)
            Text(subtitle, fontSize = (12 * scale).sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}