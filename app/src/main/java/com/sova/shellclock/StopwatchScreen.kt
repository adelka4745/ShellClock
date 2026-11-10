package com.sova.shellclock

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun StopwatchScreen() {
    var timeMillis by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var laps by remember { mutableStateOf(listOf<String>()) }

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("stopwatch_prefs", Context.MODE_PRIVATE)
    var records by remember { mutableStateOf(prefs.getStringSet("records", emptySet())?.toList() ?: emptyList()) }

    // Экспорт рекордов в .scsw
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        uri?.let {
            try {
                val content = buildString {
                    append("[SCSW]\n") // Маркер формата
                    records.forEach { record -> append("RECORD: $record\n") }
                }
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(content.toByteArray())
                }
            } catch (e: Exception) { }
        }
    }

    // Импорт рекордов из .scsw
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                    reader.readText()
                } ?: ""
                if (content.startsWith("[SCSW]")) {
                    val imported = content.lines()
                        .filter { it.startsWith("RECORD: ") }
                        .map { it.removePrefix("RECORD: ").trim() }
                    if (imported.isNotEmpty()) {
                        records = (records + imported).distinct().sorted()
                        prefs.edit().putStringSet("records", records.toSet()).apply()
                    }
                }
            } catch (e: Exception) { }
        }
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val startTime = System.currentTimeMillis() - timeMillis
            while (isRunning) {
                timeMillis = System.currentTimeMillis() - startTime
                delay(10L)
            }
        }
    }

    fun saveRecord() {
        val newRecord = formatTime(timeMillis)
        val newRecords = (records + newRecord).sorted()
        records = newRecords
        prefs.edit().putStringSet("records", newRecords.toSet()).apply()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Секундомер", fontSize = 24.sp, modifier = Modifier.padding(bottom = 32.dp))

        val timeStr = formatTime(timeMillis)
        Text(timeStr, fontSize = 64.sp, color = MaterialTheme.colorScheme.primary)

        Row(modifier = Modifier.padding(top = 32.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { isRunning = !isRunning }) {
                Text(if (isRunning) "Пауза" else "Старт")
            }
            Button(onClick = {
                if (isRunning) laps = laps + timeStr
                else { timeMillis = 0L; laps = emptyList() }
            }) {
                Text(if (isRunning) "Круг" else "Сброс")
            }
            if (!isRunning && timeMillis > 0) {
                Button(onClick = { saveRecord() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Рекорд")
                }
            }
        }

        // Кнопки экспорта/импорта для рекордов .scsw
        if (records.isNotEmpty()) {
            Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { exportLauncher.launch("records.scsw") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Экспорт .scsw")
                }
                Button(onClick = { importLauncher.launch(arrayOf("*/*")) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Импорт .scsw")
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 16.dp)) {
            item { Text("Круги:", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            items(laps.reversed()) { lap ->
                Text("Круг: $lap", fontSize = 16.sp, modifier = Modifier.padding(vertical = 2.dp))
            }

            if (records.isNotEmpty()) {
                item { Spacer(Modifier.height(16.dp)); Text("Рекорды:", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary) }
                items(records) { record ->
                    Text("🏆 $record", fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val minutes = (millis / 60000) % 60
    val seconds = (millis / 1000) % 60
    val ms = (millis / 10) % 100
    return String.format("%02d:%02d,%02d", minutes, seconds, ms)
}