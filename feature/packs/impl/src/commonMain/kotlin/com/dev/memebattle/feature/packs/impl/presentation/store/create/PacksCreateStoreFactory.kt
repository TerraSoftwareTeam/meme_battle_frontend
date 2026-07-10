package com.dev.memebattle.feature.packs.impl.presentation.store.create

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

import com.dev.memebattle.core.domain.packs.repository.PackRepository
import com.dev.network.media.current.api.MediaApiService
import kotlinx.coroutines.launch
import com.dev.memebattle.core.domain.packs.model.SafetyLevel

internal class PacksCreateStoreFactory(
    private val storeFactory: StoreFactory,
    private val packRepository: PackRepository,
    private val mediaApiService: MediaApiService,
) {
    fun create(): PacksCreateStore = object : PacksCreateStore,
        Store<PacksCreateStore.Intent, PacksCreateStore.State, PacksCreateStore.Effect> by storeFactory.create(
            name = "PacksCreateStore",
            initialState = PacksCreateStore.State(),
            executorFactory = ::Executor,
            reducer = ReducerImpl,
        ) {}

    private inner class Executor :
        CoroutineExecutor<PacksCreateStore.Intent, Nothing, PacksCreateStore.State, Message, PacksCreateStore.Effect>() {

        override fun executeIntent(intent: PacksCreateStore.Intent) {
            when (intent) {
                is PacksCreateStore.Intent.Close -> publish(PacksCreateStore.Effect.NavigateBack)
                is PacksCreateStore.Intent.UpdateName -> dispatch(Message.UpdateName(intent.name))
                is PacksCreateStore.Intent.UpdateDescription -> dispatch(Message.UpdateDescription(intent.description))
                is PacksCreateStore.Intent.UpdateType -> dispatch(Message.UpdateType(intent.type))
                is PacksCreateStore.Intent.UpdateIsPublic -> dispatch(Message.UpdateIsPublic(intent.isPublic))
                is PacksCreateStore.Intent.UpdateLanguage -> dispatch(Message.UpdateLanguage(intent.languageCode))
                is PacksCreateStore.Intent.UpdateSafetyLevel -> dispatch(Message.UpdateSafetyLevel(intent.safetyLevel))
                is PacksCreateStore.Intent.AddPrompt -> dispatch(Message.AddPrompt(intent.prompt))
                is PacksCreateStore.Intent.RemovePrompt -> dispatch(Message.RemovePrompt(intent.index))
                is PacksCreateStore.Intent.AddMemePlaceholder -> dispatch(Message.AddMemePlaceholder)
                is PacksCreateStore.Intent.RemoveMemePlaceholder -> dispatch(Message.RemoveMemePlaceholder)
                is PacksCreateStore.Intent.UpdateSelectedFiles -> dispatch(Message.UpdateSelectedFiles(intent.files))
                is PacksCreateStore.Intent.Create -> createPack()
            }
        }

        private fun createPack() {
            val currentState = state()
            println("[PacksCreateStore] createPack() called, isCreateEnabled=${currentState.isCreateEnabled}, name='${currentState.name}', files=${currentState.selectedFiles.size}")
            if (!currentState.isCreateEnabled) return
            
            scope.launch {
                try {
                    dispatch(Message.SetLoading(true))
                    
                    if (currentState.type == PacksCreateStore.PackType.Memes) {
                        val mediaIds = mutableListOf<Long>()
                        for (file in currentState.selectedFiles) {
                            println("[PacksCreateStore] Reading file: ${file.name}")
                            val rawBytes = file.readBytes()
                            println("[PacksCreateStore] File read complete: ${rawBytes.size} bytes, compressing if needed...")
                            
                            val maxSizeBytes = 2 * 1024 * 1024L // 2 MB limit
                            val byteArray = compressImageIfNeeded(rawBytes, maxSizeBytes)
                            if (byteArray.size < rawBytes.size) {
                                println("[PacksCreateStore] File compressed from ${rawBytes.size} to ${byteArray.size} bytes")
                            } else {
                                println("[PacksCreateStore] Using original file size: ${byteArray.size} bytes")
                            }
                            
                            val fileName = file.name
                            val result = mediaApiService.uploadImageMedia(byteArray, fileName)
                            println("[PacksCreateStore] Upload result: $result")
                            when (result) {
                                is com.dev.memebattle.core.network.call.NetworkResult.Success -> mediaIds.add(result.data.id)
                                is com.dev.memebattle.core.network.call.NetworkResult.Error -> {
                                    dispatch(Message.SetError(result.error.toString()))
                                    dispatch(Message.SetLoading(false))
                                    publish(PacksCreateStore.Effect.ShowError(result.error.toString()))
                                    return@launch
                                }
                            }
                        }
                        println("[PacksCreateStore] All uploads done, creating pack with mediaIds=$mediaIds")
                        val createResult = packRepository.createMemePack(
                            name = currentState.name,
                            description = currentState.description.takeIf { it.isNotBlank() },
                            isPublic = currentState.isPublic,
                            languageCode = currentState.languageCode,
                            safetyLevel = currentState.safetyLevel,
                            mediaIds = mediaIds
                        )
                        println("[PacksCreateStore] createMemePack result: $createResult")
                        createResult.onSuccess {
                            publish(PacksCreateStore.Effect.Created(it.id))
                        }.onFailure {
                            dispatch(Message.SetError(it.message ?: "Failed to create pack"))
                            publish(PacksCreateStore.Effect.ShowError(it.message ?: "Failed to create pack"))
                        }
                    } else {
                        val createResult = packRepository.createSituationPack(
                            name = currentState.name,
                            description = currentState.description.takeIf { it.isNotBlank() },
                            isPublic = currentState.isPublic,
                            languageCode = currentState.languageCode,
                            safetyLevel = currentState.safetyLevel,
                            prompts = currentState.prompts
                        )
                        createResult.onSuccess {
                            publish(PacksCreateStore.Effect.Created(it.id))
                        }.onFailure {
                            dispatch(Message.SetError(it.message ?: "Failed to create pack"))
                            publish(PacksCreateStore.Effect.ShowError(it.message ?: "Failed to create pack"))
                        }
                    }
                } catch (e: Exception) {
                    println("[PacksCreateStore] EXCEPTION in createPack: ${e::class.simpleName}: ${e.message}")
                    dispatch(Message.SetError(e.message ?: "Unexpected error"))
                    publish(PacksCreateStore.Effect.ShowError(e.message ?: "Unexpected error"))
                } finally {
                    dispatch(Message.SetLoading(false))
                }
            }
        }
    }

    private sealed interface Message {
        data class SetLoading(val isLoading: Boolean) : Message
        data class SetError(val error: String?) : Message
        data class UpdateName(val name: String) : Message
        data class UpdateDescription(val description: String) : Message
        data class UpdateType(val type: PacksCreateStore.PackType) : Message
        data class UpdateIsPublic(val isPublic: Boolean) : Message
        data class UpdateLanguage(val languageCode: String) : Message
        data class UpdateSafetyLevel(val safetyLevel: SafetyLevel) : Message
        data class AddPrompt(val prompt: String) : Message
        data class RemovePrompt(val index: Int) : Message
        data object AddMemePlaceholder : Message
        data object RemoveMemePlaceholder : Message
        data class UpdateSelectedFiles(val files: List<io.github.vinceglb.filekit.core.PlatformFile>) : Message
    }

    private object ReducerImpl : Reducer<PacksCreateStore.State, Message> {
        override fun PacksCreateStore.State.reduce(msg: Message): PacksCreateStore.State = when (msg) {
            is Message.SetLoading -> copy(isLoading = msg.isLoading, error = null)
            is Message.SetError -> copy(error = msg.error, isLoading = false)
            is Message.UpdateName -> copy(name = msg.name)
            is Message.UpdateDescription -> copy(description = msg.description)
            is Message.UpdateType -> copy(type = msg.type)
            is Message.UpdateIsPublic -> copy(isPublic = msg.isPublic)
            is Message.UpdateLanguage -> copy(languageCode = msg.languageCode)
            is Message.UpdateSafetyLevel -> copy(safetyLevel = msg.safetyLevel)
            is Message.AddPrompt -> copy(prompts = prompts + msg.prompt)
            is Message.RemovePrompt -> {
                val newPrompts = prompts.toMutableList()
                if (msg.index in newPrompts.indices) {
                    newPrompts.removeAt(msg.index)
                }
                copy(prompts = newPrompts)
            }
            is Message.AddMemePlaceholder -> this
            is Message.RemoveMemePlaceholder -> this
            is Message.UpdateSelectedFiles -> copy(selectedFiles = msg.files)
        }
    }
}
