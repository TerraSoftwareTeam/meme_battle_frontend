package com.dev.memebattle.feature.gameplay.impl.presentation.component.players

import com.dev.memebattle.feature.gameplay.impl.presentation.store.players.GameplayPlayersStore
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface GameplayPlayersComponent {
    val state: StateFlow<GameplayPlayersStore.State>
    val effects: SharedFlow<GameplayPlayersStore.Effect>
    fun onIntent(intent: GameplayPlayersStore.Intent)
}
