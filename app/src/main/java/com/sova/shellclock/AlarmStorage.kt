package com.sova.shellclock

import android.content.Context
import java.io.File

object AlarmStorage {
    private const val FILE_NAME = "alarms.sca"

    // Сохранение во внутреннюю память (автоматическое)
    fun saveAlarms(context: Context, alarms: List<AlarmItem>) {
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(serialize(alarms))
    }

    // Загрузка из внутренней памяти
    fun loadAlarms(context: Context): List<AlarmItem> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        return try {
            deserialize(file.readText())
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Превращаем список в текст для файла
    fun serialize(alarms: List<AlarmItem>): String {
        return alarms.joinToString("\n") {
            "${it.id}|${it.hour}|${it.minute}|${it.isEnabled}"
        }
    }

    // Читаем текст из файла и превращаем в список
    fun deserialize(text: String): List<AlarmItem> {
        return text.lines().mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size == 4) {
                try {
                    AlarmItem(
                        id = parts[0].toInt(),
                        hour = parts[1].toInt(),
                        minute = parts[2].toInt(),
                        isEnabled = parts[3].toBoolean()
                    )
                } catch (e: Exception) { null }
            } else null
        }
    }
}