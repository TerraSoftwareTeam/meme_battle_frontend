package com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.gameplay_submitting_btn_submit
import com.dev.memebattle.core.localization.gameplay_submitting_btn_submitted
import com.dev.memebattle.core.localization.gameplay_submitting_hand_label
import com.dev.memebattle.core.localization.gameplay_submitting_hint
import com.dev.memebattle.core.localization.gameplay_submitting_prompt_label
import com.dev.memebattle.core.localization.gameplay_submitting_prompt_loading
import com.dev.memebattle.core.localization.gameplay_submitting_submitted
import com.dev.memebattle.core.localization.gameplay_submitting_subtitle
import com.dev.memebattle.core.localization.gameplay_submitting_title
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStore
import com.dev.network.game.current.dto.MemeGameCard
import com.dev.network.game.current.dto.SituationGameCard
import org.jetbrains.compose.resources.stringResource

@Composable
fun SubmittingContent(
    state: GameplayGameStore.State,
    onSelectCard: (Int) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        val isWide = maxWidth > 600.dp

        if (isWide) {
            SubmittingWideLayout(state, onSelectCard, onSubmit)
        } else {
            SubmittingNarrowLayout(state, onSelectCard, onSubmit)
        }
    }
}

@Composable
private fun SubmittingNarrowLayout(
    state: GameplayGameStore.State,
    onSelectCard: (Int) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(44.dp))

        PhaseHeader(
            title = stringResource(Res.string.gameplay_submitting_title),
            subtitle = if (state.mySubmissionCard != null) stringResource(Res.string.gameplay_submitting_submitted)
                       else stringResource(Res.string.gameplay_submitting_hint),
            subtitleColor = if (state.mySubmissionCard != null) Color(0xFF00C853)
                            else Color.White.copy(alpha = 0.5f),
        )

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.weight(0.45f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            GameCardWidget(
                card = state.promptCard,
                label = stringResource(Res.string.gameplay_submitting_prompt_label),
                emptyLabel = stringResource(Res.string.gameplay_submitting_prompt_loading),
                modifier = Modifier.fillMaxSize(0.75f),
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(modifier = Modifier.weight(0.55f).fillMaxWidth()) {
            HandCardsFan(
                cards = state.handCards.map { card ->
                    when (card) {
                        is MemeGameCard    -> HandCardData.Meme(card.data.id, card.data.mediaUrl)
                        is SituationGameCard -> HandCardData.Situation(card.data.id, card.data.promptText)
                    }
                },
                selectedIndex = state.selectedCardIndex,
                submittedCardId = (state.mySubmissionCard as? MemeGameCard)?.data?.id
                    ?: (state.mySubmissionCard as? SituationGameCard)?.data?.id,
                onCardClick = onSelectCard,
                modifier = Modifier.fillMaxSize(),
            )
        }

        GameActionButton(
            label = if (state.mySubmissionCard != null) stringResource(Res.string.gameplay_submitting_btn_submitted) else stringResource(Res.string.gameplay_submitting_btn_submit),
            enabled = state.canSubmit,
            isLoading = state.isSubmitting,
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun SubmittingWideLayout(
    state: GameplayGameStore.State,
    onSelectCard: (Int) -> Unit,
    onSubmit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 44.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PhaseHeader(
                title = stringResource(Res.string.gameplay_submitting_prompt_label),
                subtitle = stringResource(Res.string.gameplay_submitting_hint),
                subtitleColor = Color.White.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(16.dp))
            GameCardWidget(
                card = state.promptCard,
                label = stringResource(Res.string.gameplay_submitting_prompt_label),
                modifier = Modifier.widthIn(max = 260.dp).aspectRatio(0.68f),
            )
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PhaseHeader(
                title = stringResource(Res.string.gameplay_submitting_hand_label),
                subtitle = if (state.mySubmissionCard != null) stringResource(Res.string.gameplay_submitting_submitted) else stringResource(Res.string.gameplay_submitting_subtitle, state.handCards.size),
                subtitleColor = if (state.mySubmissionCard != null) Color(0xFF00C853)
                                else Color.White.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                HandCardsFan(
                    cards = state.handCards.map { card ->
                        when (card) {
                            is MemeGameCard    -> HandCardData.Meme(card.data.id, card.data.mediaUrl)
                            is SituationGameCard -> HandCardData.Situation(card.data.id, card.data.promptText)
                        }
                    },
                    selectedIndex = state.selectedCardIndex,
                    submittedCardId = (state.mySubmissionCard as? MemeGameCard)?.data?.id
                        ?: (state.mySubmissionCard as? SituationGameCard)?.data?.id,
                    onCardClick = onSelectCard,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.height(12.dp))
            GameActionButton(
                label = if (state.mySubmissionCard != null) stringResource(Res.string.gameplay_submitting_btn_submitted) else stringResource(Res.string.gameplay_submitting_btn_submit),
                enabled = state.canSubmit,
                isLoading = state.isSubmitting,
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun PhaseHeader(
    title: String,
    subtitle: String,
    subtitleColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = subtitleColor,
        )
    }
}

@Composable
fun GameActionButton(
    label: String,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg by animateColorAsState(
        targetValue = if (enabled) Color(0xFF7C5DFA) else Color(0xFF2A2040),
        label = "btnBg",
    )

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
