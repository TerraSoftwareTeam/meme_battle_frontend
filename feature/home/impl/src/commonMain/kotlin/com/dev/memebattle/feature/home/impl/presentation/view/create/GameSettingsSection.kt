package com.dev.memebattle.feature.home.impl.presentation.view.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.lobby_create_game_settings
import com.dev.memebattle.core.localization.lobby_create_hand_size_label
import com.dev.memebattle.core.localization.lobby_create_mode_label
import com.dev.memebattle.core.localization.lobby_create_mode_meme_to_situation
import com.dev.memebattle.core.localization.lobby_create_mode_situation_to_meme
import com.dev.memebattle.core.localization.lobby_create_rounds_label
import com.dev.network.game.current.dto.GameMode
import org.jetbrains.compose.resources.stringResource

private val AccentColor = Color(0xFF7C5DFA)
private val SurfaceColor = Color(0xFF2A1F44)
private val TextSecondary = Color(0xFFB0A2C7)
private val TextMuted = Color(0xFF887A9E)

@Composable
fun GameSettingsSection(
    mode: GameMode,
    maxRounds: Int,
    handSize: Int,
    onModeChanged: (GameMode) -> Unit,
    onMaxRoundsChanged: (Int) -> Unit,
    onHandSizeChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.lobby_create_game_settings),
            color = TextSecondary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(Res.string.lobby_create_mode_label),
            color = TextMuted,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modes = listOf(
                GameMode.SITUATION_TO_MEME to stringResource(Res.string.lobby_create_mode_situation_to_meme),
                GameMode.MEME_TO_SITUATION to stringResource(Res.string.lobby_create_mode_meme_to_situation),
            )
            modes.forEach { (m, label) ->
                val isSelected = mode == m
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onModeChanged(m) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AccentColor else SurfaceColor,
                    tonalElevation = if (isSelected) 4.dp else 0.dp,
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(Res.string.lobby_create_rounds_label), color = TextMuted, fontSize = 14.sp)
            Surface(shape = RoundedCornerShape(8.dp), color = AccentColor.copy(alpha = 0.25f)) {
                Text(
                    text = "$maxRounds",
                    color = AccentColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }
        Slider(
            value = maxRounds.toFloat(),
            onValueChange = { onMaxRoundsChanged(it.roundToInt()) },
            valueRange = 1f..20f,
            steps = 18,
            colors = SliderDefaults.colors(
                thumbColor = AccentColor,
                activeTrackColor = AccentColor,
                inactiveTrackColor = SurfaceColor,
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(Res.string.lobby_create_hand_size_label), color = TextMuted, fontSize = 14.sp)
            Surface(shape = RoundedCornerShape(8.dp), color = AccentColor.copy(alpha = 0.25f)) {
                Text(
                    text = "$handSize",
                    color = AccentColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }
        Slider(
            value = handSize.toFloat(),
            onValueChange = { onHandSizeChanged(it.roundToInt()) },
            valueRange = 3f..10f,
            steps = 6,
            colors = SliderDefaults.colors(
                thumbColor = AccentColor,
                activeTrackColor = AccentColor,
                inactiveTrackColor = SurfaceColor,
            )
        )
    }
}
