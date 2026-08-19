package com.dev.memebattle.feature.gameplay.impl.presentation.component.game

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStore
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStoreFactory
import com.dev.network.game.current.api.GameApiService
import com.dev.network.game.current.dto.GameStateDto
import com.dev.network.game.current.dto.ws.GameEvent
import com.dev.network.game.current.dto.ws.PersonalEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.shareIn

class GameplayGameComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    gameApiService: GameApiService,
    override val gameId: String,
    myUserId: String,
    gameEvents: Flow<GameEvent>,
    personalEvents: Flow<PersonalEvent>,
    initialSnapshot: GameStateDto?,
    /** Резолвит handle игрока по userId — приходит из PlayersStore через лямбду */
    getPlayerHandle: (userId: String) -> String? = { null },
) : GameplayGameComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()

    private val store = GameplayGameStoreFactory(
        storeFactory = storeFactory,
        gameApiService = gameApiService,
        gameId = gameId,
        myUserId = myUserId,
        gameEvents = gameEvents,
        personalEvents = personalEvents,
        initialSnapshot = initialSnapshot,
        getPlayerHandle = getPlayerHandle,
    ).create()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<GameplayGameStore.State> = store.stateFlow(scope)
    override val effects: SharedFlow<GameplayGameStore.Effect> =
        store.labels.shareIn(scope, SharingStarted.Eagerly, replay = 0)

    override fun onIntent(intent: GameplayGameStore.Intent) = store.accept(intent)
}
