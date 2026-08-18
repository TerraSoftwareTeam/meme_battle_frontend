package com.dev.memebattle.feature.home.impl.presentation.view.create

import com.dev.network.game.current.dto.GameMode
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.core.ui.components.pack.PackCard
import com.dev.memebattle.core.ui.components.pack.PackCardKind
import com.dev.memebattle.core.ui.components.pack.PackCardSafetyLevel
import com.dev.memebattle.feature.home.impl.presentation.component.create.CreateLobbyComponent
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.lobby_create_title
import com.dev.memebattle.core.localization.lobby_create_close
import com.dev.memebattle.core.localization.lobby_create_select_meme_packs
import com.dev.memebattle.core.localization.lobby_create_select_situation_packs
import com.dev.memebattle.core.localization.lobby_create_game_settings
import com.dev.memebattle.core.localization.lobby_create_mode_label
import com.dev.memebattle.core.localization.lobby_create_mode_situation_to_meme
import com.dev.memebattle.core.localization.lobby_create_mode_meme_to_situation
import com.dev.memebattle.core.localization.lobby_create_rounds_label
import com.dev.memebattle.core.localization.lobby_create_hand_size_label
import com.dev.memebattle.core.localization.lobby_create_nickname_label
import com.dev.memebattle.core.localization.lobby_create_nickname_hint
import com.dev.memebattle.core.localization.lobby_create_submit

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
            .background(Color(0xFF1E1035))
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

            // ── Meme Packs Section ──────────────────────────────────────
            Text(
                text = stringResource(Res.string.lobby_create_select_meme_packs),
                color = Color(0xFFB0A2C7),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 1200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                // Show skeletons while loading
                if (state.isPacksLoading && state.availableMemePacks.isEmpty()) {
                    items(state.likedMemePackCount.coerceAtLeast(3)) {
                        PackCardSkeleton()
                    }
                } else {
                    items(state.availableMemePacks) { pack ->
                        val isSelected = state.selectedMemePackIds.contains(pack.id)
                        SelectablePackCard(
                            id = pack.id,
                            name = pack.name,
                            description = pack.description ?: "",
                            createdAt = pack.createdAt,
                            safetyLevel = PackCardSafetyLevel.valueOf(pack.safetyLevel.name),
                            packType = PackCardKind.MEME,
                            languageCode = pack.languageCode,
                            isSelected = isSelected,
                            onClick = { component.toggleMemePack(pack.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Situation Packs Section ─────────────────────────────────
            Text(
                text = stringResource(Res.string.lobby_create_select_situation_packs),
                color = Color(0xFFB0A2C7),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 1200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                // Show skeletons while loading
                if (state.isPacksLoading && state.availableSituationPacks.isEmpty()) {
                    items(state.likedSituationPackCount.coerceAtLeast(3)) {
                        PackCardSkeleton()
                    }
                } else {
                    items(state.availableSituationPacks) { pack ->
                        val isSelected = state.selectedSituationPackIds.contains(pack.id)
                        SelectablePackCard(
                            id = pack.id,
                            name = pack.name,
                            description = pack.description ?: "",
                            createdAt = pack.createdAt,
                            safetyLevel = PackCardSafetyLevel.valueOf(pack.safetyLevel.name),
                            packType = PackCardKind.SITUATION,
                            languageCode = pack.languageCode,
                            isSelected = isSelected,
                            onClick = { component.toggleSituationPack(pack.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Game Settings ───────────────────────────────────────────
            Text(
                text = stringResource(Res.string.lobby_create_game_settings),
                color = Color(0xFFB0A2C7),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Mode selector
            Text(
                text = stringResource(Res.string.lobby_create_mode_label),
                color = Color(0xFF887A9E),
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
                        color = if (isSelected) Color(0xFF7C5DFA) else Color(0xFF2A1F44),
                        tonalElevation = if (isSelected) 4.dp else 0.dp,
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rounds slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.lobby_create_rounds_label),
                    color = Color(0xFF887A9E),
                    fontSize = 14.sp
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF7C5DFA).copy(alpha = 0.25f),
                ) {
                    Text(
                        text = "${state.maxRounds}",
                        color = Color(0xFF7C5DFA),
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
                    thumbColor = Color(0xFF7C5DFA),
                    activeTrackColor = Color(0xFF7C5DFA),
                    inactiveTrackColor = Color(0xFF2A1F44),
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Hand size slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.lobby_create_hand_size_label),
                    color = Color(0xFF887A9E),
                    fontSize = 14.sp
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF7C5DFA).copy(alpha = 0.25f),
                ) {
                    Text(
                        text = "${state.handSize}",
                        color = Color(0xFF7C5DFA),
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
                    thumbColor = Color(0xFF7C5DFA),
                    activeTrackColor = Color(0xFF7C5DFA),
                    inactiveTrackColor = Color(0xFF2A1F44),
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.handleInput,
                onValueChange = { component.updateHandle(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.lobby_create_nickname_label)) },
                placeholder = { Text(stringResource(Res.string.lobby_create_nickname_hint), color = Color.White.copy(alpha = 0.3f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7C5DFA),
                    unfocusedBorderColor = Color(0xFF3B2F5E),
                    focusedLabelColor = Color(0xFF7C5DFA),
                    unfocusedLabelColor = Color(0xFF887A9E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF7C5DFA),
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { component.createLobby() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = state.isCreateEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C5DFA),
                    disabledContainerColor = Color(0xFF3B2F5E)
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
                        color = if (state.isCreateEnabled) Color.White else Color(0xFF887A9E)
                    )
                }
            }
        }
    }
}

@Composable
fun PackCardSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.70f)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF2A1F44))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top shimmer area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF3B2F5E))
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF3B2F5E))
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Bottom row placeholders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF3B2F5E))
                )
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF3B2F5E))
                )
            }
        }
    }
}

@Composable
fun SelectablePackCard(
    id: String,
    name: String,
    description: String,
    createdAt: String,
    safetyLevel: PackCardSafetyLevel,
    packType: PackCardKind,
    languageCode: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) Modifier.border(3.dp, Color(0xFF00E676), RoundedCornerShape(16.dp))
                else Modifier
            )
    ) {
        PackCard(
            id = id,
            name = name,
            description = description,
            createdAt = createdAt,
            safetyLevel = safetyLevel,
            packType = packType,
            languageCode = languageCode,
            onClick = onClick
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = Color(0xFF00E676),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(14.dp))
            )
        }
    }
}
