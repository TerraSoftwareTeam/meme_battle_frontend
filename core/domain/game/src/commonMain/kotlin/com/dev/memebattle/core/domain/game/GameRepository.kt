package com.dev.memebattle.core.domain.game

import com.dev.network.game.current.dto.GameStateDto
import kotlinx.coroutines.flow.StateFlow

interface GameRepository {
    val gameState: StateFlow<GameStateDto?>

    suspend fun observeGame(gameId: String, userId: String? = null)
    suspend fun disconnect()
}
