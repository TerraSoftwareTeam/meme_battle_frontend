package com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dev.network.game.current.dto.GameCard

private val CardShape = RoundedCornerShape(20.dp)

/**
 * Переиспользуемая карточка игры (промт, карта из руки, submission).
 *
 * @param card        данные карточки; null — показываем заглушку с [emptyLabel]
 * @param label       маленькая подпись над/под содержимым (например "Промт")
 * @param emptyLabel  текст-заглушка когда [card] == null
 * @param isHighlighted  рамка акцентного цвета (выделенный submission при голосовании)
 * @param isSubmitted    зелёная рамка — "вы уже подали эту карту"
 */
@Composable
fun GameCardWidget(
    card: GameCard?,
    label: String,
    modifier: Modifier = Modifier,
    emptyLabel: String = label,
    isHighlighted: Boolean = false,
    isSubmitted: Boolean = false,
    cornerRadius: Dp = 20.dp,
) {
    val shape = RoundedCornerShape(cornerRadius)

    val borderColor by animateColorAsState(
        targetValue = when {
            isSubmitted -> Color(0xFF00C853)
            isHighlighted -> Color(0xFF7C5DFA)
            else -> Color.White.copy(alpha = 0.12f)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cardBorder",
    )

    Surface(
        modifier = modifier
            .aspectRatio(0.68f)
            .clip(shape)
            .border(2.dp, borderColor, shape),
        shape = shape,
        color = Color(0xFF2A1F44),
        tonalElevation = if (isHighlighted) 8.dp else 2.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (card == null) {
                Text(
                    text = emptyLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.35f),
                    textAlign = TextAlign.Center,
                )
            } else {
                val cardId = when(card) {
                    is com.dev.network.game.current.dto.MemeGameCard -> card.data.id
                    is com.dev.network.game.current.dto.SituationGameCard -> card.data.id
                }
                val text = (card as? com.dev.network.game.current.dto.SituationGameCard)?.data?.promptText
                val imageUrl = (card as? com.dev.network.game.current.dto.MemeGameCard)?.data?.mediaUrl
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF7C5DFA),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    text?.let { t ->
                        Text(
                            text = t,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    // TODO: imageUrl → AsyncImage (после добавления Coil/Kamel)
                    if (text == null && imageUrl == null) {
                        Text(
                            text = "Карта #${cardId.take(6)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}
