package com.sova.shellclock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DescriptionScreen(sizeMultiplier: Float) {
    Column(
        modifier = Modifier.padding(top = 40.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "ShellClock",
            fontSize = (32 * sizeMultiplier).sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Программа для просмотра времени",
            fontSize = (18 * sizeMultiplier).sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Часы — часы из вашего часового пояса",
            fontSize = (16 * sizeMultiplier).sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Мир — часы из других часовых поясов",
            fontSize = (16 * sizeMultiplier).sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Секундомер — засечь время, например, скорость бега",
            fontSize = (16 * sizeMultiplier).sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Таймер — идеально на ночь. Имеет звуки будильника системы.",
            fontSize = (16 * sizeMultiplier).sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
