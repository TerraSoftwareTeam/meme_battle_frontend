package com.dev.memebattle.feature.home.impl.presentation.component.create

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.memebattle.feature.home.impl.presentation.store.create.CreateLobbyStore
import com.dev.memebattle.feature.home.impl.presentation.store.create.CreateLobbyStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class CreateLobbyComponentImpl(
    componentContext: ComponentContext,
    private val onCloseClicked: () -> Unit,
    private val onGameCreatedCallback: (String) -> Unit
) : CreateLobbyComponent, ComponentContext by componentContext, KoinComponent {

    private val store = instanceKeeper.getStore {
        CreateLobbyStoreFactory(
            storeFactory = get(),
            packRepository = get(),
            gameApiService = get()
        ).create()
    }

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

    override fun onClose() {
        onCloseClicked()
    }

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

    override fun updateHandle(handle: String) {
        store.accept(CreateLobbyStore.Intent.UpdateHandleInput(handle))
    }

    override fun createLobby() {
        store.accept(CreateLobbyStore.Intent.Create)
    }
}
