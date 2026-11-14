package com.sova.shellclock

import android.app.AlarmManager
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sova.shellclock.ui.theme.ShellClockTheme
import com.sova.shellclock.ui.theme.schemeFor
import com.sova.shellclock.citiesList
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

enum class Screen(val label: String, val icon: ImageVector) {
    Clock("Часы", Icons.Filled.Home),
    World("Мир", Icons.AutoMirrored.Filled.List),
    Alarm("Будильник", Icons.Filled.Alarm),
    Stopwatch("Секундомер", Icons.Filled.PlayArrow),
    Timer("Таймер", Icons.Filled.Build)
}

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("shellclock_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("app_language", "ru") ?: "ru"
        val locale = Locale.forLanguageTag(lang)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sharedPreferences = getSharedPreferences("shellclock_prefs", Context.MODE_PRIVATE)

        setContent {
            var appSettings by remember { mutableStateOf(loadSettings(sharedPreferences)) }
            var currentScreen by remember {
                mutableStateOf(Screen.entries.getOrElse(sharedPreferences.getInt("current_screen", 0)) { Screen.Clock })
            }
            val context = LocalContext.current
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Состояния навигации
            var showSettings by remember { mutableStateOf(false) }
            var showDescription by remember { mutableStateOf(false) }
            var showDeveloperMode by remember { mutableStateOf(false) }
            var showHelp by remember { mutableStateOf(false) }
            var showUtils by remember { mutableStateOf(false) }

            fun updateSettings(newSettings: AppSettings) {
                if (newSettings.appLanguage != appSettings.appLanguage) {
                    appSettings = newSettings
                    saveSettings(sharedPreferences, newSettings)
                    recreate()
                } else {
                    appSettings = newSettings
                    saveSettings(sharedPreferences, newSettings)
                }
            }

            // Уведомления о батарее
            LaunchedEffect(Unit) {
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
                var lastNotifiedLevel = -1
                while (true) {
                    val level = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    val isCharging = batteryManager.isCharging
                    if ((level == 15 && !isCharging && lastNotifiedLevel != 15) ||
                        (level == 100 && isCharging && lastNotifiedLevel != 100)) {
                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        val channelId = "battery_channel"
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val channel = android.app.NotificationChannel(channelId, "Battery", android.app.NotificationManager.IMPORTANCE_DEFAULT)
                            notificationManager.createNotificationChannel(channel)
                        }
                        val title = if (level == 100) "Battery Charged" else "Low Battery"
                        val text = if (level == 100) "Battery level reached 100%" else "Battery level dropped to 15%"
                        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
                            .setContentTitle(title).setContentText(text)
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT).build()
                        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            notificationManager.notify(1, notification)
                        }
                        lastNotifiedLevel = level
                    }
                    delay(60000L)
                }
            }

            val customScheme = appSettings.customColor?.let { colorInt ->
                val c = Color(colorInt)
                // База — схема выбранной темы, а не MaterialTheme: внутри setContent MaterialTheme ещё не применён
                val baseScheme = schemeFor(appSettings.themeStyle)
                baseScheme.copy(primary = c, secondary = c.copy(alpha = 0.7f), tertiary = c.copy(alpha = 0.5f))
            }

            ShellClockTheme(themeStyle = appSettings.themeStyle, customColorScheme = customScheme) {
                val currentOverlay = when {
                    showDeveloperMode -> "dev"
                    showSettings -> "settings"
                    showDescription -> "description"
                    showHelp -> "help"
                    showUtils -> "utils"
                    else -> null
                }

                AnimatedContent(targetState = currentOverlay, transitionSpec = {
                    (fadeIn(tween(300)) + slideInHorizontally(initialOffsetX = { it / 4 })).togetherWith(fadeOut(tween(300)) + slideOutHorizontally(targetOffsetX = { -it / 4 }))
                }, label = "Menu") { overlay ->
                    when (overlay) {
                        "dev" -> DeveloperModeScreen(settings = appSettings, onSettingsChange = ::updateSettings, onBack = { showDeveloperMode = false })
                        "settings" -> SettingsScreen(settings = appSettings, onSettingsChange = ::updateSettings, onBack = { showSettings = false }, onOpenDeveloperMode = { showDeveloperMode = true })
                        "description" -> DescriptionScreen(sizeMultiplier = appSettings.sizeMultiplier, onBack = { showDescription = false })
                        "help" -> HelpScreen(onBack = { showHelp = false })

                        // Тут только UtilsScreen, AiScreen вызывается внутри
                        "utils" -> UtilsScreen(
                            scaleMultiplier = appSettings.sizeMultiplier,
                            onBack = { showUtils = false },
                            onOpenAi = {}, // Заглушка, так как AI внутри
                            onOpenScreen = { screen ->
                                currentScreen = screen
                                sharedPreferences.edit().putInt("current_screen", screen.ordinal).apply()
                                showUtils = false
                            },
                            onOpenSettings = {
                                // Настройки — оверлей, флаг showSettings его и открывает
                                showSettings = true
                                showUtils = false
                            },
                            timeZone = appSettings.timeZoneId?.let { java.util.TimeZone.getTimeZone(it) } ?: java.util.TimeZone.getDefault(),
                            timeZoneLabel = (appSettings.timeZoneId ?: java.util.TimeZone.getDefault().id).substringAfterLast('/').replace('_', ' ')
                        )

                        else -> {
                            val activity = context as Activity
                            // Уведомления о батарее без разрешения молча не показываются — запрашиваем его при старте
                            LaunchedEffect(Unit) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                    activity.requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
                                }
                            }
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                containerColor = MaterialTheme.colorScheme.background,
                                bottomBar = {
                                    NavigationBar(modifier = Modifier.height(90.dp), containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                                        Screen.entries.forEach { screen ->
                                            NavigationBarItem(
                                                icon = { Icon(screen.icon, screen.label, modifier = Modifier.size((28 * appSettings.sizeMultiplier).dp)) },
                                                label = { Text(tr(screen.label), fontSize = (12 * appSettings.sizeMultiplier).sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                                selected = currentScreen == screen, alwaysShowLabel = true,
                                                onClick = {
                                                    currentScreen = screen
                                                    // Выбранная вкладка переживает поворот, смену темы и языка
                                                    sharedPreferences.edit().putInt("current_screen", screen.ordinal).apply()
                                                },
                                                colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer, selectedTextColor = MaterialTheme.colorScheme.onSurface, indicatorColor = MaterialTheme.colorScheme.secondaryContainer)
                                            )
                                        }
                                    }
                                }
                            ) { innerPadding ->
                                Surface(modifier = Modifier.padding(innerPadding), color = MaterialTheme.colorScheme.background) {
                                    AnimatedContent(targetState = currentScreen, transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) }, label = "Screen") { targetScreen ->
                                        when (targetScreen) {
                                            Screen.Clock -> ClockScreen(
                                                settings = appSettings,
                                                onOpenSettings = { showSettings = true },
                                                onOpenDescription = { showDescription = true },
                                                onOpenHelp = { showHelp = true },
                                                onOpenUtils = { showUtils = true },
                                                onOpenGitHub = {
                                                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/adelka4745/ShellClock"))
                                                    context.startActivity(intent)
                                                }
                                            )
                                            Screen.World -> WorldClockScreen(
                                                showSeconds = appSettings.showSeconds,
                                                use24HourFormat = appSettings.timeFormat == 24,
                                                sortType = appSettings.sortType.ordinal,
                                                hideEmptyRegions = appSettings.hideEmptyRegions,
                                                showTimezoneName = appSettings.showTimezoneName,
                                                mainCityName = appSettings.mainCityName,
                                                timeZoneId = appSettings.timeZoneId,
                                                onMainCityChange = { cityName ->
                                                    // Главный город в «Мире» — это ваш часовой пояс: на него смотрят остальные города
                                                    val zone = citiesList.firstOrNull { it.name == cityName }?.timezone
                                                    updateSettings(appSettings.copy(mainCityName = cityName, timeZoneId = zone))
                                                }
                                            )
                                            Screen.Alarm -> AlarmScreen(scaleMultiplier = appSettings.sizeMultiplier)
                                            Screen.Stopwatch -> StopwatchScreen()
                                            Screen.Timer -> TimerScreen()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadSettings(prefs: android.content.SharedPreferences): AppSettings {
        return AppSettings(
            themeStyle = ThemeStyle.entries.getOrElse(prefs.getInt("theme_style", 1)) { ThemeStyle.CLASSIC_DARK },
            sortType = SortType.entries.getOrElse(prefs.getInt("sort_type", 0)) { SortType.BY_NAME },
            sizeMultiplier = prefs.getFloat("size_scale", 1.0f), showSeconds = prefs.getBoolean("show_seconds", true),
            timeFormat = prefs.getInt("time_format", 24), showDateUnderClock = prefs.getBoolean("show_date", false),
            hideEmptyRegions = prefs.getBoolean("hide_empty", true), showTimezoneName = prefs.getBoolean("show_tz_name", false),
            isDeveloperMode = prefs.getBoolean("dev_mode", false),
            customColor = if (prefs.contains("custom_color")) prefs.getInt("custom_color", -1) else null,
            mainCityName = prefs.getString("main_city", null),
            appLanguage = prefs.getString("app_language", "ru") ?: "ru",
            timeZoneId = prefs.getString("time_zone", null)
        )
    }

    private fun saveSettings(prefs: android.content.SharedPreferences, settings: AppSettings) {
        prefs.edit().apply {
            putInt("theme_style", settings.themeStyle.ordinal); putInt("sort_type", settings.sortType.ordinal)
            putFloat("size_scale", settings.sizeMultiplier); putBoolean("show_seconds", settings.showSeconds)
            putInt("time_format", settings.timeFormat); putBoolean("show_date", settings.showDateUnderClock)
            putBoolean("hide_empty", settings.hideEmptyRegions); putBoolean("show_tz_name", settings.showTimezoneName)
            putBoolean("dev_mode", settings.isDeveloperMode)
            settings.customColor?.let { putInt("custom_color", it) } ?: remove("custom_color")
            settings.mainCityName?.let { putString("main_city", it) } ?: remove("main_city")
            putString("app_language", settings.appLanguage)
            settings.timeZoneId?.let { putString("time_zone", it) } ?: remove("time_zone")
            apply()
        }
    }
}