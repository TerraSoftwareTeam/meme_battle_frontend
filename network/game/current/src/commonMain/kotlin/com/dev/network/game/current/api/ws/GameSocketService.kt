package com.dev.network.game.current.api.ws

import com.dev.network.game.current.dto.ws.GameEvent
import com.dev.network.game.current.dto.ws.PersonalEvent
import com.dev.network.game.current.dto.ws.LobbyEvent
import kotlinx.coroutines.flow.Flow

interface GameSocketService {
    val gameEvents: Flow<GameEvent>
    val personalEvents: Flow<PersonalEvent>
    val lobbyEvents: Flow<LobbyEvent>
    val reconnectedEvents: Flow<Unit>
    val isConnected: kotlinx.coroutines.flow.StateFlow<Boolean>
    
    suspend fun connect()
    suspend fun disconnect()
    suspend fun reconnect()

    suspend fun subscribeToGame(gameId: String, token: String)
    suspend fun unsubscribeFromGame(gameId: String)
    suspend fun subscribeToPersonal(userId: String, token: String)
    suspend fun unsubscribeFromPersonal(userId: String)
    suspend fun subscribeToLobbies()
}
