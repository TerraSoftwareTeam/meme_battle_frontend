package com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.feature.gameplay.impl.presentation.store.players.GameplayPlayersStore

@Composable
fun LobbyContent(
    players: List<GameplayPlayersStore.PlayerUiModel>,
    readyCount: Int,
    amIReady: Boolean,
    isSettingReady: Boolean,
    onToggleReady: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalPlayers = players.size
    val readyFraction = if (totalPlayers == 0) 0f else readyCount.toFloat() / totalPlayers

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))

            // ── Заголовок ─────────────────────────────────────────────────────
            Text(
                text = "Лобби",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
            )

            Spacer(Modifier.height(4.dp))
            Text(
                text = "Ожидание игроков…",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.4f),
            )

            Spacer(Modifier.height(36.dp))

            // ── Прогресс готовности ───────────────────────────────────────────
            ReadinessProgressCard(
                readyCount = readyCount,
                totalCount = totalPlayers,
                fraction = readyFraction,
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.07f),
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
            )

            Spacer(Modifier.weight(1f))

            // ── Кнопка готовности ─────────────────────────────────────────────
            GameActionButton(
                label = if (amIReady) "Вы готовы" else "Я готов!",
                enabled = !amIReady,
                isLoading = isSettingReady,
                onClick = onToggleReady,
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
            )
        }
    }
}

// ── Карточка прогресса готовности ─────────────────────────────────────────────

@Composable
private fun ReadinessProgressCard(
    readyCount: Int,
    totalCount: Int,
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    val animFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "readinessFraction",
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1A1035).copy(alpha = 0.8f))
            .border(
                1.dp,
                Brush.linearGradient(listOf(Color(0xFF3A2860), Color(0xFF251A50))),
                RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Круговой индикатор
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .drawBehind {
                        val stroke = 6.dp.toPx()
                        // Track
                        drawArc(
                            color = Color.White.copy(alpha = 0.07f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(stroke, cap = StrokeCap.Round),
                        )
                        // Progress
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(Color(0xFF7C5DFA), Color(0xFF00C853))
                            ),
                            startAngle = -90f,
                            sweepAngle = animFraction * 360f,
                            useCenter = false,
                            style = Stroke(stroke, cap = StrokeCap.Round),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$readyCount",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "/$totalCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Готовы к игре",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                // Горизонтальный прогресс-бар
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animFraction.coerceIn(0f, 1f))
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF7C5DFA), Color(0xFF00C853))
                                )
                            ),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (totalCount < 3) "Ожидание участников (мин. 3 игрока)"
                           else if (fraction >= 1f) "Все готовы!"
                           else "Ждём остальных…",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (fraction >= 1f && totalCount >= 3) Color(0xFF00C853) else Color.White.copy(alpha = 0.4f),
                )
            }
        }
    }
}
