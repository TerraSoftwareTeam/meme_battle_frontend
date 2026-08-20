package com.dev.memebattle.feature.home.impl.presentation.view.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
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
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.lobby_create_capacity_title
import com.dev.memebattle.core.localization.lobby_create_cards_memes_count
import com.dev.memebattle.core.localization.lobby_create_cards_per_player
import com.dev.memebattle.core.localization.lobby_create_cards_situations_count
import com.dev.memebattle.core.localization.lobby_create_insufficient_cards_warning
import com.dev.memebattle.core.localization.lobby_create_max_players_value
import com.dev.memebattle.core.localization.lobby_create_min_players_warning
import com.dev.memebattle.core.localization.lobby_create_prompts_needed
import com.dev.memebattle.feature.home.impl.presentation.store.create.CreateLobbyStore
import com.dev.network.game.current.dto.GameMode
import org.jetbrains.compose.resources.stringResource

private val AccentColor = Color(0xFF7C5DFA)
private val SurfaceColor = Color(0xFF2A1F44)
private val BorderColor = Color(0xFF3B2F5E)
private val TextSecondary = Color(0xFFB0A2C7)
private val TextMuted = Color(0xFF887A9E)

@Composable
fun LobbyCapacityCard(
    state: CreateLobbyStore.State,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = AccentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = stringResource(Res.string.lobby_create_capacity_title),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (state.calculatedMaxPlayers >= 3) AccentColor.copy(alpha = 0.25f) else Color(0xFFFF5252).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = stringResource(Res.string.lobby_create_max_players_value, state.calculatedMaxPlayers),
                        color = if (state.calculatedMaxPlayers >= 3) Color.White else Color(0xFFFF5252),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1F1735))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(Res.string.lobby_create_cards_memes_count, state.totalSelectedMemesCount),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = stringResource(Res.string.lobby_create_cards_situations_count, state.totalSelectedSituationsCount),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = stringResource(
                        Res.string.lobby_create_cards_per_player,
                        state.cardsNeededPerPlayer,
                        state.handSize,
                        state.maxRounds
                    ),
                    color = TextMuted,
                    fontSize = 12.sp
                )

                val promptReserveAvailable = if (state.mode == GameMode.SITUATION_TO_MEME) {
                    state.totalSelectedSituationsCount
                } else {
                    state.totalSelectedMemesCount
                }
                Text(
                    text = stringResource(
                        Res.string.lobby_create_prompts_needed,
                        promptReserveAvailable,
                        state.maxRounds
                    ),
                    color = if (promptReserveAvailable >= state.maxRounds) TextMuted else Color(0xFFFF5252),
                    fontSize = 12.sp
                )
            }

            if (state.calculatedMaxPlayers < 3) {
                val warningText = if (state.calculatedMaxPlayers == 0) {
                    stringResource(Res.string.lobby_create_insufficient_cards_warning)
                } else {
                    stringResource(Res.string.lobby_create_min_players_warning, state.calculatedMaxPlayers)
                }
                Text(
                    text = warningText,
                    color = Color(0xFFFF5252),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
