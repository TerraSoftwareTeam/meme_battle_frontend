package com.dev.network.game.current.api.ws

import com.dev.network.game.current.dto.ws.GameEvent
import com.dev.network.game.current.dto.ws.PersonalEvent
import com.dev.network.game.current.dto.ws.LobbyEvent
import kotlinx.coroutines.flow.Flow

interface GameSocketService {
    val gameEvents: Flow<GameEvent>
    val personalEvents: Flow<PersonalEvent>
    val lobbyEvents: Flow<LobbyEvent>
    
    suspend fun connect()
    suspend fun disconnect()

    suspend fun subscribeToGame(gameId: String, token: String)
    suspend fun subscribeToPersonal(userId: String, token: String)
    suspend fun subscribeToLobbies(token: String)
}
