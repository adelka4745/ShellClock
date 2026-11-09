package com.sova.shellclock

import android.content.Context
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import kotlinx.coroutines.delay

enum class Screen(val label: String, val icon: ImageVector) {
    Clock("Часы", Icons.Filled.Home), World("Мир", Icons.Filled.List), Alarm("Будильник", Icons.Filled.Alarm),
    Stopwatch("Секундомер", Icons.Filled.PlayArrow), Timer("Таймер", Icons.Filled.Build)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sharedPreferences = getSharedPreferences("shellclock_prefs", Context.MODE_PRIVATE)

        setContent {
            var appSettings by remember { mutableStateOf(loadSettings(sharedPreferences)) }
            var currentScreen by remember { mutableStateOf(Screen.Clock) }
            val context = LocalContext.current

            var showSettings by remember { mutableStateOf(false) }
            var showDescription by remember { mutableStateOf(false) }
            var showDeveloperMode by remember { mutableStateOf(false) }

            fun updateSettings(newSettings: AppSettings) {
                appSettings = newSettings
                saveSettings(sharedPreferences, newSettings)
            }

            // Уведомления о батарее
            LaunchedEffect(Unit) {
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                var lastNotifiedLevel = -1
                while (true) {
                    val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    val isCharging = batteryManager.isCharging

                    if ((level == 15 && !isCharging && lastNotifiedLevel != 15) ||
                        (level == 100 && isCharging && lastNotifiedLevel != 100)) {

                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        val channelId = "battery_channel"

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            val channel = android.app.NotificationChannel(channelId, "Батарея", android.app.NotificationManager.IMPORTANCE_DEFAULT)
                            notificationManager.createNotificationChannel(channel)
                        }

                        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
                            .setContentTitle(if (level == 100) "Батарея заряжена" else "Низкий заряд")
                            .setContentText(if (level == 100) "Уровень заряда достиг 100%" else "Уровень заряда упал до 15%")
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                            .build()

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
                MaterialTheme.colorScheme.copy(
                    primary = c,
                    secondary = c.copy(alpha = 0.7f),
                    tertiary = c.copy(alpha = 0.5f)
                )
            }

            ShellClockTheme(themeStyle = appSettings.themeStyle, customColorScheme = customScheme) {
                AnimatedContent(
                    targetState = Triple(showSettings, showDescription, showDeveloperMode),
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInHorizontally(initialOffsetX = { it / 4 }))
                            .togetherWith(fadeOut(tween(300)) + slideOutHorizontally(targetOffsetX = { -it / 4 }))
                    }, label = "Menu"
                ) { (isSettings, isDescription, isDevMode) ->
                    if (isDevMode) {
                        DeveloperModeScreen(settings = appSettings, onSettingsChange = ::updateSettings, onBack = { showDeveloperMode = false })
                        return@AnimatedContent
                    }
                    if (isSettings) {
                        SettingsScreen(settings = appSettings, onSettingsChange = ::updateSettings, onBack = { showSettings = false }, onOpenDeveloperMode = { showDeveloperMode = true })
                        return@AnimatedContent
                    }
                    if (isDescription) {
                        DescriptionScreen(sizeMultiplier = appSettings.sizeMultiplier, onBack = { showDescription = false })
                        return@AnimatedContent
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            NavigationBar(modifier = Modifier.height(90.dp), containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                                Screen.entries.forEach { screen ->
                                    NavigationBarItem(
                                        icon = { Icon(screen.icon, screen.label, modifier = Modifier.size((28 * appSettings.sizeMultiplier).dp)) },
                                        label = { Text(screen.label, fontSize = (12 * appSettings.sizeMultiplier).sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                        selected = currentScreen == screen, alwaysShowLabel = true, onClick = { currentScreen = screen },
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
                                        onOpenGitHub = {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/adelka4745/ShellClock"))
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
                                        onMainCityChange = { cityName -> updateSettings(appSettings.copy(mainCityName = cityName)) }
                                    )
                                    Screen.Alarm -> AlarmScreen(scaleMultiplier = appSettings.sizeMultiplier)
                                    Screen.Stopwatch -> StopwatchScreen()
                                    Screen.Timer -> TimerScreen()
                                    else -> {}
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
            mainCityName = prefs.getString("main_city", null)
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
            apply()
        }
    }
}