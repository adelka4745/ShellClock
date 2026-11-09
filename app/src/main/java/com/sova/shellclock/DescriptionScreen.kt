package com.sova.shellclock

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class GitHubRelease(
    val name: String,
    val tagName: String,
    val htmlUrl: String,
    val publishedAt: String
)

@Composable
fun DescriptionScreen(
    sizeMultiplier: Float,
    onBack: () -> Unit // <-- Кнопка назад
) {
    val context = LocalContext.current
    var releases by remember { mutableStateOf<List<GitHubRelease>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val repoUrl = "https://github.com/adelka4745/ShellClock"

    // Состояние для встроенного браузера
    var showEmbeddedBrowser by remember { mutableStateOf(false) }
    var embeddedUrl by remember { mutableStateOf("") }

    // --- ЛОГИКА ПАСХАЛКИ ---
    var clickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    fun handleEasterEggClick() {
        val currentTime = System.currentTimeMillis()
        // Если между кликами прошло больше 1.5 секунд, сбрасываем счетчик
        if (currentTime - lastClickTime > 1500) {
            clickCount = 0
        }
        clickCount++
        lastClickTime = currentTime

        if (clickCount == 7) {
            // БАМ! Пасхалка сработала — открываем Google
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
            context.startActivity(intent)
            clickCount = 0 // Сбрасываем
        }
    }
    // ------------------------

    // Если открыт встроенный браузер, показываем его
    if (showEmbeddedBrowser) {
        EmbeddedWebView(
            url = embeddedUrl,
            onClose = { showEmbeddedBrowser = false }
        )
        return
    }

    // Загрузка релизов
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
                        fetchedReleases.add(
                            GitHubRelease(
                                name = name.ifBlank { obj.getString("tag_name") },
                                tagName = obj.getString("tag_name"),
                                htmlUrl = obj.getString("html_url"),
                                publishedAt = obj.getString("published_at").take(10)
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

    LaunchedEffect(Unit) {
        fetchReleases()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // --- ЗАГОЛОВОК С КНОПКОЙ НАЗАД И ПАСХАЛКОЙ ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                }
                Text(
                    text = "ShellClock",
                    fontSize = (32 * sizeMultiplier).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable { handleEasterEggClick() }
                )
            }
        }
        item {
            Text(
                text = "Программа для просмотра времени",
                fontSize = (18 * sizeMultiplier).sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item {
            Text("Часы — часы из вашего часового пояса", fontSize = (16 * sizeMultiplier).sp)
        }
        item {
            Text("Мир — часы из других часовых поясов", fontSize = (16 * sizeMultiplier).sp)
        }
        item {
            Text("Секундомер — засечь время, например, скорость бега", fontSize = (16 * sizeMultiplier).sp)
        }
        item {
            Text("Таймер — идеально на ночь. Имеет звуки будильника системы.", fontSize = (16 * sizeMultiplier).sp)
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

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
                IconButton(onClick = { isLoading = true; error = null }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Обновить", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        if (error != null) {
            item {
                Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = (14 * sizeMultiplier).sp)
            }
        }

        // Карточки версий (открывают встроенный браузер)
        items(releases) { release ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    embeddedUrl = release.htmlUrl
                    showEmbeddedBrowser = true
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(release.name, fontSize = (16 * sizeMultiplier).sp, fontWeight = FontWeight.Medium)
                        Text("Опубликовано: ${release.publishedAt}", fontSize = (12 * sizeMultiplier).sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                    Icon(Icons.Default.OpenInBrowser, contentDescription = "Открыть", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Кнопка (открывает дефолтный браузер)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(repoUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Перейти на GitHub", fontSize = (16 * sizeMultiplier).sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EmbeddedWebView(url: String, onClose: () -> Unit) {
    BackHandler {
        onClose()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
            }
            Text("Просмотр релиза", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}