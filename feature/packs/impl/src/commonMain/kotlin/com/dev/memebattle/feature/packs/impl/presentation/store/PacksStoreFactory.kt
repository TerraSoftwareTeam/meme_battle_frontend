package com.dev.memebattle.feature.packs.impl.presentation.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.core.domain.packs.repository.PackRepository
import kotlinx.coroutines.launch

internal class PacksStoreFactory(
    private val storeFactory: StoreFactory,
    private val packRepository: PackRepository,
) {
    fun create(): PacksStore = object : PacksStore,
        Store<PacksStore.Intent, PacksStore.State, PacksStore.Effect> by storeFactory.create(
            name = "PacksStore",
            initialState = PacksStore.State(),
            executorFactory = ::PacksExecutor,
            reducer = PacksReducer,
        ) {}

    private inner class PacksExecutor :
        CoroutineExecutor<PacksStore.Intent, Nothing, PacksStore.State, Message, PacksStore.Effect>() {

        override fun executeIntent(intent: PacksStore.Intent) {
            when (intent) {
                is PacksStore.Intent.Init -> init()
                is PacksStore.Intent.RefreshMemePacks -> refreshMeme()
                is PacksStore.Intent.RefreshSituationPacks -> refreshSituation()
                is PacksStore.Intent.DeleteMemePack -> deleteMemePack(intent.id)
                is PacksStore.Intent.DeleteSituationPack -> deleteSituationPack(intent.id)
            }
        }

        private fun init() {
            // Подписываемся на реактивный список — UI обновится автоматически при любом изменении
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
            // Загружаем данные только если кэш пустой (не загружаем повторно при рекомпозиции)
            scope.launch {
                if (packRepository.memePacks.value.isEmpty()) {
                    dispatch(Message.Loading(true))
                    packRepository.refreshMemePacks()
                        .onFailure { dispatch(Message.Error(it.message)) }
                    packRepository.refreshSituationPacks()
                        .onFailure { dispatch(Message.Error(it.message)) }
                    dispatch(Message.Loading(false))
                }
            }
        }

        private fun refreshMeme() {
            scope.launch {
                dispatch(Message.Refreshing(true))
                packRepository.refreshMemePacks()
                    .onFailure { publish(PacksStore.Effect.ShowError(it.message ?: "Unknown error")) }
                dispatch(Message.Refreshing(false))
            }
        }

        private fun refreshSituation() {
            scope.launch {
                dispatch(Message.Refreshing(true))
                packRepository.refreshSituationPacks()
                    .onFailure { publish(PacksStore.Effect.ShowError(it.message ?: "Unknown error")) }
                dispatch(Message.Refreshing(false))
            }
        }

        private fun deleteMemePack(id: String) {
            scope.launch {
                // StateFlow в репозитории обновится сам — UI среагирует без явного dispatch
                packRepository.deleteMemePack(id)
                    .onFailure { publish(PacksStore.Effect.ShowError(it.message ?: "Unknown error")) }
            }
        }

        private fun deleteSituationPack(id: String) {
            scope.launch {
                packRepository.deleteSituationPack(id)
                    .onFailure { publish(PacksStore.Effect.ShowError(it.message ?: "Unknown error")) }
            }
        }
    }

    private sealed interface Message {
        data class Loading(val isLoading: Boolean) : Message
        data class Refreshing(val isRefreshing: Boolean) : Message
        data class MemePacks(val packs: List<MemePack>) : Message
        data class SituationPacks(val packs: List<SituationPack>) : Message
        data class Error(val message: String?) : Message
    }

    private object PacksReducer : Reducer<PacksStore.State, Message> {
        override fun PacksStore.State.reduce(msg: Message): PacksStore.State = when (msg) {
            is Message.Loading -> copy(isLoading = msg.isLoading, error = null)
            is Message.Refreshing -> copy(isRefreshing = msg.isRefreshing)
            is Message.MemePacks -> copy(memePacks = msg.packs)
            is Message.SituationPacks -> copy(situationPacks = msg.packs)
            is Message.Error -> copy(isLoading = false, error = msg.message)
        }
    }
}
