package com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

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

            val diagColor = Color(0xFF7C5DFA).copy(alpha = 0.09f)
            var d = -size.height
            while (d < size.width) {
                drawLine(diagColor, Offset(d, 0f), Offset(d + size.height, size.height), strokeWidth = 1f)
                d += step * 2
            }
        }

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
                    cornerRadius = CornerRadius(8.dp.toPx()),
                    style = Stroke(width = 1.5f),
                )
            }
        }
    }
}
