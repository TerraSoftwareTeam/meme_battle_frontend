package com.dev.memebattle.feature.gameplay.impl.presentation.view.players.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dev.memebattle.feature.gameplay.impl.presentation.store.players.GameplayPlayersStore
import com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets.GameCardWidget

/**
 * Диалог с карточкой игрока в фазе Voting.
 * Показывает handle + submission card в крупном плане.
 *
 * @param player        игрок чью карту смотрим ([GameplayPlayersStore.State.previewPlayer])
 * @param hasAlreadyVoted  если true — кнопка "Проголосовать" задисейблена
 * @param isVoting      идёт запрос голосования
 * @param onVote        пользователь нажал "Проголосовать"
 * @param onDismiss     закрыть диалог
 */
@Composable
fun SubmissionPreviewDialog(
    player: GameplayPlayersStore.PlayerUiModel,
    hasAlreadyVoted: Boolean,
    isVoting: Boolean,
    onVote: (submissionId: String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 32.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E1035),
            tonalElevation = 16.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Аватар + ник
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlayerAvatar(handle = player.handle, size = 36.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = player.handle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Карточка игрока крупным планом
                Box(modifier = Modifier.fillMaxWidth(0.65f)) {
                    GameCardWidget(
                        card = player.submissionCard,
                        label = "Карточка",
                        emptyLabel = "Карта скрыта\n(анонимное голосование)",
                        isHighlighted = !hasAlreadyVoted,
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Закрыть", color = Color.White.copy(alpha = 0.6f))
                    }

                    Button(
                        onClick = {
                            player.submissionId?.let { onVote(it) }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !hasAlreadyVoted && player.submissionId != null && !isVoting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color(0xFF2A1F44),
                        ),
                    ) {
                        if (isVoting) {
                            CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (hasAlreadyVoted) "Проголосовано ✓" else "Голосовать",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                }

                if (player.submissionId == null && !hasAlreadyVoted) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Голосование анонимное — карта не привязана к игроку",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
