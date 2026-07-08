package com.dev.memebattle.feature.packs.impl.presentation.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor


internal class PacksStoreFactory(
    private val storeFactory: StoreFactory
) {
    fun create(): PacksStore = object : PacksStore, Store<PacksStore.Intent, PacksStore.State, PacksStore.Effect> by storeFactory.create(
        name = "PacksStore", initialState = PacksStore.State(), executorFactory = ::PacksExecutor, reducer = PacksReducer
    ) {}

    private inner class PacksExecutor : CoroutineExecutor<PacksStore.Intent, Nothing, PacksStore.State, Message, PacksStore.Effect>() {
        override fun executeIntent(intent: PacksStore.Intent) {
            when (intent) { is PacksStore.Intent.Init -> {} }
        }
    }
    private sealed interface Message { data class Loading(val isLoading: Boolean) : Message }
    private object PacksReducer : Reducer<PacksStore.State, Message> {
        override fun PacksStore.State.reduce(msg: Message): PacksStore.State = when (msg) { is Message.Loading -> copy(isLoading = msg.isLoading) }
    }
}
