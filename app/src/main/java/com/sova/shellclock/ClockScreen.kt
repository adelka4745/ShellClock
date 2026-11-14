package com.sova.shellclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClockScreen(
    settings: AppSettings,
    onOpenSettings: () -> Unit,
    onOpenDescription: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenUtils: () -> Unit, // <-- НОВЫЙ ПАРАМЕТР
    onOpenGitHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance().time) }
    val context = LocalContext.current
    var batteryLevel by remember { mutableIntStateOf(100) }
    var isCharging by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Погода и Пинг
    var weatherTemp by remember { mutableStateOf<String?>(null) }
    var isOnline by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            isOnline = withContext(Dispatchers.IO) {
                try {
                    val url = URL("https://8.8.8.8")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 2000
                    connection.readTimeout = 2000
                    connection.requestMethod = "HEAD"
                    connection.responseCode == 200
                } catch (e: Exception) { false }
            }

            if (isOnline && weatherTemp == null) {
                weatherTemp = withContext(Dispatchers.IO) {
                    try {
                        val url = URL("https://api.open-meteo.com/v1/forecast?latitude=55.75&longitude=37.62&current_weather=true")
                        val conn = url.openConnection() as HttpURLConnection
                        conn.connectTimeout = 5000
                        conn.readTimeout = 5000
                        if (conn.responseCode == 200) {
                            val response = conn.inputStream.bufferedReader().readText()
                            val json = JSONObject(response)
                            val current = json.getJSONObject("current_weather")
                            "${current.getDouble("temperature")}°C"
                        } else null
                    } catch (e: Exception) { null }
                }
            }
            delay(300_000L)
        }
    }

    DisposableEffect(Unit) {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level >= 0 && scale > 0) batteryLevel = (level * 100 / scale.toFloat()).toInt()
                val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }

    LaunchedEffect(settings.showSeconds) {
        while (true) {
            currentTime = Calendar.getInstance().time
            delay(if (settings.showSeconds) 1000L else 30_000L)
        }
    }

    val digitsPattern = if (settings.timeFormat == 24) {
        if (settings.showSeconds) "HH:mm:ss" else "HH:mm"
    } else {
        if (settings.showSeconds) "hh:mm:ss" else "hh:mm"
    }
    val digitsString = SimpleDateFormat(digitsPattern, Locale.getDefault()).format(currentTime)
    val amPmString = if (settings.timeFormat == 12) SimpleDateFormat("a", Locale.getDefault()).format(currentTime) else ""
    val dateString = SimpleDateFormat("d MMM yyyy", Locale("ru")).format(currentTime)

    val batteryColor = if (batteryLevel <= 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
    val animatedProgress by animateFloatAsState(targetValue = batteryLevel / 100f, label = "battery")

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!isOnline) Text("❌ Offline", color = MaterialTheme.colorScheme.error, fontSize = (14 * settings.sizeMultiplier).sp)
                if (weatherTemp != null) Text("☀️ $weatherTemp", fontSize = (16 * settings.sizeMultiplier).sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = digitsString, fontSize = (64 * settings.sizeMultiplier).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
            if (amPmString.isNotEmpty()) Text(text = amPmString, fontSize = (24 * settings.sizeMultiplier).sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.secondary)
            if (settings.showDateUnderClock) Text(text = dateString, fontSize = (18 * settings.sizeMultiplier).sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.width(150.dp).height(8.dp),
                    color = batteryColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isCharging) "⚡ $batteryLevel%" else "🔋 $batteryLevel%",
                        fontSize = (16 * settings.sizeMultiplier).sp,
                        fontWeight = FontWeight.Medium,
                        color = batteryColor
                    )
                }
            }
        }

        // Меню с тремя точками (ДОБАВЛЕНЫ ПУНКТЫ)
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Дополнительно", tint = MaterialTheme.colorScheme.onBackground)
            }

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text(tr("Настройки")) },
                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    onClick = { showMenu = false; onOpenSettings() }
                )
                DropdownMenuItem(
                    text = { Text(tr("Описание")) },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                    onClick = { showMenu = false; onOpenDescription() }
                )
                // НОВЫЙ ПУНКТ: Утилиты
                DropdownMenuItem(
                    text = { Text(tr("Утилиты")) },
                    leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                    onClick = { showMenu = false; onOpenUtils() }
                )
                // НОВЫЙ ПУНКТ: Помощь
                DropdownMenuItem(
                    text = { Text(tr("Помощь")) },
                    leadingIcon = { Icon(Icons.Default.HelpOutline, contentDescription = null) },
                    onClick = { showMenu = false; onOpenHelp() }
                )
                DropdownMenuItem(
                    text = { Text("GitHub") },
                    // OctoCat вместо нейтральной «стрелочки в окошке»
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_github),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = { showMenu = false; onOpenGitHub() }
                )
            }
        }
    }
}