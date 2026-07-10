package com.dev.memebattle.feature.packs.impl.presentation.store.details

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal class PacksDetailsStoreFactory(
    private val storeFactory: StoreFactory,
) {
    fun create(): PacksDetailsStore = object : PacksDetailsStore,
        Store<PacksDetailsStore.Intent, PacksDetailsStore.State, PacksDetailsStore.Effect> by storeFactory.create(
            name = "PacksDetailsStore",
            initialState = PacksDetailsStore.State(),
            executorFactory = ::Executor,
            reducer = ReducerImpl,
        ) {}

    private inner class Executor :
        CoroutineExecutor<PacksDetailsStore.Intent, Nothing, PacksDetailsStore.State, Message, PacksDetailsStore.Effect>() {

        override fun executeIntent(intent: PacksDetailsStore.Intent) {
            when (intent) {
                is PacksDetailsStore.Intent.Load -> dispatch(Message.SetPackId(intent.packId))
                is PacksDetailsStore.Intent.Close -> publish(PacksDetailsStore.Effect.NavigateBack)
            }
        }
    }

    private sealed interface Message {
        data class SetPackId(val packId: String) : Message
    }

    private object ReducerImpl : Reducer<PacksDetailsStore.State, Message> {
        override fun PacksDetailsStore.State.reduce(msg: Message): PacksDetailsStore.State = when (msg) {
            is Message.SetPackId -> copy(packId = msg.packId)
        }
    }
}
