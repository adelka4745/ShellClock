package com.sova.shellclock

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun UtilsScreen(
    scaleMultiplier: Float,
    onBack: () -> Unit
) {
    var currentUtility by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currencyText by remember { mutableStateOf("Загрузка...") }
    var factText by remember { mutableStateOf("Нажмите, чтобы получить факт") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://www.cbr-xml-daily.ru/daily_json.js")
                val conn = url.openConnection() as HttpURLConnection
                if (conn.responseCode == 200) {
                    val res = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(res).getJSONObject("Valute")
                    val usd = json.getJSONObject("USD").getDouble("Value")
                    val eur = json.getJSONObject("EUR").getDouble("Value")
                    currencyText = "USD: ${String.format("%.2f", usd)} ₽\nEUR: ${String.format("%.2f", eur)} ₽"
                } else currencyText = "Ошибка загрузки"
            } catch (e: Exception) { currencyText = "Ошибка сети" }
        }
    }

    if (currentUtility != null) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { currentUtility = null }) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") }
                Text(currentUtility!!, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            when (currentUtility) {
                "Фонарик" -> FlashlightScreen()
                "Компас" -> CompassScreen()
                "Заметки" -> NotesScreen()
                "Калькулятор" -> CalculatorScreen()
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 40.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") }
            Text("Утилиты", fontSize = (28 * scaleMultiplier).sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Курсы валют (ЦБ РФ)", fontSize = (18 * scaleMultiplier).sp, fontWeight = FontWeight.Bold)
                        Text(currencyText, fontSize = (16 * scaleMultiplier).sp)
                    }
                }
            }
            item {
                Card(modifier = Modifier.clickable {
                    scope.launch {
                        factText = "Загрузка..."
                        withContext(Dispatchers.IO) {
                            try {
                                val url = URL("https://uselessfacts.jsph.pl/api/v2/facts/random")
                                val conn = url.openConnection() as HttpURLConnection
                                if (conn.responseCode == 200) factText = JSONObject(conn.inputStream.bufferedReader().readText()).getString("text")
                                else factText = "Ошибка"
                            } catch (e: Exception) { factText = "Ошибка сети" }
                        }
                    }
                }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Случайный факт", fontSize = (18 * scaleMultiplier).sp, fontWeight = FontWeight.Bold)
                        Text(factText, fontSize = (16 * scaleMultiplier).sp)
                    }
                }
            }
            item { UtilsButton("Фонарик", "Вспышка", Icons.Default.FlashOn) { currentUtility = "Фонарик" } }
            item { UtilsButton("Компас", "Стороны света", Icons.Default.Explore) { currentUtility = "Компас" } }
            item { UtilsButton("Заметки", "Записи (.scn)", Icons.Default.Note) { currentUtility = "Заметки" } }
            item { UtilsButton("Калькулятор", "Вычисления", Icons.Default.Calculate) { currentUtility = "Калькулятор" } }
            item {
                Card(modifier = Modifier.fillMaxWidth().clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://radiopotok.ru/")))
                }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Радио", fontSize = (18 * scaleMultiplier).sp, fontWeight = FontWeight.Bold)
                            Text("Слушать Радиопоток", fontSize = (14 * scaleMultiplier).sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UtilsButton(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FlashlightScreen() {
    val context = LocalContext.current
    var isOn by remember { mutableStateOf(false) }
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraId = cameraManager.cameraIdList.firstOrNull()

    fun toggle() {
        if (cameraId == null) return
        try {
            cameraManager.setTorchMode(cameraId, !isOn)
            isOn = !isOn
        } catch (e: Exception) { }
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Button(onClick = { toggle() }, modifier = Modifier.size(150.dp)) {
            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(64.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(if (isOn) "Включен" else "Выключен", fontSize = 24.sp)
    }
}

@Composable
private fun CompassScreen() {
    val context = LocalContext.current
    var azimuth by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    val direction = when {
        azimuth >= -22.5 && azimuth < 22.5 -> "Север (С)"
        azimuth >= 22.5 && azimuth < 67.5 -> "Северо-Восток (СВ)"
        azimuth >= 67.5 && azimuth < 112.5 -> "Восток (В)"
        azimuth >= 112.5 && azimuth < 157.5 -> "Юго-Восток (ЮВ)"
        azimuth >= 157.5 || azimuth < -157.5 -> "Юг (Ю)"
        azimuth >= -157.5 && azimuth < -112.5 -> "Юго-Запад (ЮЗ)"
        azimuth >= -112.5 && azimuth < -67.5 -> "Запад (З)"
        azimuth >= -67.5 && azimuth < -22.5 -> "Северо-Запад (СЗ)"
        else -> "Север"
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Canvas(modifier = Modifier.size(300.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2 - 10
            drawCircle(Color.Gray, radius = radius, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
            drawContext.canvas.nativeCanvas.apply {
                drawText("С", center.x, center.y - radius + 30f, android.graphics.Paint().apply { color = android.graphics.Color.RED; textSize = 60f; textAlign = android.graphics.Paint.Align.CENTER })
                drawText("Ю", center.x, center.y + radius - 10f, android.graphics.Paint().apply { color = android.graphics.Color.WHITE; textSize = 60f; textAlign = android.graphics.Paint.Align.CENTER })
                drawText("В", center.x + radius - 10f, center.y + 20f, android.graphics.Paint().apply { color = android.graphics.Color.WHITE; textSize = 60f; textAlign = android.graphics.Paint.Align.CENTER })
                drawText("З", center.x - radius + 10f, center.y + 20f, android.graphics.Paint().apply { color = android.graphics.Color.WHITE; textSize = 60f; textAlign = android.graphics.Paint.Align.CENTER })
            }
            rotate(degrees = -azimuth, pivot = center) {
                val path = Path().apply {
                    moveTo(center.x, center.y - 80f); lineTo(center.x - 20f, center.y); lineTo(center.x + 20f, center.y); close()
                }
                drawPath(path, Color.Red)
                val path2 = Path().apply {
                    moveTo(center.x, center.y + 80f); lineTo(center.x - 20f, center.y); lineTo(center.x + 20f, center.y); close()
                }
                drawPath(path2, Color.White)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("${azimuth.toInt()}°", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(direction, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
    }
}

// --- ЗАМЕТКИ (.SCN БЕЗ VERSION) ---
@Composable
private fun NotesScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("notes_prefs", Context.MODE_PRIVATE)
    var text by remember { mutableStateOf(prefs.getString("note", "") ?: "") }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        uri?.let {
            try {
                val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                // Формат без VERSION
                val content = "[SHELLCLOCK NOTES]\nDATE: $date\n---\n$text"
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(content.toByteArray())
                }
            } catch (e: Exception) { }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                    reader.readText()
                } ?: ""
                if (content.startsWith("[SHELLCLOCK NOTES]")) {
                    val parts = content.split("---\n", limit = 2)
                    if (parts.size == 2) {
                        text = parts[1]
                        prefs.edit().putString("note", text).apply()
                    }
                } else {
                    text = content
                    prefs.edit().putString("note", text).apply()
                }
            } catch (e: Exception) { }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { exportLauncher.launch("note.scn") }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.FileUpload, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Экспорт .scn")
            }
            Button(onClick = { importLauncher.launch(arrayOf("*/*")) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Импорт .scn")
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it; prefs.edit().putString("note", text).apply() },
            modifier = Modifier.fillMaxSize(),
            label = { Text("Ваша заметка (.scn)...") }
        )
    }
}

@Composable
private fun CalculatorScreen() {
    var display by remember { mutableStateOf("0") }
    var currentInput by remember { mutableStateOf("") }
    var operator by remember { mutableStateOf<String?>(null) }
    var previousValue by remember { mutableStateOf<Double?>(null) }

    fun onDigit(digit: String) { if (display == "0") display = digit else display += digit; currentInput += digit }
    fun onOperator(op: String) {
        if (currentInput.isNotEmpty()) {
            if (previousValue != null && operator != null) {
                val result = calculate(previousValue!!, currentInput.toDoubleOrNull() ?: 0.0, operator!!)
                display = formatResult(result); previousValue = result
            } else previousValue = currentInput.toDoubleOrNull() ?: 0.0
            currentInput = ""
        }
        operator = op; display = formatResult(previousValue ?: 0.0) + " " + op
    }
    fun onEquals() {
        if (operator != null && previousValue != null && currentInput.isNotEmpty()) {
            val result = calculate(previousValue!!, currentInput.toDoubleOrNull() ?: 0.0, operator!!)
            display = formatResult(result); previousValue = result; currentInput = ""; operator = null
        }
    }
    fun onClear() { display = "0"; currentInput = ""; operator = null; previousValue = null }
    fun onDecimal() { if (!display.contains(".")) { display += "."; currentInput += "." } }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(display, fontSize = 48.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(16.dp), maxLines = 1)
        val buttons = listOf(
            listOf("C", "±", "%", "/"), listOf("7", "8", "9", "*"), listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"), listOf("0", ".", "=")
        )
        buttons.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { label ->
                    Button(
                        onClick = {
                            when (label) {
                                "C" -> onClear(); "±" -> display = (-(display.toDoubleOrNull() ?: 0.0)).toString()
                                "%" -> display = ((display.toDoubleOrNull() ?: 0.0) / 100).toString()
                                "/", "*", "-", "+" -> onOperator(label); "=" -> onEquals(); "." -> onDecimal()
                                else -> onDigit(label)
                            }
                        },
                        modifier = Modifier.fillMaxHeight().weight(1f),
                        colors = if (label in listOf("/", "*", "-", "+", "=")) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) { Text(label, fontSize = 24.sp) }
                }
            }
        }
    }
}

private fun calculate(a: Double, b: Double, op: String): Double = when (op) {
    "+" -> a + b; "-" -> a - b; "*" -> a * b; "/" -> if (b != 0.0) a / b else 0.0; else -> 0.0
}
private fun formatResult(value: Double): String = if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()