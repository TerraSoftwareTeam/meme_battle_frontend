package com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.SubcomposeAsyncImage
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStore
import com.dev.network.game.current.dto.MemeGameCard
import com.dev.network.game.current.dto.SituationGameCard
import kotlin.math.abs
import kotlin.math.absoluteValue

/**
 * Фаза Submitting — промт-карта сверху, веер из руки снизу с анимацией выбора.
 * На широких экранах промт + выбранная карта идут рядом, веер — ниже.
 */
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

// ── Узкий (телефон / маленький браузер) ────────────────────────────────────────

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
        Spacer(Modifier.height(12.dp))

        // Статус-заголовок
        PhaseHeader(
            title = "Выберите карту",
            subtitle = if (state.mySubmissionCard != null) "Карта подана — ждём остальных"
                       else "Выберите подходящий мем для ситуации",
            subtitleColor = if (state.mySubmissionCard != null) Color(0xFF00C853)
                            else Color.White.copy(alpha = 0.5f),
        )

        Spacer(Modifier.height(16.dp))

        // Промт-карта (50% высоты)
        Box(modifier = Modifier.weight(0.45f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            GameCardWidget(
                card = state.promptCard,
                label = "Промт",
                emptyLabel = "Промт загружается…",
                modifier = Modifier.fillMaxSize(0.75f),
            )
        }

        Spacer(Modifier.height(8.dp))

        // Веер карт из руки
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

        // Action кнопка
        GameActionButton(
            label = if (state.mySubmissionCard != null) "Подано" else "Подать",
            enabled = state.canSubmit,
            isLoading = state.isSubmitting,
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        )
    }
}

// ── Широкий (планшет / браузер) ────────────────────────────────────────────────

@Composable
private fun SubmittingWideLayout(
    state: GameplayGameStore.State,
    onSelectCard: (Int) -> Unit,
    onSubmit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Левая колонка — промт
        Column(
            modifier = Modifier.weight(1f).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PhaseHeader(
                title = "Промт",
                subtitle = "Выберите подходящий мем",
                subtitleColor = Color.White.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(16.dp))
            GameCardWidget(
                card = state.promptCard,
                label = "Промт",
                modifier = Modifier.widthIn(max = 260.dp).aspectRatio(0.68f),
            )
        }

        // Правая колонка — веер + кнопка
        Column(
            modifier = Modifier.weight(1f).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PhaseHeader(
                title = "Ваша рука",
                subtitle = if (state.mySubmissionCard != null) "Карта подана" else "${state.handCards.size} карт",
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
                label = if (state.mySubmissionCard != null) "Подано" else "Подать",
                enabled = state.canSubmit,
                isLoading = state.isSubmitting,
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── HandCardsFan ───────────────────────────────────────────────────────────────

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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🃏", fontSize = 40.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Нет карт в руке",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.4f),
                )
            }
        }
        return
    }

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val containerW = maxWidth
        val containerH = maxHeight

        // Карточка занимает 45% ширины контейнера (но не больше 200dp)
        val cardW: Dp = (containerW * 0.45f).coerceAtMost(200.dp)
        val cardH: Dp = cardW / cardAspect

        val count = cards.size
        val mid = (count - 1) / 2f

        val maxRotation = when (count) {
            1 -> 0f; 2 -> 5f; 3 -> 10f; 4 -> 14f; 5 -> 18f; else -> 22f
        }
        val maxTransX = when (count) {
            1 -> 0f; 2 -> 0.18f; 3 -> 0.25f; 4 -> 0.30f; 5 -> 0.34f; else -> 0.38f
        } * containerW.value  // в dp

        cards.forEachIndexed { i, card ->
            val dist = i - mid
            val baseRot = if (mid == 0f) 0f else dist * (maxRotation / mid)
            val baseTx = if (mid == 0f) 0f else dist * (maxTransX / mid)
            val baseTy = abs(dist) * (cardH.value * 0.07f)

            val isSelected = i == selectedIndex
            val isSubmitted = card.id == submittedCardId

            // Анимированное поднятие при выделении
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
        isSelected  -> Brush.linearGradient(listOf(Color(0xFF7C5DFA), Color(0xFFB39DDB)))
        else        -> Brush.linearGradient(listOf(Color(0xFF3A2860), Color(0xFF2A1840)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .border(
                width = if (isSelected || isSubmitted) 2.dp else 1.dp,
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
            is HandCardData.Meme -> SubcomposeAsyncImage(
                model = card.imageUrl,
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(shape),
                loading = { MemeShimmerSmall() },
                error = {
                    Box(
                        Modifier.fillMaxSize().background(Color(0xFF1A1035)),
                        contentAlignment = Alignment.Center,
                    ) { Text("🖼️", fontSize = 28.sp) }
                },
            )

            is HandCardData.Situation -> {
                val accentIndex = (card.id.hashCode() and 0x7FFFFFFF) % SituationAccents.size
                val accent = SituationAccents[accentIndex]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF1A0D3D), Color(0xFF0D0620))),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("❝", fontSize = 36.sp, color = accent.copy(alpha = 0.15f),
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
                    Text(
                        text = card.text,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 20.dp),
                    )
                }
            }

            is HandCardData.Unknown -> Box(
                Modifier.fillMaxSize().background(Color(0xFF1A1035)),
                contentAlignment = Alignment.Center,
            ) { Text("?", fontSize = 28.sp, color = Color.White.copy(alpha = 0.3f)) }
        }

        // Бейдж "Подано" если эта карта подана
        if (isSubmitted) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color(0xFF00C853), CircleShape)
                    .size(18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("·", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
        androidx.compose.material3.CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = Color(0xFF7C5DFA).copy(alpha = 0.6f),
            strokeWidth = 2.dp,
        )
    }
}

// ── Shared helpers ─────────────────────────────────────────────────────────────

@Composable
fun PhaseHeader(
    title: String,
    subtitle: String,
    subtitleColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
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
            androidx.compose.material3.CircularProgressIndicator(
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
