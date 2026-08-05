package com.dev.memebattle.core.data.game

import com.dev.network.game.current.dto.SituationGameCard
import com.dev.network.game.current.dto.ws.PersonalEvent
import com.dev.network.game.current.dto.RoundDto
import com.dev.network.game.current.dto.RoundPhase
import com.dev.network.game.current.dto.GameStatus
import com.dev.network.game.current.dto.SituationCardData
import com.dev.network.game.current.dto.MemeGameCard
import com.dev.network.game.current.dto.MemeCardData
import com.dev.memebattle.core.domain.game.GameRepository
import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.network.game.current.api.GameApiService
import com.dev.network.game.current.api.ws.GameSocketService
import com.dev.network.game.current.dto.GameStateDto
import com.dev.network.game.current.dto.ws.GameEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class GameRepositoryImpl(
    private val socketService: GameSocketService,
    private val gameApiService: GameApiService
) : GameRepository {

    private val _gameState = MutableStateFlow<GameStateDto?>(null)
    override val gameState: StateFlow<GameStateDto?> = _gameState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var observeJob: Job? = null

    override suspend fun observeGame(gameId: String, userId: String?) = coroutineScope {
        observeJob?.cancel()
        
        // 1. Fetch initial state
        val stateResult = gameApiService.getGameState(gameId)
        if (stateResult is NetworkResult.Success) {
            _gameState.value = stateResult.data
        }

        // 2. Fetch connection/subscription tokens
        val tokensResult = gameApiService.getWsToken(gameId)
        if (tokensResult !is NetworkResult.Success) {
            return@coroutineScope
        }
        val tokens = tokensResult.data

        // 3. Connect and subscribe
        scope.launch {
            socketService.connect()
            socketService.subscribeToGame(gameId, tokens.game_subscription_token)
            if (userId != null) {
                socketService.subscribeToPersonal(userId, tokens.personal_subscription_token)
            }
        }

        // 4. Listen to incoming game events
        observeJob = scope.launch {
            launch {
                socketService.gameEvents.collect { event ->
                    _gameState.update { currentState ->
                        if (currentState != null) reduceEvent(currentState, event) else null
                    }
                }
            }
            launch {
                socketService.personalEvents.collect { event ->
                    _gameState.update { currentState ->
                        if (currentState != null) reducePersonalEvent(currentState, event) else null
                    }
                }
            }
        }
    }

    override suspend fun disconnect() {
        observeJob?.cancel()
        observeJob = null
        socketService.disconnect()
        _gameState.value = null
    }

    private fun reducePersonalEvent(state: GameStateDto, event: PersonalEvent): GameStateDto {
        return when (event) {
            is PersonalEvent.HandUpdated -> {
                val newHand = event.cards.map { card ->
                    if (card.kind.equals("meme", ignoreCase = true)) {
                        MemeGameCard(
                            MemeCardData(id = card.id, mediaUrl = card.imageUrl ?: "")
                        )
                    } else {
                        SituationGameCard(
                            SituationCardData(id = card.id, promptText = card.text ?: "")
                        )
                    }
                }
                state.copy(my_hand = newHand)
            }
        }
    }

    private fun reduceEvent(state: GameStateDto, event: GameEvent): GameStateDto {
        return when (event) {
            is GameEvent.PlayerJoined -> state // We don't have players_count in GameDto anymore
            is GameEvent.GameStarted -> state // We don't have max_rounds in GameDto anymore
            is GameEvent.RoundStarted -> {
                val promptCard = if (event.promptKind.equals("meme", ignoreCase = true)) {
                    MemeGameCard(
                        MemeCardData(id = "", mediaUrl = event.promptContent)
                    )
                } else {
                    SituationGameCard(
                        SituationCardData(id = "", promptText = event.promptContent)
                    )
                }
                
                val updatedRound = state.round?.copy(
                    id = event.roundId,
                    round_number = event.roundNumber,
                    phase = RoundPhase.valueOf(event.phase.uppercase()),
                    prompt = promptCard,
                    phase_expires_at = event.phaseExpiresAt
                ) ?: RoundDto(
                    id = event.roundId,
                    round_number = event.roundNumber,
                    phase = RoundPhase.valueOf(event.phase.uppercase()),
                    prompt = promptCard,
                    phase_expires_at = event.phaseExpiresAt,
                    has_voted = false
                )
                state.copy(round = updatedRound)
            }
            is GameEvent.RoundPhaseChanged -> {
                val updatedRound = state.round?.copy(
                    phase = RoundPhase.valueOf(event.phase.uppercase()),
                    phase_expires_at = event.phaseExpiresAt
                )
                state.copy(round = updatedRound)
            }
            is GameEvent.SubmissionReceived -> state
            is GameEvent.PlayerReadyChanged -> state
            is GameEvent.VoteReceived -> state
            is GameEvent.RoundFinished -> state
            is GameEvent.GameFinished -> {
                state.copy(
                    game = state.game.copy(
                        status = GameStatus.FINISHED
                    )
                )
            }
        }
    }
}
