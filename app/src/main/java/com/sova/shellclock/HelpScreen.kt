package com.sova.shellclock

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HelpScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize()) {
        // Шапка с кнопкой назад
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 40.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Назад"))
            }
            Text(
                text = tr("Помощь"),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Ссылки на разработчика ---
            item { SectionHeader(tr("Ссылки"), 1.0f) }
            item {
                HelpButton(
                    title = "GitHub",
                    subtitle = "github.com/adelka4745",
                    icon = Icons.Default.OpenInBrowser,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/adelka4745")))
                }
            }
            item {
                HelpButton(
                    title = "Telegram",
                    subtitle = "@adelka4745",
                    icon = Icons.Default.Send,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/adelka4745")))
                }
            }

            // --- Утилиты для Android ---
            item { SectionHeader(tr("Утилиты для Android"), 1.0f) }
            item {
                HelpButton(
                    title = tr("Настройки Android"),
                    subtitle = tr("Открыть системные настройки"),
                    icon = Icons.Default.Settings,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
                }
            }
            item {
                HelpButton(
                    title = tr("Настройки приложения"),
                    subtitle = tr("Разрешения, уведомления, батарея"),
                    icon = Icons.Default.Apps,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:${context.packageName}")
                    context.startActivity(intent)
                }
            }
            item {
                HelpButton(
                    title = tr("Информация о телефоне"),
                    subtitle = tr("Версия Android, модель и т.д."),
                    icon = Icons.Default.PhoneAndroid,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    context.startActivity(Intent(android.provider.Settings.ACTION_DEVICE_INFO_SETTINGS))
                }
            }
            item {
                HelpButton(
                    title = tr("Язык и ввод"),
                    subtitle = tr("Сменить язык системы"),
                    icon = Icons.Default.Language,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    context.startActivity(Intent(android.provider.Settings.ACTION_LOCALE_SETTINGS))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, scale: Float) {
    Text(
        text = title,
        fontSize = (16 * scale).sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun HelpButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}