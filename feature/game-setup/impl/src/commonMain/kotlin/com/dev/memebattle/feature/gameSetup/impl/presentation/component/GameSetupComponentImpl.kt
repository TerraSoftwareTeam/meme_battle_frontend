package com.dev.memebattle.feature.gameSetup.impl.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.dev.memebattle.feature.gameSetup.impl.presentation.store.GameSetupStore
import com.dev.memebattle.feature.gameSetup.impl.presentation.store.GameSetupStoreFactory


class GameSetupComponentImpl(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory
) : GameSetupComponent, ComponentContext by componentContext {
    
    private val scope = coroutineScope()
    private val store = GameSetupStoreFactory(storeFactory).create()

    private val labelsFlow = store.labels.shareIn(scope, SharingStarted.Eagerly, replay = 0)
    override val effects: SharedFlow<GameSetupStore.Effect> = labelsFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<GameSetupStore.State> = store.stateFlow(scope)

    override val output: kotlinx.coroutines.flow.Flow<com.dev.memebattle.core.navigation.output.NavigationOutput> = kotlinx.coroutines.flow.emptyFlow()

    override fun onIntent(intent: GameSetupStore.Intent) = store.accept(intent)
}
