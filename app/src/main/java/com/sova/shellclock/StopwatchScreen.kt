package com.sova.shellclock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun StopwatchScreen() {
    var timeMillis by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var laps by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val startTime = System.currentTimeMillis() - timeMillis
            while (isRunning) {
                timeMillis = System.currentTimeMillis() - startTime
                delay(10L)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Секундомер", fontSize = 24.sp, modifier = Modifier.padding(bottom = 32.dp))
        
        val minutes = (timeMillis / 60000) % 60
        val seconds = (timeMillis / 1000) % 60
        val millis = (timeMillis / 10) % 100
        val timeStr = String.format("%02d:%02d,%02d", minutes, seconds, millis)

        Text(timeStr, fontSize = 64.sp, color = MaterialTheme.colorScheme.primary)

        Row(modifier = Modifier.padding(top = 32.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { isRunning = !isRunning }) {
                Text(if (isRunning) "Пауза" else "Старт")
            }
            Button(onClick = {
                if (isRunning) {
                    laps = laps + timeStr
                } else {
                    timeMillis = 0L
                    laps = emptyList()
                }
            }) {
                Text(if (isRunning) "Круг" else "Сброс")
            }
        }

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 16.dp)) {
            items(laps.reversed()) { lap ->
                Text("Круг: $lap", fontSize = 18.sp, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
