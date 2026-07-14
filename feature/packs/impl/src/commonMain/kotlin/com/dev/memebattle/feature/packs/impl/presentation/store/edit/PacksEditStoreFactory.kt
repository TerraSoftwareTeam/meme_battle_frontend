package com.dev.memebattle.feature.packs.impl.presentation.store.edit

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.memebattle.core.domain.packs.model.MemeCard
import com.dev.memebattle.core.domain.packs.model.SafetyLevel
import com.dev.memebattle.core.domain.packs.model.SituationCard
import com.dev.memebattle.core.domain.packs.repository.PackRepository
import com.dev.network.media.current.api.MediaApiService
import kotlinx.coroutines.launch
import com.dev.memebattle.feature.packs.impl.presentation.store.create.compressImageIfNeeded

internal class PacksEditStoreFactory(
    private val storeFactory: StoreFactory,
    private val packRepository: PackRepository,
    private val mediaApiService: MediaApiService,
) {
    fun create(): PacksEditStore = object : PacksEditStore,
        Store<PacksEditStore.Intent, PacksEditStore.State, PacksEditStore.Effect> by storeFactory.create(
            name = "PacksEditStore",
            initialState = PacksEditStore.State(),
            executorFactory = ::Executor,
            reducer = ReducerImpl,
        ) {}

    private inner class Executor :
        CoroutineExecutor<PacksEditStore.Intent, Nothing, PacksEditStore.State, Message, PacksEditStore.Effect>() {

        override fun executeIntent(intent: PacksEditStore.Intent) {
            when (intent) {
                is PacksEditStore.Intent.Close -> publish(PacksEditStore.Effect.NavigateBack)
                is PacksEditStore.Intent.Load -> load(intent.packId, intent.kind)
                is PacksEditStore.Intent.UpdateName -> dispatch(Message.UpdateName(intent.name))
                is PacksEditStore.Intent.UpdateDescription -> dispatch(Message.UpdateDescription(intent.description))
                is PacksEditStore.Intent.UpdateIsPublic -> dispatch(Message.UpdateIsPublic(intent.isPublic))
                is PacksEditStore.Intent.UpdateSafetyLevel -> dispatch(Message.UpdateSafetyLevel(intent.safetyLevel))
                is PacksEditStore.Intent.AddPrompt -> dispatch(Message.AddPrompt(intent.prompt))
                is PacksEditStore.Intent.RemovePrompt -> dispatch(Message.RemovePrompt(intent.index))
                is PacksEditStore.Intent.UpdateSelectedFiles -> dispatch(Message.UpdateSelectedFiles(intent.files))
                is PacksEditStore.Intent.DeleteMemeCard -> deleteMemeCard(intent.cardId)
                is PacksEditStore.Intent.DeleteSituationCard -> deleteSituationCard(intent.cardId)
                is PacksEditStore.Intent.Save -> savePack()
            }
        }

        private fun load(packId: String, kind: String) {
            dispatch(Message.SetPackInfo(packId, kind))
            dispatch(Message.SetLoading(true))
            scope.launch {
                try {
                    if (kind == "meme") {
                        val result = packRepository.getMemePackDetails(packId)
                        result.onSuccess { details ->
                            dispatch(Message.LoadedMeme(
                                name = details.pack.name,
                                description = details.pack.description ?: "",
                                isPublic = details.pack.isPublic,
                                safetyLevel = details.pack.safetyLevel,
                                languageCode = details.pack.languageCode,
                                cards = details.memes
                            ))
                        }.onFailure {
                            publish(PacksEditStore.Effect.ShowNotification(it.message ?: "Failed to load pack", true))
                        }
                    } else {
                        val result = packRepository.getSituationPackDetails(packId)
                        result.onSuccess { details ->
                            dispatch(Message.LoadedSituation(
                                name = details.pack.name,
                                description = details.pack.description ?: "",
                                isPublic = details.pack.isPublic,
                                safetyLevel = details.pack.safetyLevel,
                                languageCode = details.pack.languageCode,
                                cards = details.situations
                            ))
                        }.onFailure {
                            publish(PacksEditStore.Effect.ShowNotification(it.message ?: "Failed to load pack", true))
                        }
                    }
                } finally {
                    dispatch(Message.SetLoading(false))
                }
            }
        }

        private fun deleteMemeCard(cardId: String) {
            val currentState = state()
            dispatch(Message.SetLoading(true))
            scope.launch {
                val result = packRepository.deleteMemeFromPack(currentState.packId, cardId)
                if (result.isSuccess) {
                    dispatch(Message.MemeCardDeleted(cardId))
                } else {
                    publish(PacksEditStore.Effect.ShowNotification("Failed to delete card", true))
                }
                dispatch(Message.SetLoading(false))
            }
        }

        private fun deleteSituationCard(cardId: String) {
            val currentState = state()
            dispatch(Message.SetLoading(true))
            scope.launch {
                val result = packRepository.deleteSituationFromPack(currentState.packId, cardId)
                if (result.isSuccess) {
                    dispatch(Message.SituationCardDeleted(cardId))
                } else {
                    publish(PacksEditStore.Effect.ShowNotification("Failed to delete card", true))
                }
                dispatch(Message.SetLoading(false))
            }
        }

        private fun savePack() {
            val currentState = state()
            if (!currentState.isSaveEnabled) return

            scope.launch {
                try {
                    dispatch(Message.SetSaving(true))

                    if (currentState.kind == "meme") {
                        val mediaIds = mutableListOf<Long>()
                        for (file in currentState.selectedFiles) {
                            val rawBytes = file.readBytes()
                            val maxSizeBytes = 2 * 1024 * 1024 * 15L
                            val byteArray = compressImageIfNeeded(rawBytes, maxSizeBytes)
                            val result = mediaApiService.uploadImageMedia(byteArray, file.name)
                            when (result) {
                                is com.dev.memebattle.core.network.call.NetworkResult.Success -> mediaIds.add(result.data.id)
                                is com.dev.memebattle.core.network.call.NetworkResult.Error -> {
                                    publish(PacksEditStore.Effect.ShowNotification(result.error.toString(), true))
                                    dispatch(Message.SetSaving(false))
                                    return@launch
                                }
                            }
                        }

                        if (mediaIds.isNotEmpty()) {
                            packRepository.addMemesToPack(currentState.packId, mediaIds)
                        }

                        val updateResult = packRepository.updateMemePack(
                            id = currentState.packId,
                            name = currentState.name,
                            description = currentState.description.takeIf { it.isNotBlank() },
                            isPublic = currentState.isPublic,
                            languageCode = currentState.languageCode,
                            safetyLevel = currentState.safetyLevel
                        )
                        updateResult.onSuccess {
                            publish(PacksEditStore.Effect.ShowNotification("Pack updated successfully!"))
                            publish(PacksEditStore.Effect.Saved(currentState.packId, "meme"))
                        }.onFailure {
                            publish(PacksEditStore.Effect.ShowNotification(it.message ?: "Failed to update pack", true))
                        }
                    } else {
                        if (currentState.promptsToAdd.isNotEmpty()) {
                            packRepository.addSituationsToPack(currentState.packId, currentState.promptsToAdd)
                        }

                        val updateResult = packRepository.updateSituationPack(
                            id = currentState.packId,
                            name = currentState.name,
                            description = currentState.description.takeIf { it.isNotBlank() },
                            isPublic = currentState.isPublic,
                            languageCode = currentState.languageCode,
                            safetyLevel = currentState.safetyLevel
                        )
                        updateResult.onSuccess {
                            publish(PacksEditStore.Effect.ShowNotification("Pack updated successfully!"))
                            publish(PacksEditStore.Effect.Saved(currentState.packId, "situation"))
                        }.onFailure {
                            publish(PacksEditStore.Effect.ShowNotification(it.message ?: "Failed to update pack", true))
                        }
                    }
                } catch (e: Exception) {
                    publish(PacksEditStore.Effect.ShowNotification(e.message ?: "Unexpected error", true))
                } finally {
                    dispatch(Message.SetSaving(false))
                }
            }
        }
    }

    private sealed interface Message {
        data class SetLoading(val isLoading: Boolean) : Message
        data class SetSaving(val isSaving: Boolean) : Message
        data class SetPackInfo(val packId: String, val kind: String) : Message
        data class LoadedMeme(val name: String, val description: String, val isPublic: Boolean, val safetyLevel: SafetyLevel, val languageCode: String, val cards: List<MemeCard>) : Message
        data class LoadedSituation(val name: String, val description: String, val isPublic: Boolean, val safetyLevel: SafetyLevel, val languageCode: String, val cards: List<SituationCard>) : Message
        data class UpdateName(val name: String) : Message
        data class UpdateDescription(val description: String) : Message
        data class UpdateIsPublic(val isPublic: Boolean) : Message
        data class UpdateSafetyLevel(val safetyLevel: SafetyLevel) : Message
        data class AddPrompt(val prompt: String) : Message
        data class RemovePrompt(val index: Int) : Message
        data class UpdateSelectedFiles(val files: List<io.github.vinceglb.filekit.core.PlatformFile>) : Message
        data class MemeCardDeleted(val cardId: String) : Message
        data class SituationCardDeleted(val cardId: String) : Message
    }

    private object ReducerImpl : Reducer<PacksEditStore.State, Message> {
        override fun PacksEditStore.State.reduce(msg: Message): PacksEditStore.State = when (msg) {
            is Message.SetLoading -> copy(isLoading = msg.isLoading)
            is Message.SetSaving -> copy(isSaving = msg.isSaving)
            is Message.SetPackInfo -> copy(packId = msg.packId, kind = msg.kind)
            is Message.LoadedMeme -> copy(name = msg.name, description = msg.description, isPublic = msg.isPublic, safetyLevel = msg.safetyLevel, languageCode = msg.languageCode, memeCards = msg.cards)
            is Message.LoadedSituation -> copy(name = msg.name, description = msg.description, isPublic = msg.isPublic, safetyLevel = msg.safetyLevel, languageCode = msg.languageCode, situationCards = msg.cards)
            is Message.UpdateName -> copy(name = msg.name)
            is Message.UpdateDescription -> copy(description = msg.description)
            is Message.UpdateIsPublic -> copy(isPublic = msg.isPublic)
            is Message.UpdateSafetyLevel -> copy(safetyLevel = msg.safetyLevel)
            is Message.AddPrompt -> copy(promptsToAdd = promptsToAdd + msg.prompt)
            is Message.RemovePrompt -> {
                val newPrompts = promptsToAdd.toMutableList()
                if (msg.index in newPrompts.indices) newPrompts.removeAt(msg.index)
                copy(promptsToAdd = newPrompts)
            }
            is Message.UpdateSelectedFiles -> copy(selectedFiles = msg.files)
            is Message.MemeCardDeleted -> copy(memeCards = memeCards.filter { it.id != msg.cardId })
            is Message.SituationCardDeleted -> copy(situationCards = situationCards.filter { it.id != msg.cardId })
        }
    }
}
