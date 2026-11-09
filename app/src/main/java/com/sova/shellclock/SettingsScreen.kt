package com.sova.shellclock

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onBack: () -> Unit,
    onOpenDeveloperMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val safe = remember(settings) { settings.normalized() }
    val scale = safe.sizeMultiplier
    val context = LocalContext.current

    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showResetAllDialog by remember { mutableStateOf(false) }
    var showImportError by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun update(transform: (AppSettings) -> AppSettings) {
        onSettingsChange(transform(safe).normalized())
    }

    // ФУНКЦИЯ ДЛЯ УВЕДОМЛЕНИЙ БЕЗ ТОРМОЗОВ
    fun showSnackbar(message: String) {
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 40.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                }
                Text("Настройки", fontSize = (28 * scale).sp, fontWeight = FontWeight.Bold)
            }

            ScrollableTabRow(selectedTabIndex = tabIndex, edgePadding = 16.dp, modifier = Modifier.fillMaxWidth()) {
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
                        settings = safe,
                        scale = scale,
                        onUpdate = ::update,
                        appVersion = BuildConfig.VERSION_NAME,
                        onExport = { exportLauncher.launch("my_backup.scs") },
                        onImport = { importLauncher.launch(arrayOf("text/plain", "*/*")) },
                        onOpenLicense = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/adelka4745/ShellClock/blob/master/LICENSE"))
                            context.startActivity(intent)
                        },
                        onOpenDeveloperMode = onOpenDeveloperMode,
                        onDeveloperActivated = { showSnackbar("Вы стали разработчиком!") },
                        onCountUpdate = { remaining -> showSnackbar("Осталось $remaining кликов до режима разработчика") }
                    )
                }
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
        item { SwitchRow("Секунды", "Показывать секунды", settings.showSeconds, scale) { v -> update { it.copy(showSeconds = v) } } }
        item { SwitchRow("Дата под часами", "Показывать дату", settings.showDateUnderClock, scale) { v -> update { it.copy(showDateUnderClock = v) } } }
        item { SwitchRow("Часовой пояс", "Показывать название", settings.showTimezoneName, scale) { v -> update { it.copy(showTimezoneName = v) } } }
    }
}

@Composable
private fun DataTab(
    settings: AppSettings, scale: Float, onUpdate: ((AppSettings) -> AppSettings) -> Unit,
    appVersion: String, onExport: () -> Unit, onImport: () -> Unit,
    onOpenLicense: () -> Unit, onOpenDeveloperMode: () -> Unit,
    onDeveloperActivated: () -> Unit, onCountUpdate: (Int) -> Unit
) {
    var clickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    fun handleVersionClick() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > 1500) clickCount = 0
        clickCount++
        lastClickTime = currentTime

        if (clickCount == 7) {
            clickCount = 0
            if (!settings.isDeveloperMode) {
                onUpdate { it.copy(isDeveloperMode = true) }
                onDeveloperActivated()
            }
        } else {
            onCountUpdate(7 - clickCount)
        }
    }

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
        item {
            Column(modifier = Modifier.fillMaxWidth().clickable { handleVersionClick() }.padding(vertical = 8.dp)) {
                Text("Версия: $appVersion", fontSize = (14 * scale).sp)
            }
        }
        item {
            TextButton(onClick = onOpenLicense, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Лицензия (LICENSE)", fontSize = (15 * scale).sp)
            }
        }

        if (settings.isDeveloperMode) {
            item {
                Button(
                    onClick = onOpenDeveloperMode,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Настройки разработчика", fontSize = (15 * scale).sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
        }
    }
}

@Composable
private fun AppearanceTab(settings: AppSettings, scale: Float, update: ((AppSettings) -> AppSettings) -> Unit) {
    var showColorPicker by remember { mutableStateOf(false) }
    var red by remember { mutableFloatStateOf(1f) }
    var green by remember { mutableFloatStateOf(0.5f) }
    var blue by remember { mutableFloatStateOf(0.5f) }

    SettingsLazyList(scale) {
        item { SectionHeader("Тема оформления", scale) }
        items(ThemeStyle.entries.toList()) { style ->
            RadioRow(settings.themeStyle == style, style.label, scale) { update { it.copy(themeStyle = style) } }
        }

        item { SectionHeader("Свой цвет акцента (RGB)", scale) }
        item {
            Button(onClick = { showColorPicker = true }, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(24.dp).background(Color(red, green, blue)))
                Spacer(Modifier.width(8.dp))
                Text("Выбрать цвет")
            }
            if (showColorPicker) {
                AlertDialog(
                    onDismissRequest = { showColorPicker = false },
                    title = { Text("Выбор цвета") },
                    text = {
                        Column {
                            Text("Красный: ${(red * 255).toInt()}")
                            Slider(value = red, onValueChange = { red = it })
                            Text("Зеленый: ${(green * 255).toInt()}")
                            Slider(value = green, onValueChange = { green = it })
                            Text("Синий: ${(blue * 255).toInt()}")
                            Slider(value = blue, onValueChange = { blue = it })
                            Box(modifier = Modifier.fillMaxWidth().height(50.dp).background(Color(red, green, blue)))
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            update { it.copy(customColor = Color(red, green, blue).toArgb()) }
                            showColorPicker = false
                        }) { Text("Применить") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            update { it.copy(customColor = null) }
                            showColorPicker = false
                        }) { Text("Сбросить") }
                    }
                )
            }
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
private fun CitiesTab(settings: AppSettings, scale: Float, onUpdate: ((AppSettings) -> AppSettings) -> Unit, onResetAll: () -> Unit) {
    SettingsLazyList(scale) {
        item { SectionHeader("Сортировка", scale) }
        items(SortType.entries.toList()) { s ->
            RadioRow(settings.sortType == s, s.label, scale) { onUpdate { it.copy(sortType = s) } }
        }

        item { SectionHeader("Отображение", scale) }
        item { SwitchRow("Скрывать пустые регионы", "Не показывать вкладки", settings.hideEmptyRegions, scale) { v -> onUpdate { it.copy(hideEmptyRegions = v) } } }

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
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
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

// --- ЭТОГО НЕ ХВАТАЛО! СЕРИАЛИЗАТОР ---
object SettingsSerializer {
    fun serialize(s: AppSettings): String = buildString {
        append("theme=${s.themeStyle.name};")
        append("size=${s.sizeMultiplier};")
        append("timeFmt=${s.timeFormat};")
        append("seconds=${s.showSeconds};")
        append("sort=${s.sortType.name};")
        append("date=${s.showDateUnderClock};")
        append("hideEmpty=${s.hideEmptyRegions};")
        append("tzName=${s.showTimezoneName};")
        append("devMode=${s.isDeveloperMode};")
        append("customColor=${s.customColor ?: -1};")
        append("mainCity=${s.mainCityName ?: ""}")
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
                showTimezoneName = map["tzName"]?.toBooleanStrictOrNull() ?: false,
                isDeveloperMode = map["devMode"]?.toBooleanStrictOrNull() ?: false,
                customColor = map["customColor"]?.toIntOrNull()?.takeIf { it != -1 },
                mainCityName = map["mainCity"]?.takeIf { it.isNotBlank() }
            ).normalized()
        } catch (_: Exception) { null }
    }
}