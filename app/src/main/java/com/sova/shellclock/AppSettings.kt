package com.sova.shellclock

enum class ThemeStyle(val label: String) {
    LIGHT("Светлая"),
    CLASSIC_DARK("Классическая тёмная"),
    OLED_BLACK("Глубокая чёрная (OLED)"),
    AUTO("Авто (по системе)"),
    AURORA("Северное сияние"),
    SUNSET("Закат"),
    NEON("Неон"),
    FOREST("Лесная"),
    ROSE("Розовая"),
    MATERIAL_BLUE("Классический синий"),
    SEA_WAVE("Морская волна"),
    RETRO("Ретро-неон"),
    COSMOS("Космос"),
    MATERIAL_LIGHT("Material Light")
}

enum class SortType(val label: String) {
    BY_NAME("По названию городов (А-Я)"),
    BY_TIMEZONE("По названию часовых поясов"),
    BY_UTC_OFFSET("По смещению UTC")
}

data class AppSettings(
    val themeStyle: ThemeStyle = ThemeStyle.CLASSIC_DARK,
    val sizeMultiplier: Float = 1.0f,
    val timeFormat: Int = 24,
    val showSeconds: Boolean = true,
    val sortType: SortType = SortType.BY_NAME,
    val showDateUnderClock: Boolean = false,
    val hideEmptyRegions: Boolean = true,
    val showTimezoneName: Boolean = false,
    val isDeveloperMode: Boolean = false,
    val customColor: Int? = null,
    val mainCityName: String? = null,
    val appLanguage: String = "ru",
    // Идентификатор часового пояса, например "Europe/Moscow"; null — системный
    val timeZoneId: String? = null
) {
    companion object {
        val DEFAULT = AppSettings()
        fun clampSize(value: Float): Float =
            if (value.isNaN() || value.isInfinite()) 1.0f else value.coerceIn(0.8f, 1.5f)
    }
    fun normalized(): AppSettings = copy(sizeMultiplier = clampSize(sizeMultiplier))
}