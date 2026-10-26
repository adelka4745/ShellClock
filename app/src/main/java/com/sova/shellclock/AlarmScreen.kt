package com.sova.shellclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun AlarmScreen(scaleMultiplier: Float) {
    val context = LocalContext.current
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    var alarms by remember { mutableStateOf(AlarmStorage.loadAlarms(context)) }
    var showPicker by remember { mutableStateOf(false) }

    // --- Лаунчер для ЭКСПОРТА (Сохранение файла) ---
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        uri?.let {
            try {
                val text = AlarmStorage.serialize(alarms)
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(text.toByteArray())
                }
            } catch (e: Exception) {
                // Ошибка записи
            }
        }
    }

    // --- Лаунчер для ИМПОРТА (Открытие файла) ---
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val text = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                    reader.readText()
                } ?: ""
                val importedAlarms = AlarmStorage.deserialize(text)

                // Объединяем с текущими, чтобы не потерять старые
                alarms = alarms + importedAlarms
                AlarmStorage.saveAlarms(context, alarms)

                // Перезапускаем будильники
                importedAlarms.forEach { setAlarm(context, alarmManager, it) }
            } catch (e: Exception) {
                // Ошибка чтения
            }
        }
    }

    if (showPicker) {
        AlarmPickerDialog(
            scaleMultiplier = scaleMultiplier,
            onDismiss = { showPicker = false },
            onTimeSelected = { hour, minute ->
                val newAlarm = AlarmItem(
                    id = (0..10000).random(),
                    hour = hour,
                    minute = minute,
                    isEnabled = true
                )
                alarms = alarms + newAlarm
                AlarmStorage.saveAlarms(context, alarms)
                setAlarm(context, alarmManager, newAlarm)
                showPicker = false
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Будильник",
            fontSize = (28 * scaleMultiplier).sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
        )

        if (alarms.isEmpty()) {
            Text(
                text = "Нет будильников",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = (16 * scaleMultiplier).sp,
                modifier = Modifier.padding(top = 32.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(alarms) { alarm ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format("%02d:%02d", alarm.hour, alarm.minute),
                                fontSize = (32 * scaleMultiplier).sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )

                            Switch(
                                checked = alarm.isEnabled,
                                onCheckedChange = { isChecked ->
                                    alarms = alarms.map {
                                        if (it.id == alarm.id) it.copy(isEnabled = isChecked) else it
                                    }
                                    AlarmStorage.saveAlarms(context, alarms)
                                    if (isChecked) setAlarm(context, alarmManager, alarm)
                                    else cancelAlarm(context, alarmManager, alarm)
                                }
                            )

                            IconButton(onClick = {
                                cancelAlarm(context, alarmManager, alarm)
                                alarms = alarms.filter { it.id != alarm.id }
                                AlarmStorage.saveAlarms(context, alarms)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить")
                            }
                        }
                    }
                }
            }
        }

        // Кнопка добавления
        Button(
            onClick = { showPicker = true },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Добавить будильник", fontSize = (16 * scaleMultiplier).sp)
        }

        // Кнопки импорта и экспорта
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { exportLauncher.launch("my_alarms.sca") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Экспорт", fontSize = (14 * scaleMultiplier).sp)
            }
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("text/plain", "*/*")) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Импорт", fontSize = (14 * scaleMultiplier).sp)
            }
        }
    }
}

// ... (AlarmPickerDialog и WheelPicker остаются без изменений, они ниже в файле) ...

// --- Экран с колёсиками ---
@Composable
fun AlarmPickerDialog(
    scaleMultiplier: Float,
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите время", fontSize = (20 * scaleMultiplier).sp) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.height(150.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WheelPicker(0..23, selectedHour, { selectedHour = it }, "ЧЧ")
                    Text(":", fontSize = (40 * scaleMultiplier).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    WheelPicker(0..59, selectedMinute, { selectedMinute = it }, "ММ")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onTimeSelected(selectedHour, selectedMinute) }) {
                Text("Готово", fontSize = (16 * scaleMultiplier).sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", fontSize = (16 * scaleMultiplier).sp)
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    range: IntRange,
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String
) {
    val items = range.toList()
    val itemHeight = 50.dp
    val height = itemHeight * 3

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = value)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(value) {
        if (listState.firstVisibleItemIndex != value) {
            listState.animateScrollToItem(value)
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            onValueChange(items[listState.firstVisibleItemIndex])
        }
    }

    Box(
        modifier = Modifier.width(80.dp).height(height),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight)
        ) {
            items(items.size) { index ->
                val isSelected = index == listState.firstVisibleItemIndex
                Box(
                    modifier = Modifier.fillMaxWidth().height(itemHeight).clickable {
                        coroutineScope.launch { listState.animateScrollToItem(index) }
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d", items[index]),
                        fontSize = if (isSelected) 40.sp else 28.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth().height(itemHeight).align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, Color.Transparent)))
        )
        Box(
            modifier = Modifier.fillMaxWidth().height(itemHeight).align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.surface)))
        )
    }
}

// --- Вспомогательные функции для системного будильника ---
private fun setAlarm(context: Context, alarmManager: AlarmManager, alarm: AlarmItem) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, alarm.hour)
        set(Calendar.MINUTE, alarm.minute)
        set(Calendar.SECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("ALARM_ID", alarm.id)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context, alarm.id, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val showIntent = PendingIntent.getActivity(
        context, alarm.id, Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(calendar.timeInMillis, showIntent),
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
            )
        }
    } catch (e: SecurityException) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}

private fun cancelAlarm(context: Context, alarmManager: AlarmManager, alarm: AlarmItem) {
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context, alarm.id, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
}