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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStore

/**
 * Фаза Submitting — промт слева, карта из руки справа, навигация + кнопка Submit снизу.
 */
@Composable
fun SubmittingContent(
    state: GameplayGameStore.State,
    onSelectCard: (Int) -> Unit,
    onSubmit: () -> Unit,
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
            text = "Выберите карту",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.7f),
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = if (state.mySubmissionCard != null) "Вы уже подали карту ✓"
                   else "Подберите подходящий вариант к промту",
            style = MaterialTheme.typography.bodySmall,
            color = if (state.mySubmissionCard != null) Color(0xFF00C853) else Color.White.copy(alpha = 0.45f),
        )

        Spacer(Modifier.height(16.dp))

        // Две карточки
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
                emptyLabel = "Промт загружается…",
                modifier = Modifier.weight(1f),
            )
            // Карта из руки
            GameCardWidget(
                card = state.selectedHandCard,
                label = "Ваша карта",
                emptyLabel = "Нет карт в руке",
                isHighlighted = state.selectedHandCard != null,
                isSubmitted = state.mySubmissionCard == state.selectedHandCard && state.mySubmissionCard != null,
                modifier = Modifier.weight(1f),
            )
        }

        // Навигация и Submit
        CardActionBar(
            actionLabel = if (state.mySubmissionCard != null) "Подано ✓" else "Подать",
            actionEnabled = state.canSubmit,
            isActionLoading = state.isSubmitting,
            canNavigatePrev = state.canNavigatePrev,
            canNavigateNext = state.canNavigateNext,
            onPrev = { onSelectCard(state.selectedCardIndex - 1) },
            onNext = { onSelectCard(state.selectedCardIndex + 1) },
            onAction = onSubmit,
        )
    }
}
