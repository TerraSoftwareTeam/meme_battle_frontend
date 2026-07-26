package com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.dev.memebattle.feature.packs.impl.presentation.view.details.DeckAccent
import com.dev.memebattle.core.ui.components.pack.CardBack

@Composable
internal fun PeekingCard(
    peekFraction: Float,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    cardContent: @Composable BoxScope.() -> Unit,
) {
    val animPeek by animateFloatAsState(peekFraction, spring(0.65f, 280f), label = "peek")

    Box(modifier = modifier) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(if (isSelected) 8.dp else 2.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
        ) {
            cardContent()
        }


        if (animPeek < 0.99f) {
            val backHeight = 1f - animPeek
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(backHeight.coerceAtLeast(0.01f))
                    .align(Alignment.BottomCenter)
            ) {
                CardBack(modifier = Modifier.fillMaxSize())

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeckAccent.copy(alpha = 0.15f))
                    )
                }
            }
        }
    }
}
