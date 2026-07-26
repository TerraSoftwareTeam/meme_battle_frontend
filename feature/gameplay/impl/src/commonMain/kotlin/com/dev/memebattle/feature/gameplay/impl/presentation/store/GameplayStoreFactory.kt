package com.dev.memebattle.feature.gameplay.impl.presentation.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.network.game.current.api.ws.GameSocketService

internal class GameplayStoreFactory(
    private val storeFactory: StoreFactory,
    private val gameSocketService: GameSocketService,
    private val gameId: String,
) {
    fun create(): GameplayStore = object : GameplayStore, Store<GameplayStore.Intent, GameplayStore.State, GameplayStore.Effect> by storeFactory.create(
        name = "GameplayStore",
        initialState = GameplayStore.State(gameId = gameId),
        executorFactory = ::GameplayExecutor,
        reducer = GameplayReducer,
    ) {}

    private inner class GameplayExecutor : CoroutineExecutor<GameplayStore.Intent, Nothing, GameplayStore.State, Message, GameplayStore.Effect>() {
        override fun executeIntent(intent: GameplayStore.Intent) {
            when (intent) {
                is GameplayStore.Intent.Init -> {
                    // TODO: подключить WS и загрузить снимок состояния игры
                    // 1. gameSocketService.connect()
                    // 2. GET /games/{gameId}/state  → заполнить State
                    // 3. gameSocketService.subscribeToGame(gameId, token)
                    // 4. gameSocketService.subscribeToPersonal(userId, token)
                    // 5. Слушать gameSocketService.gameEvents / personalEvents
                }
            }
        }
    }

    private sealed interface Message {
        data class Loading(val isLoading: Boolean) : Message
    }

    private object GameplayReducer : Reducer<GameplayStore.State, Message> {
        override fun GameplayStore.State.reduce(msg: Message): GameplayStore.State = when (msg) {
            is Message.Loading -> copy(isLoading = msg.isLoading)
        }
    }
}
