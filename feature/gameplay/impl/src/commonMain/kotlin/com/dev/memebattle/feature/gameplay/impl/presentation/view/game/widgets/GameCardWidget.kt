package com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.dev.memebattle.core.network.utils.normalizeMediaUrl
import com.dev.network.game.current.dto.GameCard
import com.dev.network.game.current.dto.MemeGameCard
import com.dev.network.game.current.dto.SituationGameCard

private val MAX_CARD_WIDTH = 300.dp

private val SituationAccents = listOf(
    Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFFFFE66D),
    Color(0xFFA29BFE), Color(0xFFFF7675), Color(0xFF74B9FF),
)

@Composable
fun GameCardWidget(
    card: GameCard?,
    label: String,
    modifier: Modifier = Modifier,
    emptyLabel: String = label,
    isHighlighted: Boolean = false,
    isSubmitted: Boolean = false,
    cornerRadius: Dp = 18.dp,
) {
    // Безопасный лейбл — убираем термин "промт"
    val safeLabel = if (label.equals("Промт", ignoreCase = true)) "Ситуация" else label
    val safeEmptyLabel = if (emptyLabel.contains("Промт", ignoreCase = true)) "Загрузка…" else emptyLabel

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val shape = RoundedCornerShape(cornerRadius)

        val glowColor by animateColorAsState(
            targetValue = when {
                isSubmitted   -> Color(0xFF00C853)
                isHighlighted -> Color(0xFF7C5DFA)
                else          -> Color.Transparent
            },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "glow",
        )

        Box(
            modifier = Modifier
                .widthIn(max = MAX_CARD_WIDTH)
                .aspectRatio(0.68f)
                .drawBehind {
                    if (glowColor != Color.Transparent) {
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(glowColor.copy(alpha = 0.35f), Color.Transparent),
                                center = Offset(size.width / 2, size.height / 2),
                                radius = size.width * 0.75f,
                            ),
                            size = size,
                        )
                    }
                }
                .clip(shape)
                .border(
                    width = if (isSubmitted || isHighlighted) 2.dp else 1.dp,
                    brush = Brush.linearGradient(
                        when {
                            isSubmitted   -> listOf(Color(0xFF00C853), Color(0xFF00E676))
                            isHighlighted -> listOf(Color(0xFF7C5DFA), Color(0xFFB39DDB))
                            else          -> listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.04f))
                        }
                    ),
                    shape = shape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                card == null -> EmptyCardContent(safeEmptyLabel, shape)

                card is MemeGameCard -> MemeCardContent(
                    imageUrl = card.data.mediaUrl,
                    label = safeLabel,
                    shape = shape,
                )

                card is SituationGameCard -> SituationCardContent(
                    text = card.data.promptText,
                    label = safeLabel,
                    cardId = card.data.id,
                    shape = shape,
                )

                else -> EmptyCardContent(safeEmptyLabel, shape)
            }
        }
    }
}

// ── Meme card (image) ──────────────────────────────────────────────────────────

@Composable
private fun MemeCardContent(
    imageUrl: String,
    label: String,
    shape: RoundedCornerShape,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        SubcomposeAsyncImage(
            model = normalizeMediaUrl(imageUrl),
            contentDescription = label,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(shape),
            loading = {
                MemeShimmer()
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1A1035)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Картинка недоступна",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                }
            },
        )

        // Тип-лейбл снизу
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                    )
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun MemeShimmer() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmerAlpha",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1A1035).copy(alpha = alpha),
                        Color(0xFF2A1F44).copy(alpha = alpha * 0.6f),
                        Color(0xFF1A1035).copy(alpha = alpha),
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            color = Color(0xFF7C5DFA).copy(alpha = 0.7f),
            strokeWidth = 2.dp,
        )
    }
}

// ── Situation card (text) ──────────────────────────────────────────────────────

@Composable
private fun SituationCardContent(
    text: String,
    label: String,
    cardId: String,
    shape: RoundedCornerShape,
) {
    val accentIndex = (cardId.hashCode() and 0x7FFFFFFF) % SituationAccents.size
    val accent = SituationAccents[accentIndex]

    // Заменяем термин "Промт" на "Ситуация"
    val displayLabel = if (label.equals("Промт", ignoreCase = true)) "СИТУАЦИЯ" else label.uppercase()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1A0D3D), Color(0xFF0F0829), Color(0xFF12083A))
                ),
                shape = shape,
            ),
    ) {
        // Угловой декор — свечение
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        listOf(accent.copy(alpha = 0.15f), Color.Transparent)
                    ),
                    CircleShape,
                ),
        )

        // Декоративная рамка внутри карточки
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .drawBehind {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            listOf(accent.copy(alpha = 0.25f), accent.copy(alpha = 0.08f))
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx()),
                    )
                },
        )

        // Тип-лейбл сверху ("СИТУАЦИЯ")
        Text(
            text = displayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp),
        )

        // Текст ситуации — центр
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            fontStyle = if (text.startsWith("\"")) FontStyle.Italic else FontStyle.Normal,
            lineHeight = 22.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp, vertical = 32.dp),
        )
    }
}

// ── Empty state ────────────────────────────────────────────────────────────────

@Composable
private fun EmptyCardContent(
    label: String,
    shape: RoundedCornerShape,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1A1035), Color(0xFF0F0820))
                ),
                shape = shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFF7C5DFA).copy(alpha = 0.5f),
                strokeWidth = 2.dp,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}
