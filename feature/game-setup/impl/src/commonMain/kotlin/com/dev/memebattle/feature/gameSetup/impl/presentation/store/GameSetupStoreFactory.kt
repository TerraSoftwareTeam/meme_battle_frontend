package com.dev.memebattle.feature.gameSetup.impl.presentation.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor


internal class GameSetupStoreFactory(
    private val storeFactory: StoreFactory
) {
    fun create(): GameSetupStore = object : GameSetupStore, Store<GameSetupStore.Intent, GameSetupStore.State, GameSetupStore.Effect> by storeFactory.create(
        name = "GameSetupStore", initialState = GameSetupStore.State(), executorFactory = ::GameSetupExecutor, reducer = GameSetupReducer
    ) {}

    private inner class GameSetupExecutor : CoroutineExecutor<GameSetupStore.Intent, Nothing, GameSetupStore.State, Message, GameSetupStore.Effect>() {
        override fun executeIntent(intent: GameSetupStore.Intent) {
            when (intent) { is GameSetupStore.Intent.Init -> {} }
        }
    }
    private sealed interface Message { data class Loading(val isLoading: Boolean) : Message }
    private object GameSetupReducer : Reducer<GameSetupStore.State, Message> {
        override fun GameSetupStore.State.reduce(msg: Message): GameSetupStore.State = when (msg) { is Message.Loading -> copy(isLoading = msg.isLoading) }
    }
}
