package com.sova.shellclock

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sova.shellclock.ui.theme.ShellClockTheme

enum class Screen(val label: String, val icon: ImageVector) {
    Clock("Часы", Icons.Filled.Home),
    World("Мир", Icons.Filled.List),
    Stopwatch("Секундомер", Icons.Filled.PlayArrow),
    Timer("Таймер", Icons.Filled.Build),
    Description("Описание", Icons.Filled.Info),
    Settings("Настройки", Icons.Filled.Settings)
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

            fun updateSettings(newSettings: AppSettings) {
                appSettings = newSettings
                saveSettings(sharedPreferences, newSettings)
            }

            val colorScheme = when (appSettings.themeStyle) {
                ThemeStyle.LIGHT -> lightColorScheme()
                ThemeStyle.CLASSIC_DARK -> darkColorScheme()
                ThemeStyle.OLED_BLACK -> {
                    val base = darkColorScheme()
                    base.copy(background = Color.Black, surface = Color.Black, surfaceVariant = Color.Black)
                }
                ThemeStyle.AUTO -> if (isSystemInDarkTheme()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) dynamicDarkColorScheme(context) else darkColorScheme()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) dynamicLightColorScheme(context) else lightColorScheme()
                }
                ThemeStyle.AURORA -> darkColorScheme(
                    primary = Color(0xFF4FC3F7), secondary = Color(0xFF81D4FA),
                    background = Color(0xFF0B192C), surface = Color(0xFF1A365D),
                    surfaceVariant = Color(0xFF2B4D7A), onPrimary = Color.Black,
                    onBackground = Color.White, onSurface = Color.White
                )
                ThemeStyle.SUNSET -> darkColorScheme(
                    primary = Color(0xFFFFB74D), secondary = Color(0xFFFF8A65),
                    background = Color(0xFF2D1B0E), surface = Color(0xFF4A2E1B),
                    surfaceVariant = Color(0xFF6B462E), onPrimary = Color.Black,
                    onBackground = Color.White, onSurface = Color.White
                )
                ThemeStyle.NEON -> darkColorScheme(
                    primary = Color(0xFFE040FB), secondary = Color(0xFF00E5FF),
                    background = Color(0xFF12001A), surface = Color(0xFF260033),
                    surfaceVariant = Color(0xFF3D0052), onPrimary = Color.Black,
                    onBackground = Color(0xFFF3E5F5), onSurface = Color(0xFFF3E5F5)
                )
                ThemeStyle.FOREST -> darkColorScheme(
                    primary = Color(0xFF81C784), secondary = Color(0xFFA5D6A7),
                    background = Color(0xFF0F1A12), surface = Color(0xFF1B3323),
                    surfaceVariant = Color(0xFF2E4D37), onPrimary = Color.Black,
                    onBackground = Color(0xFFE8F5E9), onSurface = Color(0xFFE8F5E9)
                )
                ThemeStyle.ROSE -> darkColorScheme(
                    primary = Color(0xFFFF80AB), secondary = Color(0xFFFF4081),
                    background = Color(0xFF1F0F15), surface = Color(0xFF3B1A26),
                    surfaceVariant = Color(0xFF572738), onPrimary = Color.Black,
                    onBackground = Color(0xFFFFEBEE), onSurface = Color(0xFFFFEBEE)
                )
                ThemeStyle.MATERIAL_BLUE -> darkColorScheme(
                    primary = Color(0xFF90CAF9), secondary = Color(0xFF64B5F6),
                    background = Color(0xFF0D1B2A), surface = Color(0xFF1B263B),
                    surfaceVariant = Color(0xFF2E3B55), onPrimary = Color.Black,
                    onBackground = Color(0xFFE3F2FD), onSurface = Color(0xFFE3F2FD)
                )
            }

            MaterialTheme(colorScheme = colorScheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        NavigationBar {
                            Screen.entries.forEach { screen ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.label,
                                            modifier = Modifier.size((24 * appSettings.sizeMultiplier).dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.label,
                                            fontSize = (12 * appSettings.sizeMultiplier).sp
                                        )
                                    },
                                    selected = currentScreen == screen,
                                    alwaysShowLabel = false,
                                    onClick = { currentScreen = screen }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding), color = MaterialTheme.colorScheme.background) {
                        when (currentScreen) {
                            Screen.Clock -> ClockScreen(
                                showSeconds = appSettings.showSeconds,
                                use24HourFormat = appSettings.timeFormat == 24
                            )
                            Screen.World -> WorldClockScreen(
                                showSeconds = appSettings.showSeconds,
                                use24HourFormat = appSettings.timeFormat == 24,
                                sortType = appSettings.sortType.ordinal
                            )
                            Screen.Stopwatch -> StopwatchScreen()
                            Screen.Timer -> TimerScreen()
                            Screen.Description -> DescriptionScreen(appSettings.sizeMultiplier)
                            Screen.Settings -> SettingsScreen(
                                settings = appSettings,
                                onSettingsChange = ::updateSettings
                            )
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
            sizeMultiplier = prefs.getFloat("size_scale", 1.0f),
            showSeconds = prefs.getBoolean("show_seconds", true),
            timeFormat = prefs.getInt("time_format", 24)
        )
    }

    private fun saveSettings(prefs: android.content.SharedPreferences, settings: AppSettings) {
        prefs.edit().apply {
            putInt("theme_style", settings.themeStyle.ordinal)
            putInt("sort_type", settings.sortType.ordinal)
            putFloat("size_scale", settings.sizeMultiplier)
            putBoolean("show_seconds", settings.showSeconds)
            putInt("time_format", settings.timeFormat)
            apply()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClockPreview() {
    ShellClockTheme {
        ClockScreen(showSeconds = true, use24HourFormat = true)
    }
}