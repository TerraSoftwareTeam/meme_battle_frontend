package com.dev.memebattle.feature.home.impl.presentation.store.menu

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.core.utils.ExperimentalMviKotlinApi
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.network.game.current.api.GameApiService
import com.dev.network.game.current.api.ws.GameSocketService
import com.dev.network.game.current.dto.ws.LobbyEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomeMenuStoreFactory(
    private val storeFactory: StoreFactory,
    private val gameSocketService: GameSocketService,
    private val gameApiService: GameApiService,
    private val onNavigateToCreateLobby: () -> Unit,
    private val onNavigateToStore: () -> Unit,
    private val onNavigateToGame: (String) -> Unit,
) {
    @OptIn(ExperimentalMviKotlinApi::class)
    fun create(): HomeMenuStore =
        object : HomeMenuStore, Store<HomeMenuStore.Intent, HomeMenuStore.State, HomeMenuStore.Effect> by storeFactory.create(
            name = "HomeMenuStore",
            initialState = HomeMenuStore.State(),
            bootstrapper = coroutineBootstrapper {
                dispatch(Action.CheckActiveGame)
            },
            executorFactory = ::ExecutorImpl,
            reducer = { msg ->
                when (msg) {
                    is Msg.ActiveGameFound -> copy(
                        activeGameId = msg.gameId,
                        activeGameStatus = msg.status
                    )
                    is Msg.ShowLobbies -> copy(isLobbyListVisible = true)
                    is Msg.HideLobbies -> copy(isLobbyListVisible = false, lobbies = emptyList())
                    is Msg.LoadingStarted -> copy(isLoading = true)
                    is Msg.LoadingFinished -> copy(isLoading = false)
                    is Msg.LobbiesLoaded -> copy(lobbies = msg.lobbies)
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
                    is Msg.ShowJoinDialog -> copy(joinGameId = msg.gameId, joinHandleInput = "", joinError = null)
                    is Msg.UpdateJoinHandleInput -> copy(joinHandleInput = msg.handle)
                    is Msg.HideJoinDialog -> copy(joinGameId = null)
                    is Msg.SetJoining -> copy(isJoining = msg.isJoining)
                    is Msg.SetJoinError -> copy(joinError = msg.error)
                }
            }
        ) {}

    private sealed interface Action {
        data object CheckActiveGame : Action
    }

    private sealed interface Msg {
        data class ActiveGameFound(val gameId: String?, val status: com.dev.network.game.current.dto.GameStatus?) : Msg
        data object ShowLobbies : Msg
        data object HideLobbies : Msg
        data object LoadingStarted : Msg
        data object LoadingFinished : Msg
        data class LobbiesLoaded(val lobbies: List<LobbyEvent.LobbyCreated>) : Msg
        data class LobbyCreated(val event: LobbyEvent.LobbyCreated) : Msg
        data class LobbyUpdated(val event: LobbyEvent.LobbyUpdated) : Msg
        data class LobbyRemoved(val event: LobbyEvent.LobbyRemoved) : Msg
        data class ShowJoinDialog(val gameId: String) : Msg
        data class UpdateJoinHandleInput(val handle: String) : Msg
        data object HideJoinDialog : Msg
        data class SetJoining(val isJoining: Boolean) : Msg
        data class SetJoinError(val error: String?) : Msg
    }

    private inner class ExecutorImpl : CoroutineExecutor<HomeMenuStore.Intent, Action, HomeMenuStore.State, Msg, HomeMenuStore.Effect>() {
        
        private var socketJob: Job? = null

        override fun executeAction(action: Action) {
            when (action) {
                is Action.CheckActiveGame -> checkActiveGame()
            }
        }
        
        override fun executeIntent(intent: HomeMenuStore.Intent) {
            when (intent) {
                is HomeMenuStore.Intent.OnCheckActiveGame -> checkActiveGame()
                is HomeMenuStore.Intent.OnPlayClicked -> {
                    val activeId = state().activeGameId
                    if (activeId != null) {
                        onNavigateToGame(activeId)
                    } else {
                        dispatch(Msg.ShowLobbies)
                        connectToSocket()
                    }
                }
                is HomeMenuStore.Intent.OnCloseLobbiesClicked -> {
                    dispatch(Msg.HideLobbies)
                    disconnectSocket()
                    checkActiveGame()
                }
                is HomeMenuStore.Intent.OnStoreClicked -> {
                    onNavigateToStore()
                }
                is HomeMenuStore.Intent.OnCreateLobbyClicked -> {
                    onNavigateToCreateLobby()
                }
                is HomeMenuStore.Intent.OnJoinLobbyClicked -> {
                    val activeId = state().activeGameId
                    val lobby = state().lobbies.find { it.id == intent.gameId }
                    if (activeId != null && activeId != intent.gameId) {
                        dispatch(Msg.ShowJoinDialog(intent.gameId))
                        dispatch(Msg.SetJoinError("Вы уже находитесь в активной игре! Выйдите из неё перед входом в другое лобби."))
                    } else if (lobby != null && lobby.maxPlayers != null && lobby.maxPlayers!! > 0 && lobby.playersCount >= lobby.maxPlayers!!) {
                        dispatch(Msg.ShowJoinDialog(intent.gameId))
                        dispatch(Msg.SetJoinError("Достигнут лимит игроков в этом лобби!"))
                    } else {
                        dispatch(Msg.ShowJoinDialog(intent.gameId))
                    }
                }
                is HomeMenuStore.Intent.UpdateJoinHandleInput -> dispatch(Msg.UpdateJoinHandleInput(intent.handle))
                is HomeMenuStore.Intent.CancelJoin -> dispatch(Msg.HideJoinDialog)
                is HomeMenuStore.Intent.ConfirmJoin -> joinGame()
            }
        }

        private fun checkActiveGame() {
            scope.launch {
                val result = gameApiService.getActiveGame()
                if (result is NetworkResult.Success) {
                    val dto = result.data
                    if (dto.game_id.isNotBlank() && dto.status != com.dev.network.game.current.dto.GameStatus.FINISHED) {
                        dispatch(Msg.ActiveGameFound(dto.game_id, dto.status))
                    } else {
                        dispatch(Msg.ActiveGameFound(null, null))
                    }
                } else {
                    dispatch(Msg.ActiveGameFound(null, null))
                }
            }
        }
        
        private fun connectToSocket() {
            if (socketJob != null) {
                println("[HomeMenuStore] connectToSocket: already connecting/connected, skipping")
                return
            }
            println("[HomeMenuStore] connectToSocket: starting...")
            socketJob = scope.launch {
                // 1. Загружаем текущий список лобби через REST
                dispatch(Msg.LoadingStarted)
                val gamesResult = gameApiService.listActiveGames()
                if (gamesResult is NetworkResult.Success) {
                    val lobbies = gamesResult.data.games.map { dto ->
                        LobbyEvent.LobbyCreated(
                            id = dto.id,
                            name = dto.name,
                            hostId = dto.host_id,
                            mode = dto.mode.name.lowercase(),
                            maxRounds = dto.max_rounds,
                            handSize = dto.hand_size,
                            playersCount = dto.players_count,
                            maxPlayers = dto.max_players,
                            createdAt = dto.created_at,
                        )
                    }.sortedByDescending { it.createdAt }
                    dispatch(Msg.LobbiesLoaded(lobbies))
                }
                dispatch(Msg.LoadingFinished)

                // 2. Подключаем WS и слушаем обновления в реальном времени
                println("[HomeMenuStore] Calling gameSocketService.connect()...")
                gameSocketService.connect()
                println("[HomeMenuStore] gameSocketService.connect() returned, subscribing to lobbies...")
                gameSocketService.subscribeToLobbies()
                println("[HomeMenuStore] Subscribed to lobbies, collecting events...")
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

        private fun joinGame() {
            val st = state()
            val gameId = st.joinGameId ?: return
            val handle = st.joinHandleInput.trim().takeIf { it.isNotEmpty() }
            
            scope.launch {
                dispatch(Msg.SetJoining(true))
                dispatch(Msg.SetJoinError(null))
                val result = gameApiService.joinGame(
                    gameId, 
                    com.dev.network.game.current.dto.JoinGameRequest(handle = handle)
                )
                
                when (result) {
                    is NetworkResult.Success -> {
                        dispatch(Msg.SetJoining(false))
                        dispatch(Msg.HideJoinDialog)
                        onNavigateToGame(gameId)
                    }
                    is NetworkResult.Error -> {
                        // Если мы уже в игре, то joinGame вернет ошибку. Проверим, можем ли мы получить стейт
                        val stateResult = gameApiService.getGameState(gameId)
                        dispatch(Msg.SetJoining(false))
                        if (stateResult is NetworkResult.Success) {
                            dispatch(Msg.HideJoinDialog)
                            onNavigateToGame(gameId)
                        } else {
                            dispatch(Msg.SetJoinError(result.error.toString()))
                        }
                    }
                }
            }
        }
    }
}
