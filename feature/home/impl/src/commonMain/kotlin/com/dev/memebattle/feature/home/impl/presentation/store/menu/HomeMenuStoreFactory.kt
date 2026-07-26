package com.dev.memebattle.feature.home.impl.presentation.store.menu

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.core.utils.ExperimentalMviKotlinApi
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.dev.network.game.current.api.ws.GameSocketService
import com.dev.network.game.current.dto.ws.LobbyEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomeMenuStoreFactory(
    private val storeFactory: StoreFactory,
    private val gameSocketService: GameSocketService,
    private val onNavigateToCreateLobby: () -> Unit,
    private val onNavigateToStore: () -> Unit
) {
    @OptIn(ExperimentalMviKotlinApi::class)
    fun create(): HomeMenuStore =
        object : HomeMenuStore, Store<HomeMenuStore.Intent, HomeMenuStore.State, HomeMenuStore.Effect> by storeFactory.create(
            name = "HomeMenuStore",
            initialState = HomeMenuStore.State(),
            bootstrapper = coroutineBootstrapper {
                // Initial load
            },
            executorFactory = ::ExecutorImpl,
            reducer = { msg ->
                when (msg) {
                    is Msg.ShowLobbies -> copy(isLobbyListVisible = true)
                    is Msg.HideLobbies -> copy(isLobbyListVisible = false)
                    is Msg.LobbyCreated -> {
                        val newLobbies = lobbies.filter { it.id != msg.event.id } + msg.event
                        copy(lobbies = newLobbies.sortedByDescending { it.createdAt })
                    }
                    is Msg.LobbyUpdated -> {
                        val newLobbies = lobbies.map { if (it.id == msg.event.id) it.copy(playersCount = msg.event.playersCount) else it }
                        copy(lobbies = newLobbies)
                    }
                    is Msg.LobbyRemoved -> {
                        copy(lobbies = lobbies.filter { it.id != msg.event.id })
                    }
                }
            }
        ) {}

    private sealed interface Msg {
        data object ShowLobbies : Msg
        data object HideLobbies : Msg
        data class LobbyCreated(val event: LobbyEvent.LobbyCreated) : Msg
        data class LobbyUpdated(val event: LobbyEvent.LobbyUpdated) : Msg
        data class LobbyRemoved(val event: LobbyEvent.LobbyRemoved) : Msg
    }

    private inner class ExecutorImpl : CoroutineExecutor<HomeMenuStore.Intent, Unit, HomeMenuStore.State, Msg, HomeMenuStore.Effect>() {
        
        private var socketJob: Job? = null
        
        override fun executeIntent(intent: HomeMenuStore.Intent) {
            when (intent) {
                is HomeMenuStore.Intent.OnPlayClicked -> {
                    dispatch(Msg.ShowLobbies)
                    connectToSocket()
                }
                is HomeMenuStore.Intent.OnCloseLobbiesClicked -> {
                    dispatch(Msg.HideLobbies)
                    disconnectSocket()
                }
                is HomeMenuStore.Intent.OnStoreClicked -> {
                    onNavigateToStore()
                }
                is HomeMenuStore.Intent.OnCreateLobbyClicked -> {
                    onNavigateToCreateLobby()
                }
            }
        }
        
        private fun connectToSocket() {
            if (socketJob != null) return
            socketJob = scope.launch {
                gameSocketService.connect()
                gameSocketService.subscribeToLobbies()
                gameSocketService.lobbyEvents.onEach { event ->
                    when (event) {
                        is LobbyEvent.LobbyCreated -> dispatch(Msg.LobbyCreated(event))
                        is LobbyEvent.LobbyUpdated -> dispatch(Msg.LobbyUpdated(event))
                        is LobbyEvent.LobbyRemoved -> dispatch(Msg.LobbyRemoved(event))
                    }
                }.launchIn(this)
            }
        }
        
        private fun disconnectSocket() {
            // FIX: Do NOT call gameSocketService.disconnect() here.
            // GameSocketService is a singleton shared with Gameplay — disconnecting it from
            // Home would kill the connection mid-game. Simply cancel the local collection
            // job to stop reacting to lobby events.
            socketJob?.cancel()
            socketJob = null
        }
    }
}
