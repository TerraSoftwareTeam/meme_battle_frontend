package com.dev.memebattle.feature.gameplay.impl.presentation.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor


internal class GameplayStoreFactory(
    private val storeFactory: StoreFactory
) {
    fun create(): GameplayStore = object : GameplayStore, Store<GameplayStore.Intent, GameplayStore.State, GameplayStore.Effect> by storeFactory.create(
        name = "GameplayStore", initialState = GameplayStore.State(), executorFactory = ::GameplayExecutor, reducer = GameplayReducer
    ) {}

    private inner class GameplayExecutor : CoroutineExecutor<GameplayStore.Intent, Nothing, GameplayStore.State, Message, GameplayStore.Effect>() {
        override fun executeIntent(intent: GameplayStore.Intent) {
            when (intent) { is GameplayStore.Intent.Init -> {} }
        }
    }
    private sealed interface Message { data class Loading(val isLoading: Boolean) : Message }
    private object GameplayReducer : Reducer<GameplayStore.State, Message> {
        override fun GameplayStore.State.reduce(msg: Message): GameplayStore.State = when (msg) { is Message.Loading -> copy(isLoading = msg.isLoading) }
    }
}
