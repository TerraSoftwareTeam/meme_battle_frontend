package com.dev.memebattle.feature.home.impl.presentation.store.create

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.SituationPack
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
    private val initialSelectedMemeIds: Set<String> = emptySet(),
    private val initialSelectedSituationIds: Set<String> = emptySet(),
    private val initialExtraMemeIds: Set<String> = emptySet(),
    private val initialExtraSituationIds: Set<String> = emptySet(),
) {

    fun create(): CreateLobbyStore =
        object : CreateLobbyStore, Store<CreateLobbyStore.Intent, CreateLobbyStore.State, CreateLobbyStore.Label> by storeFactory.create(
            name = "CreateLobbyStore",
            initialState = CreateLobbyStore.State(
                selectedMemePackIds = initialSelectedMemeIds.take(1).toSet(),
                selectedSituationPackIds = initialSelectedSituationIds.take(1).toSet(),
            ),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data class SetLoading(val isLoading: Boolean) : Msg
        data class SetPacksLoading(val isLoading: Boolean) : Msg
        data class SetError(val error: String?) : Msg
        data class UpdateOfficialMemePacks(val packs: List<MemePack>) : Msg
        data class UpdateOfficialSituationPacks(val packs: List<SituationPack>) : Msg
        data class ToggleMemePack(val id: String) : Msg
        data class ToggleSituationPack(val id: String) : Msg
        data class SetMode(val mode: GameMode) : Msg
        data class SetMaxRounds(val rounds: Int) : Msg
        data class SetHandSize(val size: Int) : Msg
        data class UpdateLobbyNameInput(val name: String) : Msg
        data class UpdateHandleInput(val handle: String) : Msg
        data class AddPacksFromPicker(
            val extraMemePacks: List<MemePack>,
            val extraSituationPacks: List<SituationPack>,
            val newMemeIds: Set<String>,
            val newSituationIds: Set<String>
        ) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<CreateLobbyStore.Intent, Unit, CreateLobbyStore.State, Msg, CreateLobbyStore.Label>() {

        override fun executeAction(action: Unit) {
            // Observe all meme packs and filter official ones
            scope.launch {
                dispatch(Msg.SetPacksLoading(true))
                // Trigger a refresh in the background to get latest data
                packRepository.refreshMemePacks()
                packRepository.refreshSituationPacks()
            }

            scope.launch {
                packRepository.memePacks.collectLatest { allPacks ->
                    val officialPacks = allPacks.filter { pack ->
                        pack.id in CreateLobbyStore.OfficialPackIds.allMemeIds
                    }.sortedBy { it.id } // stable order
                    dispatch(Msg.UpdateOfficialMemePacks(officialPacks))
                    dispatch(Msg.SetPacksLoading(false))

                    val currentState = state()
                    val extraPacksFromRepo = allPacks.filter { pack ->
                        (pack.id in initialExtraMemeIds || pack.id in currentState.selectedMemePackIds) &&
                            pack.id !in CreateLobbyStore.OfficialPackIds.allMemeIds
                    }
                    if (extraPacksFromRepo.isNotEmpty()) {
                        val combinedExtras = (currentState.extraMemePacks + extraPacksFromRepo).distinctBy { it.id }
                        if (combinedExtras != currentState.extraMemePacks) {
                            dispatch(
                                Msg.AddPacksFromPicker(
                                    extraMemePacks = combinedExtras,
                                    extraSituationPacks = currentState.extraSituationPacks,
                                    newMemeIds = currentState.selectedMemePackIds,
                                    newSituationIds = currentState.selectedSituationPackIds
                                )
                            )
                        }
                    }

                    // Auto-select only the FIRST official meme pack if nothing selected yet
                    if (currentState.selectedMemePackIds.isEmpty() && officialPacks.isNotEmpty()) {
                        dispatch(Msg.ToggleMemePack(officialPacks.first().id))
                    }
                }
            }

            scope.launch {
                packRepository.situationPacks.collectLatest { allPacks ->
                    val officialPacks = allPacks.filter { pack ->
                        pack.id in CreateLobbyStore.OfficialPackIds.allSituationIds
                    }.sortedBy { it.id }
                    dispatch(Msg.UpdateOfficialSituationPacks(officialPacks))
                    dispatch(Msg.SetPacksLoading(false))

                    val currentState = state()
                    val extraPacksFromRepo = allPacks.filter { pack ->
                        (pack.id in initialExtraSituationIds || pack.id in currentState.selectedSituationPackIds) &&
                            pack.id !in CreateLobbyStore.OfficialPackIds.allSituationIds
                    }
                    if (extraPacksFromRepo.isNotEmpty()) {
                        val combinedExtras = (currentState.extraSituationPacks + extraPacksFromRepo).distinctBy { it.id }
                        if (combinedExtras != currentState.extraSituationPacks) {
                            dispatch(
                                Msg.AddPacksFromPicker(
                                    extraMemePacks = currentState.extraMemePacks,
                                    extraSituationPacks = combinedExtras,
                                    newMemeIds = currentState.selectedMemePackIds,
                                    newSituationIds = currentState.selectedSituationPackIds
                                )
                            )
                        }
                    }

                    // Auto-select only the FIRST official situation pack if nothing selected yet
                    if (currentState.selectedSituationPackIds.isEmpty() && officialPacks.isNotEmpty()) {
                        dispatch(Msg.ToggleSituationPack(officialPacks.first().id))
                    }
                }
            }
        }

        override fun executeIntent(intent: CreateLobbyStore.Intent) {
            when (intent) {
                is CreateLobbyStore.Intent.ToggleMemePack ->
                    dispatch(Msg.ToggleMemePack(intent.id))
                is CreateLobbyStore.Intent.ToggleSituationPack ->
                    dispatch(Msg.ToggleSituationPack(intent.id))
                is CreateLobbyStore.Intent.SetMode ->
                    dispatch(Msg.SetMode(intent.mode))
                is CreateLobbyStore.Intent.SetMaxRounds ->
                    dispatch(Msg.SetMaxRounds(intent.rounds))
                is CreateLobbyStore.Intent.SetHandSize ->
                    dispatch(Msg.SetHandSize(intent.size))
                is CreateLobbyStore.Intent.UpdateLobbyNameInput ->
                    dispatch(Msg.UpdateLobbyNameInput(intent.name))
                is CreateLobbyStore.Intent.UpdateHandleInput ->
                    dispatch(Msg.UpdateHandleInput(intent.handle))
                is CreateLobbyStore.Intent.AddPacksFromPicker ->
                    addPacksFromPicker(intent)
                CreateLobbyStore.Intent.Create ->
                    createGame(state())
            }
        }

        private fun addPacksFromPicker(intent: CreateLobbyStore.Intent.AddPacksFromPicker) {
            val currentState = state()
            val allMeme = packRepository.memePacks.value
            val allSituation = packRepository.situationPacks.value

            val newExtraMemePacks = (currentState.extraMemePacks + intent.extraMemePacks + allMeme.filter { it.id in intent.memePackIds })
                .distinctBy { it.id }
            val newExtraSituationPacks = (currentState.extraSituationPacks + intent.extraSituationPacks + allSituation.filter { it.id in intent.situationPackIds })
                .distinctBy { it.id }

            // If user selected a Meme pack in catalog, switch selection to it; otherwise keep current selection.
            val updatedMemeIds = if (intent.memePackIds.isNotEmpty()) {
                intent.memePackIds
            } else {
                currentState.selectedMemePackIds
            }

            // If user selected a Situation pack in catalog, switch selection to it; otherwise keep current selection.
            val updatedSituationIds = if (intent.situationPackIds.isNotEmpty()) {
                intent.situationPackIds
            } else {
                currentState.selectedSituationPackIds
            }

            dispatch(
                Msg.AddPacksFromPicker(
                    extraMemePacks = newExtraMemePacks,
                    extraSituationPacks = newExtraSituationPacks,
                    newMemeIds = updatedMemeIds,
                    newSituationIds = updatedSituationIds
                )
            )
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
                        dispatch(Msg.SetError(result.error.toString()))
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
                is Msg.SetError -> copy(error = msg.error)
                is Msg.UpdateOfficialMemePacks -> copy(officialMemePacks = msg.packs)
                is Msg.UpdateOfficialSituationPacks -> copy(officialSituationPacks = msg.packs)
                is Msg.ToggleMemePack -> copy(selectedMemePackIds = setOf(msg.id))
                is Msg.ToggleSituationPack -> copy(selectedSituationPackIds = setOf(msg.id))
                is Msg.SetMode -> copy(mode = msg.mode)
                is Msg.SetMaxRounds -> copy(maxRounds = msg.rounds)
                is Msg.SetHandSize -> copy(handSize = msg.size)
                is Msg.UpdateLobbyNameInput -> copy(lobbyNameInput = msg.name)
                is Msg.UpdateHandleInput -> copy(handleInput = msg.handle)
                is Msg.AddPacksFromPicker -> copy(
                    extraMemePacks = msg.extraMemePacks,
                    extraSituationPacks = msg.extraSituationPacks,
                    selectedMemePackIds = msg.newMemeIds,
                    selectedSituationPackIds = msg.newSituationIds
                )
            }
    }
}
