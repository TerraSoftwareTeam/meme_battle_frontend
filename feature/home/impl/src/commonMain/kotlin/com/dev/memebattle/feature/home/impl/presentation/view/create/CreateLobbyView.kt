package com.dev.memebattle.feature.home.impl.presentation.view.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.lobby_create_close
import com.dev.memebattle.core.localization.lobby_create_game_settings
import com.dev.memebattle.core.localization.lobby_create_hand_size_label
import com.dev.memebattle.core.localization.lobby_create_mode_label
import com.dev.memebattle.core.localization.lobby_create_mode_meme_to_situation
import com.dev.memebattle.core.localization.lobby_create_mode_situation_to_meme
import com.dev.memebattle.core.localization.lobby_create_name_hint
import com.dev.memebattle.core.localization.lobby_create_name_label
import com.dev.memebattle.core.localization.lobby_create_nickname_hint
import com.dev.memebattle.core.localization.lobby_create_nickname_label
import com.dev.memebattle.core.localization.lobby_create_rounds_label
import com.dev.memebattle.core.localization.lobby_create_select_meme_packs
import com.dev.memebattle.core.localization.lobby_create_select_situation_packs
import com.dev.memebattle.core.localization.lobby_create_submit
import com.dev.memebattle.core.localization.lobby_create_title
import com.dev.memebattle.core.ui.components.pack.PackCardKind
import com.dev.memebattle.feature.home.impl.presentation.component.create.CreateLobbyComponent
import com.dev.memebattle.feature.home.impl.presentation.store.create.CreateLobbyStore
import com.dev.network.game.current.dto.GameMode
import org.jetbrains.compose.resources.stringResource

private val AccentColor = Color(0xFF7C5DFA)
private val SurfaceColor = Color(0xFF2A1F44)
private val BorderColor = Color(0xFF3B2F5E)
private val TextSecondary = Color(0xFFB0A2C7)
private val TextMuted = Color(0xFF887A9E)

@Composable
fun CreateLobbyView(
    component: CreateLobbyComponent,
    modifier: Modifier = Modifier
) {
    val state by component.state.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.lobby_create_title),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { component.onClose() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.lobby_create_close),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PackSectionHeader(
                title = stringResource(Res.string.lobby_create_select_meme_packs),
                onAddFromCatalog = { component.onOpenPackPicker() }
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (state.isPacksLoading && state.officialMemePacks.isEmpty()) {
                SkeletonRow(count = 2)
            } else {
                PackSelectionRow(
                    packs = state.availableMemePacks,
                    selectedIds = state.selectedMemePackIds,
                    officialIds = CreateLobbyStore.OfficialPackIds.allMemeIds,
                    packKind = PackCardKind.MEME,
                    onToggle = { component.toggleMemePack(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PackSectionHeader(
                title = stringResource(Res.string.lobby_create_select_situation_packs),
                onAddFromCatalog = { component.onOpenPackPicker() }
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (state.isPacksLoading && state.officialSituationPacks.isEmpty()) {
                SkeletonRow(count = 2)
            } else {
                PackSelectionRow(
                    packs = state.availableSituationPacks,
                    selectedIds = state.selectedSituationPackIds,
                    officialIds = CreateLobbyStore.OfficialPackIds.allSituationIds,
                    packKind = PackCardKind.SITUATION,
                    onToggle = { component.toggleSituationPack(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                modes.forEach { (mode, label) ->
                    val isSelected = state.mode == mode
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { component.setMode(mode) },
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
                        text = "${state.maxRounds}",
                        color = AccentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
            Slider(
                value = state.maxRounds.toFloat(),
                onValueChange = { component.setMaxRounds(it.toInt()) },
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
                        text = "${state.handSize}",
                        color = AccentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
            Slider(
                value = state.handSize.toFloat(),
                onValueChange = { component.setHandSize(it.toInt()) },
                valueRange = 3f..10f,
                steps = 6,
                colors = SliderDefaults.colors(
                    thumbColor = AccentColor,
                    activeTrackColor = AccentColor,
                    inactiveTrackColor = SurfaceColor,
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.lobbyNameInput,
                onValueChange = { component.updateLobbyName(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.lobby_create_name_label)) },
                placeholder = { Text(stringResource(Res.string.lobby_create_name_hint), color = Color.White.copy(alpha = 0.3f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentColor,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = AccentColor,
                    unfocusedLabelColor = TextMuted,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = AccentColor,
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.handleInput,
                onValueChange = { component.updateHandle(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.lobby_create_nickname_label)) },
                placeholder = { Text(stringResource(Res.string.lobby_create_nickname_hint), color = Color.White.copy(alpha = 0.3f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentColor,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = AccentColor,
                    unfocusedLabelColor = TextMuted,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = AccentColor,
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.error != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFB71C1C).copy(alpha = 0.2f),
                ) {
                    Text(
                        text = state.error!!,
                        color = Color(0xFFFF5252),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { component.createLobby() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = state.isCreateEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor,
                    disabledContainerColor = SurfaceColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = stringResource(Res.string.lobby_create_submit),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isCreateEnabled) Color.White else TextMuted
                    )
                }
            }
        }
    }
}
