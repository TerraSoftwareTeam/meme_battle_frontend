package com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.SubcomposeAsyncImage
import com.dev.memebattle.core.network.utils.normalizeMediaUrl
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.gameplay_submitting_empty_hand
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

sealed interface HandCardData {
    val id: String
    data class Meme(override val id: String, val imageUrl: String) : HandCardData
    data class Situation(override val id: String, val text: String) : HandCardData
    data class Unknown(override val id: String) : HandCardData
}

@Composable
fun HandCardsFan(
    cards: List<HandCardData>,
    selectedIndex: Int,
    submittedCardId: String?,
    onCardClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    cardAspect: Float = 0.68f,
) {
    if (cards.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(Res.string.gameplay_submitting_empty_hand),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.4f),
            )
        }
        return
    }

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val containerW = maxWidth

        val cardW: Dp = (containerW * 0.45f).coerceAtMost(200.dp)
        val cardH: Dp = cardW / cardAspect

        val count = cards.size
        val mid = (count - 1) / 2f

        val maxRotation = when (count) {
            1 -> 0f; 2 -> 5f; 3 -> 10f; 4 -> 14f; 5 -> 18f; else -> 22f
        }
        val maxTransX = when (count) {
            1 -> 0f; 2 -> 0.18f; 3 -> 0.25f; 4 -> 0.30f; 5 -> 0.34f; else -> 0.38f
        } * containerW.value

        cards.forEachIndexed { i, card ->
            val dist = i - mid
            val baseRot = if (mid == 0f) 0f else dist * (maxRotation / mid)
            val baseTx = if (mid == 0f) 0f else dist * (maxTransX / mid)
            val baseTy = abs(dist) * (cardH.value * 0.07f)

            val isSelected = i == selectedIndex
            val isSubmitted = card.id == submittedCardId

            val liftY by animateFloatAsState(
                targetValue = if (isSelected) -cardH.value * 0.12f else 0f,
                animationSpec = spring(stiffness = 280f, dampingRatio = 0.7f),
                label = "lift$i",
            )
            val liftScale by animateFloatAsState(
                targetValue = if (isSelected) 1.06f else 1.0f,
                animationSpec = spring(stiffness = 280f, dampingRatio = 0.7f),
                label = "scale$i",
            )

            val zIndex = if (isSelected) count.toFloat() + 1f else count - abs(dist)

            Box(
                modifier = Modifier
                    .size(cardW, cardH)
                    .zIndex(zIndex)
                    .graphicsLayer {
                        rotationZ = baseRot
                        translationX = baseTx.dp.toPx()
                        translationY = (baseTy + liftY).dp.toPx()
                        scaleX = liftScale
                        scaleY = liftScale
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onCardClick(i) },
            ) {
                FanCardItem(card = card, isSelected = isSelected, isSubmitted = isSubmitted)
            }
        }
    }
}

@Composable
private fun FanCardItem(
    card: HandCardData,
    isSelected: Boolean,
    isSubmitted: Boolean,
) {
    val shape = RoundedCornerShape(14.dp)

    val borderBrush = when {
        isSubmitted -> Brush.linearGradient(listOf(Color(0xFF00C853), Color(0xFF00E676)))
        isSelected  -> Brush.linearGradient(listOf(Color(0xFF9D7BFF), Color(0xFF7C5DFA)))
        else        -> Brush.linearGradient(listOf(Color(0xFF8B6BFF).copy(alpha = 0.5f), Color(0xFF4E389E).copy(alpha = 0.35f)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(Color.Black, shape)
            .border(
                width = if (isSelected || isSubmitted) 2.5.dp else 1.5.dp,
                brush = borderBrush,
                shape = shape,
            )
            .drawBehind {
                if (isSelected) {
                    drawRect(
                        brush = Brush.radialGradient(
                            listOf(Color(0x337C5DFA), Color.Transparent),
                            center = Offset(size.width / 2, size.height),
                            radius = size.width,
                        )
                    )
                }
            },
    ) {
        when (card) {
            is HandCardData.Meme -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black, shape)
                    .clip(shape),
                contentAlignment = Alignment.Center,
            ) {
                SubcomposeAsyncImage(
                    model = normalizeMediaUrl(card.imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                    loading = { MemeShimmerSmall() },
                    error = {
                        Box(
                            Modifier.fillMaxSize().background(Color(0xFF1A1035)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Ошибка загрузки",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    },
                )
            }

            is HandCardData.Situation -> {
                val accentIndex = (card.id.hashCode() and 0x7FFFFFFF) % SituationAccents.size
                val accent = SituationAccents[accentIndex]
                val textLength = card.text.length
                val (fontSize, lineHeight) = when {
                    textLength < 40  -> 12.sp to 16.sp
                    textLength < 80  -> 10.sp to 14.sp
                    textLength < 140 -> 9.sp to 12.sp
                    else             -> 8.sp to 10.sp
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF1A0D3D), Color(0xFF0D0620))),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = card.text,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                    )
                }
            }

            is HandCardData.Unknown -> Box(
                Modifier.fillMaxSize().background(Color(0xFF1A1035)),
                contentAlignment = Alignment.Center,
            ) { Text("?", fontSize = 28.sp, color = Color.White.copy(alpha = 0.3f)) }
        }

        if (isSubmitted) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color(0xFF00C853), CircleShape)
                    .size(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("v", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private val SituationAccents = listOf(
    Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFFFFE66D),
    Color(0xFFA29BFE), Color(0xFFFF7675), Color(0xFF74B9FF),
)

@Composable
private fun MemeShimmerSmall() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1035)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = Color(0xFF7C5DFA).copy(alpha = 0.6f),
            strokeWidth = 2.dp,
        )
    }
}
