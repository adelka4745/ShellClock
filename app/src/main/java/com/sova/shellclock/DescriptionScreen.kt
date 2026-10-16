package com.sova.shellclock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DescriptionScreen() {
    Column(Modifier.padding(top = 40.dp)) {
        Text(text = "ShellClock")
        Text(text = "Программа для просмотра времени")
        Text(text = "Часы - часы из вашего часового пояса")
        Text(text = "Мир - часы из других часовых поясов")
        Text(text = "Секундомер - засечь время, например, скорость бега")
        Text(text = "Таймер - идеально на ночь. Имеет звуки будильника системы.")
        Text(text = "Версия 2.0")
    }
}