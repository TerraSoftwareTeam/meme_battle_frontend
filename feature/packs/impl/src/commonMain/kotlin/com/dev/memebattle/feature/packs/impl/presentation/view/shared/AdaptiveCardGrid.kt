package com.dev.memebattle.feature.packs.impl.presentation.view.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dev.memebattle.feature.packs.impl.presentation.view.details.DeckAccent

/**
 * Adaptive card grid — non-lazy, safe to embed inside any scrollable parent
 * (e.g. [Column] with `verticalScroll`, [LazyColumn]).
 *
 * Column count adapts to the available width: a new column is added every time
 * the card width would exceed [maxCardWidth]. At least [minColumns] columns
 * are always shown.
 *
 * Aspect ratio 3:4 is preserved for every card cell.
 * The selected card is highlighted with a 2 dp themed accent border.
 *
 * @param cards         List of card data items.
 * @param selectedIdx   Index of the currently selected (highlighted) card.
 * @param onSelect      Called with the tapped index when the user taps a card.
 * @param modifier      Applied to the root [BoxWithConstraints].
 * @param maxCardWidth  Maximum card width before a new column is added (default 130 dp).
 * @param minColumns    Minimum number of columns (default 2).
 * @param cardSpacing   Gap between cards (default 8 dp).
 * @param cardContent   Composable slot rendered inside each card cell.
 */
@Composable
internal fun <T> StaticAdaptiveCardGrid(
    cards: List<T>,
    selectedIdx: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxCardWidth: Dp = 130.dp,
    minColumns: Int = 2,
    cardSpacing: Dp = 8.dp,
    cardContent: @Composable BoxScope.(item: T, isSelected: Boolean) -> Unit,
) {
    if (cards.isEmpty()) return

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = (maxWidth / (maxCardWidth + cardSpacing))
            .toInt()
            .coerceAtLeast(minColumns)

        val rows = cards.chunked(columns)

        Column(verticalArrangement = Arrangement.spacedBy(cardSpacing)) {
            rows.forEachIndexed { rowIdx, rowCards ->
                Row(horizontalArrangement = Arrangement.spacedBy(cardSpacing)) {
                    rowCards.forEachIndexed { colIdx, item ->
                        val idx = rowIdx * columns + colIdx
                        val isSelected = idx == selectedIdx
                        Box(modifier = Modifier.weight(1f)) {
                            CardGridItem(
                                idx = idx,
                                isSelected = isSelected,
                                onSelect = onSelect,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                cardContent(item, isSelected)
                            }
                        }
                    }
                    // Fill trailing empty slots in the last row so weights balance
                    val remainder = columns - rowCards.size
                    repeat(remainder) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** Single card cell — shared styling for both grid variants. */
@Composable
private fun CardGridItem(
    idx: Int,
    isSelected: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .shadow(if (isSelected) 6.dp else 1.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (isSelected) Modifier.border(
                    border = BorderStroke(2.dp, DeckAccent),
                    shape = RoundedCornerShape(10.dp),
                ) else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onSelect(idx) },
    ) {
        content()
    }
}
