package com.sova.shellclock

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ThemeStyle(val label: String) {
    LIGHT("Светлая"),
    CLASSIC_DARK("Классическая тёмная"),
    OLED_BLACK("Глубокая чёрная (OLED)"),
    AUTO("Авто (по системе)"),
    AURORA("Северное сияние"),
    SUNSET("Закат"),
    NEON("Неон"),
    FOREST("Лесная"),
    ROSE("Розовая"),
    MATERIAL_BLUE("Классический синий"),
    SEA_WAVE("Морская волна"),
    RETRO("Ретро-неон"),
    COSMOS("Космос"),
    MATERIAL_LIGHT("Material Light")
}

enum class SortType(val label: String) {
    BY_NAME("По названию городов (А-Я)"),
    BY_TIMEZONE("По названию часовых поясов"),
    BY_UTC_OFFSET("По смещению UTC")
}

data class AppSettings(
    val themeStyle: ThemeStyle = ThemeStyle.CLASSIC_DARK,
    val sizeMultiplier: Float = 1.0f,
    val timeFormat: Int = 24,
    val showSeconds: Boolean = true,
    val sortType: SortType = SortType.BY_NAME,
    val showDateUnderClock: Boolean = false,
    val hideEmptyRegions: Boolean = true,
    val showTimezoneName: Boolean = false
) {
    companion object {
        val DEFAULT = AppSettings()

        fun clampSize(value: Float): Float =
            if (value.isNaN() || value.isInfinite()) 1.0f
            else value.coerceIn(0.8f, 1.5f)
    }

    fun normalized(): AppSettings = copy(sizeMultiplier = clampSize(sizeMultiplier))
}

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val safe = remember(settings) { settings.normalized() }
    val scale = safe.sizeMultiplier
    val context = LocalContext.current

    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showResetAllDialog by remember { mutableStateOf(false) }
    var showImportError by remember { mutableStateOf(false) }

    fun update(transform: (AppSettings) -> AppSettings) {
        onSettingsChange(transform(safe).normalized())
    }

    // Экспорт настроек в .scs файл
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        uri?.let {
            try {
                val settingsText = SettingsSerializer.serialize(safe)
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(settingsText.toByteArray())
                }
            } catch (e: Exception) { }
        }
    }

    // Импорт настроек из .scs файла
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val fullData = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                    reader.readText()
                } ?: ""
                SettingsSerializer.deserialize(fullData)?.let { importedSettings ->
                    onSettingsChange(importedSettings)
                }
            } catch (e: Exception) {
                showImportError = true
            }
        }
    }

    val tabs = listOf(
        SettingsTab("Вид", Icons.Default.Palette),
        SettingsTab("Время", Icons.Default.Schedule),
        SettingsTab("Города", Icons.Default.Public),
        SettingsTab("Данные", Icons.Default.Storage)
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
                3 -> DataTab(
                    scale = scale,
                    appVersion = BuildConfig.VERSION_NAME,
                    onExport = { exportLauncher.launch("my_backup.scs") },
                    onImport = { importLauncher.launch(arrayOf("text/plain", "*/*")) },
                    onOpenLicense = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/adelka4745/ShellClock/blob/master/LICENSE"))
                        context.startActivity(intent)
                    }
                )
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

    if (showImportError) {
        AlertDialog(
            onDismissRequest = { showImportError = false },
            title = { Text("Ошибка", fontSize = (20 * scale).sp) },
            text = { Text("Не удалось прочитать файл. Возможно, он поврежден.", fontSize = (15 * scale).sp) },
            confirmButton = { TextButton(onClick = { showImportError = false }) { Text("ОК", fontSize = (15 * scale).sp) } }
        )
    }
}

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

        item { SectionHeader("Элементы", scale) }
        item {
            SwitchRow("Секунды", "Показывать секунды", settings.showSeconds, scale) { v ->
                update { it.copy(showSeconds = v) }
            }
        }
        item {
            SwitchRow("Дата под часами", "Показывать дату", settings.showDateUnderClock, scale) { v ->
                update { it.copy(showDateUnderClock = v) }
            }
        }
        item {
            SwitchRow("Часовой пояс", "Показывать название (Europe/Moscow)", settings.showTimezoneName, scale) { v ->
                update { it.copy(showTimezoneName = v) }
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

        item { SectionHeader("Отображение", scale) }
        item {
            SwitchRow("Скрывать пустые регионы", "Не показывать вкладки", settings.hideEmptyRegions, scale) { v ->
                onUpdate { it.copy(hideEmptyRegions = v) }
            }
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

@Composable
private fun DataTab(
    scale: Float, appVersion: String,
    onExport: () -> Unit, onImport: () -> Unit, onOpenLicense: () -> Unit
) {
    SettingsLazyList(scale) {
        item { SectionHeader("Резервная копия (.scs)", scale) }
        item {
            Text("Сохраните настройки в один файл.", fontSize = (12 * scale).sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onExport, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Экспорт", fontSize = (14 * scale).sp)
                }
                Button(onClick = onImport, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Импорт", fontSize = (14 * scale).sp)
                }
            }
        }

        item { SectionHeader("О приложении", scale) }
        item { Text("Версия: $appVersion", fontSize = (14 * scale).sp) }
        item {
            TextButton(onClick = onOpenLicense, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Лицензия (LICENSE)", fontSize = (15 * scale).sp)
            }
        }
    }
}

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
            if (settings.showDateUnderClock) {
                Text("21 окт 2026", fontSize = (14 * scale).sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

object SettingsSerializer {
    fun serialize(s: AppSettings): String = buildString {
        append("theme=${s.themeStyle.name};")
        append("size=${s.sizeMultiplier};")
        append("timeFmt=${s.timeFormat};")
        append("seconds=${s.showSeconds};")
        append("sort=${s.sortType.name};")
        append("date=${s.showDateUnderClock};")
        append("hideEmpty=${s.hideEmptyRegions};")
        append("tzName=${s.showTimezoneName}")
    }

    fun deserialize(text: String): AppSettings? {
        if (!text.contains('=')) return null
        val map = text.split(';').mapNotNull { part ->
            val idx = part.indexOf('=')
            if (idx <= 0) null else part.substring(0, idx) to part.substring(idx + 1)
        }.toMap()

        fun <T : Enum<T>> enumOr(key: String, values: Array<T>, default: T): T = map[key]?.let { name -> values.firstOrNull { it.name == name } } ?: default

        return try {
            AppSettings(
                themeStyle = enumOr("theme", ThemeStyle.entries.toTypedArray(), AppSettings.DEFAULT.themeStyle),
                sizeMultiplier = AppSettings.clampSize(map["size"]?.toFloatOrNull() ?: 1f),
                timeFormat = map["timeFmt"]?.toIntOrNull() ?: 24,
                showSeconds = map["seconds"]?.toBooleanStrictOrNull() ?: true,
                sortType = enumOr("sort", SortType.entries.toTypedArray(), AppSettings.DEFAULT.sortType),
                showDateUnderClock = map["date"]?.toBooleanStrictOrNull() ?: false,
                hideEmptyRegions = map["hideEmpty"]?.toBooleanStrictOrNull() ?: true,
                showTimezoneName = map["tzName"]?.toBooleanStrictOrNull() ?: false
            ).normalized()
        } catch (_: Exception) { null }
    }
}