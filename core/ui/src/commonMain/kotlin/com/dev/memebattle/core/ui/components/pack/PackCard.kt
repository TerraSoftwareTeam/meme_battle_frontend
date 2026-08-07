package com.dev.memebattle.core.ui.components.pack

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

import kotlin.math.abs
import kotlin.math.absoluteValue

enum class PackCardKind {
    MEME, SITUATION
}

enum class PackCardSafetyLevel {
    FAMILY_FRIENDLY, SPICY, EXPLICIT
}

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
fun PackCard(
    id: String,
    name: String,
    description: String?,
    createdAt: String,
    packType: PackCardKind,
    safetyLevel: PackCardSafetyLevel,
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
            .aspectRatio(0.70f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(fanBackdropGradient),
                contentAlignment = Alignment.Center
            ) {
                CardFan(
                    packId = id,
                    packType = packType,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
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
                    .height(72.dp)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
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
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2E2452))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
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
                        PackCardSafetyLevel.FAMILY_FRIENDLY -> Triple("0+", Color(0xFF43A047), Color.White)
                        PackCardSafetyLevel.SPICY -> Triple("16+", Color(0xFFFFD54F), Color(0xFF3E2723))
                        PackCardSafetyLevel.EXPLICIT -> Triple("18+", Color(0xFFE53935), Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(safetyBg)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
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
                        fontSize = 10.sp,
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
    packType: PackCardKind,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp),
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


            val cardZIndex = count - abs(distanceFromMiddle)

            if (packType == PackCardKind.MEME) {
                Card(
                    modifier = Modifier
                        .size(width = 64.dp, height = 88.dp)
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
        modifier = modifier.size(width = 64.dp, height = 88.dp),
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
