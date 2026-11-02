package com.sova.shellclock

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimerScreen() {
    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(1) }
    var seconds by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Запрос разрешения на уведомления для Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val totalTime = (hours * 3600 + minutes * 60 + seconds).toLong()

    LaunchedEffect(isRunning, totalTime) {
        if (isRunning && totalTime > 0) {
            delay(1000L)
            if (seconds > 0) seconds--
            else if (minutes > 0) { minutes--; seconds = 59 }
            else if (hours > 0) { hours--; minutes = 59; seconds = 59 }

            if (hours == 0 && minutes == 0 && seconds == 0) {
                isRunning = false
            }
        }
    }

    fun startTimerService() {
        val intent = Intent(context, TimerForeground::class.java).apply {
            putExtra("TIME_LEFT", totalTime.toInt())
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopTimerService() {
        context.stopService(Intent(context, TimerForeground::class.java))
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Таймер", fontSize = 24.sp, modifier = Modifier.padding(bottom = 32.dp))

        Row(
            modifier = Modifier.height(150.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WheelPicker(0..23, hours, { hours = it }, "ЧЧ", !isRunning)
            Text(":", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            WheelPicker(0..59, minutes, { minutes = it }, "ММ", !isRunning)
            Text(":", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            WheelPicker(0..59, seconds, { seconds = it }, "СС", !isRunning)
        }

        Row(modifier = Modifier.padding(top = 32.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = {
                if (!isRunning) {
                    startTimerService()
                    isRunning = true
                } else {
                    isRunning = false
                    stopTimerService()
                }
            }) {
                Text(if (isRunning) "Пауза" else "Старт")
            }
            Button(onClick = {
                isRunning = false
                stopTimerService()
                hours = 0; minutes = 1; seconds = 0
            }) {
                Text("Сбросить")
            }
        }

        FlowRow(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { if (minutes < 59) minutes += 1 }, enabled = !isRunning) { Text("+1 м") }
            Button(onClick = { if (minutes > 0) minutes -= 1 }, enabled = !isRunning) { Text("-1 м") }
            Button(onClick = { if (seconds <= 30) seconds += 30 else { minutes++; seconds -= 30 } }, enabled = !isRunning) { Text("+30 с") }
            Button(onClick = { if (seconds >= 30) seconds -= 30 else if (minutes > 0) { minutes--; seconds += 30 } }, enabled = !isRunning) { Text("-30 с") }
            Button(onClick = { if (minutes <= 54) minutes += 5 else { hours++; minutes -= 55 } }, enabled = !isRunning) { Text("+5 м") }
            Button(onClick = { if (minutes >= 5) minutes -= 5 else if (hours > 0) { hours--; minutes += 55 } }, enabled = !isRunning) { Text("-5 м") }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(range: IntRange, value: Int, onValueChange: (Int) -> Unit, label: String, isEnabled: Boolean) {
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
            contentPadding = PaddingValues(vertical = itemHeight),
            userScrollEnabled = isEnabled
        ) {
            items(items.size) { index ->
                val isSelected = index == listState.firstVisibleItemIndex
                Box(
                    modifier = Modifier.fillMaxWidth().height(itemHeight).clickable(enabled = isEnabled) {
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
        Box(modifier = Modifier.fillMaxWidth().height(itemHeight).align(Alignment.TopCenter).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, Color.Transparent))))
        Box(modifier = Modifier.fillMaxWidth().height(itemHeight).align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background))))
    }
}