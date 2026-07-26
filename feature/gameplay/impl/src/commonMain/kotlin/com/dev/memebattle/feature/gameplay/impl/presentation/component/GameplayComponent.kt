package com.dev.memebattle.feature.gameplay.impl.presentation.component

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.value.Value
import com.dev.memebattle.core.navigation.entry.FeatureComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.component.game.GameplayGameComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.component.info.GameplayInfoComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.component.players.GameplayPlayersComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.component.GameplayComponentImpl.MainConfig
import com.dev.memebattle.feature.gameplay.impl.presentation.component.GameplayComponentImpl.DetailsConfig
import com.dev.memebattle.feature.gameplay.impl.presentation.component.GameplayComponentImpl.ExtraConfig
import com.dev.memebattle.feature.gameplay.impl.presentation.store.GameplayStore
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalDecomposeApi::class)
interface GameplayComponent : FeatureComponent {
    val panels: Value<ChildPanels<MainConfig, GameplayGameComponent, DetailsConfig, GameplayInfoComponent, ExtraConfig, GameplayPlayersComponent>>
    fun setAdaptiveMode(mode: ChildPanelsMode)

    // Transitional — kept until GameplayStore is fully migrated to sub-stores
    val state: StateFlow<GameplayStore.State>
    val effects: SharedFlow<GameplayStore.Effect>
    fun onIntent(intent: GameplayStore.Intent)
}
