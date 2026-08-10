package com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStore
import com.dev.network.game.current.dto.MemeGameCard
import com.dev.network.game.current.dto.SituationGameCard

/**
 * Фаза Voting — промт карта + веер submission-карт для голосования.
 * Тапнуть по карте в веере = выбрать её, затем нажать «Голосовать».
 */
@Composable
fun VotingContent(
    state: GameplayGameStore.State,
    onSelectSubmission: (Int) -> Unit,
    onVote: (submissionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        if (maxWidth > 600.dp) {
            VotingWideLayout(state, onSelectSubmission, onVote)
        } else {
            VotingNarrowLayout(state, onSelectSubmission, onVote)
        }
    }
}

// ── Узкий layout ───────────────────────────────────────────────────────────────

@Composable
private fun VotingNarrowLayout(
    state: GameplayGameStore.State,
    onSelectSubmission: (Int) -> Unit,
    onVote: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(12.dp))

        PhaseHeader(
            title = "Голосование",
            subtitle = if (state.hasVoted) "Вы проголосовали"
                       else "${state.submissionCards.size} вариантов · выберите лучший",
            subtitleColor = if (state.hasVoted) Color(0xFF00C853)
                            else Color.White.copy(alpha = 0.5f),
        )

        Spacer(Modifier.height(16.dp))

        // Промт-карта (40% высоты)
        Box(modifier = Modifier.weight(0.38f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            GameCardWidget(
                card = state.promptCard,
                label = "Промт",
                modifier = Modifier.fillMaxSize(0.7f),
            )
        }

        Spacer(Modifier.height(8.dp))

        // Веер submission-карт (55% высоты)
        Box(modifier = Modifier.weight(0.62f).fillMaxWidth()) {
            val submissionCards = state.submissionCards.mapIndexed { i, card ->
                when (card) {
                    is MemeGameCard    -> HandCardData.Meme(card.data.id, card.data.mediaUrl)
                    is SituationGameCard -> HandCardData.Situation(card.data.id, card.data.promptText)
                }
            }
            HandCardsFan(
                cards = submissionCards,
                selectedIndex = state.selectedSubmissionIndex,
                submittedCardId = if (state.hasVoted) state.selectedSubmissionId else null,
                onCardClick = onSelectSubmission,
                modifier = Modifier.fillMaxSize(),
            )
        }

        GameActionButton(
            label = if (state.hasVoted) "Проголосовано" else "Голосовать",
            enabled = state.canVote,
            isLoading = state.isVoting,
            onClick = { state.selectedSubmissionId?.let { onVote(it) } },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        )
    }
}

// ── Широкий layout ─────────────────────────────────────────────────────────────

@Composable
private fun VotingWideLayout(
    state: GameplayGameStore.State,
    onSelectSubmission: (Int) -> Unit,
    onVote: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Промт
        Column(
            modifier = Modifier.weight(1f).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PhaseHeader(
                title = "Промт раунда",
                subtitle = "Выберите лучший ответ",
                subtitleColor = Color.White.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(16.dp))
            GameCardWidget(
                card = state.promptCard,
                label = "Промт",
                modifier = Modifier.widthIn(max = 260.dp).aspectRatio(0.68f),
            )
        }

        // Веер + голосование
        Column(
            modifier = Modifier.weight(1f).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PhaseHeader(
                title = "Варианты",
                subtitle = if (state.hasVoted) "Вы проголосовали"
                           else "${state.submissionCards.size} submission-карт",
                subtitleColor = if (state.hasVoted) Color(0xFF00C853)
                                else Color.White.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(16.dp))
            val submissionCards = state.submissionCards.mapIndexed { i, card ->
                when (card) {
                    is MemeGameCard    -> HandCardData.Meme(card.data.id, card.data.mediaUrl)
                    is SituationGameCard -> HandCardData.Situation(card.data.id, card.data.promptText)
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                HandCardsFan(
                    cards = submissionCards,
                    selectedIndex = state.selectedSubmissionIndex,
                    submittedCardId = if (state.hasVoted) state.selectedSubmissionId else null,
                    onCardClick = onSelectSubmission,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.height(12.dp))
            GameActionButton(
                label = if (state.hasVoted) "Проголосовано" else "Голосовать",
                enabled = state.canVote,
                isLoading = state.isVoting,
                onClick = { state.selectedSubmissionId?.let { onVote(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
