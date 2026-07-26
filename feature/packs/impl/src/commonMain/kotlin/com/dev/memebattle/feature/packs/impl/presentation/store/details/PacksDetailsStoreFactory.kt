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
                is PacksDetailsStore.Intent.ToggleLike -> toggleLike()
            }
        }

        private fun toggleLike() {
            val s = state()
            val packId = s.packId ?: return
            val isLiked = s.isLiked
            
            dispatch(Message.LikeLoading(true))
            scope.launch {
                val result = when (s.kind) {
                    PacksDetailsStore.PackKind.Meme -> {
                        if (isLiked) packRepository.unlikeMemePack(packId)
                        else packRepository.likeMemePack(packId)
                    }
                    PacksDetailsStore.PackKind.Situation -> {
                        if (isLiked) packRepository.unlikeSituationPack(packId)
                        else packRepository.likeSituationPack(packId)
                    }
                }
                
                result.onSuccess {
                    dispatch(Message.LikeToggled(!isLiked))
                }.onFailure { err ->
                    dispatch(Message.Error(err.message))
                }
                dispatch(Message.LikeLoading(false))
            }
        }

        private fun load(packId: String, kind: PacksDetailsStore.PackKind) {
            dispatch(Message.Loading(packId, kind))
            scope.launch {
                when (kind) {
                    PacksDetailsStore.PackKind.Meme -> {
                        packRepository.getMemePackDetails(packId)
                            .onSuccess { details ->
                                val isLiked = packRepository.likedMemePacks.value.any { it.id == packId }
                                dispatch(Message.MemeDetailsLoaded(details.pack, details.memes, isLiked))
                            }
                            .onFailure { err ->
                                dispatch(Message.Error(err.message))
                            }
                    }
                    PacksDetailsStore.PackKind.Situation -> {
                        packRepository.getSituationPackDetails(packId)
                            .onSuccess { details ->
                                val isLiked = packRepository.likedSituationPacks.value.any { it.id == packId }
                                dispatch(Message.SituationDetailsLoaded(details.pack, details.situations, isLiked))
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
        data class MemeDetailsLoaded(val pack: MemePack, val cards: List<MemeCard>, val isLiked: Boolean) : Message
        data class SituationDetailsLoaded(val pack: SituationPack, val cards: List<SituationCard>, val isLiked: Boolean) : Message
        data class LikeLoading(val isLoading: Boolean) : Message
        data class LikeToggled(val isLiked: Boolean) : Message
        data class Error(val message: String?) : Message
    }

    private object ReducerImpl : Reducer<PacksDetailsStore.State, Message> {
        override fun PacksDetailsStore.State.reduce(msg: Message): PacksDetailsStore.State = when (msg) {
            is Message.Loading -> copy(isLoading = true, packId = msg.packId, kind = msg.kind, error = null)
            is Message.MemeDetailsLoaded -> copy(
                isLoading = false,
                memePack = msg.pack,
                memeCards = msg.cards,
                isLiked = msg.isLiked,
            )
            is Message.SituationDetailsLoaded -> copy(
                isLoading = false,
                situationPack = msg.pack,
                situationCards = msg.cards,
                isLiked = msg.isLiked,
            )
            is Message.LikeLoading -> copy(isLikeLoading = msg.isLoading)
            is Message.LikeToggled -> copy(isLiked = msg.isLiked)
            is Message.Error -> copy(isLoading = false, error = msg.message)
        }
    }
}
