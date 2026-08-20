package com.dev.memebattle.feature.home.impl.presentation.component.packpicker

import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.SituationPack
import kotlinx.coroutines.flow.StateFlow

/**
 * Component for the in-lobby pack picker screen.
 * Shows all available meme/situation packs from the catalog for selection.
 */
interface PackPickerComponent {
    val state: StateFlow<PackPickerState>

    fun onSwitchTab(tab: PackPickerTab)
    fun onTogglePack(id: String)
    fun onConfirm()
    fun onClose()
    fun onViewInStore(packId: String, isMeme: Boolean)
    fun onRefresh()
}

enum class PackPickerTab { MEMES, SITUATIONS }

data class PackPickerState(
    val activeTab: PackPickerTab = PackPickerTab.MEMES,
    val isLoading: Boolean = false,
    val memePacks: List<MemePack> = emptyList(),
    val situationPacks: List<SituationPack> = emptyList(),
    val selectedMemePackIds: Set<String> = emptySet(),
    val selectedSituationPackIds: Set<String> = emptySet(),
) {
    val totalSelectedCount: Int
        get() = selectedMemePackIds.size + selectedSituationPackIds.size

    fun isSelected(packId: String): Boolean =
        packId in selectedMemePackIds || packId in selectedSituationPackIds
}
