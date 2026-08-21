package com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStore
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.gameplay_round_result_next_round_auto
import com.dev.memebattle.core.localization.gameplay_round_result_no_winner
import com.dev.memebattle.core.localization.gameplay_round_result_title
import com.dev.memebattle.core.localization.gameplay_round_result_winner
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextOverflow

/**
 * BottomSheet-оверлей с итогами раунда.
 * Показывается поверх SubmittingContent ~3 сек, затем автоматически скрывается.
 *
 * @param visible  управляется снаружи через [GameplayGameStore.State.uiPhase]
 */
@Composable
fun RoundResultOverlay(
    visible: Boolean,
    result: GameplayGameStore.RoundResultData?,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible && result != null,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMedium),
        ),
        exit = fadeOut() + slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMedium),
        ),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            result?.let { data ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = Color(0xFF1E1035),
                    tonalElevation = 8.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(24.dp),
                    ) {
                        // Заголовок
                        Text(
                            text = stringResource(Res.string.gameplay_round_result_title, data.roundNumber),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )

                        // Победитель раунда — тот, у кого наибольший score в round_scoreboard
                        val roundWinner = data.roundScoreboard
                            .filter { it.score > 0 }
                            .maxByOrNull { it.score }
                        if (roundWinner != null) {
                            Spacer(Modifier.height(4.dp))
                            val winnerName = (roundWinner.handle ?: roundWinner.userId.take(8)).take(20)
                            Text(
                                text = stringResource(Res.string.gameplay_round_result_winner, winnerName),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        } else if (data.winnerUserId != null && data.winnerUserId != "00000000-0000-0000-0000-000000000000") {
                            Spacer(Modifier.height(4.dp))
                            val winnerName = (data.winnerHandle ?: data.winnerUserId.take(8)).take(20)
                            Text(
                                text = stringResource(Res.string.gameplay_round_result_winner, winnerName),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        } else {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(Res.string.gameplay_round_result_no_winner),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.5f),
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(Modifier.height(12.dp))

                        // Мини-таблица очков
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            itemsIndexed(
                                data.roundScoreboard.sortedByDescending { it.score },
                            ) { index, entry ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    val entryName = (entry.handle ?: entry.userId.take(8)).take(20)
                                    Text(
                                        text = "${index + 1}. $entryName",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false),
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    ) {
                                        Text(
                                            text = "+${entry.score}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = stringResource(Res.string.gameplay_round_result_next_round_auto),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.4f),
                        )
                    }
                }
            }
        }
    }
}
