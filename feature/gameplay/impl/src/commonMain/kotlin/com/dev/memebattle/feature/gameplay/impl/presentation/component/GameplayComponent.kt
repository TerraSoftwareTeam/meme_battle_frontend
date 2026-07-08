package com.dev.memebattle.feature.gameplay.impl.presentation.component

import com.dev.memebattle.core.navigation.entry.FeatureComponent
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import com.dev.memebattle.feature.gameplay.impl.presentation.store.GameplayStore

interface GameplayComponent : FeatureComponent {
    val state: StateFlow<GameplayStore.State>
    val effects: SharedFlow<GameplayStore.Effect>
    fun onIntent(intent: GameplayStore.Intent)
}
