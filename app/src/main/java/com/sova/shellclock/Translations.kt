package com.sova.shellclock

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

val englishTranslations = mapOf(
    // Экраны
    "Часы" to "Clock", "Мир" to "World", "Будильник" to "Alarm", "Секундомер" to "Stopwatch",
    "Таймер" to "Timer", "Описание" to "About", "Настройки" to "Settings",
    // Часы
    "Дата" to "Date", "Показывать секунды" to "Show seconds", "Показывать дату" to "Show date",
    "Часовой пояс" to "Timezone", "Показывать название" to "Show name",
    // Секундомер
    "Старт" to "Start", "Пауза" to "Pause", "Сброс" to "Reset", "Круг" to "Lap",
    // Таймер
    "Выберите время" to "Select time", "ЧЧ" to "HH", "ММ" to "MM", "СС" to "SS",
    "Время вышло!" to "Time is up!", "Остановить" to "Stop",
    // Мировое время
    "Мировое время" to "World Time", "Поиск города..." to "Search city...", "Нет городов" to "No cities",
    "Главный" to "Main", "(Главный)" to "(Main)", "Избранное" to "Favorites",
    // Регионы и города
    "Европа" to "Europe", "Азия" to "Asia", "Америка" to "America", "Африка" to "Africa",
    "Австралия и Океания" to "Australia & Oceania",
    "Лондон" to "London", "Париж" to "Paris", "Берлин" to "Berlin", "Рим" to "Rome", "Мадрид" to "Madrid",
    "Амстердам" to "Amsterdam", "Афины" to "Athens", "Москва" to "Moscow", "Стамбул" to "Istanbul",
    "Киев" to "Kyiv", "Минск" to "Minsk", "Вена" to "Vienna", "Брюссель" to "Brussels", "Будапешт" to "Budapest",
    "Варшава" to "Warsaw", "Копенгаген" to "Copenhagen", "Лиссабон" to "Lisbon", "Осло" to "Oslo",
    "Прага" to "Prague", "Стокгольм" to "Stockholm", "Хельсинки" to "Helsinki", "Дублин" to "Dublin",
    "Цюрих" to "Zurich", "Женева" to "Geneva", "Мюнхен" to "Munich", "Гамбург" to "Hamburg",
    "Милан" to "Milan", "Неаполь" to "Naples", "Барселона" to "Barcelona", "Валенсия" to "Valencia",
    "Эдинбург" to "Edinburgh", "Манчестер" to "Manchester", "Дубай" to "Dubai", "Екатеринбург" to "Yekaterinburg",
    "Ташкент" to "Tashkent", "Алматы" to "Almaty", "Нью-Дели" to "New Delhi", "Бангкок" to "Bangkok",
    "Сингапур" to "Singapore", "Шанхай" to "Shanghai", "Гонконг" to "Hong Kong", "Токио" to "Tokyo",
    "Сеул" to "Seoul", "Владивосток" to "Vladivostok", "Новосибирск" to "Novosibirsk", "Иерусалим" to "Jerusalem",
    "Абу-Даби" to "Abu Dhabi", "Астана" to "Astana", "Баку" to "Baku", "Багдад" to "Baghdad",
    "Бейрут" to "Beirut", "Джакарта" to "Jakarta", "Доха" to "Doha", "Ереван" to "Yerevan",
    "Кабул" to "Kabul", "Карачи" to "Karachi", "Катманду" to "Kathmandu", "Куала-Лумпур" to "Kuala Lumpur",
    "Манила" to "Manila", "Маскат" to "Muscat", "Омск" to "Omsk", "Красноярск" to "Krasnoyarsk",
    "Иркутск" to "Irkutsk", "Эр-Рияд" to "Riyadh", "Уфа" to "Ufa", "Самара" to "Samara",
    "Челябинск" to "Chelyabinsk", "Пермь" to "Perm", "Хабаровск" to "Khabarovsk", "Якутск" to "Yakutsk",
    "Магадан" to "Magadan", "Гонолулу" to "Honolulu", "Лос-Анджелес" to "Los Angeles", "Денвер" to "Denver",
    "Чикаго" to "Chicago", "Нью-Йорк" to "New York", "Торонто" to "Toronto", "Мехико" to "Mexico City",
    "Сан-Паулу" to "Sao Paulo", "Рио-де-Жанейро" to "Rio de Janeiro", "Буэнос-Айрес" to "Buenos Aires",
    "Сантьяго" to "Santiago", "Анкоридж" to "Anchorage", "Богота" to "Bogota", "Бостон" to "Boston",
    "Вашингтон" to "Washington", "Ванкувер" to "Vancouver", "Гавана" to "Havana", "Каракас" to "Caracas",
    "Лима" to "Lima", "Майами" to "Miami", "Монреаль" to "Montreal", "Панама" to "Panama",
    "Сан-Франциско" to "San Francisco", "Сиэтл" to "Seattle", "Филадельфия" to "Philadelphia", "Хьюстон" to "Houston",
    "Даллас" to "Dallas", "Сан-Диего" to "San Diego", "Оттава" to "Ottawa", "Калгари" to "Calgary",
    "Монтевидео" to "Montevideo", "Кито" to "Quito", "Каир" to "Cairo", "Йоханнесбург" to "Johannesburg",
    "Найроби" to "Nairobi", "Лагос" to "Lagos", "Касабланка" to "Casablanca", "Аддис-Абеба" to "Addis Ababa",
    "Алжир" to "Algiers", "Аккра" to "Accra", "Дакар" to "Dakar", "Киншаса" to "Kinshasa",
    "Луанда" to "Luanda", "Мапуту" to "Maputo", "Рабат" to "Rabat", "Тунис" to "Tunis",
    "Хартум" to "Khartoum", "Дар-эс-Салам" to "Dar es Salaam", "Сидней" to "Sydney", "Мельбурн" to "Melbourne",
    "Перт" to "Perth", "Окленд" to "Auckland", "Сува" to "Suva", "Брисбен" to "Brisbane",
    "Аделаида" to "Adelaide", "Веллингтон" to "Wellington", "Хобарт" to "Hobart", "Дарвин" to "Darwin",
    "Порт-Морсби" to "Port Moresby",
    // Будильник
    "Добавить будильник" to "Add alarm", "Удалить" to "Delete", "Нет будильников" to "No alarms",
    "Пора вставать!" to "Time to wake up!", "Добавить" to "Add",
    "Нажмите, чтобы открыть приложение" to "Tap to open app", "Будильник" to "Alarm",
    // Настройки (Темы и Вид)
    "Язык интерфейса" to "Interface Language", "Тема оформления" to "Theme",
    "Классическая тёмная" to "Classic Dark", "Светлая" to "Light",
    "Глубокая чёрная (OLED)" to "Deep Black (OLED)", "Авто (по системе)" to "Auto (System)",
    "Северное сияние" to "Aurora", "Закат" to "Sunset", "Неон" to "Neon", "Лесная" to "Forest",
    "Розовая" to "Rose", "Классический синий" to "Classic Blue", "Морская волна" to "Sea Wave",
    "Ретро-неон" to "Retro Neon", "Космос" to "Cosmos", "Material Light" to "Material Light",
    "Свой цвет акцента (RGB)" to "Custom Accent (RGB)", "Выбрать цвет" to "Choose color",
    "Выбор цвета" to "Color Picker", "Красный" to "Red", "Зеленый" to "Green", "Синий" to "Blue",
    "Применить" to "Apply", "Масштаб интерфейса" to "UI Scale", "Масштаб" to "Scale",
    // Настройки (Время)
    "Время" to "Time", "Предпросмотр" to "Preview", "Формат времени" to "Time Format",
    "24-часовой" to "24-hour", "12-часовой" to "12-hour", "Элементы" to "Elements",
    "Дата под часами" to "Date under clock",
    // Настройки (Города)
    "Города" to "Cities", "Сортировка" to "Sorting", "По названию городов (А-Я)" to "By city name (A-Z)",
    "По названию часовых поясов" to "By timezone name", "По смещению UTC" to "By UTC offset",
    "Отображение" to "Display", "Скрывать пустые регионы" to "Hide empty regions",
    "Не показывать вкладки" to "Don't show tabs",
    // Настройки (Данные и Сброс)
    "Данные" to "Data", "Резервная копия (.scs)" to "Backup (.scs)",
    "Сохраните настройки в один файл." to "Save settings to one file.",
    "Экспорт" to "Export", "Импорт" to "Import", "О приложении" to "About app",
    "Версия:" to "Version:", "Лицензия (LICENSE)" to "License (LICENSE)",
    "Настройки разработчика" to "Developer Settings", "Сбросить все настройки" to "Reset all settings",
    "Сброс настроек" to "Reset settings", "Сбросить всё к заводским настройкам?" to "Reset to factory settings?",
    "Сбросить" to "Reset", "Отмена" to "Cancel", "Ошибка" to "Error",
    "Не удалось прочитать файл. Возможно, он поврежден." to "Failed to read file.",
    "ОК" to "OK", "Вид" to "Appearance", "Назад" to "Back", "Вы стали разработчиком!" to "You are now a developer!",
    "Осталось" to "Remaining", "кликов до режима разработчика" to "clicks to developer mode",
    // Разработчик
    "Для разработчика" to "For Developer",
    "Внимание! Эти настройки предназначены только для отладки. Будьте осторожны." to "Warning! Debug settings only.",
    "Диагностика" to "Diagnostics", "Версия ОС" to "OS Version", "Модель устройства" to "Device Model",
    "Плотность экрана" to "Screen Density", "Версия приложения" to "App Version",
    "Тесты и отладка" to "Tests & Debug", "Показывать границы (Border)" to "Show borders",
    "Включает отрисовку красной рамки вокруг экрана" to "Draws a red border",
    "Логировать нажатия" to "Log clicks", "Показывает Toast при нажатии на кнопки" to "Shows Toast on clicks",
    "Тест звука будильника" to "Test alarm sound", "Играет системный будильник" to "Playing system alarm",
    "Опасная зона" to "Danger Zone", "Сбросить ВСЁ и выйти" to "Reset ALL and exit",
    "Выключить режим разработчика" to "Disable developer mode", "Все настройки сброшены!" to "All settings reset!",
    "Режим разработчика выключен" to "Developer mode disabled",
    // Описание
    "Программа для просмотра времени" to "Time viewer application",
    "Часы — часы из вашего часового пояса" to "Clock — time in your timezone",
    "Мир — часы из других часовых поясов" to "World — time in other timezones",
    "Секундомер — засечь время, например, скорость бега" to "Stopwatch — measure time",
    "Таймер — идеально на ночь. Имеет звуки будильника системы." to "Timer — perfect for night.",
    "Версии приложения" to "App versions", "Опубликовано:" to "Published:",
    "Перейти на GitHub" to "Go to GitHub", "Просмотр релиза" to "View release",
    "Обновить" to "Refresh", "Ошибка загрузки" to "Load error", "Ошибка сети" to "Network error",
    // Уведомления о батарее
    "Батарея заряжена" to "Battery Charged", "Низкий заряд" to "Low Battery",
    "Уровень заряда достиг 100%" to "Battery level reached 100%",
    "Уровень заряда упал до 15%" to "Battery level dropped to 15%",
    // Утилиты и Помощь
    "Утилиты" to "Utilities",
    "Фонарик" to "Flashlight",
    "Включить/выключить вспышку" to "Toggle flash",
    "Компас" to "Compass",
    "Стороны света" to "Cardinal directions",
    "Заметки" to "Notes",
    "Быстрые записи" to "Quick notes",
    "Калькулятор" to "Calculator",
    "Простые вычисления" to "Simple calculations",
    "Радио" to "Radio",
    "Слушать онлайн (Радиопоток)" to "Listen online (RadioPotok)",
    "Помощь" to "Help",
    "Ссылки" to "Links",
    "Утилиты для Android" to "Android Utilities",
    "Настройки Android" to "Android Settings",
    "Открыть системные настройки" to "Open system settings",
    "Настройки приложения" to "App Settings",
    "Разрешения, уведомления, батарея" to "Permissions, notifications, battery",
    "Информация о телефоне" to "Phone Info",
    "Версия Android, модель и т.д." to "Android version, model, etc.",
    "Язык и ввод" to "Language & input",
    "Сменить язык системы" to "Change system language",
    // Новые строки для улучшенного компаса
    "Север (С)" to "North (N)",
    "Северо-Восток (СВ)" to "North-East (NE)",
    "Восток (В)" to "East (E)",
    "Юго-Восток (ЮВ)" to "South-East (SE)",
    "Юг (Ю)" to "South (S)",
    "Юго-Запад (ЮЗ)" to "South-West (SW)",
    "Запад (З)" to "West (W)",
    "Северо-Запад (СЗ)" to "North-West (NW)"
)

@Composable
fun tr(ruText: String): String {
    val context = LocalContext.current
    val currentLocale = context.resources.configuration.locales[0]
    return if (currentLocale.language == "en") {
        englishTranslations[ruText] ?: ruText
    } else {
        ruText
    }
}