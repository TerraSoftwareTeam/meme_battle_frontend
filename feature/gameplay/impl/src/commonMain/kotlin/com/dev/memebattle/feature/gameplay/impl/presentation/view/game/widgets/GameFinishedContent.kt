package com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.network.game.current.dto.ws.ScoreboardEntry

@Composable
fun GameFinishedContent(
    winnerUserId: String?,
    finalScoreboard: List<ScoreboardEntry>,
    myUserId: String,
    getPlayerHandle: (String) -> String? = { null },
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sorted = finalScoreboard
        .map { entry ->
            if (!entry.handle.isNullOrBlank()) entry
            else entry.copy(handle = getPlayerHandle(entry.userId))
        }
        .sortedByDescending { it.score }
    val isWinner = winnerUserId == myUserId

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))

            // ── Иконка замененная на верстку без артефактов шрифтов ───────────────
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            if (isWinner)
                                listOf(Color(0x40FFD700), Color(0x10FFD700))
                            else
                                listOf(Color(0x407C5DFA), Color(0x107C5DFA))
                        )
                    )
                    .border(
                        2.dp,
                        if (isWinner) Color(0xFFFFD700).copy(alpha = 0.6f)
                        else Color(0xFF7C5DFA).copy(alpha = 0.4f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isWinner) "1" else "#",
                    fontSize = 28.sp,
                    color = if (isWinner) Color(0xFFFFD700) else Color(0xFF7C5DFA),
                    fontWeight = FontWeight.Black,
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = if (isWinner) "Победа!" else "Игра завершена",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isWinner) Color(0xFFFFD700) else Color.White,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(6.dp))

            sorted.firstOrNull()?.let { topEntry ->
                val displayName = topEntry.handle
                    ?: winnerUserId?.let { getPlayerHandle(it) }
                    ?: topEntry.userId.take(8)
                Text(
                    text = "Победитель: $displayName",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFFFD700).copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(28.dp))
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            Text(
                text = "Итоговые результаты",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.align(Alignment.Start).widthIn(max = 480.dp),
            )

            Spacer(Modifier.height(12.dp))

            // ── Таблица результатов ─────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(sorted) { index, entry ->
                    LeaderboardRow(
                        position = index + 1,
                        entry = entry,
                        isMe = entry.userId == myUserId,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            GameActionButton(
                label = "Выйти в меню",
                enabled = true,
                isLoading = false,
                onClick = onExit,
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun LeaderboardRow(
    position: Int,
    entry: ScoreboardEntry,
    isMe: Boolean,
    modifier: Modifier = Modifier,
) {
    val medalColor = when (position) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFB0BEC5)
        3 -> Color(0xFFBF8B60)
        else -> null
    }

    val bgBrush = when {
        isMe && position == 1 -> Brush.horizontalGradient(listOf(Color(0x33FFD700), Color(0x1AFFD700)))
        isMe -> Brush.horizontalGradient(listOf(Color(0x227C5DFA), Color(0x157C5DFA)))
        position == 1 -> Brush.horizontalGradient(listOf(Color(0x14FFD700), Color(0x0AFFD700)))
        else -> Brush.horizontalGradient(listOf(Color(0xFF1A1035).copy(alpha = 0.6f), Color(0xFF14102A).copy(alpha = 0.6f)))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgBrush)
            .border(
                1.dp,
                if (isMe)
                    Brush.horizontalGradient(listOf(Color(0x557C5DFA), Color(0x337C5DFA)))
                else if (position == 1)
                    Brush.horizontalGradient(listOf(Color(0x44FFD700), Color(0x22FFD700)))
                else
                    Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)),
                RoundedCornerShape(14.dp),
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Кружок номера позиции
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        medalColor?.copy(alpha = 0.25f)
                            ?: Color.White.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$position",
                    style = MaterialTheme.typography.labelMedium,
                    color = medalColor ?: Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                )
            }

            // Ник
            Text(
                text = (entry.handle ?: entry.userId.take(8)) + if (isMe) " (вы)" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isMe) Color(0xFFB39DDB) else Color.White,
                fontWeight = if (isMe || position == 1) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f),
            )

            // Очки
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF7C5DFA).copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "${entry.score}",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFB39DDB),
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}
