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
import com.dev.network.game.current.dto.GameMode
import com.dev.network.game.current.dto.RoundPhase
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.gameplay_info_header_game
import com.dev.memebattle.core.localization.gameplay_info_min_players
import com.dev.memebattle.core.localization.gameplay_info_mode
import com.dev.memebattle.core.localization.gameplay_info_phase
import com.dev.memebattle.core.localization.gameplay_info_players
import com.dev.memebattle.core.localization.gameplay_info_ready
import com.dev.memebattle.core.localization.gameplay_info_round
import com.dev.memebattle.core.localization.gameplay_info_start_game
import com.dev.memebattle.core.localization.gameplay_info_submitted
import com.dev.memebattle.core.localization.gameplay_info_title
import com.dev.memebattle.core.localization.gameplay_info_exceeded_tag
import com.dev.memebattle.core.localization.gameplay_info_max_players_exceeded
import com.dev.memebattle.core.localization.gameplay_info_max_players_label
import com.dev.memebattle.core.localization.gameplay_info_min_players_label
import com.dev.memebattle.core.localization.gameplay_info_too_many_players
import com.dev.memebattle.core.localization.gameplay_info_voted
import com.dev.memebattle.core.localization.gameplay_info_wait_ready
import com.dev.memebattle.core.localization.gameplay_phase_finished
import com.dev.memebattle.core.localization.gameplay_phase_submitting
import com.dev.memebattle.core.localization.gameplay_phase_voting
import com.dev.memebattle.core.localization.gameplay_phase_waiting
import com.dev.memebattle.core.localization.lobby_create_mode_meme_to_situation
import com.dev.memebattle.core.localization.lobby_create_mode_situation_to_meme
import org.jetbrains.compose.resources.stringResource

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
    val isHost = state.isHost
    val maxPlayers = state.maxPlayers
    val isTooManyPlayersError = state.isTooManyPlayersError
            || (state.blockedAtPlayerCount != null && playersCount >= state.blockedAtPlayerCount!!)
    val isMaxExceeded = isTooManyPlayersError || (maxPlayers != null && maxPlayers > 0 && playersCount > maxPlayers)

    val canStartGame = isHost && state.phase == RoundPhase.WAITING
            && playersCount >= 3
            && !isMaxExceeded
            && readyCount == playersCount
            && !state.isStartingGame

    val modeStr = when (state.mode) {
        GameMode.SITUATION_TO_MEME -> stringResource(Res.string.lobby_create_mode_situation_to_meme)
        GameMode.MEME_TO_SITUATION -> stringResource(Res.string.lobby_create_mode_meme_to_situation)
        null -> "—"
    }

    val phaseStr = when (state.phase) {
        RoundPhase.WAITING -> stringResource(Res.string.gameplay_phase_waiting)
        RoundPhase.SUBMITTING -> stringResource(Res.string.gameplay_phase_submitting)
        RoundPhase.VOTING -> stringResource(Res.string.gameplay_phase_voting)
        RoundPhase.FINISHED -> stringResource(Res.string.gameplay_phase_finished)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E1035), Color(0xFF0F081D), Color(0xFF08040F))
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
                text = stringResource(Res.string.gameplay_info_header_game),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                text = stringResource(Res.string.gameplay_info_title),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7C5DFA),
                fontWeight = FontWeight.Medium,
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(Modifier.height(16.dp))

            // ── Статы ─────────────────────────────────────────────────────────
            InfoStatRow(label = stringResource(Res.string.gameplay_info_mode), value = modeStr)
            InfoStatRow(label = stringResource(Res.string.gameplay_info_phase), value = phaseStr)

            if (state.roundNumber > 0) {
                InfoStatRow(
                    label = stringResource(Res.string.gameplay_info_round),
                    value = if (state.totalRounds > 0)
                        "${state.roundNumber} / ${state.totalRounds}"
                    else "${state.roundNumber}",
                )
            }

            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(Modifier.height(12.dp))

            // ── Игроки ────────────────────────────────────────────────────────
            InfoStatRow(label = stringResource(Res.string.gameplay_info_players), value = "$playersCount")
            InfoStatRow(label = stringResource(Res.string.gameplay_info_ready), value = "$readyCount / $playersCount")
            InfoStatRow(label = stringResource(Res.string.gameplay_info_min_players_label), value = "${state.minPlayers}")
            if (maxPlayers != null && maxPlayers > 0) {
                val tagExceeded = stringResource(Res.string.gameplay_info_exceeded_tag)
                InfoStatRow(
                    label = stringResource(Res.string.gameplay_info_max_players_label),
                    value = if (isMaxExceeded) "$maxPlayers $tagExceeded" else "$maxPlayers"
                )
            }

            if (state.phase == RoundPhase.SUBMITTING) {
                InfoStatRow(label = stringResource(Res.string.gameplay_info_submitted), value = "${state.submittedCount} / $playersCount")
            }
            if (state.phase == RoundPhase.VOTING) {
                InfoStatRow(label = stringResource(Res.string.gameplay_info_voted), value = "${state.votedCount} / $playersCount")
            }

            Spacer(Modifier.weight(1f))
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
