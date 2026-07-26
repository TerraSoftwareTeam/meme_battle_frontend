package com.dev.memebattle.feature.gameplay.impl.presentation.view.players.widgets

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Маленький цветной бейдж статуса (Готов / Подал / Проголосовал).
 * Ничего не рендерит если [label] пустой.
 */
@Composable
fun PlayerStatusBadge(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (label.isEmpty()) return
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
    ) {
        Text(
            text = label,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}
