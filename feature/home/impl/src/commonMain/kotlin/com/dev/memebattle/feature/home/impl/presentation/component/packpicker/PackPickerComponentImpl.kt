package com.dev.memebattle.feature.home.impl.presentation.component.packpicker

import com.arkivanov.decompose.ComponentContext
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.core.domain.packs.repository.PackRepository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class PackPickerComponentImpl(
    componentContext: ComponentContext,
    /** Already-selected meme pack IDs from CreateLobby (to pre-highlight them). */
    private val initialSelectedMemeIds: Set<String> = emptySet(),
    /** Already-selected situation pack IDs from CreateLobby. */
    private val initialSelectedSituationIds: Set<String> = emptySet(),
    private val onConfirmed: (memeIds: Set<String>, situationIds: Set<String>,
                              extraMeme: List<MemePack>, extraSituation: List<SituationPack>) -> Unit,
    private val onClosed: () -> Unit,
    private val onNavigateToStore: (() -> Unit) = {},
) : PackPickerComponent, ComponentContext by componentContext, KoinComponent {

    private val packRepository: PackRepository = get()
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(
        PackPickerState(
            selectedMemePackIds = emptySet(),
            selectedSituationPackIds = emptySet(),
        )
    )
    override val state: StateFlow<PackPickerState> = _state.asStateFlow()

    init {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            packRepository.refreshMemePacks()
            packRepository.refreshSituationPacks()
        }

        scope.launch {
            packRepository.memePacks.collect { packs ->
                _state.update { it.copy(memePacks = packs, isLoading = false) }
            }
        }

        scope.launch {
            packRepository.situationPacks.collect { packs ->
                _state.update { it.copy(situationPacks = packs, isLoading = false) }
            }
        }
    }

    override fun onSwitchTab(tab: PackPickerTab) {
        _state.update { it.copy(activeTab = tab) }
    }

    override fun onTogglePack(id: String) {
        val current = _state.value
        val isMeme = current.memePacks.any { it.id == id }
        if (isMeme) {
            _state.update {
                val newSet = if (it.selectedMemePackIds.contains(id)) emptySet() else setOf(id)
                it.copy(selectedMemePackIds = newSet)
            }
        } else {
            _state.update {
                val newSet = if (it.selectedSituationPackIds.contains(id)) emptySet() else setOf(id)
                it.copy(selectedSituationPackIds = newSet)
            }
        }
    }

    override fun onConfirm() {
        val s = _state.value
        val allMeme = packRepository.memePacks.value
        val allSituation = packRepository.situationPacks.value

        val extraMeme = allMeme.filter { it.id in s.selectedMemePackIds }
        val extraSituation = allSituation.filter { it.id in s.selectedSituationPackIds }

        onConfirmed(s.selectedMemePackIds, s.selectedSituationPackIds, extraMeme, extraSituation)
    }

    override fun onClose() = onClosed()

    override fun onViewInStore(packId: String, isMeme: Boolean) {
        onNavigateToStore()
    }

    override fun onRefresh() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            packRepository.refreshMemePacks()
            packRepository.refreshSituationPacks()
        }
    }
}
