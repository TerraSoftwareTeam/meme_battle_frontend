package com.dev.memebattle.feature.home.impl.presentation.view.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.lobby_create_capacity_title
import com.dev.memebattle.core.localization.lobby_create_insufficient_cards_warning
import com.dev.memebattle.core.localization.lobby_create_max_players_value
import com.dev.memebattle.core.localization.lobby_create_min_players_warning
import com.dev.memebattle.core.localization.packs_type_memes
import com.dev.memebattle.core.localization.packs_type_situations
import com.dev.memebattle.feature.home.impl.presentation.store.create.CreateLobbyStore
import org.jetbrains.compose.resources.stringResource

private val AccentColor = Color(0xFF7C5DFA)
private val SurfaceColor = Color(0xFF2A1F44)
private val BorderColor = Color(0xFF3B2F5E)
private val CardBg = Color(0xFF1F1735)
private val TextSecondary = Color(0xFFB0A2C7)

@Composable
fun LobbyCapacityCard(
    state: CreateLobbyStore.State,
    modifier: Modifier = Modifier
) {
    val isEnough = state.calculatedMaxPlayers >= 3

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Icon + Title & Capacity Badge
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
                    color = if (isEnough) AccentColor.copy(alpha = 0.25f) else Color(0xFFFF5252).copy(alpha = 0.2f),
                    border = BorderStroke(
                        1.dp,
                        if (isEnough) AccentColor.copy(alpha = 0.4f) else Color(0xFFFF5252).copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.lobby_create_max_players_value, state.calculatedMaxPlayers),
                        color = if (isEnough) Color.White else Color(0xFFFF5252),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Compact Cards Summary Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = CardBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(Res.string.packs_type_memes),
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${state.totalSelectedMemesCount}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = CardBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(Res.string.packs_type_situations),
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${state.totalSelectedSituationsCount}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Warning Banner if capacity is less than 3 players
            if (!isEnough) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFF5252).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                        val warningText = if (state.calculatedMaxPlayers == 0) {
                            stringResource(Res.string.lobby_create_insufficient_cards_warning)
                        } else {
                            stringResource(Res.string.lobby_create_min_players_warning, state.calculatedMaxPlayers)
                        }
                        Text(
                            text = warningText,
                            color = Color(0xFFFF8888),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
