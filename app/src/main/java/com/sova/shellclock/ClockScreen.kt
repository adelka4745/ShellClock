package com.sova.shellclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClockScreen(
    showSeconds: Boolean,
    use24HourFormat: Boolean,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance().time) }

    // --- Логика батареи ---
    val context = LocalContext.current
    var batteryLevel by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        // Получаем начальный уровень
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        // Слушаем изменения
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level >= 0 && scale > 0) {
                    batteryLevel = (level * 100 / scale.toFloat()).toInt()
                }
            }
        }

        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // --- Логика времени ---
    LaunchedEffect(showSeconds) {
        while (true) {
            currentTime = Calendar.getInstance().time
            delay(if (showSeconds) 1000L else 30_000L)
        }
    }

    val digitsPattern = if (use24HourFormat) {
        if (showSeconds) "HH:mm:ss" else "HH:mm"
    } else {
        if (showSeconds) "hh:mm:ss" else "hh:mm"
    }

    val digitsFormat = SimpleDateFormat(digitsPattern, Locale.getDefault())
    val digitsString = digitsFormat.format(currentTime)

    val amPmString = if (!use24HourFormat) {
        SimpleDateFormat("a", Locale.getDefault()).format(currentTime)
    } else {
        ""
    }

    // Цвет батареи: красный, если меньше 20%
    val batteryColor = if (batteryLevel <= 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = digitsString,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )

            if (amPmString.isNotEmpty()) {
                Text(
                    text = amPmString,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Отображение заряда батареи
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "🔋 $batteryLevel%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = batteryColor
            )
        }
    }
}