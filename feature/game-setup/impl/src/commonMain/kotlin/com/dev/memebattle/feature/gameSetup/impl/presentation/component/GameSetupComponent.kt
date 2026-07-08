package com.dev.memebattle.feature.gameSetup.impl.presentation.component

import com.dev.memebattle.core.navigation.entry.FeatureComponent
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import com.dev.memebattle.feature.gameSetup.impl.presentation.store.GameSetupStore

interface GameSetupComponent : FeatureComponent {
    val state: StateFlow<GameSetupStore.State>
    val effects: SharedFlow<GameSetupStore.Effect>
    fun onIntent(intent: GameSetupStore.Intent)
}
