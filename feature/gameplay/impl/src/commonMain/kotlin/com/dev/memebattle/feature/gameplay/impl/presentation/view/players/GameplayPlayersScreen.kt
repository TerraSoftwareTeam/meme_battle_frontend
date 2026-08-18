package com.dev.memebattle.feature.gameplay.impl.presentation.view.players

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.dev.memebattle.feature.gameplay.impl.presentation.component.players.GameplayPlayersComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStore
import com.dev.memebattle.feature.gameplay.impl.presentation.store.players.GameplayPlayersStore
import com.dev.memebattle.feature.gameplay.impl.presentation.view.players.widgets.PlayerAvatar
import com.dev.memebattle.feature.gameplay.impl.presentation.view.players.widgets.SubmissionPreviewDialog
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.gameplay_players_me_badge
import com.dev.memebattle.core.localization.gameplay_players_status_ready
import com.dev.memebattle.core.localization.gameplay_players_status_submitted
import com.dev.memebattle.core.localization.gameplay_players_status_voted
import com.dev.memebattle.core.localization.gameplay_players_status_waiting
import com.dev.memebattle.core.localization.gameplay_players_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameplayPlayersScreen(
    component: GameplayPlayersComponent,
    uiPhase: GameplayGameStore.UiPhase = GameplayGameStore.UiPhase.Lobby,
    hasVoted: Boolean = false,
    isVoting: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()

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
                .padding(top = 16.dp, start = 12.dp, end = 12.dp),
        ) {
            // ── Заголовок ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.gameplay_players_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                // Счётчик
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF7C5DFA).copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = "${state.players.size}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB39DDB),
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(Modifier.height(8.dp))

            // ── Список ────────────────────────────────────────────────────────
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(state.players, key = { it.userId }) { player ->
                    PlayerRow(
                        player = player,
                        uiPhase = uiPhase,
                        onShowCard = {
                            component.onIntent(
                                GameplayPlayersStore.Intent.ShowSubmissionPreview(player.userId)
                            )
                        },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Диалог предпросмотра карты
        state.previewPlayer?.let { player ->
            SubmissionPreviewDialog(
                player = player,
                hasAlreadyVoted = hasVoted,
                isVoting = isVoting,
                onVote = { submissionId ->
                    component.onIntent(GameplayPlayersStore.Intent.VoteForPlayer(submissionId))
                },
                onDismiss = {
                    component.onIntent(GameplayPlayersStore.Intent.HideSubmissionPreview)
                },
            )
        }
    }
}

// ── Строка игрока ─────────────────────────────────────────────────────────────

@Composable
private fun PlayerRow(
    player: GameplayPlayersStore.PlayerUiModel,
    uiPhase: GameplayGameStore.UiPhase,
    onShowCard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (player.isMe) Color(0xFF7C5DFA).copy(alpha = 0.12f)
                else Color.White.copy(alpha = 0.04f)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Аватар
        PlayerAvatar(handle = player.handle, size = 32.dp)

        // Ник
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = player.handle.ifBlank { player.userId.take(8) },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (player.isMe) FontWeight.Bold else FontWeight.Normal,
                color = if (player.isMe) Color(0xFFB39DDB) else Color.White,
                maxLines = 1,
            )
            if (player.isMe) {
                Text(
                    text = stringResource(Res.string.gameplay_players_me_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF7C5DFA),
                )
            }
        }

        // Счёт
        if (player.score > 0) {
            Text(
                text = "${player.score}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
            )
        }

        // Статус-бейдж
        PlayerStatusBadge(player = player, uiPhase = uiPhase)
    }
}

// ── Статус-бейдж ──────────────────────────────────────────────────────────────

@Composable
private fun PlayerStatusBadge(
    player: GameplayPlayersStore.PlayerUiModel,
    uiPhase: GameplayGameStore.UiPhase,
) {
    val text: String
    val color: Color

    when (uiPhase) {
        GameplayGameStore.UiPhase.Lobby -> {
            text = if (player.isReady) stringResource(Res.string.gameplay_players_status_ready)
                   else stringResource(Res.string.gameplay_players_status_waiting)
            color = if (player.isReady) Color(0xFF00C853)
                    else Color.White.copy(alpha = 0.35f)
        }
        GameplayGameStore.UiPhase.Submitting -> {
            text = if (player.hasSubmitted) stringResource(Res.string.gameplay_players_status_submitted)
                   else "..."
            color = if (player.hasSubmitted) Color(0xFF7C5DFA)
                    else Color.White.copy(alpha = 0.25f)
        }
        GameplayGameStore.UiPhase.Voting -> {
            text = if (player.hasVoted) stringResource(Res.string.gameplay_players_status_voted)
                   else "..."
            color = if (player.hasVoted) Color(0xFF00BCD4)
                    else Color.White.copy(alpha = 0.25f)
        }
        else -> return
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}
