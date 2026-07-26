package com.dev.memebattle.feature.gameplay.impl.presentation.view.players.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val AvatarPalette = listOf(
    Color(0xFF6650A4), Color(0xFF0288D1), Color(0xFF2E7D32),
    Color(0xFFE65100), Color(0xFF880E4F), Color(0xFF00695C),
)

/** Аватар с инициалом — цвет детерминирован из handle. */
@Composable
fun PlayerAvatar(
    handle: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val color = AvatarPalette[(handle.hashCode() and Int.MAX_VALUE) % AvatarPalette.size]
    val letter = handle.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}
