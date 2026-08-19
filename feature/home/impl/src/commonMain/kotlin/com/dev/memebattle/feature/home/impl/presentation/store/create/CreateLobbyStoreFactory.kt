package com.dev.memebattle.feature.home.impl.presentation.store.create

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.core.domain.packs.repository.LikedPacksState
import com.dev.memebattle.core.domain.packs.repository.PackRepository
import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.network.game.current.api.GameApiService
import com.dev.network.game.current.dto.CreateGameRequest
import com.dev.network.game.current.dto.GameMode
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateLobbyStoreFactory(
    private val storeFactory: StoreFactory,
    private val packRepository: PackRepository,
    private val gameApiService: GameApiService,
) {

    fun create(): CreateLobbyStore =
        object : CreateLobbyStore, Store<CreateLobbyStore.Intent, CreateLobbyStore.State, CreateLobbyStore.Label> by storeFactory.create(
            name = "CreateLobbyStore",
            initialState = CreateLobbyStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data class SetLoading(val isLoading: Boolean) : Msg
        data class SetPacksLoading(val isLoading: Boolean) : Msg
        data class SetLikedMemePackCount(val count: Int) : Msg
        data class SetLikedSituationPackCount(val count: Int) : Msg
        data class SetError(val error: String?) : Msg
        data class UpdateMemePacks(val packs: List<MemePack>) : Msg
        data class UpdateSituationPacks(val packs: List<SituationPack>) : Msg
        data class ToggleMemePack(val id: String) : Msg
        data class ToggleSituationPack(val id: String) : Msg
        data class SetMode(val mode: GameMode) : Msg
        data class SetMaxRounds(val rounds: Int) : Msg
        data class SetHandSize(val size: Int) : Msg
        data class UpdateLobbyNameInput(val name: String) : Msg
        data class UpdateHandleInput(val handle: String) : Msg
    }

    private inner class ExecutorImpl : CoroutineExecutor<CreateLobbyStore.Intent, Unit, CreateLobbyStore.State, Msg, CreateLobbyStore.Label>() {
        override fun executeAction(action: Unit) {
            // Collect liked meme packs with loading state
            scope.launch {
                packRepository.observeLikedMemePacks().collectLatest { state ->
                    when (state) {
                        is LikedPacksState.Loading -> {
                            dispatch(Msg.SetPacksLoading(true))
                            dispatch(Msg.SetLikedMemePackCount(state.count))
                        }
                        is LikedPacksState.Success -> {
                            dispatch(Msg.UpdateMemePacks(state.packs))
                            dispatch(Msg.SetPacksLoading(false))
                        }
                    }
                }
            }
            
            // Collect liked situation packs with loading state
            scope.launch {
                packRepository.observeLikedSituationPacks().collectLatest { state ->
                    when (state) {
                        is LikedPacksState.Loading -> {
                            dispatch(Msg.SetPacksLoading(true))
                            dispatch(Msg.SetLikedSituationPackCount(state.count))
                        }
                        is LikedPacksState.Success -> {
                            dispatch(Msg.UpdateSituationPacks(state.packs))
                            dispatch(Msg.SetPacksLoading(false))
                        }
                    }
                }
            }
        }

        override fun executeIntent(intent: CreateLobbyStore.Intent) {
            when (intent) {
                is CreateLobbyStore.Intent.ToggleMemePack -> dispatch(Msg.ToggleMemePack(intent.id))
                is CreateLobbyStore.Intent.ToggleSituationPack -> dispatch(Msg.ToggleSituationPack(intent.id))
                is CreateLobbyStore.Intent.SetMode -> dispatch(Msg.SetMode(intent.mode))
                is CreateLobbyStore.Intent.SetMaxRounds -> dispatch(Msg.SetMaxRounds(intent.rounds))
                is CreateLobbyStore.Intent.SetHandSize -> dispatch(Msg.SetHandSize(intent.size))
                is CreateLobbyStore.Intent.UpdateLobbyNameInput -> dispatch(Msg.UpdateLobbyNameInput(intent.name))
                is CreateLobbyStore.Intent.UpdateHandleInput -> dispatch(Msg.UpdateHandleInput(intent.handle))
                CreateLobbyStore.Intent.Create -> createGame(state())
            }
        }

        private fun createGame(state: CreateLobbyStore.State) {
            if (!state.isCreateEnabled) return

            scope.launch {
                dispatch(Msg.SetLoading(true))
                dispatch(Msg.SetError(null))

                val lobbyName = state.lobbyNameInput.trim()
                val handleInput = state.handleInput.trim().takeIf { it.isNotEmpty() }
                val request = CreateGameRequest(
                    hand_size = state.handSize,
                    handle = handleInput,
                    max_rounds = state.maxRounds,
                    mode = state.mode,
                    name = lobbyName,
                    selected_meme_pack_ids = state.selectedMemePackIds.toList(),
                    selected_situation_pack_ids = state.selectedSituationPackIds.toList()
                )

                val result = gameApiService.createGame(request)
                dispatch(Msg.SetLoading(false))

                when (result) {
                    is NetworkResult.Success -> {
                        publish(CreateLobbyStore.Label.LobbyCreated(result.data.id))
                    }
                    is NetworkResult.Error -> {
                        dispatch(Msg.SetError(result.error.toString() ?: "Failed to create lobby"))
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<CreateLobbyStore.State, Msg> {
        override fun CreateLobbyStore.State.reduce(msg: Msg): CreateLobbyStore.State =
            when (msg) {
                is Msg.SetLoading -> copy(isLoading = msg.isLoading)
                is Msg.SetPacksLoading -> copy(isPacksLoading = msg.isLoading)
                is Msg.SetLikedMemePackCount -> copy(likedMemePackCount = msg.count)
                is Msg.SetLikedSituationPackCount -> copy(likedSituationPackCount = msg.count)
                is Msg.SetError -> copy(error = msg.error)
                is Msg.UpdateMemePacks -> copy(availableMemePacks = msg.packs)
                is Msg.UpdateSituationPacks -> copy(availableSituationPacks = msg.packs)
                is Msg.ToggleMemePack -> {
                    val newSet = selectedMemePackIds.toMutableSet()
                    if (newSet.contains(msg.id)) newSet.remove(msg.id) else newSet.add(msg.id)
                    copy(selectedMemePackIds = newSet)
                }
                is Msg.ToggleSituationPack -> {
                    val newSet = selectedSituationPackIds.toMutableSet()
                    if (newSet.contains(msg.id)) newSet.remove(msg.id) else newSet.add(msg.id)
                    copy(selectedSituationPackIds = newSet)
                }
                is Msg.SetMode -> copy(mode = msg.mode)
                is Msg.SetMaxRounds -> copy(maxRounds = msg.rounds)
                is Msg.SetHandSize -> copy(handSize = msg.size)
                is Msg.UpdateLobbyNameInput -> copy(lobbyNameInput = msg.name)
                is Msg.UpdateHandleInput -> copy(handleInput = msg.handle)
            }
    }
}
