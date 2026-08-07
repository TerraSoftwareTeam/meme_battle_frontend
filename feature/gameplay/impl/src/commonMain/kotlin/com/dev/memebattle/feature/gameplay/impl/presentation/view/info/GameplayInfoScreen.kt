package com.dev.memebattle.feature.gameplay.impl.presentation.view.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.feature.gameplay.impl.presentation.component.info.GameplayInfoComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.store.info.GameplayInfoStore
import com.dev.memebattle.feature.gameplay.impl.presentation.view.info.widgets.CountdownTimer
import com.dev.memebattle.feature.gameplay.impl.presentation.view.info.widgets.InfoRow
import com.dev.network.game.current.dto.RoundPhase

/**
 * Боковая панель с информацией об игре.
 * Содержит только вёрстку — вся логика в [GameplayInfoStore].
 */
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import com.dev.memebattle.feature.gameplay.impl.presentation.store.players.GameplayPlayersStore

@Composable
fun GameplayInfoScreen(
    component: GameplayInfoComponent,
    lobbyPlayersState: GameplayPlayersStore.State?,
    myUserId: String,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()

    val playersCount = lobbyPlayersState?.players?.size ?: state.playerCount
    val readyCount = lobbyPlayersState?.players?.count { it.isReady } ?: state.readyCount
    val isHost = lobbyPlayersState?.players?.firstOrNull()?.userId == myUserId
    val canStartGame = isHost && state.phase == RoundPhase.WAITING && playersCount >= 2 && readyCount == playersCount && !state.isStartingGame

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E1035),
            Color(0xFF0F081D),
            Color(0xFF08040F)
        )
    )

    if (state.isLoading) {
        Box(modifier.fillMaxSize().background(backgroundBrush), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF7C5DFA))
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Meme Battle",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

            InfoRow(label = "Режим", value = state.modeLabel)
            InfoRow(label = "Фаза", value = state.phaseLabel)

            if (state.roundNumber > 0) {
                InfoRow(
                    label = "Раунд",
                    value = if (state.totalRounds > 0) "${state.roundNumber} / ${state.totalRounds}"
                    else "${state.roundNumber}",
                )
            }

            state.phaseExpiresAt?.let {
                CountdownTimer(expiresAt = it)
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

            InfoRow(label = "Игроков", value = "$playersCount")
            InfoRow(label = "Готовы", value = "$readyCount / $playersCount")

            if (state.phase == RoundPhase.SUBMITTING) {
                InfoRow(label = "Подали", value = "${state.submittedCount} / $playersCount")
            }
            if (state.phase == RoundPhase.VOTING) {
                InfoRow(label = "Проголосовали", value = "${state.votedCount} / $playersCount")
            }

            Spacer(Modifier.weight(1f))

            // Кнопка "Начать игру" — только хост, только в лобби
            if (isHost && state.phase == RoundPhase.WAITING) {
                Button(
                    onClick = { component.onIntent(GameplayInfoStore.Intent.StartGame) },
                    enabled = canStartGame,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C5DFA),
                        disabledContainerColor = Color(0xFF2A1F44),
                    ),
                ) {
                    if (state.isStartingGame) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = if (readyCount < playersCount)
                                "Ждём готовности ($readyCount/$playersCount)"
                            else "Начать игру",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}
