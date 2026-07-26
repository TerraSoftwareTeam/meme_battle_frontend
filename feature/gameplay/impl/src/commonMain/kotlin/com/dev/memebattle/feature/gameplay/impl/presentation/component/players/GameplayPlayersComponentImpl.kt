package com.dev.memebattle.feature.gameplay.impl.presentation.component.players

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.memebattle.feature.gameplay.impl.presentation.store.players.GameplayPlayersStore
import com.dev.memebattle.feature.gameplay.impl.presentation.store.players.GameplayPlayersStoreFactory
import com.dev.network.game.current.dto.GameStateDto
import com.dev.network.game.current.dto.ws.GameEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.shareIn

class GameplayPlayersComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    myUserId: String,
    gameEvents: Flow<GameEvent>,
    initialSnapshot: GameStateDto?,
) : GameplayPlayersComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()

    private val store = GameplayPlayersStoreFactory(
        storeFactory = storeFactory,
        myUserId = myUserId,
        gameEvents = gameEvents,
        initialSnapshot = initialSnapshot,
    ).create()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<GameplayPlayersStore.State> = store.stateFlow(scope)
    override val effects: SharedFlow<GameplayPlayersStore.Effect> =
        store.labels.shareIn(scope, SharingStarted.Eagerly, replay = 0)

    override fun onIntent(intent: GameplayPlayersStore.Intent) = store.accept(intent)
}
