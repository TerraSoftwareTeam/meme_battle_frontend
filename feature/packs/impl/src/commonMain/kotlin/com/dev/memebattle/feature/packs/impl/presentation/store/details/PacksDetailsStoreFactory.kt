package com.dev.memebattle.feature.packs.impl.presentation.store.details

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.memebattle.core.domain.packs.model.MemeCard
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.SituationCard
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.core.domain.packs.repository.PackRepository
import kotlinx.coroutines.launch

internal class PacksDetailsStoreFactory(
    private val storeFactory: StoreFactory,
    private val packRepository: PackRepository,
) {
    fun create(): PacksDetailsStore = object : PacksDetailsStore,
        Store<PacksDetailsStore.Intent, PacksDetailsStore.State, PacksDetailsStore.Effect> by storeFactory.create(
            name = "PacksDetailsStore",
            initialState = PacksDetailsStore.State(),
            bootstrapper = com.arkivanov.mvikotlin.core.store.SimpleBootstrapper(Unit),
            executorFactory = ::Executor,
            reducer = ReducerImpl,
        ) {}

    private inner class Executor :
        CoroutineExecutor<PacksDetailsStore.Intent, Unit, PacksDetailsStore.State, Message, PacksDetailsStore.Effect>() {

        override fun executeAction(action: Unit) {
            scope.launch {
                packRepository.packUpdates.collect { updatedId ->
                    val s = state()
                    if (s.packId == updatedId && s.kind != null) {
                        load(s.packId, s.kind)
                    }
                }
            }
        }

        override fun executeIntent(intent: PacksDetailsStore.Intent) {
            when (intent) {
                is PacksDetailsStore.Intent.Load -> load(intent.packId, intent.kind)
                is PacksDetailsStore.Intent.Close -> publish(PacksDetailsStore.Effect.NavigateBack)
            }
        }

        private fun load(packId: String, kind: PacksDetailsStore.PackKind) {
            dispatch(Message.Loading(packId, kind))
            scope.launch {
                when (kind) {
                    PacksDetailsStore.PackKind.Meme -> {
                        packRepository.getMemePackDetails(packId)
                            .onSuccess { details ->
                                dispatch(Message.MemeDetailsLoaded(details.pack, details.memes))
                            }
                            .onFailure { err ->
                                dispatch(Message.Error(err.message))
                            }
                    }
                    PacksDetailsStore.PackKind.Situation -> {
                        packRepository.getSituationPackDetails(packId)
                            .onSuccess { details ->
                                dispatch(Message.SituationDetailsLoaded(details.pack, details.situations))
                            }
                            .onFailure { err ->
                                dispatch(Message.Error(err.message))
                            }
                    }
                }
            }
        }
    }

    private sealed interface Message {
        data class Loading(val packId: String, val kind: PacksDetailsStore.PackKind) : Message
        data class MemeDetailsLoaded(val pack: MemePack, val cards: List<MemeCard>) : Message
        data class SituationDetailsLoaded(val pack: SituationPack, val cards: List<SituationCard>) : Message
        data class Error(val message: String?) : Message
    }

    private object ReducerImpl : Reducer<PacksDetailsStore.State, Message> {
        override fun PacksDetailsStore.State.reduce(msg: Message): PacksDetailsStore.State = when (msg) {
            is Message.Loading -> copy(isLoading = true, packId = msg.packId, kind = msg.kind, error = null)
            is Message.MemeDetailsLoaded -> copy(
                isLoading = false,
                memePack = msg.pack,
                memeCards = msg.cards,
            )
            is Message.SituationDetailsLoaded -> copy(
                isLoading = false,
                situationPack = msg.pack,
                situationCards = msg.cards,
            )
            is Message.Error -> copy(isLoading = false, error = msg.message)
        }
    }
}
