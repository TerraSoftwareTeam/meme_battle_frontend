package com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun PhaseTimerHud(
    phaseExpiresAt: String?,
    totalSeconds: Int = 60,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = phaseExpiresAt != null,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { -it },
        exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it },
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp),
    ) {
        if (phaseExpiresAt != null) {
            TimerPill(expiresAt = phaseExpiresAt, totalSeconds = totalSeconds)
        }
    }
}

@Composable
private fun TimerPill(expiresAt: String, totalSeconds: Int) {
    var secondsLeft by remember(expiresAt) {
        mutableIntStateOf(computeSecondsLeft(expiresAt, totalSeconds))
    }

    LaunchedEffect(expiresAt) {
        while (secondsLeft > 0) {
            delay(500)
            secondsLeft = computeSecondsLeft(expiresAt, totalSeconds)
        }
    }

    val fraction = (secondsLeft.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    val animFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(600),
        label = "timerFraction",
    )

    val accentColor = when {
        fraction > 0.5f -> Color(0xFF7C5DFA)
        fraction > 0.2f -> Color(0xFFFFAB00)
        else -> Color(0xFFE53935)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF160C33).copy(alpha = 0.92f))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(accentColor.copy(alpha = 0.7f), accentColor.copy(alpha = 0.25f))
                    ),
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.1f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animFraction)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(accentColor),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = formatSeconds(secondsLeft),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
            }
        }
    }
}

private fun computeSecondsLeft(isoString: String, totalSeconds: Int): Int {
    return try {
        val deadlineMs = parseIso8601ToEpochMs(isoString)
        val nowMs = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val diff = ((deadlineMs - nowMs) / 1000L).toInt()
        diff.coerceIn(0, totalSeconds)
    } catch (_: Exception) {
        totalSeconds
    }
}

private fun parseIso8601ToEpochMs(iso: String): Long {
    val clean = iso.trimEnd('Z').substringBefore('.')
    val parts = clean.split('T')
    val dateParts = parts[0].split('-').map { it.toInt() }
    val timeParts = parts[1].split(':').map { it.toInt() }

    val year = dateParts[0]
    val month = dateParts[1]
    val day = dateParts[2]
    val hour = timeParts[0]
    val min = timeParts[1]
    val sec = timeParts[2]

    val y = if (month <= 2) year - 1 else year
    val m = if (month <= 2) month + 12 else month
    val A = y / 100
    val B = 2 - A + A / 4

    val jdn = (365.25 * (y + 4716)).toLong() +
            (30.6001 * (m + 1)).toLong() +
            day + B - 1524

    val daysSinceEpoch = jdn - 2440588L
    val secondsSinceEpoch = daysSinceEpoch * 86400L + hour * 3600L + min * 60L + sec
    return secondsSinceEpoch * 1000L
}

private fun formatSeconds(total: Int): String {
    val m = total / 60
    val s = total % 60
    return if (m > 0) "$m:${s.toString().padStart(2, '0')}" else "$s с"
}
