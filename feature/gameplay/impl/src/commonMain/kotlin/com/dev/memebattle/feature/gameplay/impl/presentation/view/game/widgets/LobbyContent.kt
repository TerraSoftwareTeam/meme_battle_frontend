package com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.feature.gameplay.impl.presentation.store.players.GameplayPlayersStore

/**
 * Экран лобби — список игроков, кнопка "Готов", счётчик.
 * Отображается пока game.status = LOBBY.
 *
 * @param players      список игроков из PlayersStore
 * @param readyCount   сколько игроков готовы (из InfoStore)
 * @param amIReady     готов ли текущий пользователь
 * @param isSettingReady  идёт запрос set_ready
 * @param onToggleReady   колбэк переключения готовности
 */
@Composable
fun LobbyContent(
    players: List<GameplayPlayersStore.PlayerUiModel>,
    readyCount: Int,
    amIReady: Boolean,
    isSettingReady: Boolean,
    onToggleReady: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        // Заголовок
        Text(
            text = "Лобби",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "$readyCount из ${players.size} готовы",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF7C5DFA),
            modifier = Modifier.padding(top = 2.dp),
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Spacer(Modifier.height(12.dp))

        // Список игроков
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(players, key = { it.userId }) { player ->
                LobbyPlayerRow(player = player)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Кнопка готовности
        if (amIReady) {
            OutlinedButton(
                onClick = onToggleReady,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isSettingReady,
                shape = RoundedCornerShape(16.dp),
            ) {
                if (isSettingReady) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("✓ Готов  —  Отменить", fontWeight = FontWeight.Bold, color = Color(0xFF00C853))
                }
            }
        } else {
            Button(
                onClick = onToggleReady,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isSettingReady,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C5DFA)),
            ) {
                if (isSettingReady) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Готов", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun LobbyPlayerRow(
    player: GameplayPlayersStore.PlayerUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Статус-точка
        val dotColor = if (player.isReady) Color(0xFF00C853) else Color.White.copy(alpha = 0.3f)
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Spacer(Modifier.width(12.dp))

        // Ник
        Text(
            text = player.handle + if (player.isMe) " (я)" else "",
            style = MaterialTheme.typography.bodyMedium,
            color = if (player.isMe) Color(0xFF7C5DFA) else Color.White,
            fontWeight = if (player.isMe) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )

        // Бейдж готовности
        if (player.isReady) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF00C853).copy(alpha = 0.15f),
            ) {
                Text(
                    "Готов",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00C853),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}
