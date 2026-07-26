package com.dev.memebattle.feature.gameplay.impl.presentation.view.info.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Обратный отсчёт до [expiresAt].
 *
 * TODO: заменить наивный tick на вычисление по kotlinx-datetime когда зависимость добавится.
 * Сейчас каждый раз при появлении нового [expiresAt] счётчик стартует с 60 сек.
 */
@Composable
fun CountdownTimer(
    expiresAt: String,
    label: String = "Осталось",
) {
    var secondsLeft by remember(expiresAt) { mutableIntStateOf(60) }

    LaunchedEffect(expiresAt) {
        while (secondsLeft > 0) {
            delay(1_000)
            secondsLeft--
        }
    }

    InfoRow(
        label = label,
        value = "${secondsLeft / 60}:${(secondsLeft % 60).toString().padStart(2, '0')}",
    )
}
