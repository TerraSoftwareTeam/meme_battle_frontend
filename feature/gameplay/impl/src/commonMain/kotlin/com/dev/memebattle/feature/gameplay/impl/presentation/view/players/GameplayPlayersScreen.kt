package com.dev.memebattle.feature.gameplay.impl.presentation.view.players

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.memebattle.feature.gameplay.impl.presentation.component.players.GameplayPlayersComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStore
import com.dev.memebattle.feature.gameplay.impl.presentation.store.players.GameplayPlayersStore
import com.dev.memebattle.feature.gameplay.impl.presentation.view.players.widgets.PlayerCard
import com.dev.memebattle.feature.gameplay.impl.presentation.view.players.widgets.SubmissionPreviewDialog

/**
 * Экран списка игроков.
 *
 * @param uiPhase   текущая фаза из GameStore — нужна чтобы показывать кнопку "Посмотреть карту"
 *                  только в Voting.
 * @param hasVoted  уже ли проголосовал текущий пользователь (из GameStore.state.hasVoted)
 * @param isVoting  идёт запрос голосования
 */
@Composable
fun GameplayPlayersScreen(
    component: GameplayPlayersComponent,
    uiPhase: GameplayGameStore.UiPhase = GameplayGameStore.UiPhase.Lobby,
    hasVoted: Boolean = false,
    isVoting: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .padding(top = 16.dp, start = 12.dp, end = 12.dp),
    ) {
        Text(
            text = "Игроки (${state.players.size})",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.players, key = { it.userId }) { player ->
                PlayerCard(
                    player = player,
                    uiPhase = uiPhase,
                    onShowCard = {
                        component.onIntent(GameplayPlayersStore.Intent.ShowSubmissionPreview(player.userId))
                    },
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    // Диалог предпросмотра карты (показывается поверх всего)
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
