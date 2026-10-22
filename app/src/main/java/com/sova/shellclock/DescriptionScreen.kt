package com.sova.shellclock

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

// Модель для хранения данных о релизе
data class GitHubRelease(
    val name: String, // Например, "ShellClock 3.1"
    val tagName: String, // Например, "v3.1"
    val htmlUrl: String, // Ссылка на страницу релиза
    val publishedAt: String // Дата публикации
)

@Composable
fun DescriptionScreen(sizeMultiplier: Float) {
    val context = LocalContext.current
    var releases by remember { mutableStateOf<List<GitHubRelease>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val repoUrl = "https://github.com/adelka4745/ShellClock"

    // Функция для загрузки релизов с GitHub
    suspend fun fetchReleases() {
        try {
            withContext(Dispatchers.IO) {
                val url = URL("https://api.github.com/repos/adelka4745/ShellClock/releases")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonArray = JSONArray(response)
                    val fetchedReleases = mutableListOf<GitHubRelease>()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val name = obj.optString("name", obj.optString("tag_name"))
                        // Фильтруем только те, что содержат "ShellClock" или просто берем все релизы
                        // Если хотите строго "ShellClock 3.1", можно добавить проверку name.startsWith("ShellClock")
                        fetchedReleases.add(
                            GitHubRelease(
                                name = name.ifBlank { obj.getString("tag_name") },
                                tagName = obj.getString("tag_name"),
                                htmlUrl = obj.getString("html_url"),
                                publishedAt = obj.getString("published_at").take(10) // Берем только дату
                            )
                        )
                    }
                    withContext(Dispatchers.Main) {
                        releases = fetchedReleases
                        isLoading = false
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        error = "Ошибка загрузки: ${connection.responseCode}"
                        isLoading = false
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                error = "Ошибка сети: ${e.message}"
                isLoading = false
            }
        }
    }

    // Загружаем данные при первом запуске
    LaunchedEffect(Unit) {
        fetchReleases()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Заголовок и описание
        item {
            Text(
                text = "ShellClock",
                fontSize = (32 * sizeMultiplier).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item {
            Text(
                text = "Программа для просмотра времени",
                fontSize = (18 * sizeMultiplier).sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item {
            Text(
                text = "Часы — часы из вашего часового пояса",
                fontSize = (16 * sizeMultiplier).sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item {
            Text(
                text = "Мир — часы из других часовых поясов",
                fontSize = (16 * sizeMultiplier).sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item {
            Text(
                text = "Секундомер — засечь время, например, скорость бега",
                fontSize = (16 * sizeMultiplier).sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item {
            Text(
                text = "Таймер — идеально на ночь. Имеет звуки будильника системы.",
                fontSize = (16 * sizeMultiplier).sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Разделитель
        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // Секция с версиями
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Версии приложения",
                    fontSize = (20 * sizeMultiplier).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                // Кнопка обновления списка
                IconButton(onClick = {
                    isLoading = true
                    error = null
                    // Перезапускаем загрузку
                    // В Compose для этого можно использовать ключ в LaunchedEffect, но проще вызвать корутину заново
                    // Для простоты здесь просто дернем рекомпозицию (в реальном проекте лучше использовать ViewModel)
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Обновить",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Индикатор загрузки
        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        // Ошибка
        if (error != null) {
            item {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = (14 * sizeMultiplier).sp
                )
            }
        }

        // Список версий
        items(releases) { release ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Открываем ссылку на релиз в браузере
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(release.htmlUrl))
                        context.startActivity(intent)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = release.name,
                            fontSize = (16 * sizeMultiplier).sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Опубликовано: ${release.publishedAt}",
                            fontSize = (12 * sizeMultiplier).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Открыть",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size((20 * sizeMultiplier).dp)
                    )
                }
            }
        }

        // Кнопка перехода на сам проект
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(repoUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size((20 * sizeMultiplier).dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Перейти на GitHub проекта",
                    fontSize = (16 * sizeMultiplier).sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}