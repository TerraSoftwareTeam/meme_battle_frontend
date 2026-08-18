package com.dev.memebattle.feature.gameplay.impl.presentation.view.players.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.gameplay_players_me_badge
import com.dev.memebattle.core.localization.gameplay_players_score
import com.dev.memebattle.core.localization.gameplay_players_status_ready
import com.dev.memebattle.core.localization.gameplay_players_status_not_ready
import com.dev.memebattle.core.localization.gameplay_players_status_submitted
import com.dev.memebattle.core.localization.gameplay_players_status_voted
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStore
import com.dev.memebattle.feature.gameplay.impl.presentation.store.players.GameplayPlayersStore

/**
 * Карточка одного игрока в PlayersScreen.
 *
 * @param uiPhase    текущая фаза игры — в [GameplayGameStore.UiPhase.Voting] показывает кнопку "Посмотреть карту"
 * @param onShowCard колбэк для открытия диалога с картой игрока
 */
@Composable
fun PlayerCard(
    player: GameplayPlayersStore.PlayerUiModel,
    uiPhase: GameplayGameStore.UiPhase,
    onShowCard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (player.isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color(0xFF2A1F44),
        tonalElevation = if (player.isMe) 6.dp else 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerAvatar(handle = player.handle)

            Spacer(Modifier.width(12.dp))

            // Имя и очки
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.handle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    if (player.isMe) {
                        Spacer(Modifier.width(6.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text(stringResource(Res.string.gameplay_players_me_badge), style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
                Text(
                    text = stringResource(Res.string.gameplay_players_score, player.score),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.55f),
                )
            }

            // Статусы
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                PlayerStatusBadge(
                    label = if (player.isReady) stringResource(Res.string.gameplay_players_status_ready) else stringResource(Res.string.gameplay_players_status_not_ready),
                    color = if (player.isReady) Color(0xFF00C853) else Color.White.copy(alpha = 0.4f),
                )
                if (player.hasSubmitted) {
                    PlayerStatusBadge(label = stringResource(Res.string.gameplay_players_status_submitted), color = Color(0xFF00B0FF))
                }
                if (player.hasVoted) {
                    PlayerStatusBadge(label = stringResource(Res.string.gameplay_players_status_voted), color = MaterialTheme.colorScheme.primary)
                }
            }

            // Кнопка просмотра карты — только в фазе Voting и если игрок подал карту
            if (uiPhase == GameplayGameStore.UiPhase.Voting && player.hasSubmitted) {
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onShowCard) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
