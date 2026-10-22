package com.sova.shellclock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Перечисления ───────────────────────────────────────────────
enum class ThemeStyle(val label: String) {
    LIGHT("Светлая"),
    CLASSIC_DARK("Классическая тёмная"),
    OLED_BLACK("Глубокая чёрная (OLED)"),
    AUTO("Авто (по системе)"),
    AURORA("Северное сияние (Синяя)"),
    SUNSET("Закат (Оранжевая)"),
    NEON("Неон (Фиолетовая)"),
    FOREST("Лесная (Зелёная)"),
    ROSE("Розовая"),
    MATERIAL_BLUE("Классический синий")
}

enum class SortType(val label: String) {
    BY_NAME("По названию городов (А-Я)"),
    BY_TIMEZONE("По названию часовых поясов"),
    BY_UTC_OFFSET("По смещению UTC")
}

// ── Модель настроек ────────────────────────────────────────────
data class AppSettings(
    val themeStyle: ThemeStyle = ThemeStyle.CLASSIC_DARK,
    val sizeMultiplier: Float = 1.0f,
    val timeFormat: Int = 24, // 24 или 12 часов
    val showSeconds: Boolean = true,
    val sortType: SortType = SortType.BY_NAME
) {
    companion object {
        val DEFAULT = AppSettings()

        fun clampSize(value: Float): Float =
            if (value.isNaN() || value.isInfinite()) 1.0f
            else value.coerceIn(0.8f, 1.5f)
    }

    fun normalized(): AppSettings = copy(sizeMultiplier = clampSize(sizeMultiplier))
}

// ── Главный экран настроек ─────────────────────────────────────
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val safe = remember(settings) { settings.normalized() }
    val scale = safe.sizeMultiplier

    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showResetAllDialog by remember { mutableStateOf(false) }

    fun update(transform: (AppSettings) -> AppSettings) {
        onSettingsChange(transform(safe).normalized())
    }

    val tabs = listOf(
        SettingsTab("Внешний вид", Icons.Default.Palette),
        SettingsTab("Время", Icons.Default.Schedule),
        SettingsTab("Города", Icons.Default.Public)
    )

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Настройки",
            fontSize = (28 * scale).sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 40.dp, bottom = 12.dp)
        )

        ScrollableTabRow(
            selectedTabIndex = tabIndex,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { i, tab ->
                Tab(
                    selected = tabIndex == i,
                    onClick = { tabIndex = i },
                    text = { Text(tab.title, fontSize = (13 * scale).sp) },
                    icon = { Icon(tab.icon, contentDescription = tab.title) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (tabIndex) {
                0 -> AppearanceTab(safe, scale, ::update)
                1 -> TimeTab(safe, scale, ::update)
                2 -> CitiesTab(safe, scale, ::update, onResetAll = { showResetAllDialog = true })
            }
        }
    }

    if (showResetAllDialog) {
        AlertDialog(
            onDismissRequest = { showResetAllDialog = false },
            title = { Text("Сброс настроек", fontSize = (20 * scale).sp) },
            text = { Text("Сбросить всё к заводским настройкам?", fontSize = (15 * scale).sp) },
            confirmButton = {
                TextButton(onClick = { onSettingsChange(AppSettings.DEFAULT); showResetAllDialog = false }) {
                    Text("Сбросить", color = MaterialTheme.colorScheme.error, fontSize = (15 * scale).sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAllDialog = false }) {
                    Text("Отмена", fontSize = (15 * scale).sp)
                }
            }
        )
    }
}

// ── Вкладки ────────────────────────────────────────────────────

@Composable
private fun AppearanceTab(settings: AppSettings, scale: Float, update: ((AppSettings) -> AppSettings) -> Unit) {
    SettingsLazyList(scale) {
        item { SectionHeader("Тема оформления", scale) }
        items(ThemeStyle.entries.toList()) { style ->
            RadioRow(settings.themeStyle == style, style.label, scale) { update { it.copy(themeStyle = style) } }
        }

        item { SectionHeader("Масштаб интерфейса", scale) }
        item {
            Text("Масштаб: ${(settings.sizeMultiplier * 100).toInt()}%", fontSize = (14 * scale).sp)
            Slider(
                value = settings.sizeMultiplier,
                onValueChange = { newSize -> update { it.copy(sizeMultiplier = AppSettings.clampSize(newSize)) } },
                valueRange = 0.8f..1.5f, steps = 6,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TimeTab(settings: AppSettings, scale: Float, update: ((AppSettings) -> AppSettings) -> Unit) {
    SettingsLazyList(scale) {
        item { SectionHeader("Предпросмотр", scale) }
        item { TimePreviewCard(settings = settings, scale = scale) }

        item { SectionHeader("Формат времени", scale) }
        item {
            Row(modifier = Modifier.fillMaxWidth().clickable { update { it.copy(timeFormat = 24) } }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = settings.timeFormat == 24, onClick = { update { it.copy(timeFormat = 24) } })
                Text("24-часовой", fontSize = (15 * scale).sp)
            }
            Row(modifier = Modifier.fillMaxWidth().clickable { update { it.copy(timeFormat = 12) } }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = settings.timeFormat == 12, onClick = { update { it.copy(timeFormat = 12) } })
                Text("12-часовой", fontSize = (15 * scale).sp)
            }
        }

        item {
            SwitchRow("Показывать секунды", "Обновление каждую секунду", settings.showSeconds, scale) { v ->
                update { it.copy(showSeconds = v) }
            }
        }
    }
}

@Composable
private fun CitiesTab(settings: AppSettings, scale: Float, onUpdate: ((AppSettings) -> AppSettings) -> Unit, onResetAll: () -> Unit) {
    SettingsLazyList(scale) {
        item { SectionHeader("Сортировка", scale) }
        items(SortType.entries.toList()) { s ->
            RadioRow(settings.sortType == s, s.label, scale) { onUpdate { it.copy(sortType = s) } }
        }

        item { SectionHeader("Сброс", scale) }
        item {
            Button(
                onClick = onResetAll,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Сбросить все настройки", fontSize = (16 * scale).sp, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

// ── Вспомогательные компоненты ─────────────────────────────────

private data class SettingsTab(val title: String, val icon: ImageVector)

@Composable
private fun SettingsLazyList(scale: Float, content: LazyListScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = content
    )
}

@Composable
private fun SectionHeader(title: String, scale: Float) {
    Text(title, fontSize = (16 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
}

@Composable
private fun RadioRow(selected: Boolean, label: String, scale: Float, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, fontSize = (15 * scale).sp)
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, scale: Float, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = (15 * scale).sp)
            Text(subtitle, fontSize = (12 * scale).sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun TimePreviewCard(settings: AppSettings, scale: Float) {
    val now = remember { Date() }
    val timePattern = if (settings.timeFormat == 24) {
        if (settings.showSeconds) "HH:mm:ss" else "HH:mm"
    } else {
        if (settings.showSeconds) "hh:mm:ss a" else "hh:mm a"
    }
    val locale = if (settings.timeFormat == 12) Locale.US else Locale("ru")
    val timeText = remember(timePattern, locale) { SimpleDateFormat(timePattern, locale).format(now) }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(timeText, fontSize = (28 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}