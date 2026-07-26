package com.dev.memebattle.feature.home.impl.presentation.component.menu

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
import com.dev.memebattle.feature.home.impl.presentation.store.menu.HomeMenuStore
import com.dev.memebattle.feature.home.impl.presentation.store.menu.HomeMenuStoreFactory

import com.dev.network.game.current.api.ws.GameSocketService

class HomeMenuComponentImpl(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val gameSocketService: GameSocketService,
    private val onNavigateToCreateLobby: () -> Unit,
    private val onNavigateToStore: () -> Unit
) : HomeMenuComponent, ComponentContext by componentContext {
    
    private val scope = coroutineScope()
    private val store = HomeMenuStoreFactory(
        storeFactory = storeFactory,
        gameSocketService = gameSocketService,
        onNavigateToCreateLobby = onNavigateToCreateLobby,
        onNavigateToStore = onNavigateToStore
    ).create()

    private val labelsFlow = store.labels.shareIn(scope, SharingStarted.Eagerly, replay = 0)
    override val effects: SharedFlow<HomeMenuStore.Effect> = labelsFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<HomeMenuStore.State> = store.stateFlow(scope)

    override fun onIntent(intent: HomeMenuStore.Intent) = store.accept(intent)
}
