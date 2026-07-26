package com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Панель навигации и действия внизу GameScreen.
 *
 * Содержит:
 * - Стрелка влево (← предыдущая карта)
 * - Кнопка основного действия (Submit / Vote / задизейблена)
 * - Стрелка вправо (→ следующая карта)
 */
@Composable
fun CardActionBar(
    actionLabel: String,
    actionEnabled: Boolean,
    isActionLoading: Boolean,
    canNavigatePrev: Boolean,
    canNavigateNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledIconButton(
            onClick = onPrev,
            enabled = canNavigatePrev,
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color(0xFF2A1F44),
                contentColor = Color(0xFF7C5DFA),
                disabledContainerColor = Color(0xFF1A1030),
                disabledContentColor = Color.White.copy(alpha = 0.2f),
            ),
            modifier = Modifier.size(48.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Предыдущая карта")
        }

        Spacer(Modifier.width(16.dp))

        Button(
            onClick = onAction,
            enabled = actionEnabled && !isActionLoading,
            modifier = Modifier.width(180.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7C5DFA),
                disabledContainerColor = Color(0xFF2A1F44),
            ),
        ) {
            AnimatedContent(
                targetState = isActionLoading,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "actionButton",
            ) { loading ->
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        actionLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (actionEnabled) Color.White else Color.White.copy(alpha = 0.4f),
                    )
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        FilledIconButton(
            onClick = onNext,
            enabled = canNavigateNext,
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color(0xFF2A1F44),
                contentColor = Color(0xFF7C5DFA),
                disabledContainerColor = Color(0xFF1A1030),
                disabledContentColor = Color.White.copy(alpha = 0.2f),
            ),
            modifier = Modifier.size(48.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Следующая карта")
        }
    }
}
