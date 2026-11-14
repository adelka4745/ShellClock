package com.sova.shellclock

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun UtilsScreen(
    scaleMultiplier: Float,
    onBack: () -> Unit,
    onOpenAi: () -> Unit, // Оставляем на всякий случай, но используем внутренний
    onOpenScreen: (Screen) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    timeZone: java.util.TimeZone = java.util.TimeZone.getDefault(),
    timeZoneLabel: String = "системный"
) {
    var currentUtility by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currencyText by remember { mutableStateOf("Загрузка...") }

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

    // Если открыта вкладка утилиты (AI, Фонарик и т.д.)
    if (currentUtility != null) {
        // Шапка нужна, чтобы свои заголовки не дублировались с названием раздела
        val showHeader = currentUtility != "AI"
        Column(modifier = Modifier.fillMaxSize()) {
            if (showHeader) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { currentUtility = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад") }
                    Text(currentUtility!!, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            when (currentUtility) {
                "AI" -> AiScreenInternal(
                    onBack = { currentUtility = null },
                    onOpenScreen = { currentUtility = null; onOpenScreen(it) },
                    onOpenUtility = { currentUtility = it },
                    onOpenSettings = { currentUtility = null; onOpenSettings() },
                    timeZone = timeZone,
                    timeZoneLabel = timeZoneLabel
                )
                "Фонарик" -> FlashlightScreen()
                "Компас" -> CompassScreen()
                "Заметки" -> NotesScreen()
                "Калькулятор" -> CalculatorScreen()
                "Мемы" -> MemeScreen()
            }
        }
        return
    }

    // Главный экран Утилит
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 40.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Назад")) }
            Text(tr("Утилиты"), fontSize = (28 * scaleMultiplier).sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // AI (внутренний вызов)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { currentUtility = "AI" },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("ShellClock AI", fontSize = (18 * scaleMultiplier).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Управление, поиск, команды", fontSize = (12 * scaleMultiplier).sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            // Курсы валют
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(tr("Курсы валют (ЦБ РФ)"), fontSize = (18 * scaleMultiplier).sp, fontWeight = FontWeight.Bold)
                        Text(currencyText, fontSize = (16 * scaleMultiplier).sp)
                    }
                }
            }

            // Зал Славы Мемов
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { currentUtility = "Мемы" },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SentimentVerySatisfied, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(tr("Зал Славы Мемов"), fontSize = (18 * scaleMultiplier).sp, fontWeight = FontWeight.Bold)
                            Text(tr("Золотая коллекция мемов и рофлов"), fontSize = (14 * scaleMultiplier).sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Утилиты
            item { UtilsButton(tr("Зал Славы Мемов"), tr("Все мемы и рофлы"), Icons.Default.SentimentVerySatisfied) { currentUtility = "Мемы" } }
            item { UtilsButton(tr("Фонарик"), tr("Включить/выключить вспышку"), Icons.Default.FlashOn) { currentUtility = "Фонарик" } }
            item { UtilsButton(tr("Компас"), tr("Стороны света"), Icons.Default.Explore) { currentUtility = "Компас" } }
            item { UtilsButton(tr("Заметки"), tr("Быстрые записи"), Icons.Default.Note) { currentUtility = "Заметки" } }
            item { UtilsButton(tr("Калькулятор"), tr("Простые вычисления"), Icons.Default.Calculate) { currentUtility = "Калькулятор" } }

            // Радио
            item {
                Card(modifier = Modifier.fillMaxWidth().clickable {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://radiopotok.ru/"))) } catch (e: Exception) { }
                }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(tr("Радио"), fontSize = (18 * scaleMultiplier).sp, fontWeight = FontWeight.Bold)
                            Text(tr("Слушать онлайн (Радиопоток)"), fontSize = (14 * scaleMultiplier).sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ВСТРОЕННЫЙ AI (Внутри UtilsScreen)
// ==========================================

// Убираем знаки препинания и лишние пробелы, чтобы «Камера?!» и «камера» давали одно и то же
private fun normalizeCommand(text: String): String = text.lowercase()
    .replace(Regex("[?!.,;:\"«»()]"), " ")
    .replace(Regex("\\s+"), " ")
    .trim()

private fun searchUrl(query: String): String = "https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8")

private fun plural(value: Int, one: String, few: String, many: String): String = when {
    value % 10 == 1 && value % 100 != 11 -> one
    value % 10 in 2..4 && value % 100 !in 12..14 -> few
    else -> many
}

// Убираем служебные слова поиска из любого места фразы, а не только с начала
private fun cleanupQuestion(text: String): String = text
    .replace(Regex("(?i)\\b(найди|поищи|поиск|что такое|расскажи про|расскажи о|кто такой|кто такая|объясни)\\b"), " ")
    .replace(Regex("\\s+"), " ")
    .trim()

// Маленький разборщик арифметики: только + - * / и скобки, без eval
private fun evaluateExpression(text: String): Double? {
    val expression = text.lowercase()
        .replace("сколько будет", " ")
        .replace("посчитай", " ")
        .replace("равно", " ")
        .replace("= ?".toRegex(), " ")
        .replace("х", "*")
        .replace(",", ".")
        .replace(" ", "")
        .trim()

    // Слишком длинное или содержащее буквы — не арифметика
    if (expression.length !in 3..40 || (!expression.contains("+") && !expression.contains("-") && !expression.contains("*") && !expression.contains("/"))) return null
    if (!expression.all { it.isDigit() || it in "+-*/()." }) return null
    if (expression.any { it == '.' } && Regex("""\d+\.\d+\.\d+""").containsMatchIn(expression)) return null

    var position = 0

    fun parseNumber(): Double? {
        val start = position
        while (position < expression.length && (expression[position].isDigit() || expression[position] == '.')) position++
        return expression.substring(start, position).toDoubleOrNull()
    }

    lateinit var parseSum: () -> Double?

    fun parseFactor(): Double? {
        // Унарный минус: «-5 * -2»
        if (position < expression.length && (expression[position] == '-' || expression[position] == '+')) {
            val negative = expression[position] == '-'
            position++
            val value = parseFactor() ?: return null
            return if (negative) -value else value
        }
        if (position < expression.length && expression[position] == '(') {
            position++
            val value = parseSum() ?: return null
            if (position >= expression.length || expression[position] != ')') return null
            position++
            return value
        }
        return parseNumber()
    }

    fun parseProduct(): Double? {
        var value = parseFactor() ?: return null
        while (position < expression.length && (expression[position] == '*' || expression[position] == '/')) {
            val op = expression[position++]
            val right = parseFactor() ?: return null
            value = if (op == '*') value * right else if (right == 0.0) return null else value / right
        }
        return value
    }

    parseSum = sum@{
        var value = parseProduct() ?: return@sum null
        while (position < expression.length && (expression[position] == '+' || expression[position] == '-')) {
            val op = expression[position++]
            val right = parseProduct() ?: return@sum null
            value = if (op == '+') value + right else value - right
        }
        value
    }

    val result = try { parseSum() } catch (e: Exception) { null }
    return if (result != null && position == expression.length && !result.isNaN() && !result.isInfinite()) result else null
}

private val AI_GREETINGS = listOf("привет", "приветик", "здравствуй", "здравствуйте", "хай", "hello", "hi", "доброе утро", "добрый день", "добрый вечер", "здрасьте", "здарова", "добрый", "приветики", "йо", "hey", "greetings")
private val AI_THANKS = listOf("спасибо", "благодарю", "thanks", "thank you", "спс", "спасибочки", "большое спасибо", "благодарен", "thx", "merci", "спб")
private val AI_FAREWELLS = listOf("пока", "до свидания", "бб", "bye", "до встречи", "покасики", "до завтра", "прощай", "увидимся", "goodbye")

// Справочники: общие сведения, которые не зависят от состояния телефона и не требуют сети
private val AI_FACTS = listOf(
    // --- ПРОГРАММИРОВАНИЕ И ИТ ---
    "котлин" to "Kotlin — современный лаконичный язык программирования от JetBrains, являющийся официальным языком разработки для Android.",
    "питон" to "Python — высокоуровневый язык программирования с чистым синтаксисом, широко применяемый в Data Science, ИИ и веб-разработке.",
    "джава" to "Java — надежный объектно-ориентированный язык программирования, работающий на миллиардах устройств благодаря JVM.",
    "си плюс плюс" to "C++ — мощный компилируемый язык высокого и низкого уровня, применяемый в системном ПО, играх и движках.",
    "html" to "HTML (HyperText Markup Language) — язык гипертекстовой разметки для создания структуры веб-страниц.",
    "css" to "CSS (Cascading Style Sheets) — каскадные таблицы стилей для оформления внешнего вида и верстки веб-страниц.",
    "sql" to "SQL — язык декларативных запросов для работы с реляционными базами данных (SQLite, PostgreSQL, MySQL).",
    "git" to "Git — распределенная система контроля версий исходного кода (команды: commit, push, pull, branch, merge).",
    "ооп" to "ООП (Объектно-Ориентированное Программирование) строится на 4 столпах: Инкапсуляция, Наследование, Полиморфизм, Абстракция.",
    "алгоритм" to "Алгоритм — точный набор инструкций, описывающих последовательность действий для решения задачи.",
    "структуры данных" to "Основные структуры данных: Массивы, Связные списки, Стеки (LIFO), Очереди (FIFO), Деревья, Хеш-таблицы и Графы.",
    "json" to "JSON (JavaScript Object Notation) — легкий текстовый формат обмена данными, основанный на парах ключ-значение.",
    "rest api" to "REST API — архитектурный стиль взаимодействия клиентов и серверов по протоколу HTTP (GET, POST, PUT, DELETE).",

    // --- ТЕХНИКА И ГАДЖЕТЫ ---
    "процессор" to "Процессор (CPU) — главный вычислительный чип устройства, выполняющий миллиарды операций в секунду.",
    "видеокарта" to "Видеокарта (GPU) — специализированный процессор для обработки 3D-графики, видео и параллельных вычислений.",
    "оперативная память" to "ОЗУ (RAM) — быстрая энергозависимая память для временного хранения данных запущенных программ.",
    "накопитель" to "SSD — твердотельный накопитель на базе флеш-памяти, работающий в разы быстрее традиционных жестких дисков (HDD).",
    "экран" to "Дисплеи бывают OLED (органические светодиоды с глубоким черным) и IPS (натуральные цвета и широкие углы обзора).",
    "аккумулятор" to "В смартфонах используются Li-Ion и Li-Polymer аккумуляторы. Для долговременности не перегревайте устройство.",
    "wi-fi" to "Wi-Fi — стандарт беспроводной связи (Wi-Fi 6/6E/7) для локальной передачи данных на высоких скоростях.",
    "bluetooth" to "Bluetooth — технология беспроводной связи малого радиуса для подключения наушников, часов и аксессуаров.",
    "5g" to "5G — пятое поколение мобильной связи с высокими скоростями (до 10 Гбит/с) и минимальной задержкой сигнала.",
    "nfc" to "NFC — бесконтактная технология связи на расстоянии до 10 см для оплаты и передачи данных.",
    "usb-c" to "USB Type-C — универсальный симметричный разъем для быстрой зарядки, передачи файлов и вывода видео.",

    // --- ЕДА И КУЛИНАРИЯ ---
    "пицца" to "Пицца — итальянское блюдо на тонком или пышном тесте с томатным соусом, моцареллой и разнообразными начинками.",
    "суши" to "Суши и роллы — традиционные блюда японской кухни из риса, морепродуктов, нори и свежей рыбы.",
    "паста" to "Итальянская паста производится из твердых сортов пшеницы (карбонара, болоньезе, феттуччине, пенне).",
    "кофе" to "Кофе содержит кофеин для бодрости. Популярные напитки: Эспрессо, Капучино, Латте, Американо, Флэт Уайт.",
    "чай" to "Чай богат антиоксидантами. Основные виды: Зеленый, Черный, Белый, Улун, Пуэр и Травяной.",
    "борщ" to "Борщ — знаменитый свекольный суп с мясом, капустой, картофелем, зеленью и сметаной.",
    "калории" to "Калория — единица энергии. Белки и углеводы содержат по 4 ккал/г, жиры — 9 ккал/г.",
    "витамины" to "Витамины — жизненно важные органические соединения (A, B-комплекс, C, D, E, K) для работы организма.",

    // --- СЛОВАРЬ ОТ А ДО Я ---
    "атом" to "Атом — микроскопическая частица вещества, состоящая из ядра (протоны, нейтроны) и электронов.",
    "байт" to "Байт — базовая единица хранения цифровой информации, равная 8 битам.",
    "вода" to "Вода (H₂O) — прозрачная жидкость, покрывающая 71% поверхности Земли и необходимая для жизни.",
    "гравитация" to "Гравитация — фундаментальная сила притяжения между всеми объектами, имеющими массу.",
    "днк" to "ДНК — молекула, хранящая и передающая генетическую информацию живых организмов.",
    "единица измерения" to "Основные единицы системы СИ: Метр (длина), Килограмм (масса), Секунда (время), Ампер (ток).",
    "жиры" to "Жиры необходимы организму для запаса энергии, строения клеток и усвоения витаминов A, D, E, K.",
    "звезда" to "Звезда — массивный газовый шар, излучающий свет благодаря термоядерным реакциям (как наше Солнце).",
    "интернет" to "Интернет — глобальная сеть объединенных компьютеров, работающая по протоколу TCP/IP.",
    "калория" to "Калория — единица измерения энергии, получаемой организмом из пищи.",
    "лазер" to "Лазер — когерентный источник узконаправленного светового луча высокой интенсивности.",
    "молекула" to "Молекула — наименьшая частица вещества, сохраняющая его химические свойства.",
    "нейросеть" to "Нейросеть — алгоритм машинного обучения, моделирующий работу биологических нейронов головного мозга.",
    "океан" to "На Земле 5 океанов: Тихий (самый большой), Атлантический, Индийский, Северный Ледовитый и Южный.",
    "протокол" to "Сетевой протокол — набор правил и соглашений для передачи данных между устройствами.",
    "робот" to "Робот — автоматическое устройство, действующее по заданной программе и выполняющее задачи.",
    "солнце" to "Солнце — центральная звезда нашей системы, находящаяся на расстоянии 149.6 млн км от Земли.",
    "транзистор" to "Транзистор — полупроводниковый радиоэлектронный элемент для управления электрическим током.",
    "углеводы" to "Углеводы — главный источник быстрого и медленного топлива для мышц и мозга.",
    "физика" to "Физика — естественная наука о свойствах материи, энергии, пространстве и времени.",
    "химия" to "Химия — наука о строении веществ, их превращениях и химических реакциях.",
    "цикл" to "В программировании цикл (for, while) повторяет блок кода заданное число раз или до выполнения условия.",
    "часы" to "Часы — прибор для определения времени. В физике секунда базируется на колебаниях атома цезия-133.",
    "шифрование" to "Шифрование — процесс криптографического преобразования информации для защиты от чтения.",
    "электричество" to "Электричество — направленное движение заряженных частиц (электронов) под действием поля.",
    "юникод" to "Unicode — универсальный стандарт кодирования текста, поддерживающий символы всех языков мира.",
    "ядро" to "Ядро — центральная часть атома, клетки организма или операционной системы (например, ядро Linux).",

    // --- ОБЩИЕ ФАКТЫ И ВОПРОСЫ ---
    "ты кто" to "Я ShellClock AI — встроенный помощник ShellClock. Живу офлайн: открываю разделы и утилиты, ставлю будильники, пишу код, знаю факты и ищу ответы в интернете.",
    "что ты умеешь" to "Открываю разделы и утилиты, ставлю будильник, пишу код на Kotlin/Python/C++/Java/HTML, знаю всё про технику, еду, науку и словари от А до Я!",
    "сколько планет" to "В Солнечной системе 8 планет: Меркурий, Венера, Земля, Марс, Юпитер, Сатурн, Уран и Нептун. Плутон — карликовая планета.",
    "сколько дней в году" to "В обычном году 365 дней, в високосном — 366.",
    "сколько часов в сутках" to "В сутках 24 часа, 1440 минут и 86400 секунд.",
    "столица россии" to "Москва — столица России, крупнейший город страны.",
    "столица беларуси" to "Минск — столица Беларуси.",
    "столица казахстана" to "Астана — столица Казахстана.",
    "столица франции" to "Париж — столица Франции.",
    "столица японии" to "Токио — столица Японии.",
    "столица сша" to "Вашингтон — столица США.",
    "самая большая планета" to "Юпитер — самая большая планета Солнечной системы.",
    "самая большая гора" to "Эверест (Джомолунгма) — 8849 метров над уровнем моря.",
    "самое глубокое место" to "Марианский жёлоб в Тихом океане: около 11 000 метров глубины.",
    "скорость света" to "Скорость света в вакууме равна 299 792 458 м/с (около 300 000 км/с).",
    "скорость звука" to "Скорость звука в воздухе при 20°C составляет 343 м/с (1235 км/ч).",
    "число пи" to "Число Пи (π) — математическая константа, приблизительно равная 3.14159.",
    "какой сейчас год" to "Идёт ${Calendar.getInstance().get(Calendar.YEAR)} год.",
    "какой сейчас месяц" to "Сейчас ${SimpleDateFormat("MMMM", Locale("ru")).format(Date())}.",
    "какой сегодня день недели" to "Сегодня ${SimpleDateFormat("EEEE", Locale("ru")).format(Date())}.",
    "сколько сейчас времени" to "Сейчас ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}.",
    "как дела" to "Всё отлично — тикаю, считаю и пишу код. А у вас?",
    "как тебя зовут" to "ShellClock AI. Я встроен в приложение ShellClock и работаю офлайн.",
    "что такое shellclock" to "ShellClock — приложение с часами, мировым временем, будильниками, секундомером, таймером, утилитами и AI.",
    "искусственный интеллект" to "Искусственный интеллект (ИИ) — свойство интеллектуальных систем выполнять творческие функции и решать сложные задачи.",
    "квантовый компьютер" to "Квантовый компьютер использует кубиты для квантовых вычислений, решая сложные задачи в миллионы раз быстрее обычных ПК.",
    "черная дыра" to "Чёрная дыра — область пространства-времени с колоссальной гравитацией, из которой не может вырваться даже свет.",
    "млечный путь" to "Млечный Путь — наша галактика, спиральная система, содержащая сотни миллиардов звёзд.",
    "андромеда" to "Туманность Андромеды — гигантская спиральная галактика, ближайшая к Млечному Пути соседка.",
    "марианская впадина" to "Марианский желоб — глубочайшая океаническая впадина на Земле (около 11 034 метров глубиной).",
    "эверест" to "Эверест (Джомолунгма) — высочайшая точка планеты на границе Непала и Китая (8848.86 метров).",
    "солнечная система" to "Солнечная система образовалась около 4.57 миллиардов лет назад и состоит из Солнца и обращающихся вокруг него тел.",
    "время" to "Время — необратимая координата физического пространства-времени, определяющая последовательность событий.",
    "таймер" to "Таймер — прибор для отсчета заданных интервалов времени с последующим звуковым сигналом.",
    "секундомер" to "Секундомер позволяет измерять время с точностью до миллисекунд.",
    "компас" to "Компас указывает на магнитные полюса Земли благодаря намагниченной стрелке."
)

private data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val text: String,
    val isUser: Boolean
)

// --- Сохранение и экспорт/импорт диалогов ИИ (.scaidialogs) ---
private fun serializeDialog(messages: List<AiChatMessage>): String {
    val json = JSONObject()
    json.put("version", 1)
    json.put("type", "scaidialogs")
    json.put("timestamp", System.currentTimeMillis())
    val array = JSONArray()
    messages.forEach { msg ->
        val item = JSONObject()
        item.put("sender", msg.sender)
        item.put("text", msg.text)
        item.put("isUser", msg.isUser)
        array.put(item)
    }
    json.put("messages", array)
    return json.toString(2)
}

private fun deserializeDialog(text: String): List<AiChatMessage> = try {
    val messages = mutableListOf<AiChatMessage>()
    val json = JSONObject(text)
    val array = json.getJSONArray("messages")
    for (i in 0 until array.length()) {
        val item = array.getJSONObject(i)
        messages.add(
            AiChatMessage(
                sender = item.getString("sender"),
                text = item.getString("text"),
                isUser = item.getBoolean("isUser")
            )
        )
    }
    messages
} catch (e: Exception) {
    emptyList()
}

private fun saveDialogToLocal(context: Context, messages: List<AiChatMessage>) {
    try {
        val file = File(context.filesDir, "ai_current_dialog.scaidialogs")
        file.writeText(serializeDialog(messages))
    } catch (e: Exception) { }
}

private fun loadDialogFromLocal(context: Context): List<AiChatMessage> {
    return try {
        val file = File(context.filesDir, "ai_current_dialog.scaidialogs")
        if (file.exists()) deserializeDialog(file.readText()) else emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

@Composable
private fun AiScreenInternal(
    onBack: () -> Unit,
    onOpenScreen: (Screen) -> Unit,
    onOpenUtility: (String) -> Unit,
    onOpenSettings: () -> Unit,
    timeZone: java.util.TimeZone,
    timeZoneLabel: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var prediction by remember { mutableStateOf("") }
    var showBrowser by remember { mutableStateOf(false) }
    var browserUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var lastQuery by remember { mutableStateOf("") }

    val chatHistory = remember { mutableStateListOf<AiChatMessage>() }

    LaunchedEffect(Unit) {
        if (chatHistory.isEmpty()) {
            val saved = loadDialogFromLocal(context)
            if (saved.isNotEmpty()) chatHistory.addAll(saved)
        }
    }

    fun addAiResponse(reply: String) {
        chatHistory.add(AiChatMessage(sender = "ShellClock AI", text = reply, isUser = false))
        saveDialogToLocal(context, chatHistory)
    }

    fun predictNextWord(text: String): String {
        val words = text.trim().lowercase().split(" ").filter { it.isNotBlank() }
        val lastWord = words.lastOrNull() ?: return ""
        return wordFrequency[lastWord]?.maxByOrNull { it.value }?.key ?: ""
    }

    fun fetchWikipediaSummary(query: String): String? = try {
        val encoded = URLEncoder.encode(query, "UTF-8").replace("+", "%20")
        val conn = (URL("https://ru.wikipedia.org/api/rest_v1/page/summary/$encoded").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 6000
            readTimeout = 6000
            setRequestProperty("User-Agent", "ShellClockAI/1.0")
        }
        if (conn.responseCode == 200) {
            val extract = JSONObject(conn.inputStream.bufferedReader().readText()).optString("extract", "")
            extract.ifBlank { null }
        } else null
    } catch (e: Exception) {
        null
    }

    fun ask(question: String) {
        scope.launch {
            isLoading = true
            withContext(Dispatchers.IO) {
                lastQuery = question
                val normalized = normalizeCommand(question)
                val expression = evaluateExpression(question)
                val fact = AI_FACTS.firstOrNull { (key, _) -> normalized.contains(key.lowercase()) }?.second

                when {
                    expression != null -> addAiResponse("$question = ${formatResult(expression)}" + (fact?.let { "\n\n$it" } ?: ""))
                    fact != null -> addAiResponse(fact)
                    else -> {
                        val article = fetchWikipediaSummary(question)
                        if (article != null) {
                            addAiResponse(article)
                        } else {
                            addAiResponse("Открываю поиск в Google по вашему запросу...")
                            browserUrl = searchUrl(question)
                            showBrowser = true
                        }
                    }
                }
            }
            isLoading = false
        }
    }

    fun executeCommand(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        chatHistory.add(AiChatMessage(sender = "Вы", text = trimmed, isUser = true))
        saveDialogToLocal(context, chatHistory)
        inputText = ""
        prediction = ""

        val normalized = normalizeCommand(trimmed)

        // Контекст браузера
        if (normalized.contains("открой в браузере") || normalized.contains("гугл") || normalized.contains("google") || normalized.contains("найди в инете")) {
            val queryToSearch = if (lastQuery.isNotEmpty()) lastQuery else trimmed
            browserUrl = searchUrl(queryToSearch)
            showBrowser = true
            addAiResponse("Открываю браузер по запросу: $queryToSearch")
            return
        }

        // Программирование и код
        val isCodeReq = normalized.contains("код") || normalized.contains("программ") || normalized.contains("напиши") || normalized.contains("скрипт") || normalized.contains("как написать")
        if (isCodeReq) {
            when {
                normalized.contains("python") || normalized.contains("пайтон") || normalized.contains("питон") -> {
                    addAiResponse("""
                        # Пример кода на Python
                        def greet(name):
                            return f"Привет, {name}! Код от ShellClock AI."

                        def fibonacci(n):
                            a, b = 0, 1
                            res = []
                            for _ in range(n):
                                res.append(a)
                                a, b = b, a + b
                            return res

                        print(greet("Разработчик"))
                        print("Числа Фибоначчи:", fibonacci(8))
                    """.trimIndent())
                    return
                }
                normalized.contains("kotlin") || normalized.contains("котлин") -> {
                    addAiResponse("""
                        // Пример кода на Kotlin
                        fun main() {
                            val name = "Разработчик"
                            println("Привет, ${'$'}name! Код от ShellClock AI.")
                            
                            val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                            val evenSum = numbers.filter { it % 2 == 0 }.sum()
                            println("Сумма чётных чисел: ${'$'}evenSum")
                        }
                    """.trimIndent())
                    return
                }
                normalized.contains("java") || normalized.contains("джава") -> {
                    addAiResponse("""
                        // Пример кода на Java
                        public class Main {
                            public static void main(String[] args) {
                                System.out.println("Привет от ShellClock AI на Java!");
                                int[] nums = {10, 20, 30, 40, 50};
                                int sum = 0;
                                for (int n : nums) sum += n;
                                System.out.println("Сумма: " + sum);
                            }
                        }
                    """.trimIndent())
                    return
                }
                normalized.contains("c++") || normalized.contains("си плюс плюс") || normalized.contains("плюс плюс") -> {
                    addAiResponse("""
                        // Пример кода на C++
                        #include <iostream>
                        #include <vector>

                        int main() {
                            std::cout << "Привет от ShellClock AI на C++!" << std::endl;
                            std::vector<int> vec = {1, 2, 3, 4, 5};
                            for (int v : vec) {
                                std::cout << "Элемент: " << v * v << std::endl;
                            }
                            return 0;
                        }
                    """.trimIndent())
                    return
                }
                normalized.contains("html") || normalized.contains("css") || normalized.contains("верстк") -> {
                    addAiResponse("""
                        <!DOCTYPE html>
                        <html lang="ru">
                        <head>
                            <meta charset="UTF-8">
                            <title>ShellClock AI Page</title>
                            <style>
                                body { font-family: sans-serif; background: #121212; color: #fff; padding: 20px; }
                                .card { background: #1e1e1e; padding: 20px; border-radius: 12px; }
                            </style>
                        </head>
                        <body>
                            <div class="card">
                                <h1>ShellClock AI</h1>
                                <p>Сгенерированная веб-страница!</p>
                            </div>
                        </body>
                        </html>
                    """.trimIndent())
                    return
                }
                normalized.contains("sql") || normalized.contains("баз") -> {
                    addAiResponse("""
                        -- Пример SQL запроса
                        SELECT category, COUNT(*) as total, AVG(price) as avg_price
                        FROM products
                        WHERE status = 'active'
                        GROUP BY category
                        ORDER BY total DESC;
                    """.trimIndent())
                    return
                }
            }
        }

        fun userTime(formant: String): String {
            val calendar = java.util.Calendar.getInstance(timeZone)
            val format = java.text.SimpleDateFormat(formant, java.util.Locale("ru"))
            format.timeZone = timeZone
            return format.format(calendar.time)
        }

        when {
            AI_GREETINGS.any { normalized == it || normalized.startsWith("$it ") } -> {
                addAiResponse("Привет! Я ShellClock AI. Могу написать код (Kotlin, Python, Java, C++, HTML, SQL), открыть утилиту или раздел, поставить будильник, рассказать факты от А до Я или найти ответ в Google!")
                return
            }
            AI_THANKS.any { normalized == it || normalized.startsWith("$it ") } -> {
                addAiResponse("Пожалуйста! Обращайтесь ещё!")
                return
            }
            AI_FAREWELLS.any { normalized == it } -> {
                addAiResponse("До свидания! Всегда к вашим услугам!")
                return
            }
        }

        // Продвинутый парсер и установка реального будильника (вместо заглушки)
        val wantsAlarm = normalized.contains("будильник") || normalized.startsWith("разбуди") || normalized.contains("поставь будильник")
        if (wantsAlarm) {
            // 1. Относительное время (через X минут/часов/секунд)
            val relMinMatch = Regex("""через\s+(\d+)\s*(мин|минут|минуту|минуты)""").find(normalized)
            val relHourMatch = Regex("""через\s+(\d+)\s*(час|часа|часов)""").find(normalized)
            val relSecMatch = Regex("""через\s+(\d+)\s*(сек|секунд|секунду|секунды)""").find(normalized)

            if (relMinMatch != null || relHourMatch != null || relSecMatch != null) {
                val addMin = relMinMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val addHour = relHourMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val addSec = relSecMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0

                val cal = Calendar.getInstance(timeZone).apply {
                    add(Calendar.HOUR_OF_DAY, addHour)
                    add(Calendar.MINUTE, addMin)
                    add(Calendar.SECOND, addSec)
                }
                val h = cal.get(Calendar.HOUR_OF_DAY)
                val m = cal.get(Calendar.MINUTE)
                val s = cal.get(Calendar.SECOND)

                addAlarm(context, h, m, s)
                addAiResponse("⏰ Будильник успешно установлен через ${if (addHour > 0) "$addHour ч. " else ""}${if (addMin > 0) "$addMin мин. " else ""}${if (addSec > 0) "$addSec сек." else ""} — на %02d:%02d:%02d.".format(h, m, s))
                return
            }

            // 2. Абсолютное время (HH:MM:SS, HH:MM или часы)
            val timeMatch = Regex("""(\d{1,2})[:.\-](\d{2})(?:[:\-](\d{2}))?""").find(normalized)
            val hourOnlyMatch = Regex("""\b(в|на)?\s*(\d{1,2})\s*(час|часа|часов)?\b""").find(normalized)

            var hour = 0
            var minute = 0
            var second = 0
            var parsed = false

            if (timeMatch != null) {
                hour = timeMatch.groupValues[1].toIntOrNull() ?: 0
                minute = timeMatch.groupValues[2].toIntOrNull() ?: 0
                second = timeMatch.groupValues[3].toIntOrNull() ?: 0
                parsed = true
            } else if (hourOnlyMatch != null) {
                hour = hourOnlyMatch.groupValues[2].toIntOrNull() ?: 0
                minute = 0
                second = 0
                parsed = true
            }

            if (parsed) {
                val isEvening = normalized.contains("вечер") || normalized.contains("вечера") || (normalized.contains("ночи") && hour < 12)
                val isDay = normalized.contains("дня") || normalized.contains("день")
                val isMorning = normalized.contains("утра") || normalized.contains("утро")

                if ((isEvening || isDay) && hour < 12) {
                    hour += 12
                } else if (isMorning && hour == 12) {
                    hour = 0
                }

                if (hour > 23 || minute > 59 || second > 59) {
                    addAiResponse("❌ Неверное время: %02d:%02d:%02d. Часы 0-23, минуты/секунды 0-59.".format(hour, minute, second))
                    return
                }

                addAlarm(context, hour, minute, second)
                addAiResponse("⏰ Будильник успешно установлен на %02d:%02d:%02d.".format(hour, minute, second))
                return
            } else {
                addAiResponse("⏰ Укажите время для будильника, например: «Будильник на 07:30», «Разбуди в 8 утра» или «Поставь будильник через 20 минут».")
                return
            }
        }

        fun openActivity(intent: Intent, notFoundMessage: String): Boolean = try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            addAiResponse(notFoundMessage)
            false
        }

        val tabCommands = listOf(
            Screen.Clock to listOf("часы", "главный экран"),
            Screen.World to listOf("мир", "мировое время", "мировые часы"),
            Screen.Alarm to listOf("будильник", "будильники"),
            Screen.Stopwatch to listOf("секундомер"),
            Screen.Timer to listOf("таймер")
        )
        val utilityCommands = mapOf(
            "Фонарик" to listOf("фонарик", "фонарь", "вспышка"),
            "Компас" to listOf("компас"),
            "Заметки" to listOf("заметки", "заметка", "записать"),
            "Калькулятор" to listOf("калькулятор"),
            "Мемы" to listOf("мемы", "мем", "зал славы", "зал славы мемов", "рофл", "рофлы"),
            "AI" to listOf("ai", "ии")
        )

        fun openTab(keyword: String): Boolean {
            val screen = tabCommands.firstOrNull { (_, words) -> words.any { Regex("\\b$it").containsMatchIn(keyword) } }?.first ?: return false
            onOpenScreen(screen)
            addAiResponse("Открываю: ${screen.label}")
            return true
        }

        fun openUtility(keyword: String): Boolean {
            val utility = utilityCommands.entries.firstOrNull { (_, words) -> words.any { Regex("\\b$it").containsMatchIn(keyword) } }?.key ?: return false
            onOpenUtility(utility)
            addAiResponse("Открываю: $utility")
            return true
        }

        val wantsSettings = Regex("\\b(настройки|настройка|settings)\\b").containsMatchIn(normalized)
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
        val batteryLevel = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)

        when {
            openUtility(normalized) -> Unit
            wantsSettings -> {
                onOpenSettings()
                addAiResponse("Открываю настройки")
            }
            Regex("\\b(который час|сколько времени|время|часовой пояс|таймзона|зона)\\b").containsMatchIn(normalized) && !normalized.contains("миров") -> {
                val offsetMinutes = timeZone.getOffset(System.currentTimeMillis()) / 60000
                val offsetText = "UTC%s%02d:%02d".format(if (offsetMinutes >= 0) "+" else "-", Math.abs(offsetMinutes) / 60, Math.abs(offsetMinutes) % 60)
                addAiResponse("Сейчас ${userTime("HH:mm:ss")}.\nВаш часовой пояс: $timeZoneLabel ($offsetText)")
            }
            Regex("\\b(дата|число|какой день)\\b").containsMatchIn(normalized) -> {
                addAiResponse("Сегодня ${userTime("d MMMM yyyy, EEEE")}")
            }
            Regex("\\b(заряд|батарея|батарейка|аккумулятор)\\b").containsMatchIn(normalized) -> {
                val advice = when {
                    batteryLevel >= 80 -> "Можно не заряжать."
                    batteryLevel >= 40 -> "Заряда хватит надолго."
                    batteryLevel >= 20 -> "Скоро стоит подключить зарядку."
                    else -> "Срочно на зарядку!"
                }
                addAiResponse("Заряд батареи: $batteryLevel%. $advice")
            }
            Regex("\\b(заметка|запиши|записать)\\b").containsMatchIn(normalized) -> {
                val note = text.substringAfter("запиши", text.substringAfter("заметка", text.substringAfter("записать", ""))).trim()
                if (note.isEmpty()) {
                    openUtility("заметки")
                } else {
                    val prefs = context.getSharedPreferences("notes_prefs", Context.MODE_PRIVATE)
                    val existing = prefs.getString("note", "") ?: ""
                    val updated = if (existing.isBlank()) note else existing.trimEnd() + "\n" + note
                    prefs.edit().putString("note", updated).apply()
                    addAiResponse("Добавил в заметки: \"$note\"")
                }
            }
            openTab(normalized) -> Unit

            normalized.contains("камера") || normalized.contains("фото") ->
                openActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE), "Нет приложения для камеры")
            normalized.contains("позвони") || normalized.contains("звонок") || normalized.contains("набрать") -> {
                val digits = normalized.filter { it.isDigit() }
                openActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")), "Нет приложения для звонков")
            }
            normalized.contains("сообщение") || normalized.contains("смс") ->
                openActivity(Intent(Intent.ACTION_VIEW, Uri.parse("sms:")), "Нет приложения для сообщений")
            normalized.contains("github") ->
                openActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/adelka4745/ShellClock")), "Нет браузера")
            normalized.contains("погода") -> {
                lastQuery = "погода"
                addAiResponse("Открываю прогноз в Google...")
                browserUrl = searchUrl("погода")
                showBrowser = true
            }
            evaluateExpression(text) != null -> addAiResponse("${text.trim()} = ${formatResult(evaluateExpression(text)!!)}")
            else -> {
                val query = cleanupQuestion(text)
                if (query.isNotEmpty()) ask(query)
                else addAiResponse("Не понял команду. Скажите «который час», «напиши код на python», «будильник на 07:30», «заряд», «заметка …» или задайте любой вопрос!")
            }
        }
    }

    if (showBrowser) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showBrowser = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад") }
                Text("Браузер", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            AndroidView(
                factory = { ctx ->
                    android.webkit.WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = android.webkit.WebViewClient()
                    }
                },
                update = { it.loadUrl(browserUrl) },
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(serializeDialog(chatHistory).toByteArray())
                }
            } catch (e: Exception) { }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { r -> r.readText() } ?: ""
                val loaded = deserializeDialog(content)
                if (loaded.isNotEmpty()) {
                    chatHistory.clear()
                    chatHistory.addAll(loaded)
                    saveDialogToLocal(context, chatHistory)
                }
            } catch (e: Exception) { }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).imePadding()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад") }
                Text("ShellClock AI", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { exportLauncher.launch("my_dialog.scaidialogs") }) {
                    Icon(Icons.Default.FileUpload, contentDescription = "Сохранить .scaidialogs")
                }
                IconButton(onClick = { importLauncher.launch(arrayOf("text/plain", "*/*")) }) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Загрузить .scaidialogs")
                }
                if (chatHistory.isNotEmpty()) {
                    IconButton(onClick = { chatHistory.clear(); saveDialogToLocal(context, chatHistory) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Очистить чат")
                    }
                }
            }
        }

        // История чата
        Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
            if (chatHistory.isEmpty()) {
                Spacer(Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("👋 Задайте любой вопрос или дайте команду!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("• 'напиши код на python' / kotlin / java / c++\n• 'столица германии', 'скорость света', 'ооп'\n• 'будильник на 07:30', 'секундомер', 'заряд'\n• 'что такое пицца', 'расскажи анекдот'", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                chatHistory.forEach { msg ->
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (msg.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth(0.88f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(msg.sender, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (msg.isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(Modifier.height(4.dp))
                                Text(msg.text, fontSize = 15.sp, color = if (msg.isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it; prediction = predictNextWord(it) },
            label = { Text("Спроси или дай команду...") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { executeCommand(inputText) }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Отправить")
                }
            }
        )
        if (prediction.isNotEmpty()) {
            Text("Подсказка: $prediction", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 2.dp))
        }

        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Будильник 07:30", "Код Python", "Код Kotlin", "Погода").forEach { cmd ->
                SuggestionChip(
                    onClick = { inputText = cmd; executeCommand(cmd) },
                    label = { Text(cmd, fontSize = 12.sp) }
                )
            }
        }
    }
}

// ==========================================
// ОСТАЛЬНЫЕ ЭКРАНЫ УТИЛИТ
// ==========================================

@Composable
private fun MemeScreen() {
    val jokesList = listOf(
        "Дэвид Базуки ненавидит свое имя. Поэтому в мемах его называют David Bazooka (Дэвид Базука).",
        "Что делает Дэвид Базуки, когда видит классические головы в Roblox? Он их удаляет. Это его личная миссия.",
        "Почему Дэвид Базуки не играет в свою игру? Потому что он заходит с фейкового аккаунта, чтобы его не закидали проклятиями.",
        "Дэвид Базуки — единственный человек, которого проклинают всем Роблоксом, а он все равно выпускает обновления.",
        "Если ты скажешь «Дэвид Базуки», Roblox автоматически удалит твою классическую голову.",
        "Дэвид Базуки не боится хейтеров. Он боится, что игроки вернут себе классические лица.",
        "Что сказал Дэвид Базуки, когда увидел гору проклятий в свой адрес? «Это фича, а не баг».",
        "Дэвид Базуки — это человек, который смог превратить игру в маркетплейс и назвать это «эволюцией».",
        "Почему Дэвид Базуки не заходит на Reddit? Потому что там его уже забанили (как и всех нас).",
        "Дэвид Базуки думает, что «Roblox Plus» — это хорошая идея. Мы думаем иначе.",
        "Дэвид Базуки однажды сказал: «Мы хотим, чтобы дети могли играть бесплатно». А потом добавил подписку.",
        "Если бы Дэвид Базуки был супергероем, его суперсила была бы «Удалять классические головы на расстоянии».",
        "Что общего у Дэвида Базуки и страшной черепахи? Оба выглядят мило, пока не откусят тебе руку.",
        "Страшная черепаха из мемов — это просто Дэвид Базуки в панцире. Она тоже удаляет контент, который ей не нравится.",
        "Дэвид Базуки сказал, что Roblox будет как Pixar. Но пока что он похож на страшную черепаху, которая медленно ползет к твоему кошельку.",
        "Почему Дэвид Базуки не боится страшной черепахи? Потому что он уже забрал у нее классическую голову.",
        "Дэвид Базуки — это доказательство того, что даже страшная черепаха может стать CEO, если у нее достаточно патентов.",
        "Когда Дэвид Базуки видит страшную черепаху, он говорит: «А можно это запаковать в Roblox Plus?»",
        "Страшная черепаха боится только одного: что Дэвид Базуки удалит ее панцирь и заменит на «динамический».",
        "Дэвид Базуки не боится проклятий. Он боится, что игроки перестанут покупать Robux.",
        "Существует 10 типов людей: те, кто понимают двоичную систему, и те, кто не понимают.",
        "Программист ставит на ночь два стакана: один с водой — если захочет пить, а другой пустой — если не захочет.",
        "Код работает? Не трогай. Код не работает? Скажи всем, что это концептуальная фича.",
        "В 3 часа ночи даже самая банальная синтаксическая ошибка кажется глубокой философской тайной.",
        "Заходит тестировщик в бар. Заказывает кружку пива. 0 кружек пива. 999999 кружек. Ящерицу. -1 кружку. Всё работает!",
        "Первое правило разработки: если программа заработала с первого раза, проверь, тот ли файл ты запустил.",
        "Почему программисты предпочитают тёмную тему? Потому что свет привлекает баги!",
        "Джуниор разработчик: «Я починил баг!». Сеньор разработчик: «Отлично, теперь найди 5 новых, которые ты только что создал».",
        "Будильник в 06:00: «ВСТАВАЙ, МИР ЖДЁТ!». Я в 06:01: «Ещё 5 минуточек...» *Просыпаюсь в 14:30*.",
        "Самый опасный экстремальный спорт — это ставить будильник на 7:00 и ложиться спать в 4:30 со словами «ну часа три посплю».",
        "Когда поставил будильник на 07:00, а сам проснулся в 06:59 — абсолютная победа над разумом и временем!",
        "Таймер на 10 минут во время варки макарон — вечность. Таймер на 10 минут в соцсетях — полторы секунды.",
        "Фонарик на телефоне в 2 часа ночи: слепит как второе Солнце. Фонарик в темноте когда ищешь ключи: подсвечивает полсантиметра.",
        "Мой мозг в 3:00 ночи: «А помнишь, как в 2015 году ты случайно сказал \"спасибо вам тоже\" кассиру?»",
        "Когда открываешь калькулятор на телефоне, чтобы перепроверить «7 + 5 = 12» — чисто для уверенности.",
        "Заряд аккумулятора: 1%. Я: «Держись, браток, нам ещё видео досмотреть!». Телефон: *гасит экран*.",
        "Заходит разработчик в кафе и заказывает кастомную тему. Кафе крашится с NullPointerException.",
        "Когда заказчик говорит «тут делов на 5 минут», планируй проект на три месяца.",
        "Классический баг: на моём устройстве всё работает!",
        "Словарь ИИ в ShellClock настолько мощный, что знает ответы даже на те вопросы, которые ты ещё не придумал.",
        "Страшная черепаха увидела, сколько мемов добавили в ShellClock, и решила уйти на пенсию.",
        "Компас показывает на север, но душа программиста всегда стремится к кофеварке.",
        "Секундомер зафиксировал рекорд: 0.01 секунды — именно столько времени нужно, чтобы отклонить будильник утром.",
        "Калькулятор ShellClock — единственное место, где деление на ноль не приводит к взрыву Вселенной.",
        "Когда спросил у AI «как дела», а он ответил «Тикаю и считаю». Уважение +100.",
        "Почему секундомер никогда не опаздывает? Потому что у него все секунды под контролем!",
        "Лучший будильник — это громкий звук падающего телефона на плитку.",
        "Офлайн AI в ShellClock: работает даже в бункере и на необитаемом острове!",
        "Дэвид Базуки пытается удалить эту строчку мема, но у него нет прав доступа."
    ).distinct()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(jokesList) { joke ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                Text(joke, modifier = Modifier.padding(16.dp), fontSize = 16.sp)
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

    // Иначе фонарик остаётся включённым после выхода с экрана утилит
    DisposableEffect(cameraId) {
        onDispose {
            if (isOn && cameraId != null) try { cameraManager.setTorchMode(cameraId, false) } catch (e: Exception) { }
        }
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

    // Азимут устройства (куда смотрит верх телефона). Ноль — на север.
    // Поворот, а не абсолютный угол: между соседними событиями датчика бывает переход через 0/360,
    // из-за него стрелка раньше дёргалась на пол-оборота. Угол накапливаем и берём сумму по модулю 360.
    val rotation = remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    val azimuth = ((rotation.floatValue % 360f) + 360f) % 360f

    // Направление «на север» после сглаживания, чтобы стрелка не дрожала
    val needle = remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    // Сглаженное значение уже само лежит в диапазоне 0..360 — не приводим его повторно,
    // иначе кратчайший путь (с учётом перехода через 0) теряется
    LaunchedEffect(azimuth) {
        val diff = ((azimuth - needle.floatValue + 540f) % 360f) - 180f
        needle.floatValue = (((needle.floatValue + diff * 0.2f) % 360f) + 360f) % 360f
    }

    var hasSensor by remember { mutableStateOf(true) }
    var hasMagnetometer by remember { mutableStateOf(true) }
    var accuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        // Запасной путь для телефонов без ROTATION_VECTOR — акселерометр + магнитометр
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        var gravity: FloatArray? = null
        var geomagnetic: FloatArray? = null
        var lastAzimuth: Float? = null
        val lastRotation = FloatArray(8)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val rotationMatrix = FloatArray(9)
                if (rotationSensor != null) {
                    // Значения ROTATION_VECTOR бывают шумными — усредняем, иначе матрица «плывёт»
                    val smoothed = FloatArray(event.values.size)
                    for (i in event.values.indices) smoothed[i] = lowPass(event.values[i], if (i < lastRotation.size) lastRotation[i] else null).also { lastRotation[i] = it }
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, smoothed)
                } else {
                    when (event.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> gravity = lowPassArray(event.values.clone(), gravity)
                        Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = lowPassArray(event.values.clone(), geomagnetic)
                    }
                    val g = gravity ?: return
                    val m = geomagnetic ?: return
                    if (!SensorManager.getRotationMatrix(rotationMatrix, null, g, m)) return
                }

                // Матрицу надо привести к портретной ориентации экрана, иначе в альбомной стрелка косит на 90°
                val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    context.display
                } else {
                    @Suppress("DEPRECATION")
                    (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay
                } ?: return
                val remapped = FloatArray(9)
                when (display.rotation) {
                    android.view.Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, remapped)
                    android.view.Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, remapped)
                    android.view.Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, remapped)
                    else -> System.arraycopy(rotationMatrix, 0, remapped, 0, 9)
                }

                val orientation = FloatArray(3)
                SensorManager.getOrientation(remapped, orientation)
                // getOrientation возвращает радианы, а не градусы
                val current = Math.toDegrees(orientation[0].toDouble()).toFloat()

                // Накапливаем относительное изменение: кратчайший поворот от предыдущего значения
                val last = lastAzimuth
                rotation.floatValue += if (last == null) current else (((current - last) % 360f + 540f) % 360f) - 180f
                lastAzimuth = current
            }

            override fun onAccuracyChanged(sensor: Sensor?, newAccuracy: Int) {
                if (sensor?.type == Sensor.TYPE_ROTATION_VECTOR || sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) accuracy = newAccuracy
            }
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
        } else if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_GAME)
        } else {
            hasMagnetometer = magnetometer != null
            hasSensor = false
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }

    val direction = when {
        azimuth < 22.5 || azimuth >= 337.5 -> "Север (С)"
        azimuth < 67.5 -> "Северо-Восток (СВ)"
        azimuth < 112.5 -> "Восток (В)"
        azimuth < 157.5 -> "Юго-Восток (ЮВ)"
        azimuth < 202.5 -> "Юг (Ю)"
        azimuth < 247.5 -> "Юго-Запад (ЮЗ)"
        azimuth < 292.5 -> "Запад (З)"
        else -> "Северо-Запад (СЗ)"
    }

    val textColor = MaterialTheme.colorScheme.onBackground
    val accentColor = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        if (!hasSensor) {
            Text(
                if (!hasMagnetometer) "В телефоне нет магнитометра — компас недоступен" else "Компас недоступен: нет подходящих датчиков",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
        Canvas(modifier = Modifier.size(300.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2 - 10
            drawCircle(Color.Gray, radius = radius, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))

            // Шкала через 30°: без неё по диску нельзя было прочитать направление
            for (degree in 0 until 360 step 30) {
                val radians = Math.toRadians((degree - needle.floatValue).toDouble())
                val outer = Offset(center.x + radius * kotlin.math.sin(radians).toFloat(), center.y - radius * kotlin.math.cos(radians).toFloat())
                val inner = Offset(
                    center.x + (radius - 14f) * kotlin.math.sin(radians).toFloat(),
                    center.y - (radius - 14f) * kotlin.math.cos(radians).toFloat()
                )
                drawLine(Color.Gray, start = inner, end = outer, strokeWidth = 4f)
            }

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply { color = textColor.toArgb(); textSize = 44f; textAlign = android.graphics.Paint.Align.CENTER }
                // Подписи прибиты к диску и двигаются вместе с ним — это «где север», а не «куда я смотрю»
                fun letter(deg: Double, text: String, color: Int) {
                    val radians = Math.toRadians((deg - needle.floatValue).toDouble())
                    val x = center.x + (radius - 46f) * kotlin.math.sin(radians).toFloat()
                    val y = center.y - (radius - 46f) * kotlin.math.cos(radians).toFloat() + 16f
                    drawText(text, x, y, android.graphics.Paint(paint).apply { this.color = color })
                }
                letter(0.0, "С", android.graphics.Color.RED)
                letter(90.0, "В", textColor.toArgb())
                letter(180.0, "Ю", textColor.toArgb())
                letter(270.0, "З", textColor.toArgb())
            }

            // Стрелка поворачивается на −azimuth: телефон повернули — стрелка остаётся на север
            rotate(degrees = -needle.floatValue, pivot = center) {
                val path = Path().apply {
                    moveTo(center.x, center.y - 80f); lineTo(center.x - 20f, center.y); lineTo(center.x + 20f, center.y); close()
                }
                drawPath(path, Color.Red)
                val path2 = Path().apply {
                    moveTo(center.x, center.y + 80f); lineTo(center.x - 20f, center.y); lineTo(center.x + 20f, center.y); close()
                }
                drawPath(path2, Color.Gray)
            }
            drawCircle(textColor, radius = 6f, center = center)

            // Указатель «куда смотрит телефон»: сверху корпуса. Без него было непонятно,
            // вращается диск под неподвижной стрелкой или наоборот.
            val markSize = 12f
            val heading = Path().apply {
                moveTo(center.x, center.y - radius + 4f)
                lineTo(center.x - markSize, center.y - radius - 10f)
                lineTo(center.x + markSize, center.y - radius - 10f)
                close()
            }
            drawPath(heading, accentColor)
        }
        Spacer(Modifier.height(16.dp))
        Text("${azimuth.toInt()}°", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(direction, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        if (hasSensor) {
            val accuracyText = when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE, SensorManager.SENSOR_STATUS_NO_CONTACT -> "Датчик неточен: поводите телефоном «восьмёркой»"
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Низкая точность — откалибруйте телефон движением «восьмёркой»"
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Средняя точность компаса"
                else -> "Точность высокая"
            }
            Text(accuracyText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

// Сглаживание потока значений датчика, чтобы матрица поворота не дрожала на шуме
private fun lowPass(input: Float, previous: Float?): Float =
    if (previous == null) input else previous + 0.25f * (input - previous)

// То же для векторов акселерометра/магнитометра
private fun lowPassArray(input: FloatArray, output: FloatArray?): FloatArray {
    if (output == null) return input.copyOf()
    for (i in input.indices) output[i] = lowPass(input[i], output[i])
    return output
}

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
                val content = "[SHELLCLOCK NOTES]\nDATE: $date\n---\n$text"
                context.contentResolver.openOutputStream(it)?.use { stream -> stream.write(content.toByteArray()) }
            } catch (e: Exception) { }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() } ?: ""
                // Файл мог быть перенесён на Windows с CRLF — тогда разделитель "---\n" не находится
                val normalized = content.replace("\r\n", "\n")
                if (normalized.startsWith("[SHELLCLOCK NOTES]")) {
                    val parts = normalized.split("---\n", limit = 2)
                    if (parts.size == 2) {
                        text = parts[1]
                        prefs.edit().putString("note", text).apply()
                    }
                } else {
                    text = normalized
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
    // Ввод после оператора/сброса заменяет экран, а не дописывается к нему
    var replaceOnNextDigit by remember { mutableStateOf(true) }
    var currentInput by remember { mutableStateOf("") }
    var operator by remember { mutableStateOf<String?>(null) }
    var previousValue by remember { mutableStateOf<Double?>(null) }

    fun format(value: Double): String = if (value.isNaN() || value.isInfinite()) "Ошибка" else formatResult(value)

    fun onDigit(digit: String) {
        if (replaceOnNextDigit) { display = digit; currentInput = digit; replaceOnNextDigit = false }
        else { display += digit; currentInput += digit }
    }
    fun onOperator(op: String) {
        if (currentInput.isNotEmpty()) {
            if (previousValue != null && operator != null) {
                val result = calculate(previousValue!!, currentInput.toDoubleOrNull() ?: 0.0, operator!!)
                display = format(result); previousValue = result
            } else previousValue = currentInput.toDoubleOrNull() ?: 0.0
            currentInput = ""
        }
        operator = op; display = format(previousValue ?: 0.0) + " " + op
        replaceOnNextDigit = true
    }
    fun onEquals() {
        if (operator != null && previousValue != null && currentInput.isNotEmpty()) {
            val result = calculate(previousValue!!, currentInput.toDoubleOrNull() ?: 0.0, operator!!)
            display = format(result); previousValue = result; currentInput = ""
            // Сохраняем результат как ввод — иначе после «=» его нельзя было продолжить без сброса
            currentInput = display; operator = null; replaceOnNextDigit = true
        }
    }
    fun onClear() { display = "0"; currentInput = ""; operator = null; previousValue = null; replaceOnNextDigit = true }
    fun onDecimal() {
        if (replaceOnNextDigit) { display = "0."; currentInput = "0."; replaceOnNextDigit = false }
        else if (!currentInput.contains(".")) {
            if (currentInput.isEmpty()) { display += "0."; currentInput = "0." }
            else { display += "."; currentInput += "." }
        }
    }

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
                                "C" -> onClear()
                                // Смена знака — только одного числа, иначе ломалась следующая операция
                                "±" -> {
                                    val value = currentInput.toDoubleOrNull() ?: return@Button
                                    display = format(-value) + (operator?.let { " $it" } ?: "")
                                    currentInput = format(-value)
                                }
                                "%" -> {
                                    val value = currentInput.toDoubleOrNull() ?: return@Button
                                    display = format(value / 100); currentInput = format(value / 100)
                                }
                                "/", "*", "-", "+" -> onOperator(label)
                                "=" -> onEquals()
                                "." -> onDecimal()
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

private fun calculate(a: Double, b: Double, op: String): Double = when (op) {
    "+" -> a + b; "-" -> a - b; "*" -> a * b; "/" -> if (b != 0.0) a / b else Double.NaN; else -> 0.0
}
private fun formatResult(value: Double): String = if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()

// --- Огромный словарь для угадывания ---
val wordFrequency = mapOf(
    "привет" to mapOf("как" to 5, "мир" to 3, "дела" to 4, "пока" to 2, "друг" to 2),
    "здрасьте" to mapOf("как" to 4, "дела" to 3, "ai" to 2),
    "здарова" to mapOf("как" to 4, "бро" to 2, "дела" to 2),
    "как" to mapOf("дела" to 5, "ты" to 3, "жизнь" to 2, "погода" to 2, "пройти" to 1, "поставить" to 2, "включить" to 2),
    "дела" to mapOf("хорошо" to 3, "нормально" to 2, "плохо" to 1, "как" to 2),
    "что" to mapOf("делаешь" to 4, "нового" to 3, "это" to 2, "как" to 2, "такое" to 4, "ты" to 3, "умеешь" to 3),
    "где" to mapOf("ты" to 4, "находишься" to 3, "это" to 2, "купить" to 1, "живёшь" to 3),
    "когда" to mapOf("придёшь" to 3, "будет" to 2, "начнётся" to 2, "закончится" to 1, "високосный" to 2),
    "почему" to mapOf("ты" to 3, "это" to 2, "не" to 2, "так" to 1, "небо" to 3, "идёт" to 3),
    "погода" to mapOf("сегодня" to 5, "завтра" to 3, "хорошая" to 2, "плохая" to 2, "на" to 1),
    "сегодня" to mapOf("погода" to 3, "вечером" to 2, "утром" to 2, "днём" to 2, "какой" to 3, "число" to 3),
    "завтра" to mapOf("погода" to 3, "утром" to 2, "вечером" to 2, "будет" to 2),
    "время" to mapOf("сейчас" to 3, "идёт" to 2, "летит" to 2, "сколько" to 2, "часовой" to 2),
    "сейчас" to mapOf("время" to 3, "пойду" to 2, "работаю" to 2, "отдыхаю" to 1, "который" to 2),
    "я" to mapOf("хочу" to 5, "люблю" to 3, "знаю" to 2, "думаю" to 2, "устал" to 2),
    "хочу" to mapOf("спать" to 3, "есть" to 2, "гулять" to 2, "домой" to 2, "учиться" to 1),
    "люблю" to mapOf("тебя" to 3, "музыку" to 2, "спорт" to 2, "читать" to 1),
    "знаю" to mapOf("что" to 2, "это" to 2, "как" to 1),
    "думаю" to mapOf("что" to 3, "о" to 2, "это" to 1),
    "можно" to mapOf("ли" to 3, "мне" to 2, "это" to 2),
    "нужно" to mapOf("сделать" to 3, "идти" to 2, "купить" to 2),
    "давай" to mapOf("пойдём" to 3, "сделаем" to 2, "встретимся" to 2),
    "пойдём" to mapOf("гулять" to 3, "домой" to 2, "в" to 2),
    "гулять" to mapOf("пойдём" to 2, "вечером" to 1),
    "домой" to mapOf("пойду" to 2, "хочу" to 2),
    "спать" to mapOf("хочу" to 3, "пора" to 2),
    "есть" to mapOf("хочу" to 3, "что" to 2),
    "работа" to mapOf("сегодня" to 2, "завтра" to 2, "устал" to 1),
    "учёба" to mapOf("сегодня" to 2, "завтра" to 2, "сложно" to 1),
    "школа" to mapOf("сегодня" to 2, "завтра" to 2),
    "телефон" to mapOf("сел" to 2, "зарядить" to 2),
    "зарядка" to mapOf("телефон" to 2, "нужна" to 1),
    "интернет" to mapOf("есть" to 2, "нет" to 2, "медленный" to 1),
    "музыка" to mapOf("играет" to 2, "громко" to 1),
    "фильм" to mapOf("смотреть" to 2, "интересный" to 1),
    "книга" to mapOf("читать" to 2, "интересная" to 1),
    "игра" to mapOf("играть" to 2, "интересная" to 1),
    "радио" to mapOf("слушать" to 2, "играет" to 1),
    "фонарик" to mapOf("включить" to 3, "выключить" to 2, "открыть" to 2),
    "компас" to mapOf("открыть" to 3, "север" to 2),
    "заметки" to mapOf("открыть" to 3, "записать" to 2),
    "калькулятор" to mapOf("открыть" to 3, "посчитать" to 2),
    "будильник" to mapOf("на" to 5, "поставить" to 3, "выключить" to 2),
    "таймер" to mapOf("запустить" to 3, "поставить" to 2, "остановить" to 2),
    "секундомер" to mapOf("запустить" to 3, "остановить" to 2, "сброс" to 1),
    "звонок" to mapOf("сделать" to 2, "принять" to 1),
    "сообщение" to mapOf("отправить" to 2, "получить" to 1),
    "камера" to mapOf("открыть" to 2, "сделать" to 2),
    "фото" to mapOf("сделать" to 2, "посмотреть" to 1),
    "видео" to mapOf("снять" to 2, "посмотреть" to 1),
    "позвони" to mapOf("маме" to 3, "папе" to 2, "другу" to 2),
    "напиши" to mapOf("смс" to 2, "сообщение" to 2),
    "найди" to mapOf("в" to 2, "информацию" to 2, "google" to 3),
    "поиск" to mapOf("в" to 2, "интернете" to 2, "google" to 3),
    "включи" to mapOf("фонарик" to 4, "музыку" to 2, "свет" to 1),
    "выключи" to mapOf("фонарик" to 4, "музыку" to 2, "свет" to 1),
    "открой" to mapOf("заметки" to 3, "калькулятор" to 3, "мемы" to 3, "часы" to 2),
    "закрой" to mapOf("заметки" to 2, "калькулятор" to 1),
    "спасибо" to mapOf("за" to 2, "тебе" to 2, "большое" to 3),
    "пожалуйста" to mapOf("помоги" to 2, "скажи" to 2),
    "извини" to mapOf("меня" to 2),
    "прости" to mapOf("меня" to 2),
    "пока" to mapOf("до" to 2, "свидания" to 2),
    "до" to mapOf("свидания" to 3, "встречи" to 2),
    "свидания" to mapOf("до" to 2, "пока" to 2),
    "встречи" to mapOf("до" to 2, "завтра" to 1),
    "завтра" to mapOf("встретимся" to 2, "поговорим" to 1),
    "сегодня" to mapOf("вечером" to 2, "утром" to 2, "днём" to 2),
    "вечером" to mapOf("пойдём" to 2, "отдохну" to 1),
    "утром" to mapOf("встану" to 2, "пойду" to 1),
    "днём" to mapOf("пойду" to 2, "работаю" to 1),
    "ночью" to mapOf("спать" to 2, "гулять" to 1),
    // Запросы к базе знаний AI и поиску
    "сколько" to mapOf("будет" to 5, "времени" to 4, "планет" to 3, "дней" to 3, "часов" to 3, "минут" to 2, "секунд" to 2, "материков" to 2, "океанов" to 2),
    "будет" to mapOf("завтра" to 3, "сегодня" to 2, "погода" to 2),
    "скажи" to mapOf("мне" to 3, "пожалуйста" to 2, "сколько" to 2, "время" to 3),
    "расскажи" to mapOf("про" to 4, "о" to 3, "мне" to 2, "мем" to 3, "анекдот" to 3, "шутку" to 3),
    "объясни" to mapOf("пожалуйста" to 2, "мне" to 2, "просто" to 1),
    "напиши" to mapOf("мне" to 2, "сообщение" to 2),
    "куда" to mapOf("пойти" to 2, "идёшь" to 2),
    "самый" to mapOf("большой" to 3, "быстрый" to 2, "высокий" to 2, "маленький" to 1),
    "самая" to mapOf("большая" to 3, "высокая" to 2, "глубокая" to 2),
    "самое" to mapOf("большое" to 3, "глубокое" to 2, "быстрое" to 2),
    "большой" to mapOf("город" to 2, "дом" to 2, "человек" to 1),
    "столица" to mapOf("россии" to 4, "франции" to 3, "японии" to 3, "германии" to 3, "великобритании" to 3, "китая" to 3),
    "планета" to mapOf("земля" to 3, "марс" to 2, "юпитер" to 2),
    "звезда" to mapOf("солнце" to 3, "ближайшая" to 2, "яркая" to 1),
    "галактика" to mapOf("млечный" to 3, "путь" to 2, "андромеда" to 1),
    "космос" to mapOf("бесконечный" to 2, "интересный" to 2, "большой" to 1),
    "наука" to mapOf("физика" to 2, "химия" to 2, "биология" to 1),
    "история" to mapOf("россии" to 2, "мира" to 2, "древняя" to 1),
    "будильник" to mapOf("на" to 5, "утром" to 2, "поставить" to 3, "выключить" to 2),
    "поставь" to mapOf("будильник" to 5, "таймер" to 3, "напоминание" to 2),
    "запусти" to mapOf("таймер" to 4, "секундомер" to 3, "музыку" to 1),
    "настройки" to mapOf("приложения" to 3, "темы" to 2, "шрифта" to 2),
    "заметка" to mapOf("новая" to 3, "записать" to 2, "сохранить" to 2),
    "запомни" to mapOf("что" to 2, "это" to 2, "меня" to 1),
    "переведи" to mapOf("на" to 3, "текст" to 2, "слово" to 2),
    "посчитай" to mapOf("мне" to 2, "сколько" to 3, "пример" to 2),
    "реши" to mapOf("пример" to 3, "задачу" to 2),
    "помоги" to mapOf("мне" to 3, "пожалуйста" to 2, "с" to 2),
    "какой" to mapOf("сейчас" to 3, "сегодня" to 3, "день" to 2, "год" to 2),
    "какая" to mapOf("погода" to 3, "сегодня" to 3, "дата" to 2),
    "какое" to mapOf("сегодня" to 3, "число" to 3, "время" to 2),
    "который" to mapOf("час" to 5, "сейчас" to 2),
    "день" to mapOf("недели" to 3, "сегодня" to 2),
    "недели" to mapOf("сегодня" to 2, "конец" to 1),
    "год" to mapOf("сейчас" to 2, "какой" to 2, "новый" to 2),
    "месяц" to mapOf("сейчас" to 2, "какой" to 2),
    "дата" to mapOf("сегодня" to 3, "какая" to 2),
    "число" to mapOf("сегодня" to 3, "какое" to 2),
    "заряд" to mapOf("батареи" to 3, "телефона" to 2, "сколько" to 2),
    "батарея" to mapOf("садится" to 2, "зарядка" to 2, "сколько" to 1),
    "найди" to mapOf("в" to 2, "google" to 3, "информацию" to 2),
    "google" to mapOf("поиск" to 3, "найди" to 2),
    "википедия" to mapOf("статья" to 2, "что" to 1),
    "github" to mapOf("shellclock" to 2, "проект" to 1),
    "shellclock" to mapOf("это" to 2, "ai" to 2, "часы" to 2),
    "приложение" to mapOf("shellclock" to 2, "открыть" to 2, "настройки" to 1),
    "мем" to mapOf("смешной" to 3, "покажи" to 3, "расскажи" to 3),
    "мемы" to mapOf("открыть" to 3, "покажи" to 2),
    "секундомер" to mapOf("запустить" to 3, "остановить" to 2, "сброс" to 1),
    "таймер" to mapOf("запустить" to 3, "поставить" to 2, "остановить" to 2),
    "часы" to mapOf("открыть" to 2, "мировые" to 2, "аналоговые" to 1),
    "мир" to mapOf("время" to 3, "часы" to 2),
    "мировое" to mapOf("время" to 3, "часы" to 2),
    "смешной" to mapOf("мем" to 3, "анекдот" to 2),
    "факт" to mapOf("интересный" to 2, "про" to 2),
    "скорость" to mapOf("света" to 4, "звука" to 4),
    "кто" to mapOf("такой" to 4, "ты" to 3, "разработчик" to 3)
)