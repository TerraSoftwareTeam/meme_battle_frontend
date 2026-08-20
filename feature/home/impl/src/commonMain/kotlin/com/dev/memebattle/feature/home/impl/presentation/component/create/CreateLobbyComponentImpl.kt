package com.dev.memebattle.feature.home.impl.presentation.component.create

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.feature.home.impl.presentation.store.create.CreateLobbyStore
import com.dev.memebattle.feature.home.impl.presentation.store.create.CreateLobbyStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class CreateLobbyComponentImpl(
    componentContext: ComponentContext,
    private val initialSelectedMemeIds: Set<String> = emptySet(),
    private val initialSelectedSituationIds: Set<String> = emptySet(),
    private val initialExtraMemeIds: Set<String> = emptySet(),
    private val initialExtraSituationIds: Set<String> = emptySet(),
    private val onCloseClicked: () -> Unit,
    private val onGameCreatedCallback: (String) -> Unit,
    private val onOpenPackPickerClicked: (
        selectedMeme: Set<String>,
        selectedSituation: Set<String>,
        extraMeme: Set<String>,
        extraSituation: Set<String>
    ) -> Unit = { _, _, _, _ -> },
    private val onGoToStoreClicked: () -> Unit = {}
) : CreateLobbyComponent, ComponentContext by componentContext, KoinComponent {

    private val store = instanceKeeper.getStore {
        CreateLobbyStoreFactory(
            storeFactory = get(),
            packRepository = get(),
            gameApiService = get(),
            initialSelectedMemeIds = initialSelectedMemeIds,
            initialSelectedSituationIds = initialSelectedSituationIds,
            initialExtraMemeIds = initialExtraMemeIds,
            initialExtraSituationIds = initialExtraSituationIds,
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<CreateLobbyStore.State> = store.stateFlow

    init {
        CoroutineScope(Dispatchers.Main).launch {
            store.labels.collect { label ->
                when (label) {
                    is CreateLobbyStore.Label.LobbyCreated -> {
                        onGameCreatedCallback(label.gameId)
                    }
                }
            }
        }
    }

    /**
     * Called by HomeComponentImpl after returning from PackPickerComponent.
     * Passes the confirmed pack selection back into the store.
     */
    fun applyPickerResult(
        memeIds: Set<String>,
        situationIds: Set<String>,
        extraMeme: List<MemePack>,
        extraSituation: List<SituationPack>
    ) {
        store.accept(
            CreateLobbyStore.Intent.AddPacksFromPicker(
                extraMemePacks = extraMeme,
                extraSituationPacks = extraSituation,
                memePackIds = memeIds,
                situationPackIds = situationIds
            )
        )
    }

    override fun onClose() = onCloseClicked()

    override fun onOpenPackPicker() {
        val s = store.state
        val extraMemeIds = s.extraMemePacks.map { it.id }.toSet()
        val extraSituationIds = s.extraSituationPacks.map { it.id }.toSet()
        onOpenPackPickerClicked(s.selectedMemePackIds, s.selectedSituationPackIds, extraMemeIds, extraSituationIds)
    }

    override fun onGoToStore() = onGoToStoreClicked()

    override fun onGameCreated(gameId: String) {
        // Obsolete, replaced by Intent.Create
    }

    override fun toggleMemePack(id: String) {
        store.accept(CreateLobbyStore.Intent.ToggleMemePack(id))
    }

    override fun toggleSituationPack(id: String) {
        store.accept(CreateLobbyStore.Intent.ToggleSituationPack(id))
    }

    override fun setMode(mode: com.dev.network.game.current.dto.GameMode) {
        store.accept(CreateLobbyStore.Intent.SetMode(mode))
    }

    override fun setMaxRounds(rounds: Int) {
        store.accept(CreateLobbyStore.Intent.SetMaxRounds(rounds))
    }

    override fun setHandSize(size: Int) {
        store.accept(CreateLobbyStore.Intent.SetHandSize(size))
    }

    override fun updateLobbyName(name: String) {
        store.accept(CreateLobbyStore.Intent.UpdateLobbyNameInput(name))
    }

    override fun updateHandle(handle: String) {
        store.accept(CreateLobbyStore.Intent.UpdateHandleInput(handle))
    }

    override fun createLobby() {
        store.accept(CreateLobbyStore.Intent.Create)
    }
}
