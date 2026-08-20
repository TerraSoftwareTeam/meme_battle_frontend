package com.dev.memebattle.feature.home.impl.presentation.store.create

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.feature.home.impl.presentation.store.create.CreateLobbyStore.Intent
import com.dev.memebattle.feature.home.impl.presentation.store.create.CreateLobbyStore.Label
import com.dev.memebattle.feature.home.impl.presentation.store.create.CreateLobbyStore.State
import com.dev.network.game.current.dto.GameMode

interface CreateLobbyStore : Store<Intent, State, Label> {

    /** Official hardcoded pack IDs pre-loaded from the backend. */
    object OfficialPackIds {
        const val SITUATIONS_RU = "00000000-0000-0000-0000-000000000001"
        const val SITUATIONS_EN = "00000000-0000-0000-0000-000000000002"
        const val MEMES_RU     = "00000000-0000-0000-0000-000000000011"
        const val MEMES_EN     = "00000000-0000-0000-0000-000000000012"

        val allMemeIds = setOf(MEMES_RU, MEMES_EN)
        val allSituationIds = setOf(SITUATIONS_RU, SITUATIONS_EN)
        val allIds = allMemeIds + allSituationIds
    }

    sealed interface Intent {
        data class ToggleMemePack(val id: String) : Intent
        data class ToggleSituationPack(val id: String) : Intent
        data class SetMode(val mode: GameMode) : Intent
        data class SetMaxRounds(val rounds: Int) : Intent
        data class SetHandSize(val size: Int) : Intent
        data class UpdateLobbyNameInput(val name: String) : Intent
        data class UpdateHandleInput(val handle: String) : Intent
        /** Called when user confirms pack selection from the in-app catalog picker. */
        data class AddPacksFromPicker(
            val extraMemePacks: List<MemePack> = emptyList(),
            val extraSituationPacks: List<SituationPack> = emptyList(),
            val memePackIds: Set<String>,
            val situationPackIds: Set<String>
        ) : Intent
        data object Create : Intent
    }

    data class State(
        val isLoading: Boolean = false,
        val isPacksLoading: Boolean = false,
        // Official packs available for selection
        val officialMemePacks: List<MemePack> = emptyList(),
        val officialSituationPacks: List<SituationPack> = emptyList(),
        // All selected pack IDs (official + extra from picker)
        val selectedMemePackIds: Set<String> = emptySet(),
        val selectedSituationPackIds: Set<String> = emptySet(),
        // Extra packs added from the in-app catalog picker
        val extraMemePacks: List<MemePack> = emptyList(),
        val extraSituationPacks: List<SituationPack> = emptyList(),
        val mode: GameMode = GameMode.SITUATION_TO_MEME,
        val maxRounds: Int = 5,
        val handSize: Int = 5,
        val lobbyNameInput: String = "",
        val handleInput: String = "",
        val error: String? = null
    ) {
        /** All meme packs to show in the selection list (official first, then extras). */
        val availableMemePacks: List<MemePack>
            get() = officialMemePacks + extraMemePacks.filter { extra ->
                officialMemePacks.none { it.id == extra.id }
            }

        /** All situation packs to show in the selection list (official first, then extras). */
        val availableSituationPacks: List<SituationPack>
            get() = officialSituationPacks + extraSituationPacks.filter { extra ->
                officialSituationPacks.none { it.id == extra.id }
            }

        val isCreateEnabled: Boolean
            get() = lobbyNameInput.isNotBlank()
                && selectedMemePackIds.isNotEmpty()
                && selectedSituationPackIds.isNotEmpty()
                && !isLoading
    }

    sealed interface Label {
        data class LobbyCreated(val gameId: String) : Label
    }
}
