package com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStore

/**
 * Фаза Voting — промт слева, submission справа, навигация по submissions + Vote.
 * На small-экране игроки голосуют через PlayersScreen (карточка → диалог).
 */
@Composable
fun VotingContent(
    state: GameplayGameStore.State,
    onSelectSubmission: (Int) -> Unit,
    onVote: (submissionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Голосование",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.7f),
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = if (state.hasVoted) "Вы проголосовали ✓"
                   else "Выберите лучший ответ на промт  (${state.submissionCards.size} вариантов)",
            style = MaterialTheme.typography.bodySmall,
            color = if (state.hasVoted) Color(0xFF00C853) else Color.White.copy(alpha = 0.45f),
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Промт
            GameCardWidget(
                card = state.promptCard,
                label = "Промт",
                modifier = Modifier.weight(1f),
            )
            // Submission для голосования (анонимный)
            GameCardWidget(
                card = state.selectedSubmissionCard,
                label = "Вариант ${state.selectedSubmissionIndex + 1}",
                emptyLabel = "Нет вариантов",
                isHighlighted = !state.hasVoted && state.selectedSubmissionCard != null,
                isSubmitted = state.hasVoted,
                modifier = Modifier.weight(1f),
            )
        }

        CardActionBar(
            actionLabel = if (state.hasVoted) "Проголосовано ✓" else "Голосовать",
            actionEnabled = state.canVote,
            isActionLoading = state.isVoting,
            canNavigatePrev = state.canNavigatePrev,
            canNavigateNext = state.canNavigateNext,
            onPrev = { onSelectSubmission(state.selectedSubmissionIndex - 1) },
            onNext = { onSelectSubmission(state.selectedSubmissionIndex + 1) },
            onAction = {
                state.selectedSubmissionId?.let { onVote(it) }
            },
        )
    }
}
