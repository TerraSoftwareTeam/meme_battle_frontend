package com.dev.memebattle.feature.home.impl.presentation.view.create

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.lobby_create_close
import com.dev.memebattle.core.localization.lobby_create_select_meme_packs
import com.dev.memebattle.core.localization.lobby_create_select_situation_packs
import com.dev.memebattle.core.localization.lobby_create_title
import com.dev.memebattle.core.ui.components.pack.PackCardKind
import com.dev.memebattle.feature.home.impl.presentation.component.create.CreateLobbyComponent
import com.dev.memebattle.feature.home.impl.presentation.store.create.CreateLobbyStore
import org.jetbrains.compose.resources.stringResource

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
            // Header
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

            // Meme Packs Selection Section
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
                    cardCounts = state.memePackCardCounts,
                    onToggle = { component.toggleMemePack(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Situation Packs Selection Section
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
                    cardCounts = state.situationPackCardCounts,
                    onToggle = { component.toggleSituationPack(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game Settings (Mode, Rounds, Hand size)
            GameSettingsSection(
                mode = state.mode,
                maxRounds = state.maxRounds,
                handSize = state.handSize,
                onModeChanged = { component.setMode(it) },
                onMaxRoundsChanged = { component.setMaxRounds(it) },
                onHandSizeChanged = { component.setHandSize(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lobby Capacity Analysis Card
            LobbyCapacityCard(state = state)

            Spacer(modifier = Modifier.height(16.dp))

            // Lobby Form Text Fields & Submit Button
            LobbyFormFields(
                lobbyName = state.lobbyNameInput,
                handle = state.handleInput,
                error = state.error,
                isLoading = state.isLoading,
                isCreateEnabled = state.isCreateEnabled,
                onLobbyNameChanged = { component.updateLobbyName(it) },
                onHandleChanged = { component.updateHandle(it) },
                onCreateLobby = { component.createLobby() }
            )
        }
    }
}
