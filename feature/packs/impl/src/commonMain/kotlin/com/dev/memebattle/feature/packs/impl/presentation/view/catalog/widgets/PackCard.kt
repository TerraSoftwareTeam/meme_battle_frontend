package com.dev.memebattle.feature.packs.impl.presentation.view.catalog.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.dev.memebattle.core.domain.packs.model.SafetyLevel
import com.dev.memebattle.feature.packs.impl.presentation.store.catalog.PacksCatalogStore
import com.dev.memebattle.feature.packs.impl.presentation.view.details.CardBack
import kotlin.math.abs
import kotlin.math.absoluteValue

// Curated humorous situations for previews
private val CuratedSituations = listOf(
    "Баг на проде в пятницу в 17:59",
    "Тимлид зашел в голосовой канал",
    "Я: это таск на 5 минут\nТакже я через 3 дня:",
    "Дизайнер опять перерисовал макет",
    "Git push --force в main ветку",
    "Собеседование по зуму без камеры",
    "Код работает, но никто не знает почему",
)

private val MemeGradients = listOf(
    Brush.linearGradient(listOf(Color(0xFF8A2387), Color(0xFFF27121))),
    Brush.linearGradient(listOf(Color(0xFFF27121), Color(0xFFE94057))),
    Brush.linearGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF))),
    Brush.linearGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D))),
    Brush.linearGradient(listOf(Color(0xFFF12711), Color(0xFFF5AF19))),
    Brush.linearGradient(listOf(Color(0xFFFF007F), Color(0xFF7F00FF))),
    Brush.linearGradient(listOf(Color(0xFF4CA1AF), Color(0xFFC4E0E5))),
    Brush.linearGradient(listOf(Color(0xFF1D976C), Color(0xFF93F9B9))),
    Brush.linearGradient(listOf(Color(0xFFFF5F6D), Color(0xFFFFC371)))
)

private val SituationAccents = listOf(
    Color(0xFFFF5252),
    Color(0xFFFFEB3B),
    Color(0xFF00E676),
    Color(0xFF00B0FF),
    Color(0xFFD500F9),
    Color(0xFFFF6D00)
)

private fun formatCreationDate(createdAtStr: String): String {
    if (createdAtStr.isBlank()) return ""
    try {
        val datePart = createdAtStr.split("T").firstOrNull() ?: createdAtStr
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val month = parts[1]
            val day = parts[2]
            return "$day.$month.$year"
        }
        return datePart
    } catch (e: Exception) {
        return createdAtStr
    }
}

@Composable
internal fun PackCard(
    id: String,
    name: String,
    description: String?,
    createdAt: String,
    packType: PacksCatalogStore.PackType,
    safetyLevel: SafetyLevel,
    languageCode: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Elegant vertical gradient backdrop for the fan area to create depth
    val fanBackdropGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF281C4F),
            CardBackground
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Fan of cards container with depth backdrop
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(fanBackdropGradient)
                    .padding(top = 28.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CardFan(
                    packId = id,
                    packType = packType
                )
            }

            // Pack details section (fixed height of 84.dp ensures all cards have identical layout and sizes)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header (Name + Language Badge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayName = if (name.isBlank()) {
                        if (languageCode.equals("ru", ignoreCase = true)) "Без названия" else "Unnamed"
                    } else name

                    Text(
                        text = displayName,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    // Language Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2E2452))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = languageCode.uppercase(),
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Bottom row: Safety badge & creation date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Safety Level Badge
                    val (safetyText, safetyBg, safetyTextCol) = when (safetyLevel) {
                        SafetyLevel.FAMILY_FRIENDLY -> Triple("0+", Color(0xFF43A047), Color.White)
                        SafetyLevel.SPICY -> Triple("16+", Color(0xFFFFD54F), Color(0xFF3E2723))
                        SafetyLevel.EXPLICIT -> Triple("18+", Color(0xFFE53935), Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(safetyBg)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = safetyText,
                            color = safetyTextCol,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Creation date (properly formatted, falling back to a dash if blank)
                    val formattedDate = formatCreationDate(createdAt).ifBlank { "-" }
                    Text(
                        text = formattedDate,
                        color = TextSecondary.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun CardFan(
    packId: String,
    packType: PacksCatalogStore.PackType,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp),
        contentAlignment = Alignment.Center
    ) {
        // Deterministic count between 3 and 7 cards in the preview
        val count = (packId.hashCode().absoluteValue % 5) + 3
        val middleIndex = (count - 1) / 2f

        for (i in 0 until count) {
            val distanceFromMiddle = i - middleIndex

            // Rotation angle: fanning out symmetrically and tightly
            val maxRotation = when (count) {
                1 -> 0f
                2 -> 4f
                3 -> 8f
                4 -> 12f
                5 -> 15f
                6 -> 18f
                else -> 20f
            }
            val rotation = distanceFromMiddle * (maxRotation / (if (middleIndex == 0f) 1f else middleIndex))

            // Horizontal displacement: keeps cards within bounds
            val maxTransX = when (count) {
                1 -> 0.dp
                2 -> 10.dp
                3 -> 16.dp
                4 -> 20.dp
                5 -> 24.dp
                6 -> 28.dp
                else -> 32.dp
            }
            val translationX = distanceFromMiddle * (maxTransX.value / (if (middleIndex == 0f) 1f else middleIndex))

            // Vertical displacement: arches slightly down on the sides
            val maxTransY = when (count) {
                1 -> 0.dp
                2 -> 2.dp
                3 -> 4.dp
                4 -> 5.dp
                5 -> 6.dp
                6 -> 7.dp
                else -> 8.dp
            }
            val translationY = abs(distanceFromMiddle) * (maxTransY.value / (if (middleIndex == 0f) 1f else middleIndex))

            // Symmetrical z-index ensures middle card is fully on top
            val cardZIndex = (count - abs(distanceFromMiddle)).toFloat()

            if (packType == PacksCatalogStore.PackType.Memes) {
                Card(
                    modifier = Modifier
                        .size(width = 72.dp, height = 98.dp)
                        .zIndex(cardZIndex)
                        .graphicsLayer {
                            this.rotationZ = rotation
                            this.translationX = translationX.dp.toPx()
                            this.translationY = translationY.dp.toPx()
                        },
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    CardBack(modifier = Modifier.fillMaxSize())
                }
            } else {
                val accentIndex = (packId.hashCode().absoluteValue + i) % SituationAccents.size
                val accentColor = SituationAccents[accentIndex]
                MiniSituationCard(
                    accentColor = accentColor,
                    modifier = Modifier
                        .zIndex(cardZIndex)
                        .graphicsLayer {
                            this.rotationZ = rotation
                            this.translationX = translationX.dp.toPx()
                            this.translationY = translationY.dp.toPx()
                        }
                )
            }
        }
    }
}

@Composable
private fun MiniSituationCard(
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.size(width = 72.dp, height = 98.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B1E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(accentColor.copy(alpha = 0.3f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )
        }
    }
}
