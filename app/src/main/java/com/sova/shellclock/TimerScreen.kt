package com.sova.shellclock

import android.media.RingtoneManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun TimerScreen() {
    var timeLeft by remember { mutableLongStateOf(60L) } // 60 seconds by default
    val context = LocalContext.current
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, timeLeft) {
        if (isRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1

            if (timeLeft == 0L) {
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                RingtoneManager.getRingtone(context, notification)?.play()
            }
        } else if (timeLeft == 0L) {
            isRunning = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Таймер", fontSize = 24.sp, modifier = Modifier.padding(bottom = 32.dp))
        
        val minutes = timeLeft / 60
        val seconds = timeLeft % 60
        Text(String.format("%02d:%02d", minutes, seconds), fontSize = 64.sp, color = MaterialTheme.colorScheme.primary)

        Row(modifier = Modifier.padding(top = 32.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { if (timeLeft > 0) isRunning = !isRunning }) {
                Text(if (isRunning) "Пауза" else "Старт")
            }
            Button(onClick = {
                isRunning = false
                timeLeft = 60L
            }) {
                Text("Сбросить")
            }
        }
        
        FlowRow(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { timeLeft += 60 }) { Text("+1 м") }
            Button(onClick = { if (timeLeft >= 60) timeLeft -= 60 }) { Text("-1 м") }
            Button(onClick = { timeLeft += 30 }) { Text("+30 с") }
            Button(onClick = { if (timeLeft >= 30) timeLeft -= 30 }) { Text("-30 с") }
            Button(onClick = { timeLeft += 300 }) { Text("+5 м") }
            Button(onClick = { if (timeLeft >= 300) timeLeft -= 300 }) { Text("-5 м") }
        }
    }
}
