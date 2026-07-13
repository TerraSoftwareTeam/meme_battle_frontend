package com.dev.memebattle.feature.packs.impl.presentation.view.details

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.abs

internal val DeckAccent  = Color(0xFF7C5DFA)
internal val DeckSurface = Color(0xFF211640)
internal val DeckBorder  = Color(0xFF3A2860)
internal val DeckTextPri = Color(0xFFFFFFFF)
internal val DeckTextSec = Color(0xFFB0A2C7)

internal val SituationAccents = listOf(
    Color(0xFFFF5252), Color(0xFFFFEB3B), Color(0xFF00E676),
    Color(0xFF00B0FF), Color(0xFFD500F9), Color(0xFFFF6D00),
    Color(0xFF00BCD4), Color(0xFFE91E63),
)

// ── Card back (рубашка) ───────────────────────────────────────────────────────
@Composable
internal fun CardBack(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(listOf(Color(0xFF2A1B5E), Color(0xFF1A0D3D)))
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 18f
            val lineColor = Color(0xFF7C5DFA).copy(alpha = 0.18f)
            var x = 0f
            while (x < size.width) {
                drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += step
            }
            // Diagonal pattern
            val diagColor = Color(0xFF7C5DFA).copy(alpha = 0.09f)
            var d = -size.height
            while (d < size.width) {
                drawLine(diagColor, Offset(d, 0f), Offset(d + size.height, size.height), strokeWidth = 1f)
                d += step * 2
            }
        }
        // Inner border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Transparent)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = Color(0xFF7C5DFA).copy(alpha = 0.3f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f),
                )
            }
        }
    }
}

// ── Slot-Machine Lever ────────────────────────────────────────────────────────
@Composable
internal fun SlotMachineLever(
    onFlipUp: () -> Unit,
    onFlipDown: () -> Unit,
    canUp: Boolean,
    canDown: Boolean,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val leverOffset = remember { Animatable(0f) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val thresholdPx = with(density) { 24.dp.toPx() }

    val leverColor by animateColorAsState(
        targetValue = when {
            leverOffset.value < -thresholdPx && canUp -> DeckAccent
            leverOffset.value > thresholdPx && canDown -> DeckAccent
            else -> Color(0xFF4A3880)
        },
        animationSpec = tween(150), label = "lc",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Track
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight(0.8f)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF2E2452))
        )
        // Thumb
        Box(
            modifier = Modifier
                .size(36.dp)
                .graphicsLayer { translationY = leverOffset.value }
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(leverColor)
                .pointerInput(canUp, canDown) {
                    val maxTravelPx = 36.dp.toPx()
                    val triggerPx = 24.dp.toPx()
                    detectVerticalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                when {
                                    leverOffset.value < -triggerPx && canUp -> onFlipUp()
                                    leverOffset.value > triggerPx && canDown -> onFlipDown()
                                }
                                leverOffset.animateTo(0f, spring(dampingRatio = 0.4f, stiffness = 500f))
                            }
                        },
                        onVerticalDrag = { _, dy ->
                            coroutineScope.launch {
                                leverOffset.snapTo((leverOffset.value + dy).coerceIn(-maxTravelPx, maxTravelPx))
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                repeat(3) {
                    Box(Modifier.width(16.dp).height(2.dp).background(Color.White.copy(0.5f), RoundedCornerShape(1.dp)))
                }
            }
        }
    }
}

@Composable
internal fun PeekingCard(
    peekFraction: Float,      // how much of the real content is visible (0..1)
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    cardContent: @Composable BoxScope.() -> Unit,
) {
    val animPeek by animateFloatAsState(peekFraction, spring(0.65f, 280f), label = "peek")

    Box(modifier = modifier) {
        // Full card (image/text) — fills the entire container so it maintains original ratio
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(if (isSelected) 8.dp else 2.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
        ) {
            cardContent()
        }

        // Рубашка (card back) — draws on top, covering the bottom portion
        if (animPeek < 0.99f) {
            val backHeight = 1f - animPeek
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(backHeight.coerceAtLeast(0.01f))
                    .align(Alignment.BottomCenter)
            ) {
                CardBack(modifier = Modifier.fillMaxSize())
                // Selection highlight border on the рубашка
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

// ── 3D Deck Flip Container ────────────────────────────────────────────────────
// Wraps content in a 3D flip animation on the X axis (top-to-bottom).
// flipAngle: 0f = front, ±90f = edge (invisible), ±180f = back
@Composable
internal fun FlipContainer(flipAngle: Float, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.graphicsLayer {
            rotationX = flipAngle
            cameraDistance = 14f * density
        }
    ) { content() }
}

// ── Full Card Deck Selector ───────────────────────────────────────────────────
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

        // ── Dot indicators (left) — one dot per PAGE ──────────────────────
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
                        .background(if (active) DeckAccent else DeckTextSec.copy(0.3f), CircleShape)
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

        // ── Row of cards (center) — flip animation applied to the whole row ─
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
            val startIdx = (displayedIdx - 2).coerceIn(0, (totalCount - 5).coerceAtLeast(0))
            val endIdx   = (startIdx + 4).coerceAtMost(totalCount - 1)
            val visibleCount = endIdx - startIdx + 1

            // Measure sizes in Dp dynamically based on container constraints
            val spacingDp = 5.dp
            val totalSpacing = spacingDp * (visibleCount - 1)
            val maxCardWidth = (maxWidth - totalSpacing) / visibleCount
            val maxCardHeight = maxHeight * 0.82f

            // Fit height using 3:4 ratio constraint
            val cardHeight = minOf(maxCardHeight, maxCardWidth * 4f / 3f)
            val cardWidth = cardHeight * 3f / 4f

            Row(
                horizontalArrangement = Arrangement.spacedBy(spacingDp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (cardIdx in startIdx..endIdx) {
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
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        // ── Lever (right) — navigates by page (5 cards) ───────────────────
        SlotMachineLever(
            onFlipUp   = { flipToPage((currentPage + 1).coerceAtMost(totalPages - 1)) },
            onFlipDown = { flipToPage((currentPage - 1).coerceAtLeast(0)) },
            canUp      = currentPage < totalPages - 1,
            canDown    = currentPage > 0,
            modifier   = Modifier.width(38.dp).fillMaxHeight(),
        )
    }
}

