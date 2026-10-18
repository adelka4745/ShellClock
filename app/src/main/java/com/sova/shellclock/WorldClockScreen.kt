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
fun WorldClockScreen(
    showSeconds: Boolean,       // Принимаем настройку секунд
    use24HourFormat: Boolean    // Принимаем настройку формата
) {
    val cities = listOf(
        CityTime("Гонолулу", "Pacific/Honolulu"),
        CityTime("Лос-Анджелес", "America/Los_Angeles"),
        CityTime("Нью-Йорк", "America/New_York"),
        CityTime("Рио-де-Жанейро", "America/Sao_Paulo"),
        CityTime("Лондон", "Europe/London"),
        CityTime("Берлин", "Europe/Berlin"),
        CityTime("Париж", "Europe/Paris"),
        CityTime("Каир", "Africa/Cairo"),
        CityTime("Стамбул", "Europe/Istanbul"),
        CityTime("Москва", "Europe/Moscow"),
        CityTime("Дубай", "Asia/Dubai"),
        CityTime("Екатеринбург", "Asia/Yekaterinburg"),
        CityTime("Нью-Дели", "Asia/Kolkata"),
        CityTime("Бангкок", "Asia/Bangkok"),
        CityTime("Шанхай", "Asia/Shanghai"),
        CityTime("Сингапур", "Asia/Singapore"),
        CityTime("Токио", "Asia/Tokyo"),
        CityTime("Сеул", "Asia/Seoul"),
        CityTime("Владивосток", "Asia/Vladivostok"),
        CityTime("Сидней", "Australia/Sydney"),
        CityTime("Окленд", "Pacific/Auckland")
    )

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000L)
        }
    }

    // Динамически выбираем шаблон времени в зависимости от переключателей в настройках
    val timePattern = when {
        use24HourFormat && showSeconds -> "HH:mm:ss"
        use24HourFormat && !showSeconds -> "HH:mm"
        !use24HourFormat && showSeconds -> "hh:mm:ss a"
        else -> "hh:mm a"
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Мировое время", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        }
        items(cities) { city ->
            val sdf = SimpleDateFormat(timePattern, Locale.getDefault())
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
                Text(text = "Скоро будет ещё больше!", fontSize = 20.sp)
                Text(text = "Планируется добавить больше городов", fontSize = 16.sp)
            }
        }
    }
}
