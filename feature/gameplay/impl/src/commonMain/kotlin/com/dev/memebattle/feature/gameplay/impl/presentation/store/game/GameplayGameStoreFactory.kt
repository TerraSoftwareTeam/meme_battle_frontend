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
import kotlinx.coroutines.Job
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
    /** Резолвит handle игрока по userId из PlayersStore (живые данные) */
    private val getPlayerHandle: (userId: String) -> String? = { null },
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
            return GameplayGameStore.State(isLoading = true, uiPhase = GameplayGameStore.UiPhase.Lobby)
        }
        
        return when (snapshot.game.status) {
            GameStatus.LOBBY -> GameplayGameStore.State(
                isLoading = false, 
                uiPhase = GameplayGameStore.UiPhase.Lobby,
            )
            GameStatus.PLAYING -> GameplayGameStore.State(
                isLoading = false,
                uiPhase = GameplayGameStore.UiPhase.Lobby, // WebSocket события переключат фазу
                handCards = snapshot.my_hand,
            )
            GameStatus.FINISHED -> GameplayGameStore.State(
                isLoading = false,
                uiPhase = GameplayGameStore.UiPhase.GameFinished,
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
            val gameStatus: GameStatus?,
            val hand: List<GameCard>,
        ) : Msg

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

        /** Job авто-dismiss результата раунда. Отменяется при получении RoundStarted, чтобы избежать гонки. */
        private var dismissJob: Job? = null

        override fun executeAction(action: Action) {
            when (action) {
                Action.ObserveEvents -> observeEvents()
            }
        }

        override fun executeIntent(intent: GameplayGameStore.Intent) {
            when (intent) {
                is GameplayGameStore.Intent.Initialize      -> initialize(intent.snapshot)
                is GameplayGameStore.Intent.SelectCard      -> selectCard(intent.index)
                is GameplayGameStore.Intent.Submit          -> submitCard()
                is GameplayGameStore.Intent.Vote            -> vote(intent.submissionId)
                is GameplayGameStore.Intent.TogglePromptVisible -> dispatch(Msg.TogglePrompt)
                is GameplayGameStore.Intent.LoadSubmissions -> dispatch(
                    Msg.SubmissionsLoaded(intent.cards, intent.ids)
                )
                is GameplayGameStore.Intent.ExitGame        -> publish(GameplayGameStore.Effect.ExitGame)
            }
        }

        // ── Initialization ─────────────────────────────────────────────────

        private fun initialize(snapshot: GameStateDto?) {
            if (snapshot == null) {
                dispatch(Msg.Initialized(
                    gameStatus = null,
                    hand = emptyList(),
                ))
                return
            }

            dispatch(Msg.Initialized(
                gameStatus = snapshot.game.status,
                hand = snapshot.my_hand,
            ))
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
                        if (state().uiPhase == GameplayGameStore.UiPhase.RoundResult) {
                            // Бэк прислал round_started почти сразу после round_finished.
                            // Отменяем предыдущий dismissJob и запускаем новый,
                            // чтобы показать результаты минимум 5 секунд
                            dismissJob?.cancel()
                            dismissJob = scope.launch {
                                delay(5000)
                                if (state().uiPhase == GameplayGameStore.UiPhase.RoundResult) {
                                    dispatch(Msg.RoundResultDismissed)
                                    publish(GameplayGameStore.Effect.RoundResultDismissed)
                                }
                                dispatch(Msg.RoundStarted(event.roundId, promptCard, emptyList()))
                                dismissJob = null
                            }
                        } else {
                            // Нормальный случай: сразу переходим
                            dismissJob?.cancel()
                            dismissJob = null
                            dispatch(Msg.RoundStarted(event.roundId, promptCard, emptyList()))
                        }
                    }
                    is GameEvent.RoundPhaseChanged -> {
                        if (event.phase == "voting") {
                            dispatch(Msg.PhaseChangedToVoting)
                            // submission-карты придут через снимок getGameState — инициирует ComponentImpl
                        }
                    }
                    is GameEvent.RoundFinished -> {
                        // Резолвим handles из PlayersStore (живые данные)
                        val enrichedScoreboard = event.roundScoreboard.map { entry ->
                            if (entry.handle != null) entry
                            else entry.copy(handle = getPlayerHandle(entry.userId))
                        }
                        val winnerHandle = event.winnerUserId?.let { uid ->
                            getPlayerHandle(uid) ?: enrichedScoreboard
                                .firstOrNull { it.userId == uid }?.handle
                        }
                        val result = GameplayGameStore.RoundResultData(
                            roundNumber = event.roundNumber,
                            winnerUserId = event.winnerUserId,
                            winnerHandle = winnerHandle,
                            roundScoreboard = enrichedScoreboard,
                        )
                        dispatch(Msg.RoundResultReceived(result))
                        // Авто-dismiss через 5 сек (достаточно чтобы успеть прочитать)
                        dismissJob?.cancel()
                        dismissJob = scope.launch {
                            delay(5000)
                            if (state().uiPhase == GameplayGameStore.UiPhase.RoundResult) {
                                dispatch(Msg.RoundResultDismissed)
                                publish(GameplayGameStore.Effect.RoundResultDismissed)
                            }
                            dismissJob = null
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
            is Msg.Initialized -> copy(
                isLoading = false,
                uiPhase = when (msg.gameStatus) {
                    null, GameStatus.LOBBY -> GameplayGameStore.UiPhase.Lobby
                    GameStatus.PLAYING -> GameplayGameStore.UiPhase.Lobby
                    GameStatus.FINISHED -> GameplayGameStore.UiPhase.GameFinished
                },
                handCards = msg.hand,
            )
            is Msg.EnteredLobby -> copy(uiPhase = GameplayGameStore.UiPhase.Lobby)

            is Msg.RoundStarted -> copy(
                uiPhase = GameplayGameStore.UiPhase.Submitting,
                roundId = msg.roundId,
                promptCard = msg.promptCard,
                // Бэк присылает hand_updated одновременно с round_finished до round_started.
                // Если карты уже пришли — сохраняем их, не сбрасываем.
                handCards = if (msg.hand.isNotEmpty()) msg.hand else handCards,
                selectedCardIndex = 0,
                selectedSubmissionIndex = 0,
                submissionCards = emptyList(),
                submissionIds = emptyList(),
                hasVoted = false,
                mySubmissionCard = null,
                isSubmitting = false,
                isVoting = false,
            )
            is Msg.PhaseChangedToVoting -> copy(uiPhase = GameplayGameStore.UiPhase.Voting)
            is Msg.SubmissionsLoaded -> copy(
                submissionCards = msg.cards,
                submissionIds = msg.ids,
            )

            is Msg.HandUpdated -> copy(handCards = msg.cards)
            is Msg.CardIndexChanged -> copy(selectedCardIndex = msg.index)
            is Msg.SubmittingStarted -> copy(isSubmitting = true)
            is Msg.SubmittingFinished -> copy(isSubmitting = false)
            is Msg.MySubmissionConfirmed -> copy(mySubmissionCard = msg.card)

            is Msg.SubmissionIndexChanged -> copy(selectedSubmissionIndex = msg.index)
            is Msg.VotingStarted -> copy(isVoting = true)
            is Msg.VotingFinished -> copy(isVoting = false)
            is Msg.VotedConfirmed -> copy(hasVoted = true)

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
