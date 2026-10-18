package com.sova.shellclock

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sova.shellclock.ui.theme.ShellClockTheme

enum class Screen(val label: String, val icon: ImageVector) {
    Clock("Часы", Icons.Default.Home),
    World("Мир", Icons.Default.List),
    Stopwatch("Секундомер", Icons.Default.PlayArrow),
    Timer("Таймер", Icons.Default.Build),
    Description(label = "Описание", Icons.Default.Info),
    Settings(label = "Настройки", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPreferences = getSharedPreferences("shellclock_prefs", Context.MODE_PRIVATE)

        setContent {
            var isDarkTheme by remember { mutableStateOf(sharedPreferences.getBoolean("is_dark", false)) }
            var sizeMultiplier by remember { mutableStateOf(sharedPreferences.getFloat("size_scale", 1.0f)) }
            var showSeconds by remember { mutableStateOf(sharedPreferences.getBoolean("show_seconds", true)) }
            var use24HourFormat by remember { mutableStateOf(sharedPreferences.getBoolean("use_24h", true)) }
            var currentScreen by remember { mutableStateOf(Screen.Clock) }
            val context = LocalContext.current

            // Безопасная проверка версии Android для Material You цветов
            val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isDarkTheme) darkColorScheme() else lightColorScheme()
            }

            MaterialTheme(colorScheme = colorScheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        NavigationBar {
                            Screen.entries.forEach { screen ->
                                val isSelected = currentScreen == screen
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.label,
                                            modifier = Modifier.size((24 * sizeMultiplier).dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.label,
                                            fontSize = (12 * sizeMultiplier).sp
                                        )
                                    },
                                    selected = isSelected,
                                    alwaysShowLabel = false,
                                    onClick = { currentScreen = screen }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (currentScreen) {
                            // Передаем настройки отображения времени в экраны
                            Screen.Clock -> ClockScreen(showSeconds = showSeconds, use24HourFormat = use24HourFormat)
                            Screen.World -> WorldClockScreen(showSeconds = showSeconds, use24HourFormat = use24HourFormat)

                            Screen.Stopwatch -> StopwatchScreen()
                            Screen.Timer -> TimerScreen()
                            Screen.Description -> DescriptionScreen(sizeMultiplier)
                            Screen.Settings -> SettingsScreen(
                                isDarkTheme = isDarkTheme,
                                onThemeChanged = { dark ->
                                    isDarkTheme = dark
                                    sharedPreferences.edit().putBoolean("is_dark", dark).apply()
                                },
                                sizeMultiplier = sizeMultiplier,
                                onSizeChanged = { scale ->
                                    sizeMultiplier = scale
                                    sharedPreferences.edit().putFloat("size_scale", scale).apply()
                                },
                                showSeconds = showSeconds,
                                onShowSecondsChanged = { show ->
                                    showSeconds = show
                                    sharedPreferences.edit().putBoolean("show_seconds", show).apply()
                                },
                                use24HourFormat = use24HourFormat,
                                on24HourFormatChanged = { use24h ->
                                    use24HourFormat = use24h
                                    sharedPreferences.edit().putBoolean("use_24h", use24h).apply()
                                }
                            )
                        }
                    }
                }
            }
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
