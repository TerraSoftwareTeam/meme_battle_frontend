package com.dev.memebattle.feature.home.impl.presentation.component.create

import com.dev.memebattle.feature.home.impl.presentation.store.create.CreateLobbyStore
import com.dev.network.game.current.dto.GameMode
import kotlinx.coroutines.flow.StateFlow

interface CreateLobbyComponent {
    val state: StateFlow<CreateLobbyStore.State>
    
    fun toggleMemePack(id: String)
    fun toggleSituationPack(id: String)
    fun setMode(mode: GameMode)
    fun setMaxRounds(rounds: Int)
    fun setHandSize(size: Int)
    fun createLobby()
    
    fun onClose()
    fun onGameCreated(gameId: String)
}
