package com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStore
import com.dev.network.game.current.dto.MemeGameCard
import com.dev.network.game.current.dto.SituationGameCard
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.gameplay_voting_btn_vote
import com.dev.memebattle.core.localization.gameplay_voting_btn_voted
import com.dev.memebattle.core.localization.gameplay_voting_cannot_vote_self
import com.dev.memebattle.core.localization.gameplay_voting_prompt_label
import com.dev.memebattle.core.localization.gameplay_voting_submissions_label
import com.dev.memebattle.core.localization.gameplay_voting_subtitle
import com.dev.memebattle.core.localization.gameplay_voting_title
import com.dev.memebattle.core.localization.gameplay_voting_voted
import com.dev.memebattle.core.localization.gameplay_voting_your_card
import org.jetbrains.compose.resources.stringResource

/**
 * Фаза Voting — карта ситуации + веер вариантов для голосования.
 */
@Composable
fun VotingContent(
    state: GameplayGameStore.State,
    onSelectSubmission: (Int) -> Unit,
    onVote: (submissionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Проверка голосования за собственную карту
    val selectedCard = state.selectedSubmissionCard
    val isMyCard = state.mySubmissionCard != null && selectedCard != null && isSameCard(selectedCard, state.mySubmissionCard)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        if (maxWidth > 600.dp) {
            VotingWideLayout(state, isMyCard, onSelectSubmission, onVote)
        } else {
            VotingNarrowLayout(state, isMyCard, onSelectSubmission, onVote)
        }
    }
}

private fun isSameCard(a: com.dev.network.game.current.dto.GameCard, b: com.dev.network.game.current.dto.GameCard): Boolean {
    return when {
        a is MemeGameCard && b is MemeGameCard -> a.data.id == b.data.id || a.data.mediaUrl == b.data.mediaUrl
        a is SituationGameCard && b is SituationGameCard -> a.data.id == b.data.id || a.data.promptText == b.data.promptText
        else -> false
    }
}

// ── Узкий layout ───────────────────────────────────────────────────────────────

@Composable
private fun VotingNarrowLayout(
    state: GameplayGameStore.State,
    isMyCard: Boolean,
    onSelectSubmission: (Int) -> Unit,
    onVote: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(44.dp))

        PhaseHeader(
            title = stringResource(Res.string.gameplay_voting_title),
            subtitle = if (state.hasVoted) stringResource(Res.string.gameplay_voting_voted)
                       else if (isMyCard) stringResource(Res.string.gameplay_voting_your_card)
                       else stringResource(Res.string.gameplay_voting_subtitle, state.submissionCards.size),
            subtitleColor = if (state.hasVoted) Color(0xFF00C853)
                            else if (isMyCard) Color(0xFFFFAB00)
                            else Color.White.copy(alpha = 0.5f),
        )

        Spacer(Modifier.height(12.dp))

        // Предупреждающий баннер при выборе своей карты
        AnimatedVisibility(visible = isMyCard && !state.hasVoted, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFAB00).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFFFAB00).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.gameplay_voting_cannot_vote_self),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFAB00),
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Карта ситуации
        Box(modifier = Modifier.weight(0.38f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            GameCardWidget(
                card = state.promptCard,
                label = stringResource(Res.string.gameplay_voting_prompt_label),
                modifier = Modifier.fillMaxSize(0.7f),
            )
        }

        Spacer(Modifier.height(8.dp))

        // Веер вариантов
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
            label = when {
                state.hasVoted -> stringResource(Res.string.gameplay_voting_btn_voted)
                isMyCard -> stringResource(Res.string.gameplay_voting_your_card)
                else -> stringResource(Res.string.gameplay_voting_btn_vote)
            },
            enabled = state.canVote && !isMyCard,
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
    isMyCard: Boolean,
    onSelectSubmission: (Int) -> Unit,
    onVote: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 44.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Ситуация
        Column(
            modifier = Modifier.weight(1f).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PhaseHeader(
                title = stringResource(Res.string.gameplay_voting_prompt_label),
                subtitle = stringResource(Res.string.gameplay_voting_subtitle, state.submissionCards.size),
                subtitleColor = Color.White.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(16.dp))
            GameCardWidget(
                card = state.promptCard,
                label = stringResource(Res.string.gameplay_voting_prompt_label),
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
                title = stringResource(Res.string.gameplay_voting_submissions_label),
                subtitle = if (state.hasVoted) stringResource(Res.string.gameplay_voting_voted)
                           else if (isMyCard) stringResource(Res.string.gameplay_voting_your_card)
                           else stringResource(Res.string.gameplay_voting_subtitle, state.submissionCards.size),
                subtitleColor = if (state.hasVoted) Color(0xFF00C853)
                                else if (isMyCard) Color(0xFFFFAB00)
                                else Color.White.copy(alpha = 0.5f),
            )

            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(visible = isMyCard && !state.hasVoted, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFAB00).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFFFAB00).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.gameplay_voting_cannot_vote_self),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFAB00),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

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
                label = when {
                    state.hasVoted -> stringResource(Res.string.gameplay_voting_btn_voted)
                    isMyCard -> stringResource(Res.string.gameplay_voting_your_card)
                    else -> stringResource(Res.string.gameplay_voting_btn_vote)
                },
                enabled = state.canVote && !isMyCard,
                isLoading = state.isVoting,
                onClick = { state.selectedSubmissionId?.let { onVote(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
