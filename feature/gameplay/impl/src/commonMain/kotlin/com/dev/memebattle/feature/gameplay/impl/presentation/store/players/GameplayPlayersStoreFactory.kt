package com.dev.memebattle.feature.gameplay.impl.presentation.store.players

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.core.utils.ExperimentalMviKotlinApi
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.dev.network.game.current.dto.GameStateDto
import com.dev.network.game.current.dto.ws.GameEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class GameplayPlayersStoreFactory(
    private val storeFactory: StoreFactory,
    private val myUserId: String,
    private val gameEvents: Flow<GameEvent>,
    private val initialSnapshot: GameStateDto? = null,
) {
    @OptIn(ExperimentalMviKotlinApi::class)
    fun create(): GameplayPlayersStore = object : GameplayPlayersStore,
        Store<GameplayPlayersStore.Intent, GameplayPlayersStore.State, GameplayPlayersStore.Effect> by storeFactory.create(
            name = "GameplayPlayersStore",
            initialState = GameplayPlayersStore.State(isLoading = initialSnapshot == null),
            bootstrapper = coroutineBootstrapper {
                initialSnapshot?.let { dispatch(Action.HydrateFromSnapshot(it)) }
                dispatch(Action.ObserveEvents)
            },
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    // ── Actions ────────────────────────────────────────────────────────────

    private sealed interface Action {
        data class HydrateFromSnapshot(val snapshot: GameStateDto) : Action
        data object ObserveEvents : Action
    }

    // ── Messages ───────────────────────────────────────────────────────────

    private sealed interface Msg {
        data class PlayersLoaded(val players: List<GameplayPlayersStore.PlayerUiModel>) : Msg
        data class PlayerReadyChanged(val userId: String, val isReady: Boolean) : Msg
        data class PlayerSubmitted(val userId: String) : Msg
        data class PlayerVoted(val userId: String) : Msg
        data class ScoresUpdated(val scores: Map<String, Int>) : Msg
        data class ShowPreview(val userId: String) : Msg
        data object HidePreview : Msg
        data object ResetSubmittedStatus : Msg
        data object LoadingFinished : Msg
    }

    // ── Executor ───────────────────────────────────────────────────────────

    private inner class ExecutorImpl :
        CoroutineExecutor<GameplayPlayersStore.Intent, Action, GameplayPlayersStore.State, Msg, GameplayPlayersStore.Effect>() {

        override fun executeAction(action: Action) {
            when (action) {
                is Action.HydrateFromSnapshot -> hydrateFromSnapshot(action.snapshot)
                is Action.ObserveEvents -> observeEvents()
            }
        }

        override fun executeIntent(intent: GameplayPlayersStore.Intent) {
            when (intent) {
                is GameplayPlayersStore.Intent.Init -> Unit
                is GameplayPlayersStore.Intent.ShowSubmissionPreview ->
                    dispatch(Msg.ShowPreview(intent.userId))
                is GameplayPlayersStore.Intent.HideSubmissionPreview ->
                    dispatch(Msg.HidePreview)
                is GameplayPlayersStore.Intent.VoteForPlayer -> {
                    dispatch(Msg.HidePreview)
                    // Маршрутизируем Vote через Effect — GameplayComponentImpl поймает и вызовет API
                    publish(GameplayPlayersStore.Effect.VoteRequested(intent.submissionId))
                }
            }
        }

        private fun hydrateFromSnapshot(snapshot: GameStateDto) {
            val players = snapshot.players.map { dto ->
                GameplayPlayersStore.PlayerUiModel(
                    userId = dto.user_id,
                    handle = dto.handle,
                    score = dto.score,
                    isReady = dto.is_ready,
                    hasSubmitted = dto.has_submitted,
                    isMe = dto.user_id == myUserId,
                )
            }
            dispatch(Msg.PlayersLoaded(players))
            dispatch(Msg.LoadingFinished)
        }

        private fun observeEvents() {
            gameEvents.onEach { event ->
                when (event) {
                    is GameEvent.PlayerJoined -> {
                        // handle нового игрока не приходит в событии —
                        // GameplayComponentImpl должен сделать refresh getGameState и перегидрировать
                    }
                    is GameEvent.PlayerReadyChanged ->
                        dispatch(Msg.PlayerReadyChanged(event.userId, event.isReady))

                    is GameEvent.SubmissionReceived ->
                        dispatch(Msg.PlayerSubmitted(event.userId))

                    is GameEvent.VoteReceived ->
                        dispatch(Msg.PlayerVoted(event.voterId))

                    is GameEvent.RoundStarted ->
                        dispatch(Msg.ResetSubmittedStatus)

                    is GameEvent.RoundFinished -> {
                        // scoreboard — накопленные очки после раунда (не дельта)
                        val scoreMap = event.scoreboard.associate { it.userId to it.score }
                        dispatch(Msg.ScoresUpdated(scoreMap))
                    }

                    is GameEvent.GameFinished -> {
                        val scoreMap = event.finalScoreboard.associate { it.userId to it.score }
                        dispatch(Msg.ScoresUpdated(scoreMap))
                    }

                    else -> Unit
                }
            }.launchIn(scope)
        }
    }

    // ── Reducer ────────────────────────────────────────────────────────────

    private object ReducerImpl : Reducer<GameplayPlayersStore.State, Msg> {
        override fun GameplayPlayersStore.State.reduce(msg: Msg): GameplayPlayersStore.State = when (msg) {
            is Msg.PlayersLoaded -> copy(players = msg.players)
            is Msg.PlayerReadyChanged -> copy(players = players.map {
                if (it.userId == msg.userId) it.copy(isReady = msg.isReady) else it
            })
            is Msg.PlayerSubmitted -> copy(players = players.map {
                if (it.userId == msg.userId) it.copy(hasSubmitted = true) else it
            })
            is Msg.PlayerVoted -> copy(players = players.map {
                if (it.userId == msg.userId) it.copy(hasVoted = true) else it
            })
            is Msg.ScoresUpdated -> copy(players = players.map { p ->
                msg.scores[p.userId]?.let { p.copy(score = it) } ?: p
            })
            is Msg.ShowPreview -> copy(previewingSubmissionForUserId = msg.userId)
            is Msg.HidePreview -> copy(previewingSubmissionForUserId = null)
            is Msg.ResetSubmittedStatus -> copy(
                players = players.map { it.copy(hasSubmitted = false, hasVoted = false) }
            )
            is Msg.LoadingFinished -> copy(isLoading = false)
        }
    }
}
