package com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
            Spacer(Modifier.height(24.dp))

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

            Spacer(Modifier.height(28.dp))

            // ── Прогресс готовности ───────────────────────────────────────────
            ReadinessProgressCard(
                readyCount = readyCount,
                totalCount = totalPlayers,
                fraction = readyFraction,
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
            )

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.07f),
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            // ── Список игроков ─────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(players, key = { it.userId }) { player ->
                    LobbyPlayerCard(player = player)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Кнопка готовности ─────────────────────────────────────────────
            GameActionButton(
                label = if (amIReady) "Вы готовы" else "Я готов!",
                enabled = !amIReady,
                isLoading = isSettingReady,
                onClick = onToggleReady,
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
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
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Круговой индикатор
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .drawBehind {
                        val stroke = 5.dp.toPx()
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
                        style = MaterialTheme.typography.titleMedium,
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

            Column {
                Text(
                    text = "Готовы к игре",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                // Горизонтальный прогресс-бар
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animFraction.coerceIn(0f, 1f))
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF7C5DFA), Color(0xFF00C853))
                                )
                            ),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (fraction >= 1f && totalCount > 0) "Игра начнётся!" else "Ждём остальных…",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (fraction >= 1f) Color(0xFF00C853) else Color.White.copy(alpha = 0.4f),
                )
            }
        }
    }
}

// ── Строка игрока ──────────────────────────────────────────────────────────────

@Composable
private fun LobbyPlayerCard(
    player: GameplayPlayersStore.PlayerUiModel,
    modifier: Modifier = Modifier,
) {
    val isReady = player.isReady

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isReady)
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0D2A1A), Color(0xFF0A2018))
                    )
                else
                    Brush.horizontalGradient(
                        listOf(Color(0xFF1A1035).copy(alpha = 0.6f), Color(0xFF14102A).copy(alpha = 0.6f))
                    )
            )
            .border(
                1.dp,
                if (isReady)
                    Brush.horizontalGradient(listOf(Color(0xFF00C853).copy(alpha = 0.4f), Color(0xFF00E676).copy(alpha = 0.2f)))
                else
                    Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.03f))),
                RoundedCornerShape(14.dp),
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Аватар-заглушка с инициалом
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (player.isMe) Color(0xFF7C5DFA) else Color(0xFF2A1F44)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = player.handle.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Ник + бейдж
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.handle + if (player.isMe) " (я)" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (player.isMe) Color(0xFFB39DDB) else Color.White,
                    fontWeight = if (player.isMe) FontWeight.Bold else FontWeight.Normal,
                )
                if (player.score > 0) {
                    Text(
                        text = "${player.score} очков",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f),
                    )
                }
            }

            // Статус-индикатор
            AnimatedContent(
                targetState = isReady,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "readyBadge",
            ) { ready ->
                if (ready) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF00C853).copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            "Готов",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00C853),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                    )
                }
            }
        }
    }
}
