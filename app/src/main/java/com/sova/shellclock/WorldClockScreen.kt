package com.sova.shellclock

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class CityTime(val name: String, val timezone: String, val region: String)

@Composable
fun WorldClockScreen(
    showSeconds: Boolean,
    use24HourFormat: Boolean,
    sortType: Int,
    hideEmptyRegions: Boolean = true,
    showTimezoneName: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("shellclock_prefs", Context.MODE_PRIVATE) }

    val cities = remember {
        listOf(
            // Европа
            CityTime("Лондон", "Europe/London", "Европа"),
            CityTime("Париж", "Europe/Paris", "Европа"),
            CityTime("Берлин", "Europe/Berlin", "Европа"),
            CityTime("Рим", "Europe/Rome", "Европа"),
            CityTime("Мадрид", "Europe/Madrid", "Европа"),
            CityTime("Амстердам", "Europe/Amsterdam", "Европа"),
            CityTime("Афины", "Europe/Athens", "Европа"),
            CityTime("Москва", "Europe/Moscow", "Европа"),
            CityTime("Стамбул", "Europe/Istanbul", "Европа"),
            CityTime("Киев", "Europe/Kyiv", "Европа"),
            CityTime("Минск", "Europe/Minsk", "Европа"),
            CityTime("Вена", "Europe/Vienna", "Европа"),
            CityTime("Брюссель", "Europe/Brussels", "Европа"),
            CityTime("Будапешт", "Europe/Budapest", "Европа"),
            CityTime("Варшава", "Europe/Warsaw", "Европа"),
            CityTime("Копенгаген", "Europe/Copenhagen", "Европа"),
            CityTime("Лиссабон", "Europe/Lisbon", "Европа"),
            CityTime("Осло", "Europe/Oslo", "Европа"),
            CityTime("Прага", "Europe/Prague", "Европа"),
            CityTime("Стокгольм", "Europe/Stockholm", "Европа"),
            CityTime("Хельсинки", "Europe/Helsinki", "Европа"),
            CityTime("Дублин", "Europe/Dublin", "Европа"),

            // Азия
            CityTime("Дубай", "Asia/Dubai", "Азия"),
            CityTime("Екатеринбург", "Asia/Yekaterinburg", "Азия"),
            CityTime("Ташкент", "Asia/Tashkent", "Азия"),
            CityTime("Алматы", "Asia/Almaty", "Азия"),
            CityTime("Нью-Дели", "Asia/Kolkata", "Азия"),
            CityTime("Бангкок", "Asia/Bangkok", "Азия"),
            CityTime("Сингапур", "Asia/Singapore", "Азия"),
            CityTime("Шанхай", "Asia/Shanghai", "Азия"),
            CityTime("Гонконг", "Asia/Hong_Kong", "Азия"),
            CityTime("Токио", "Asia/Tokyo", "Азия"),
            CityTime("Сеул", "Asia/Seoul", "Азия"),
            CityTime("Владивосток", "Asia/Vladivostok", "Азия"),
            CityTime("Новосибирск", "Asia/Novosibirsk", "Азия"),
            CityTime("Иерусалим", "Asia/Jerusalem", "Азия"),
            CityTime("Абу-Даби", "Asia/Dubai", "Азия"),
            CityTime("Астана", "Asia/Qostanay", "Азия"),
            CityTime("Баку", "Asia/Baku", "Азия"),
            CityTime("Багдад", "Asia/Baghdad", "Азия"),
            CityTime("Бейрут", "Asia/Beirut", "Азия"),
            CityTime("Джакарта", "Asia/Jakarta", "Азия"),
            CityTime("Доха", "Asia/Qatar", "Азия"),
            CityTime("Ереван", "Asia/Yerevan", "Азия"),
            CityTime("Кабул", "Asia/Kabul", "Азия"),
            CityTime("Карачи", "Asia/Karachi", "Азия"),
            CityTime("Катманду", "Asia/Kathmandu", "Азия"),
            CityTime("Куала-Лумпур", "Asia/Kuala_Lumpur", "Азия"),
            CityTime("Манила", "Asia/Manila", "Азия"),
            CityTime("Маскат", "Asia/Muscat", "Азия"),
            CityTime("Омск", "Asia/Omsk", "Азия"),
            CityTime("Красноярск", "Asia/Krasnoyarsk", "Азия"),
            CityTime("Иркутск", "Asia/Irkutsk", "Азия"),
            CityTime("Эр-Рияд", "Asia/Riyadh", "Азия"),

            // Америка
            CityTime("Гонолулу", "Pacific/Honolulu", "Америка"),
            CityTime("Лос-Анджелес", "America/Los_Angeles", "Америка"),
            CityTime("Денвер", "America/Denver", "Америка"),
            CityTime("Чикаго", "America/Chicago", "Америка"),
            CityTime("Нью-Йорк", "America/New_York", "Америка"),
            CityTime("Торонто", "America/Toronto", "Америка"),
            CityTime("Мехико", "America/Mexico_City", "Америка"),
            CityTime("Сан-Паулу", "America/Sao_Paulo", "Америка"),
            CityTime("Рио-де-Жанейро", "America/Sao_Paulo", "Америка"),
            CityTime("Буэнос-Айрес", "America/Argentina/Buenos_Aires", "Америка"),
            CityTime("Сантьяго", "America/Santiago", "Америка"),
            CityTime("Анкоридж", "America/Anchorage", "Америка"),
            CityTime("Богота", "America/Bogota", "Америка"),
            CityTime("Бостон", "America/New_York", "Америка"),
            CityTime("Вашингтон", "America/New_York", "Америка"),
            CityTime("Ванкувер", "America/Vancouver", "Америка"),
            CityTime("Гавана", "America/Havana", "Америка"),
            CityTime("Каракас", "America/Caracas", "Америка"),
            CityTime("Лима", "America/Lima", "Америка"),
            CityTime("Майами", "America/New_York", "Америка"),
            CityTime("Монреаль", "America/Toronto", "Америка"),
            CityTime("Панама", "America/Panama", "Америка"),
            CityTime("Сан-Франциско", "America/Los_Angeles", "Америка"),
            CityTime("Сиэтл", "America/Los_Angeles", "Америка"),
            CityTime("Филадельфия", "America/New_York", "Америка"),
            CityTime("Хьюстон", "America/Chicago", "Америка"),

            // Африка
            CityTime("Каир", "Africa/Cairo", "Африка"),
            CityTime("Йоханнесбург", "Africa/Johannesburg", "Африка"),
            CityTime("Найроби", "Africa/Nairobi", "Африка"),
            CityTime("Лагос", "Africa/Lagos", "Африка"),
            CityTime("Касабланка", "Africa/Casablanca", "Африка"),
            CityTime("Аддис-Абеба", "Africa/Addis_Ababa", "Африка"),
            CityTime("Алжир", "Africa/Algiers", "Африка"),
            CityTime("Аккра", "Africa/Accra", "Африка"),
            CityTime("Дакар", "Africa/Dakar", "Африка"),
            CityTime("Киншаса", "Africa/Kinshasa", "Африка"),
            CityTime("Луанда", "Africa/Luanda", "Африка"),
            CityTime("Мапуту", "Africa/Maputo", "Африка"),
            CityTime("Рабат", "Africa/Casablanca", "Африка"),
            CityTime("Тунис", "Africa/Tunis", "Африка"),
            CityTime("Хартум", "Africa/Khartoum", "Африка"),

            // Австралия и Океания
            CityTime("Сидней", "Australia/Sydney", "Австралия и Океания"),
            CityTime("Мельбурн", "Australia/Melbourne", "Австралия и Океания"),
            CityTime("Перт", "Australia/Perth", "Австралия и Океания"),
            CityTime("Окленд", "Pacific/Auckland", "Австралия и Океания"),
            CityTime("Сува", "Pacific/Fiji", "Австралия и Океания"),
            CityTime("Брисбен", "Australia/Brisbane", "Австралия и Океания"),
            CityTime("Аделаида", "Australia/Adelaide", "Австралия и Океания"),
            CityTime("Веллингтон", "Pacific/Auckland", "Австралия и Океания")
        )
    }

    var favorites by remember { mutableStateOf(prefs.getStringSet("favorites", emptySet())?.toSet() ?: emptySet()) }

    fun toggleFavorite(cityName: String) {
        val newFavs = if (favorites.contains(cityName)) favorites - cityName else favorites + cityName
        favorites = newFavs
        prefs.edit().putStringSet("favorites", newFavs).apply()
    }

    val regions = remember { cities.map { it.region }.distinct() }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(showSeconds) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(if (showSeconds) 1_000L else 30_000L)
        }
    }

    val timePattern = if (use24HourFormat) "HH:mm" else "hh:mm a"
    val timeLocale = if (use24HourFormat) Locale.getDefault() else Locale.US

    val filteredCities = remember(selectedTabIndex, sortType, regions, currentTime) {
        if (regions.isEmpty() || selectedTabIndex !in regions.indices) {
            emptyList()
        } else {
            val regionCities = cities.filter { it.region == regions[selectedTabIndex] }
            when (sortType) {
                0 -> regionCities.sortedBy { it.name }
                1 -> regionCities.sortedBy { it.timezone }
                2 -> regionCities.sortedBy { TimeZone.getTimeZone(it.timezone).getOffset(currentTime) }
                else -> regionCities
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text("Мировое время", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

        ScrollableTabRow(selectedTabIndex = selectedTabIndex, edgePadding = 16.dp) {
            regions.forEachIndexed { index, region ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(region) }
                )
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            items(filteredCities) { city ->
                val sdf = remember(timePattern, city.timezone) {
                    SimpleDateFormat(timePattern, timeLocale).apply {
                        timeZone = TimeZone.getTimeZone(city.timezone)
                    }
                }
                val isFav = favorites.contains(city.name)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { toggleFavorite(city.name) }) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "В избранное",
                            tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(city.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        if (showTimezoneName) {
                            Text(city.timezone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(sdf.format(Date(currentTime)), fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
                HorizontalDivider()
            }
        }
    }
}