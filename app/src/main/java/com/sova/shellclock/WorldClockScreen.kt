package com.sova.shellclock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class CityTime(val name: String, val timezone: String)

@Composable
fun WorldClockScreen() {
    val cities = listOf(
        CityTime("Лондон", "GMT"),
        CityTime("Нью-Йорк", "America/New_York"),
        CityTime("Токио", "Asia/Tokyo"),
        CityTime("Москва", "Europe/Moscow"),
        CityTime("Дубай", "Asia/Dubai"),
        CityTime(name = "Шанхай", timezone = "Asia/Shanghai"),
        CityTime(name = "Париж", timezone = "Europe/Paris"),
        CityTime(name = "Лос-Анджелес", timezone = "America/Los_Angeles"),
        CityTime(name = "Берлин", timezone = "Europe/Berlin"),
        CityTime(name = "Сингапур", timezone = "Asia/Singapore")
    )

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000L)
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Мировое время", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        }
        items(cities) { city ->
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone(city.timezone)
            val time = sdf.format(Date(currentTime))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(city.name, fontSize = 20.sp)
                Text(time, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
            }
            HorizontalDivider()
        }
        item {
            Column {
                Text(text = "Скоро будет ещё больше!", fontSize = 16.sp)
                Text(text = "Планируется добавить больше городов", fontSize = 16.sp)
            }
        }
    }
}
