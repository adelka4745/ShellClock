package com.sova.shellclock.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sova.shellclock.ThemeStyle

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF), secondary = Color(0xFFCCC2DC), tertiary = Color(0xFFEFB8C8),
    background = Color(0xFF141218), surface = Color(0xFF141218),
    onBackground = Color(0xFFE6E0E9), onSurface = Color(0xFFE6E0E9)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4), secondary = Color(0xFF625B71), tertiary = Color(0xFF7D5260),
    background = Color(0xFFFEF7FF), surface = Color(0xFFFEF7FF),
    onBackground = Color(0xFF1D1B20), onSurface = Color(0xFF1D1B20)
)

@Composable
fun ShellClockTheme(
    themeStyle: ThemeStyle,
    customColorScheme: ColorScheme? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var colorScheme = when (themeStyle) {
        ThemeStyle.LIGHT -> LightColorScheme
        ThemeStyle.MATERIAL_LIGHT -> LightColorScheme
        ThemeStyle.CLASSIC_DARK -> DarkColorScheme
        ThemeStyle.OLED_BLACK -> DarkColorScheme.copy(background = Color.Black, surface = Color.Black, surfaceVariant = Color.Black, onBackground = Color.White, onSurface = Color.White)
        ThemeStyle.AUTO -> if (isSystemInDarkTheme()) { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) dynamicDarkColorScheme(context) else DarkColorScheme } else { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) dynamicLightColorScheme(context) else LightColorScheme }
        ThemeStyle.AURORA -> DarkColorScheme.copy(primary = Color(0xFF4FC3F7), secondary = Color(0xFF81D4FA), background = Color(0xFF0B192C), surface = Color(0xFF1A365D), surfaceVariant = Color(0xFF2B4D7A), onPrimary = Color.Black, onBackground = Color.White, onSurface = Color.White)
        ThemeStyle.SUNSET -> DarkColorScheme.copy(primary = Color(0xFFFFB74D), secondary = Color(0xFFFF8A65), background = Color(0xFF2D1B0E), surface = Color(0xFF4A2E1B), surfaceVariant = Color(0xFF6B462E), onPrimary = Color.Black, onBackground = Color.White, onSurface = Color.White)
        ThemeStyle.NEON -> DarkColorScheme.copy(primary = Color(0xFFE040FB), secondary = Color(0xFF00E5FF), background = Color(0xFF12001A), surface = Color(0xFF260033), surfaceVariant = Color(0xFF3D0052), onPrimary = Color.Black, onBackground = Color(0xFFF3E5F5), onSurface = Color(0xFFF3E5F5))
        ThemeStyle.FOREST -> DarkColorScheme.copy(primary = Color(0xFF81C784), secondary = Color(0xFFA5D6A7), background = Color(0xFF0F1A12), surface = Color(0xFF1B3323), surfaceVariant = Color(0xFF2E4D37), onPrimary = Color.Black, onBackground = Color(0xFFE8F5E9), onSurface = Color(0xFFE8F5E9))
        ThemeStyle.ROSE -> DarkColorScheme.copy(primary = Color(0xFFFF80AB), secondary = Color(0xFFFF4081), background = Color(0xFF1F0F15), surface = Color(0xFF3B1A26), surfaceVariant = Color(0xFF572738), onPrimary = Color.Black, onBackground = Color(0xFFFFEBEE), onSurface = Color(0xFFFFEBEE))
        ThemeStyle.MATERIAL_BLUE -> DarkColorScheme.copy(primary = Color(0xFF90CAF9), secondary = Color(0xFF64B5F6), background = Color(0xFF0D1B2A), surface = Color(0xFF1B263B), surfaceVariant = Color(0xFF2E3B55), onPrimary = Color.Black, onBackground = Color(0xFFE3F2FD), onSurface = Color(0xFFE3F2FD))
        ThemeStyle.SEA_WAVE -> DarkColorScheme.copy(primary = Color(0xFF4DD0E1), secondary = Color(0xFF80DEEA), background = Color(0xFF001F24), surface = Color(0xFF00363D), surfaceVariant = Color(0xFF004F58), onPrimary = Color.Black, onBackground = Color.White, onSurface = Color.White)
        ThemeStyle.RETRO -> DarkColorScheme.copy(primary = Color(0xFFFFD54F), secondary = Color(0xFFFF8A80), background = Color(0xFF1A1010), surface = Color(0xFF2D1C1C), surfaceVariant = Color(0xFF4A2E2E), onPrimary = Color.Black, onBackground = Color.White, onSurface = Color.White)
        ThemeStyle.COSMOS -> DarkColorScheme.copy(primary = Color(0xFFB388FF), secondary = Color(0xFF82B1FF), background = Color(0xFF000000), surface = Color(0xFF1A0B2E), surfaceVariant = Color(0xFF2D1B4E), onPrimary = Color.Black, onBackground = Color.White, onSurface = Color.White)
    }

    // Применяем кастомный цвет, если он есть
    if (customColorScheme != null) colorScheme = customColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}