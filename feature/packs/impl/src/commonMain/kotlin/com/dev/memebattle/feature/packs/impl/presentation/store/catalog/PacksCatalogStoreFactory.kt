package com.dev.memebattle.feature.packs.impl.presentation.store.catalog

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.core.domain.packs.repository.PackRepository
import kotlinx.coroutines.launch

internal class PacksCatalogStoreFactory(
    private val storeFactory: StoreFactory,
    private val packRepository: PackRepository,
) {
    fun create(): PacksCatalogStore = object : PacksCatalogStore,
        Store<PacksCatalogStore.Intent, PacksCatalogStore.State, PacksCatalogStore.Effect> by storeFactory.create(
            name = "PacksCatalogStore",
            initialState = PacksCatalogStore.State(),
            executorFactory = ::Executor,
            reducer = ReducerImpl,
        ) {}

    private inner class Executor :
        CoroutineExecutor<PacksCatalogStore.Intent, Nothing, PacksCatalogStore.State, Message, PacksCatalogStore.Effect>() {

        override fun executeIntent(intent: PacksCatalogStore.Intent) {
            when (intent) {
                is PacksCatalogStore.Intent.Init -> init()
                is PacksCatalogStore.Intent.Refresh -> refresh()
                is PacksCatalogStore.Intent.SwitchPackType -> switchType(intent.type)
                is PacksCatalogStore.Intent.SwitchPackFilter -> switchFilter(intent.filter)
                is PacksCatalogStore.Intent.OpenDetails -> publish(PacksCatalogStore.Effect.NavigateToDetails(intent.packId))
                is PacksCatalogStore.Intent.OpenCreate -> publish(PacksCatalogStore.Effect.NavigateToCreate)
                is PacksCatalogStore.Intent.OpenEdit -> {} // Intercepted in component
                is PacksCatalogStore.Intent.GoBack -> {} // Intercepted in component
            }
        }

        private fun init() {
            scope.launch {
                packRepository.memePacks.collect { packs ->
                    dispatch(Message.MemePacks(packs))
                }
            }
            scope.launch {
                packRepository.situationPacks.collect { packs ->
                    dispatch(Message.SituationPacks(packs))
                }
            }
            scope.launch {
                packRepository.myMemePacks.collect { packs ->
                    dispatch(Message.MyMemePacks(packs))
                }
            }
            scope.launch {
                packRepository.mySituationPacks.collect { packs ->
                    dispatch(Message.MySituationPacks(packs))
                }
            }
            scope.launch {
                packRepository.likedMemePacks.collect { packs ->
                    dispatch(Message.LikedMemePacks(packs))
                }
            }
            scope.launch {
                packRepository.likedSituationPacks.collect { packs ->
                    dispatch(Message.LikedSituationPacks(packs))
                }
            }
            scope.launch {
                if (packRepository.memePacks.value.isEmpty()) {
                    dispatch(Message.Loading(true))
                    packRepository.refreshMemePacks()
                        .onFailure { dispatch(Message.Error(it.message)) }
                    packRepository.refreshSituationPacks()
                        .onFailure { dispatch(Message.Error(it.message)) }
                    packRepository.refreshMyMemePacks()
                    packRepository.refreshMySituationPacks()
                    packRepository.refreshLikedMemePacks()
                    packRepository.refreshLikedSituationPacks()
                    dispatch(Message.Loading(false))
                }
            }
        }

        private fun refresh() {
            scope.launch {
                dispatch(Message.Refreshing(true))
                val type = state().activeType
                val filter = state().activeFilter
                
                val result = when {
                    type == PacksCatalogStore.PackType.Memes && filter == PacksCatalogStore.PackFilter.All ->
                        packRepository.refreshMemePacks()
                    type == PacksCatalogStore.PackType.Memes && filter == PacksCatalogStore.PackFilter.Personal ->
                        packRepository.refreshMyMemePacks()
                    type == PacksCatalogStore.PackType.Memes && filter == PacksCatalogStore.PackFilter.Liked ->
                        packRepository.refreshLikedMemePacks()
                    type == PacksCatalogStore.PackType.Situations && filter == PacksCatalogStore.PackFilter.All ->
                        packRepository.refreshSituationPacks()
                    type == PacksCatalogStore.PackType.Situations && filter == PacksCatalogStore.PackFilter.Personal ->
                        packRepository.refreshMySituationPacks()
                    type == PacksCatalogStore.PackType.Situations && filter == PacksCatalogStore.PackFilter.Liked ->
                        packRepository.refreshLikedSituationPacks()
                    else -> Result.success(Unit)
                }

                result.onFailure { publish(PacksCatalogStore.Effect.ShowError(it.message ?: "Unknown error")) }
                dispatch(Message.Refreshing(false))
            }
        }

        private fun switchType(type: PacksCatalogStore.PackType) {
            dispatch(Message.SwitchType(type))
        }

        private fun switchFilter(filter: PacksCatalogStore.PackFilter) {
            dispatch(Message.SwitchFilter(filter))
        }
    }

    private sealed interface Message {
        data class Loading(val isLoading: Boolean) : Message
        data class Refreshing(val isRefreshing: Boolean) : Message
        data class MemePacks(val packs: List<MemePack>) : Message
        data class SituationPacks(val packs: List<SituationPack>) : Message
        data class MyMemePacks(val packs: List<MemePack>) : Message
        data class MySituationPacks(val packs: List<SituationPack>) : Message
        data class LikedMemePacks(val packs: List<MemePack>) : Message
        data class LikedSituationPacks(val packs: List<SituationPack>) : Message
        data class Error(val message: String?) : Message
        data class SwitchType(val type: PacksCatalogStore.PackType) : Message
        data class SwitchFilter(val filter: PacksCatalogStore.PackFilter) : Message
    }

    private object ReducerImpl : Reducer<PacksCatalogStore.State, Message> {
        override fun PacksCatalogStore.State.reduce(msg: Message): PacksCatalogStore.State = when (msg) {
            is Message.Loading -> copy(isLoading = msg.isLoading, error = null)
            is Message.Refreshing -> copy(isRefreshing = msg.isRefreshing)
            is Message.MemePacks -> copy(memePacks = msg.packs)
            is Message.SituationPacks -> copy(situationPacks = msg.packs)
            is Message.MyMemePacks -> copy(myMemePacks = msg.packs)
            is Message.MySituationPacks -> copy(mySituationPacks = msg.packs)
            is Message.LikedMemePacks -> copy(likedMemePacks = msg.packs)
            is Message.LikedSituationPacks -> copy(likedSituationPacks = msg.packs)
            is Message.Error -> copy(isLoading = false, error = msg.message)
            is Message.SwitchType -> copy(activeType = msg.type)
            is Message.SwitchFilter -> copy(activeFilter = msg.filter)
        }
    }
}
