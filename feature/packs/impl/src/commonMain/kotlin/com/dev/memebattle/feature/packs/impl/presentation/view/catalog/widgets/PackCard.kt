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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets.CardBack
import kotlin.math.abs
import kotlin.math.absoluteValue

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
    onEditClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {

    val fanBackdropGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF281C4F),
            CardBackground
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(fanBackdropGradient),
                contentAlignment = Alignment.Center
            ) {
                CardFan(
                    packId = id,
                    packType = packType,
                    modifier = Modifier.padding(top = 28.dp, bottom = 16.dp)
                )
                
                if (onEditClick != null) {
                    IconButton(
                        onClick = { onEditClick() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

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


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

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

        val count = (packId.hashCode().absoluteValue % 5) + 3
        val middleIndex = (count - 1) / 2f

        for (i in 0 until count) {
            val distanceFromMiddle = i - middleIndex


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
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
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
