package com.dev.memebattle.feature.home.impl.presentation.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor


internal class HomeStoreFactory(
    private val storeFactory: StoreFactory
) {
    fun create(): HomeStore = object : HomeStore, Store<HomeStore.Intent, HomeStore.State, HomeStore.Effect> by storeFactory.create(
        name = "HomeStore", initialState = HomeStore.State(), executorFactory = ::HomeExecutor, reducer = HomeReducer
    ) {}

    private inner class HomeExecutor : CoroutineExecutor<HomeStore.Intent, Nothing, HomeStore.State, Message, HomeStore.Effect>() {
        override fun executeIntent(intent: HomeStore.Intent) {
            when (intent) { is HomeStore.Intent.Init -> {} }
        }
    }
    private sealed interface Message { data class Loading(val isLoading: Boolean) : Message }
    private object HomeReducer : Reducer<HomeStore.State, Message> {
        override fun HomeStore.State.reduce(msg: Message): HomeStore.State = when (msg) { is Message.Loading -> copy(isLoading = msg.isLoading) }
    }
}
