package com.sova.shellclock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance().time
            delay(1000L)
        }
    }

    // Теперь мы отдельно берём цифры времени и отдельно маркер AM/PM
    val digitsPattern = if (use24HourFormat) {
        if (showSeconds) "HH:mm:ss" else "HH:mm"
    } else {
        if (showSeconds) "hh:mm:ss" else "hh:mm"
    }

    val digitsFormat = SimpleDateFormat(digitsPattern, Locale.getDefault())
    val digitsString = digitsFormat.format(currentTime)

    // Получаем "AM" или "PM", если выбран 12-часовой формат
    val amPmString = if (!use24HourFormat) {
        SimpleDateFormat("a", Locale.getDefault()).format(currentTime)
    } else {
        ""
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Крупные цифры времени (никогда не сплющатся)
            Text(
                text = digitsString,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )

            // Если включен американский формат, аккуратно выводим AM/PM снизу
            if (amPmString.isNotEmpty()) {
                Text(
                    text = amPmString,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
