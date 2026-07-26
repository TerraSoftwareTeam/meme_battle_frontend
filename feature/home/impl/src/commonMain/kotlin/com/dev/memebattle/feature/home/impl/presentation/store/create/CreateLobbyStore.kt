package com.dev.memebattle.feature.home.impl.presentation.store.create

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.feature.home.impl.presentation.store.create.CreateLobbyStore.Intent
import com.dev.memebattle.feature.home.impl.presentation.store.create.CreateLobbyStore.Label
import com.dev.memebattle.feature.home.impl.presentation.store.create.CreateLobbyStore.State
import com.dev.network.game.current.dto.GameMode

interface CreateLobbyStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class ToggleMemePack(val id: String) : Intent
        data class ToggleSituationPack(val id: String) : Intent
        data class SetMode(val mode: GameMode) : Intent
        data class SetMaxRounds(val rounds: Int) : Intent
        data class SetHandSize(val size: Int) : Intent
        data object Create : Intent
    }

    data class State(
        val isLoading: Boolean = false,
        val isPacksLoading: Boolean = false,
        val likedMemePackCount: Int = 0,
        val likedSituationPackCount: Int = 0,
        val availableMemePacks: List<MemePack> = emptyList(),
        val availableSituationPacks: List<SituationPack> = emptyList(),
        val selectedMemePackIds: Set<String> = emptySet(),
        val selectedSituationPackIds: Set<String> = emptySet(),
        val mode: GameMode = GameMode.SITUATION_TO_MEME,
        val maxRounds: Int = 5,
        val handSize: Int = 5,
        val error: String? = null
    ) {
        val isCreateEnabled: Boolean
            get() = selectedMemePackIds.isNotEmpty() && selectedSituationPackIds.isNotEmpty() && !isLoading
    }

    sealed interface Label {
        data class LobbyCreated(val gameId: String) : Label
    }
}
