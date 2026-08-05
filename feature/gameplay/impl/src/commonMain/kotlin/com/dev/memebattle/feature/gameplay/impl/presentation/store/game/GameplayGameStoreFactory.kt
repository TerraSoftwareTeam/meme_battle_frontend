package com.dev.memebattle.feature.gameplay.impl.presentation.store.game

import com.dev.network.game.current.dto.SituationGameCard
import com.dev.network.game.current.dto.SituationCardData
import com.dev.network.game.current.dto.MemeGameCard
import com.dev.network.game.current.dto.MemeCardData
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.core.utils.ExperimentalMviKotlinApi
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.network.game.current.api.GameApiService
import com.dev.network.game.current.dto.GameCard
import com.dev.network.game.current.dto.GameStateDto
import com.dev.network.game.current.dto.GameStatus
import com.dev.network.game.current.dto.JoinGameRequest
import com.dev.network.game.current.dto.SubmitCardRequest
import com.dev.network.game.current.dto.VoteRequest
import com.dev.network.game.current.dto.ws.GameEvent
import com.dev.network.game.current.dto.ws.HandCard
import com.dev.network.game.current.dto.ws.PersonalEvent
import com.dev.network.game.current.dto.ws.ScoreboardEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class GameplayGameStoreFactory(
    private val storeFactory: StoreFactory,
    private val gameApiService: GameApiService,
    private val gameId: String,
    private val myUserId: String,
    private val gameEvents: Flow<GameEvent>,
    private val personalEvents: Flow<PersonalEvent>,
    private val initialSnapshot: GameStateDto? = null,
) {
    @OptIn(ExperimentalMviKotlinApi::class)
    fun create(): GameplayGameStore = object : GameplayGameStore,
        Store<GameplayGameStore.Intent, GameplayGameStore.State, GameplayGameStore.Effect> by storeFactory.create(
            name = "GameplayGameStore",
            initialState = computeInitialState(),
            bootstrapper = coroutineBootstrapper {
                dispatch(Action.ObserveEvents)
            },
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    private fun computeInitialState(): GameplayGameStore.State {
        val snapshot = initialSnapshot
        
        // Snapshot ещё не загружен - показываем загрузку
        if (snapshot == null) {
            return GameplayGameStore.State(isLoading = true, uiPhase = GameplayGameStore.UiPhase.HandleInput)
        }
        
        // Проверяем, есть ли текущий пользователь в игре
        val myPlayer = snapshot.players.find { it.user_id == myUserId }
        
        // Пользователь не в игре - показываем экран ввода handle
        if (myPlayer == null) {
            return GameplayGameStore.State(
                isLoading = false, 
                uiPhase = GameplayGameStore.UiPhase.HandleInput,
                isJoining = false,
            )
        }
        
        // Пользователь уже в игре - заполняем его handle и показываем Lobby
        // (handle уже присвоен сервером при создании лобби или предыдущем join)
        return when (snapshot.game.status) {
            GameStatus.LOBBY -> GameplayGameStore.State(
                isLoading = false, 
                uiPhase = GameplayGameStore.UiPhase.Lobby,
                handleInput = myPlayer.handle, // Показываем текущий handle
                isJoining = false,
            )
            GameStatus.PLAYING -> GameplayGameStore.State(
                isLoading = false,
                uiPhase = GameplayGameStore.UiPhase.Lobby, // WebSocket события переключат фазу
                handleInput = myPlayer.handle,
                handCards = snapshot.my_hand,
                isJoining = false,
            )
            GameStatus.FINISHED -> GameplayGameStore.State(
                isLoading = false,
                uiPhase = GameplayGameStore.UiPhase.GameFinished,
                handleInput = myPlayer.handle,
                isJoining = false,
            )
        }
    }

    // ── Actions (от Bootstrapper) ──────────────────────────────────────────

    private sealed interface Action {
        data object ObserveEvents : Action
    }

    // ── Messages (от Executor → Reducer) ──────────────────────────────────

    private sealed interface Msg {
        // Initialization
        data class Initialized(
            val isPlayerInGame: Boolean,
            val handle: String,
            val gameStatus: GameStatus?,
            val hand: List<GameCard>,
        ) : Msg

        // HandleInput
        data class HandleInputChanged(val text: String) : Msg
        data object JoiningStarted : Msg
        data object JoiningFailed : Msg

        // Lobby
        data object EnteredLobby : Msg

        // Round lifecycle
        data class RoundStarted(
            val roundId: String,
            val promptCard: GameCard,
            val hand: List<GameCard>,
        ) : Msg
        data object PhaseChangedToVoting : Msg
        data class SubmissionsLoaded(
            val cards: List<GameCard>,
            val ids: List<String>,
        ) : Msg

        // Submitting
        data class HandUpdated(val cards: List<GameCard>) : Msg
        data class CardIndexChanged(val index: Int) : Msg
        data object SubmittingStarted : Msg
        data object SubmittingFinished : Msg
        data class MySubmissionConfirmed(val card: GameCard) : Msg

        // Voting
        data class SubmissionIndexChanged(val index: Int) : Msg
        data object VotingStarted : Msg
        data object VotingFinished : Msg
        data object VotedConfirmed : Msg

        // Toggle
        data object TogglePrompt : Msg

        // RoundResult
        data class RoundResultReceived(val data: GameplayGameStore.RoundResultData) : Msg
        data object RoundResultDismissed : Msg

        // GameFinished
        data class GameFinishedReceived(
            val winnerUserId: String?,
            val finalScoreboard: List<ScoreboardEntry>,
        ) : Msg
    }

    // ── Executor ───────────────────────────────────────────────────────────

    private inner class ExecutorImpl :
        CoroutineExecutor<GameplayGameStore.Intent, Action, GameplayGameStore.State, Msg, GameplayGameStore.Effect>() {

        override fun executeAction(action: Action) {
            when (action) {
                Action.ObserveEvents -> observeEvents()
            }
        }

        override fun executeIntent(intent: GameplayGameStore.Intent) {
            when (intent) {
                is GameplayGameStore.Intent.Initialize -> initialize(intent.snapshot)
                is GameplayGameStore.Intent.TypeHandle -> dispatch(Msg.HandleInputChanged(intent.text))
                is GameplayGameStore.Intent.JoinLobby -> joinLobby(intent.handle)
                is GameplayGameStore.Intent.SelectCard -> selectCard(intent.index)
                is GameplayGameStore.Intent.Submit -> submitCard()
                is GameplayGameStore.Intent.Vote -> vote(intent.submissionId)
                is GameplayGameStore.Intent.TogglePromptVisible -> dispatch(Msg.TogglePrompt)
            }
        }

        // ── Initialization ─────────────────────────────────────────────────

        private fun initialize(snapshot: GameStateDto?) {
            if (snapshot == null) {
                // Нет snapshot - показываем экран ввода handle
                dispatch(Msg.Initialized(
                    isPlayerInGame = false,
                    handle = "",
                    gameStatus = null,
                    hand = emptyList(),
                ))
                return
            }

            val myPlayer = snapshot.players.find { it.user_id == myUserId }
            
            if (myPlayer == null) {
                // Пользователь не в игре - показываем экран ввода handle
                dispatch(Msg.Initialized(
                    isPlayerInGame = false,
                    handle = "",
                    gameStatus = snapshot.game.status,
                    hand = emptyList(),
                ))
            } else {
                // Пользователь уже в игре - переходим сразу в Lobby
                dispatch(Msg.Initialized(
                    isPlayerInGame = true,
                    handle = myPlayer.handle,
                    gameStatus = snapshot.game.status,
                    hand = snapshot.my_hand,
                ))
            }
        }

        // ── HandleInput ────────────────────────────────────────────────────

        private fun joinLobby(handle: String?) {
            if (state().isJoining) return
            dispatch(Msg.JoiningStarted)
            scope.launch {
                val result = gameApiService.joinGame(
                    id = gameId,
                    body = JoinGameRequest(handle = handle?.trim()?.takeIf { it.isNotEmpty() })
                )
                when (result) {
                    is NetworkResult.Success -> dispatch(Msg.EnteredLobby)
                    is NetworkResult.Error -> {
                        dispatch(Msg.JoiningFailed)
                        publish(GameplayGameStore.Effect.ShowError(
                            result.error.userMessage()
                        ))
                    }
                }
            }
        }

        // ── Submitting ─────────────────────────────────────────────────────

        private fun selectCard(index: Int) {
            when (state().uiPhase) {
                GameplayGameStore.UiPhase.Submitting ->
                    dispatch(Msg.CardIndexChanged(index.coerceIn(0, state().handCards.lastIndex)))
                GameplayGameStore.UiPhase.Voting ->
                    dispatch(Msg.SubmissionIndexChanged(index.coerceIn(0, state().submissionIds.lastIndex)))
                else -> Unit
            }
        }

        private fun submitCard() {
            val st = state()
            if (!st.canSubmit) return
            val card = st.selectedHandCard ?: return
            val roundId = st.roundId ?: return
            val cardId = when(card) {
                is MemeGameCard -> card.data.id
                is SituationGameCard -> card.data.id
            }
            dispatch(Msg.SubmittingStarted)
            scope.launch {
                val result = gameApiService.submitCard(
                    id = gameId,
                    body = SubmitCardRequest(card_id = cardId)
                )
                when (result) {
                    is NetworkResult.Success -> dispatch(Msg.MySubmissionConfirmed(card))
                    is NetworkResult.Error -> publish(
                        GameplayGameStore.Effect.ShowError(result.error.userMessage())
                    )
                }
                dispatch(Msg.SubmittingFinished)
            }
        }

        // ── Voting ─────────────────────────────────────────────────────────

        private fun vote(submissionId: String) {
            val st = state()
            if (!st.canVote) return
            val roundId = st.roundId ?: return
            dispatch(Msg.VotingStarted)
            scope.launch {
                val result = gameApiService.voteCard(
                    id = gameId,
                    body = VoteRequest(submission_id = submissionId)
                )
                when (result) {
                    is NetworkResult.Success -> dispatch(Msg.VotedConfirmed)
                    is NetworkResult.Error -> publish(
                        GameplayGameStore.Effect.ShowError(result.error.userMessage())
                    )
                }
                dispatch(Msg.VotingFinished)
            }
        }

        // ── WS fan-out ─────────────────────────────────────────────────────

        private fun observeEvents() {
            gameEvents.onEach { event ->
                when (event) {
                    is GameEvent.RoundStarted -> {
                        val promptCardId = "prompt_${event.roundId}"
                        val promptCard: GameCard = if (event.promptKind == "meme") {
                            MemeGameCard(MemeCardData(promptCardId, event.promptContent))
                        } else {
                            SituationGameCard(SituationCardData(promptCardId, event.promptContent))
                        }
                        // hand придёт через PersonalEvent.HandUpdated
                        dispatch(Msg.RoundStarted(event.roundId, promptCard, emptyList()))
                    }
                    is GameEvent.RoundPhaseChanged -> {
                        if (event.phase == "voting") {
                            dispatch(Msg.PhaseChangedToVoting)
                            // submission-карты придут через снимок getGameState — инициирует ComponentImpl
                        }
                    }
                    is GameEvent.RoundFinished -> {
                        val result = GameplayGameStore.RoundResultData(
                            roundNumber = event.roundNumber,
                            winnerUserId = event.winnerUserId,
                            winnerHandle = null, // handle придёт из PlayersStore
                            roundScoreboard = event.roundScoreboard,
                        )
                        dispatch(Msg.RoundResultReceived(result))
                        // Авто-dismiss через 3 сек
                        scope.launch {
                            delay(3000)
                            dispatch(Msg.RoundResultDismissed)
                            publish(GameplayGameStore.Effect.RoundResultDismissed)
                        }
                    }
                    is GameEvent.GameFinished -> {
                        dispatch(Msg.GameFinishedReceived(event.winnerUserId, event.finalScoreboard))
                    }
                    else -> Unit
                }
            }.launchIn(scope)

            personalEvents.onEach { event ->
                when (event) {
                    is PersonalEvent.HandUpdated -> {
                        val cards = event.cards.map { it.toGameCard() }
                        dispatch(Msg.HandUpdated(cards))
                    }
                }
            }.launchIn(scope)
        }
    }

    // ── Reducer ────────────────────────────────────────────────────────────

    private object ReducerImpl : Reducer<GameplayGameStore.State, Msg> {
        override fun GameplayGameStore.State.reduce(msg: Msg): GameplayGameStore.State = when (msg) {
            // Initialization
            is Msg.Initialized -> {
                if (msg.isPlayerInGame) {
                    // Пользователь уже в игре - показываем Lobby или другую фазу
                    when (msg.gameStatus) {
                        GameStatus.LOBBY -> copy(
                            isLoading = false,
                            uiPhase = GameplayGameStore.UiPhase.Lobby,
                            handleInput = msg.handle,
                            isJoining = false,
                        )
                        GameStatus.PLAYING -> copy(
                            isLoading = false,
                            uiPhase = GameplayGameStore.UiPhase.Lobby, // WebSocket переключит
                            handleInput = msg.handle,
                            handCards = msg.hand,
                            isJoining = false,
                        )
                        GameStatus.FINISHED -> copy(
                            isLoading = false,
                            uiPhase = GameplayGameStore.UiPhase.GameFinished,
                            handleInput = msg.handle,
                            isJoining = false,
                        )
                        null -> copy(isLoading = false, uiPhase = GameplayGameStore.UiPhase.HandleInput)
                    }
                } else {
                    // Пользователь не в игре - показываем экран ввода handle
                    copy(
                        isLoading = false,
                        uiPhase = GameplayGameStore.UiPhase.HandleInput,
                        handleInput = "",
                        isJoining = false,
                    )
                }
            }

            // HandleInput
            is Msg.HandleInputChanged -> copy(handleInput = msg.text)
            is Msg.JoiningStarted -> copy(isJoining = true)
            is Msg.JoiningFailed -> copy(isJoining = false)
            is Msg.EnteredLobby -> copy(isJoining = false, uiPhase = GameplayGameStore.UiPhase.Lobby)

            // Round
            is Msg.RoundStarted -> copy(
                uiPhase = GameplayGameStore.UiPhase.Submitting,
                roundId = msg.roundId,
                promptCard = msg.promptCard,
                handCards = msg.hand,
                selectedCardIndex = 0,
                mySubmissionCard = null,
                hasVoted = false,
                submissionCards = emptyList(),
                submissionIds = emptyList(),
            )
            is Msg.PhaseChangedToVoting -> copy(
                uiPhase = GameplayGameStore.UiPhase.Voting,
                selectedSubmissionIndex = 0,
            )
            is Msg.SubmissionsLoaded -> copy(
                submissionCards = msg.cards,
                submissionIds = msg.ids,
                selectedSubmissionIndex = 0,
            )

            // Submitting
            is Msg.HandUpdated -> copy(handCards = msg.cards, selectedCardIndex = 0)
            is Msg.CardIndexChanged -> copy(selectedCardIndex = msg.index)
            is Msg.SubmittingStarted -> copy(isSubmitting = true)
            is Msg.SubmittingFinished -> copy(isSubmitting = false)
            is Msg.MySubmissionConfirmed -> copy(mySubmissionCard = msg.card)

            // Voting
            is Msg.SubmissionIndexChanged -> copy(selectedSubmissionIndex = msg.index)
            is Msg.VotingStarted -> copy(isVoting = true)
            is Msg.VotingFinished -> copy(isVoting = false)
            is Msg.VotedConfirmed -> copy(hasVoted = true)

            // Toggle
            is Msg.TogglePrompt -> copy(showPrompt = !showPrompt)

            // RoundResult
            is Msg.RoundResultReceived -> copy(
                uiPhase = GameplayGameStore.UiPhase.RoundResult,
                roundResult = msg.data,
            )
            is Msg.RoundResultDismissed -> copy(
                uiPhase = GameplayGameStore.UiPhase.Submitting, // следующий раунд придёт через RoundStarted
                roundResult = null,
            )

            // GameFinished
            is Msg.GameFinishedReceived -> copy(
                uiPhase = GameplayGameStore.UiPhase.GameFinished,
                gameWinnerUserId = msg.winnerUserId,
                finalScoreboard = msg.finalScoreboard,
            )
        }
    }
}

// ── Extensions ─────────────────────────────────────────────────────────────

private fun HandCard.toGameCard(): GameCard {
    val imgUrl = imageUrl
    return if (kind == "meme" && imgUrl != null) {
        MemeGameCard(MemeCardData(id, imgUrl))
    } else {
        SituationGameCard(SituationCardData(id, text ?: ""))
    }
}

private fun com.dev.memebattle.core.network.error.NetworkError.userMessage(): String = when (this) {
    is com.dev.memebattle.core.network.error.NetworkError.ApiException -> message ?: "Ошибка сервера ($code)"
    is com.dev.memebattle.core.network.error.NetworkError.ServerError -> "Ошибка сервера ($code)"
    is com.dev.memebattle.core.network.error.NetworkError.Exception -> cause.message ?: "Неизвестная ошибка"
    else -> "Произошла ошибка"
}
