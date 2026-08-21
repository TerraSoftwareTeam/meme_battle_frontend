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
import com.dev.memebattle.core.network.utils.normalizeMediaUrl

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
                            MemeCardData(id = card.id, mediaUrl = normalizeMediaUrl(card.imageUrl))
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
            is GameEvent.PlayerJoined -> {
                val existing = state.players.any { it.user_id == event.userId }
                val updatedPlayers = if (!existing) {
                    state.players + com.dev.network.game.current.dto.PlayerDto(
                        user_id = event.userId,
                        handle = event.handle.ifEmpty { event.userId.take(8) },
                        score = 0,
                        is_ready = false,
                        has_submitted = false,
                    )
                } else {
                    state.players
                }
                state.copy(players = updatedPlayers)
            }
            is GameEvent.PlayerLeft -> {
                val updatedPlayers = state.players.filterNot { it.user_id == event.userId }
                state.copy(players = updatedPlayers)
            }
            is GameEvent.PlayerReadyChanged -> {
                val updatedPlayers = state.players.map { player ->
                    if (player.user_id == event.userId) {
                        player.copy(is_ready = event.isReady)
                    } else {
                        player
                    }
                }
                state.copy(players = updatedPlayers)
            }
            is GameEvent.GameStarted -> {
                state.copy(
                    game = state.game.copy(status = GameStatus.PLAYING)
                )
            }
            is GameEvent.RoundStarted -> {
                val promptCard = if (event.promptKind.equals("meme", ignoreCase = true)) {
                    MemeGameCard(
                        MemeCardData(id = "", mediaUrl = normalizeMediaUrl(event.promptContent))
                    )
                } else {
                    SituationGameCard(
                        SituationCardData(id = "", promptText = event.promptContent)
                    )
                }
                val resetPlayers = state.players.map { it.copy(has_submitted = false) }
                
                val updatedRound = RoundDto(
                    id = event.roundId,
                    round_number = event.roundNumber,
                    phase = RoundPhase.valueOf(event.phase.uppercase()),
                    prompt = promptCard,
                    phase_expires_at = event.phaseExpiresAt,
                    has_voted = false,
                    my_submission = null,
                    submissions = null,
                )
                state.copy(round = updatedRound, players = resetPlayers)
            }
            is GameEvent.RoundPhaseChanged -> {
                val newPhase = try {
                    RoundPhase.valueOf(event.phase.uppercase())
                } catch (_: Exception) {
                    RoundPhase.WAITING
                }
                val updatedRound = state.round?.copy(
                    phase = newPhase,
                    phase_expires_at = event.phaseExpiresAt
                )
                state.copy(round = updatedRound)
            }
            is GameEvent.SubmissionReceived -> {
                val updatedPlayers = state.players.map { player ->
                    if (player.user_id == event.userId) {
                        player.copy(has_submitted = true)
                    } else {
                        player
                    }
                }
                state.copy(players = updatedPlayers)
            }
            is GameEvent.VoteReceived -> state
            is GameEvent.RoundFinished -> {
                val scoreMap = event.scoreboard.associate { it.userId to it.score }
                val updatedPlayers = state.players.map { player ->
                    val newScore = scoreMap[player.user_id]
                    if (newScore != null) player.copy(score = newScore) else player
                }
                val updatedRound = state.round?.copy(phase = RoundPhase.FINISHED)
                state.copy(round = updatedRound, players = updatedPlayers)
            }
            is GameEvent.GameFinished -> {
                val scoreMap = event.finalScoreboard.associate { it.userId to it.score }
                val updatedPlayers = state.players.map { player ->
                    val newScore = scoreMap[player.user_id]
                    if (newScore != null) player.copy(score = newScore) else player
                }
                state.copy(
                    game = state.game.copy(status = GameStatus.FINISHED),
                    players = updatedPlayers
                )
            }
        }
    }
}
