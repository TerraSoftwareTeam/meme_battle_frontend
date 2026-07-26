package com.dev.memebattle.feature.gameplay.impl.presentation.component.info

import com.dev.memebattle.feature.gameplay.impl.presentation.store.info.GameplayInfoStore
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface GameplayInfoComponent {
    val state: StateFlow<GameplayInfoStore.State>
    val effects: SharedFlow<GameplayInfoStore.Effect>
    fun onIntent(intent: GameplayInfoStore.Intent)
}
