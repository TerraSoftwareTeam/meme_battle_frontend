package com.dev.memebattle.feature.gameplay.impl.presentation.component.info

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.memebattle.feature.gameplay.impl.presentation.store.info.GameplayInfoStore
import com.dev.memebattle.feature.gameplay.impl.presentation.store.info.GameplayInfoStoreFactory
import com.dev.network.game.current.api.GameApiService
import com.dev.network.game.current.dto.GameStateDto
import com.dev.network.game.current.dto.ws.GameEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.shareIn

class GameplayInfoComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    gameApiService: GameApiService,
    gameId: String,
    myUserId: String,
    gameEvents: Flow<GameEvent>,
    initialSnapshot: GameStateDto?,
) : GameplayInfoComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()

    private val store = GameplayInfoStoreFactory(
        storeFactory = storeFactory,
        gameApiService = gameApiService,
        gameId = gameId,
        myUserId = myUserId,
        gameEvents = gameEvents,
        initialState = initialSnapshot,
    ).create()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<GameplayInfoStore.State> = store.stateFlow(scope)
    override val effects: SharedFlow<GameplayInfoStore.Effect> =
        store.labels.shareIn(scope, SharingStarted.Eagerly, replay = 0)

    override fun onIntent(intent: GameplayInfoStore.Intent) = store.accept(intent)
}
