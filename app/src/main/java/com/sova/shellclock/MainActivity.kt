package com.sova.shellclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.sova.shellclock.ui.theme.ShellClockTheme

enum class Screen(val label: String, val icon: ImageVector) {
    Clock("Часы", Icons.Default.Home),
    World("Мир", Icons.Default.List),
    Stopwatch("Секундомер", Icons.Default.PlayArrow),
    Timer("Таймер", Icons.Default.Build),
    Description(label = "Описание", Icons.Default.Info)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShellClockTheme {
                var currentScreen by remember { mutableStateOf(Screen.Clock) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            Screen.values().forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.label) },
                                    label = { Text(screen.label) },
                                    selected = currentScreen == screen,
                                    onClick = { currentScreen = screen }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            Screen.Clock -> ClockScreen()
                            Screen.World -> WorldClockScreen()
                            Screen.Stopwatch -> StopwatchScreen()
                            Screen.Timer -> TimerScreen()
                            Screen.Description -> DescriptionScreen()
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
        ClockScreen()
    }
}
