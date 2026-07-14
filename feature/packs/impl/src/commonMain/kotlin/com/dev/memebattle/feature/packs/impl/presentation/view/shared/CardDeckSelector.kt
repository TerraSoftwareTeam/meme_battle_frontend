package com.dev.memebattle.feature.packs.impl.presentation.view.shared

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.feature.packs.impl.presentation.view.details.DeckAccent
import com.dev.memebattle.feature.packs.impl.presentation.view.details.DeckTextSec
import com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets.PeekingCard
import com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets.SlotMachineLever
import kotlinx.coroutines.launch

@Composable
internal fun CardDeckSelector(
    totalCount: Int,
    selectedIdx: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    cardSlot: @Composable BoxScope.(idx: Int, isSelected: Boolean) -> Unit,
) {
    if (totalCount == 0) return

    val coroutineScope = rememberCoroutineScope()
    val flipAngle = remember { Animatable(0f) }
    var displayedIdx by remember { mutableStateOf(selectedIdx) }

    LaunchedEffect(selectedIdx) { displayedIdx = selectedIdx }

    fun flipTo(newIdx: Int) {
        if (newIdx == displayedIdx) return
        coroutineScope.launch {
            flipAngle.animateTo(-90f, tween(200, easing = FastOutSlowInEasing))
            displayedIdx = newIdx
            onSelect(newIdx)
            flipAngle.snapTo(90f)
            flipAngle.animateTo(0f, tween(200, easing = FastOutLinearInEasing))
        }
    }

    val pageSize = 5
    val totalPages = ((totalCount - 1) / pageSize) + 1
    val currentPage = displayedIdx / pageSize

    fun flipToPage(page: Int) {
        val targetIdx = (page * pageSize).coerceIn(0, totalCount - 1)
        flipTo(targetIdx)
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {


        Column(
            modifier = Modifier.width(16.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            repeat(totalPages.coerceAtMost(10)) { pageIdx ->
                val active = pageIdx == currentPage
                val sz by animateDpAsState(if (active) 7.dp else 4.dp, label = "ds")
                Box(
                    modifier = Modifier
                        .size(sz)
                        .clip(CircleShape)
                        .background(if (active) DeckAccent else DeckTextSec.copy(0.3f))
                        .clickable { flipToPage(pageIdx) }
                )
                if (pageIdx < totalPages.coerceAtMost(10) - 1) Spacer(Modifier.height(5.dp))
            }
            if (totalPages > 10) {
                Spacer(Modifier.height(4.dp))
                Text("…", color = DeckTextSec.copy(0.5f), fontSize = 9.sp)
            }
        }

        Spacer(Modifier.width(8.dp))


        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .graphicsLayer {
                    rotationX = flipAngle.value
                    cameraDistance = 12f * density
                },
            contentAlignment = Alignment.Center
        ) {
            val startIdx = currentPage * pageSize
            val visibleCount = pageSize

            val spacingDp = 5.dp
            val totalSpacing = spacingDp * (visibleCount - 1)
            val maxCardWidth = (maxWidth - totalSpacing) / visibleCount
            val maxCardHeight = maxHeight * 0.82f

            val cardHeight = minOf(maxCardHeight, maxCardWidth * 4f / 3f)
            val cardWidth = cardHeight * 3f / 4f

            Row(
                horizontalArrangement = Arrangement.spacedBy(spacingDp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (slotIdx in 0 until visibleCount) {
                    val cardIdx = startIdx + slotIdx
                    if (cardIdx < totalCount) {
                        val isSelected   = cardIdx == displayedIdx
                        val peekFraction = if (isSelected) 0.80f else 0.36f

                        Box(
                            modifier = Modifier
                                .size(width = cardWidth, height = cardHeight)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (!isSelected) {
                                        displayedIdx = cardIdx
                                        onSelect(cardIdx)
                                    }
                                }
                        ) {
                            PeekingCard(
                                peekFraction = peekFraction,
                                isSelected   = isSelected,
                                modifier     = Modifier.fillMaxSize(),
                            ) {
                                cardSlot(cardIdx, isSelected)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.size(width = cardWidth, height = cardHeight)
                        ) {
                            com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets.CardBack(
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.width(8.dp))


        SlotMachineLever(
            onFlipUp   = { flipToPage((currentPage - 1).coerceAtLeast(0)) },
            onFlipDown = { flipToPage((currentPage + 1).coerceAtMost(totalPages - 1)) },
            canUp      = currentPage > 0,
            canDown    = currentPage < totalPages - 1,
            modifier   = Modifier.width(38.dp).fillMaxHeight(),
        )
    }
}
