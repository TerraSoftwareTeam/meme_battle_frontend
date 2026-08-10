package com.dev.memebattle.feature.gameplay.impl.presentation.view.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.feature.gameplay.impl.presentation.component.info.GameplayInfoComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.store.info.GameplayInfoStore
import com.dev.memebattle.feature.gameplay.impl.presentation.store.players.GameplayPlayersStore
import com.dev.memebattle.feature.gameplay.impl.presentation.view.info.widgets.CountdownTimer
import com.dev.network.game.current.dto.RoundPhase

@Composable
fun GameplayInfoScreen(
    component: GameplayInfoComponent,
    lobbyPlayersState: GameplayPlayersStore.State? = null,
    myUserId: String = "",
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()

    val playersCount = lobbyPlayersState?.players?.size ?: state.playerCount
    val readyCount = lobbyPlayersState?.players?.count { it.isReady } ?: state.readyCount
    val isHost = lobbyPlayersState?.players?.firstOrNull()?.userId == myUserId
    val canStartGame = isHost && state.phase == RoundPhase.WAITING
            && playersCount >= 2 && readyCount == playersCount && !state.isStartingGame

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A0F38), Color(0xFF100820), Color(0xFF080412))
                )
            ),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                Modifier.align(Alignment.Center),
                color = Color(0xFF7C5DFA),
            )
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // ── Заголовок ─────────────────────────────────────────────────────
            Text(
                text = "Игра",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                text = "Meme Battle",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7C5DFA),
                fontWeight = FontWeight.Medium,
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(Modifier.height(16.dp))

            // ── Статы ─────────────────────────────────────────────────────────
            InfoStatRow(label = "Режим", value = state.modeLabel)
            InfoStatRow(label = "Фаза", value = state.phaseLabel)

            if (state.roundNumber > 0) {
                InfoStatRow(
                    label = "Раунд",
                    value = if (state.totalRounds > 0)
                        "${state.roundNumber} / ${state.totalRounds}"
                    else "${state.roundNumber}",
                )
            }

            Spacer(Modifier.height(4.dp))

            state.phaseExpiresAt?.let {
                CountdownTimer(expiresAt = it)
                Spacer(Modifier.height(4.dp))
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(Modifier.height(12.dp))

            // ── Игроки ────────────────────────────────────────────────────────
            InfoStatRow(label = "Игроков", value = "$playersCount")
            InfoStatRow(label = "Готовы", value = "$readyCount / $playersCount")

            if (state.phase == RoundPhase.SUBMITTING) {
                InfoStatRow(label = "Подали", value = "${state.submittedCount} / $playersCount")
            }
            if (state.phase == RoundPhase.VOTING) {
                InfoStatRow(label = "Голоса", value = "${state.votedCount} / $playersCount")
            }

            Spacer(Modifier.weight(1f))

            // ── Кнопка Start (только хост, только в лобби) ────────────────────
            if (isHost && state.phase == RoundPhase.WAITING) {
                Button(
                    onClick = { component.onIntent(GameplayInfoStore.Intent.StartGame) },
                    enabled = canStartGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 280.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C5DFA),
                        disabledContainerColor = Color(0xFF2A1F44),
                    ),
                ) {
                    if (state.isStartingGame) {
                        CircularProgressIndicator(
                            Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = if (readyCount < playersCount)
                                "Ждём ($readyCount/$playersCount)"
                            else "Начать игру",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun InfoStatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f),
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .padding(horizontal = 10.dp, vertical = 3.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }
}
