package com.dev.memebattle.feature.gameplay.impl.presentation.component.game

import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStore
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface GameplayGameComponent {
    val gameId: String
    val state: StateFlow<GameplayGameStore.State>
    val effects: SharedFlow<GameplayGameStore.Effect>
    fun onIntent(intent: GameplayGameStore.Intent)
}
