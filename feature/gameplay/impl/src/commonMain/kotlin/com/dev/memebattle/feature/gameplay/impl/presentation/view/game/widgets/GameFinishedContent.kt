package com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.network.game.current.dto.ws.ScoreboardEntry

/**
 * Финальный полноэкранный оверлей с пьедесталом победителей.
 * Показывается когда [GameplayGameStore.UiPhase] == GameFinished.
 */
@Composable
fun GameFinishedContent(
    winnerUserId: String?,
    finalScoreboard: List<ScoreboardEntry>,
    myUserId: String,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sorted = finalScoreboard.sortedByDescending { it.score }

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

            val isWinner = winnerUserId == myUserId
            Text(
                text = if (isWinner) "Вы победили!" else "Игра завершена",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )

            sorted.firstOrNull()?.let { winner ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Победитель: ${winner.handle ?: winner.userId.take(8)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(32.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(Modifier.height(16.dp))

            // Таблица лидеров
            LazyColumn(
                modifier = Modifier.weight(1f),
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

            Button(
                onClick = onExit,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text("Выйти в главное меню", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(Modifier.height(24.dp))
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (isMe) 4.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "$position.",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
            Text(
                text = (entry.handle ?: entry.userId.take(8)) + if (isMe) " (я)" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "Очки: ${entry.score}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
