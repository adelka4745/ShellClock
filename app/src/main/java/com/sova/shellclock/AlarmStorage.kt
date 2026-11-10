package com.sova.shellclock
import android.content.Context
import java.io.File
object AlarmStorage {
    private const val FILE_NAME = "alarms.sca"
    fun saveAlarms(context: Context, alarms: List<AlarmItem>) { File(context.filesDir, FILE_NAME).writeText(serialize(alarms)) }
    fun loadAlarms(context: Context): List<AlarmItem> { val file = File(context.filesDir, FILE_NAME); if (!file.exists()) return emptyList(); return try { deserialize(file.readText()) } catch (e: Exception) { emptyList() } }
    fun serialize(alarms: List<AlarmItem>): String = alarms.joinToString("\n") { "${it.id}|${it.hour}|${it.minute}|${it.second}|${it.isEnabled}" }
    fun deserialize(text: String): List<AlarmItem> = text.lines().mapNotNull { line ->
        val parts = line.split("|")
        if (parts.size == 5) try { AlarmItem(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), parts[3].toInt(), parts[4].toBoolean()) } catch (e: Exception) { null } else null
    }
}