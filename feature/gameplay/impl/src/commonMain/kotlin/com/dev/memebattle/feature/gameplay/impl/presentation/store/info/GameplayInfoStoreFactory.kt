package com.dev.memebattle.feature.gameplay.impl.presentation.store.info

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.core.utils.ExperimentalMviKotlinApi
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.network.game.current.api.GameApiService
import com.dev.network.game.current.dto.GameMode
import com.dev.network.game.current.dto.GameStateDto
import com.dev.network.game.current.dto.ReadyRequest
import com.dev.network.game.current.dto.RoundPhase
import com.dev.network.game.current.dto.ws.GameEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class GameplayInfoStoreFactory(
    private val storeFactory: StoreFactory,
    private val gameApiService: GameApiService,
    private val gameId: String,
    private val myUserId: String,
    private val gameEvents: Flow<GameEvent>,
    private val initialState: GameStateDto? = null,
) {
    @OptIn(ExperimentalMviKotlinApi::class)
    fun create(): GameplayInfoStore = object : GameplayInfoStore,
        Store<GameplayInfoStore.Intent, GameplayInfoStore.State, GameplayInfoStore.Effect> by storeFactory.create(
            name = "GameplayInfoStore",
            initialState = GameplayInfoStore.State(isLoading = initialState == null),
            bootstrapper = coroutineBootstrapper {
                initialState?.let { dispatch(Action.HydrateFromSnapshot(it)) }
                dispatch(Action.ObserveEvents)
            },
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    private sealed interface Action {
        data class HydrateFromSnapshot(val snapshot: GameStateDto) : Action
        data object ObserveEvents : Action
    }

    private sealed interface Msg {
        data class PhaseChanged(val phase: RoundPhase) : Msg
        data class ModeSet(val mode: GameMode) : Msg
        data class RoundUpdated(val number: Int, val expiresAt: String?) : Msg
        data class TotalRoundsSet(val count: Int) : Msg
        data class PlayerCountChanged(val count: Int) : Msg
        data class ReadyCountChanged(val count: Int) : Msg
        data class VotedCountChanged(val count: Int) : Msg
        data class IsHostSet(val isHost: Boolean) : Msg
        data class AmIReadyChanged(val isReady: Boolean) : Msg
        data class IsSettingReadyChanged(val isSettingReady: Boolean) : Msg
        data object StartingGame : Msg
        data object StartingGameFinished : Msg
        data object LoadingFinished : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<GameplayInfoStore.Intent, Action, GameplayInfoStore.State, Msg, GameplayInfoStore.Effect>() {

        override fun executeAction(action: Action) {
            when (action) {
                is Action.HydrateFromSnapshot -> hydrateFromSnapshot(action.snapshot)
                is Action.ObserveEvents -> observeEvents()
            }
        }

        override fun executeIntent(intent: GameplayInfoStore.Intent) {
            when (intent) {
                is GameplayInfoStore.Intent.Initialize -> intent.snapshot?.let { hydrateFromSnapshot(it) }
                is GameplayInfoStore.Intent.Init -> Unit
                is GameplayInfoStore.Intent.StartGame -> startGame()
                is GameplayInfoStore.Intent.SetReady -> setReady(intent.isReady)
            }
        }

        private fun hydrateFromSnapshot(snapshot: GameStateDto) {
            dispatch(Msg.ModeSet(snapshot.game.mode))
            dispatch(Msg.PlayerCountChanged(snapshot.players.size))
            dispatch(Msg.ReadyCountChanged(snapshot.players.count { it.is_ready }))
            
            val me = snapshot.players.find { it.user_id == myUserId }
            if (me != null) {
                dispatch(Msg.AmIReadyChanged(me.is_ready))
            }

            snapshot.round?.let { round ->
                val phase = round.phase
                dispatch(Msg.PhaseChanged(phase))
                dispatch(Msg.RoundUpdated(round.round_number, round.phase_expires_at))
            }
            // isHost — тот кто создал игру; пока нет в DTO, определяем первым игроком
            // TODO: добавить host_user_id в GameDto когда бэк добавит
            dispatch(Msg.LoadingFinished)
        }

        private fun observeEvents() {
            gameEvents.onEach { event ->
                when (event) {
                    is GameEvent.PlayerJoined -> {
                        dispatch(Msg.PlayerCountChanged(event.playersCount))
                    }
                    is GameEvent.PlayerReadyChanged -> {
                        if (event.userId == myUserId) {
                            dispatch(Msg.AmIReadyChanged(event.isReady))
                            dispatch(Msg.IsSettingReadyChanged(false))
                        }
                    }
                    is GameEvent.GameStarted -> {
                        dispatch(Msg.TotalRoundsSet(event.roundsCount))
                        dispatch(Msg.PhaseChanged(RoundPhase.WAITING))
                    }
                    is GameEvent.RoundStarted -> {
                        dispatch(Msg.PhaseChanged(RoundPhase.SUBMITTING))
                        dispatch(Msg.RoundUpdated(event.roundNumber, event.phaseExpiresAt))
                    }
                    is GameEvent.RoundPhaseChanged -> {
                        val phase = when (event.phase) {
                            "submitting" -> RoundPhase.SUBMITTING
                            "voting" -> RoundPhase.VOTING
                            "finished" -> RoundPhase.FINISHED
                            else -> RoundPhase.WAITING
                        }
                        dispatch(Msg.PhaseChanged(phase))
                        dispatch(Msg.RoundUpdated(state().roundNumber, event.phaseExpiresAt))
                        if (phase == RoundPhase.VOTING) dispatch(Msg.VotedCountChanged(0))
                    }
                    is GameEvent.VoteReceived -> {
                        dispatch(Msg.VotedCountChanged(state().votedCount + 1))
                    }
                    is GameEvent.RoundFinished -> {
                        dispatch(Msg.PhaseChanged(RoundPhase.FINISHED))
                    }
                    is GameEvent.GameFinished -> {
                        dispatch(Msg.PhaseChanged(RoundPhase.FINISHED))
                    }
                    else -> Unit
                }
            }.launchIn(scope)
        }

        private fun startGame() {
            if (state().isStartingGame) return
            dispatch(Msg.StartingGame)
            scope.launch {
                val result = gameApiService.startGameSession(gameId)
                if (result is NetworkResult.Error) {
                    publish(GameplayInfoStore.Effect.ShowError(result.error.userMessage()))
                }
                dispatch(Msg.StartingGameFinished)
            }
        }

        private fun setReady(isReady: Boolean) {
            dispatch(Msg.IsSettingReadyChanged(true))
            scope.launch {
                val result = gameApiService.setReady(gameId, ReadyRequest(is_ready = isReady))
                if (result is NetworkResult.Error) {
                    dispatch(Msg.IsSettingReadyChanged(false))
                    publish(GameplayInfoStore.Effect.ShowError(result.error.userMessage()))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<GameplayInfoStore.State, Msg> {
        override fun GameplayInfoStore.State.reduce(msg: Msg): GameplayInfoStore.State = when (msg) {
            is Msg.PhaseChanged -> copy(phase = msg.phase)
            is Msg.ModeSet -> copy(mode = msg.mode)
            is Msg.RoundUpdated -> copy(roundNumber = msg.number, phaseExpiresAt = msg.expiresAt)
            is Msg.TotalRoundsSet -> copy(totalRounds = msg.count)
            is Msg.PlayerCountChanged -> copy(playerCount = msg.count)
            is Msg.ReadyCountChanged -> copy(readyCount = msg.count)
            is Msg.VotedCountChanged -> copy(votedCount = msg.count)
            is Msg.IsHostSet -> copy(isHost = msg.isHost)
            is Msg.AmIReadyChanged -> copy(amIReady = msg.isReady)
            is Msg.IsSettingReadyChanged -> copy(isSettingReady = msg.isSettingReady)
            is Msg.StartingGame -> copy(isStartingGame = true)
            is Msg.StartingGameFinished -> copy(isStartingGame = false)
            is Msg.LoadingFinished -> copy(isLoading = false)
        }
    }
}

private fun com.dev.memebattle.core.network.error.NetworkError.userMessage(): String = when (this) {
    is com.dev.memebattle.core.network.error.NetworkError.ApiException -> message ?: "Ошибка сервера ($code)"
    is com.dev.memebattle.core.network.error.NetworkError.ServerError -> "Ошибка сервера ($code)"
    is com.dev.memebattle.core.network.error.NetworkError.Exception -> cause.message ?: "Неизвестная ошибка"
    else -> "Произошла ошибка"
}
