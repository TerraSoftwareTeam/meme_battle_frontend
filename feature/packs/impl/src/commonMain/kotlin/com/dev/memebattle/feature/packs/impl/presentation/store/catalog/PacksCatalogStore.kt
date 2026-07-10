package com.dev.memebattle.feature.packs.impl.presentation.store.catalog

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.SituationPack

interface PacksCatalogStore : Store<PacksCatalogStore.Intent, PacksCatalogStore.State, PacksCatalogStore.Effect> {

    enum class PackType { Memes, Situations }

    sealed interface Intent {
        data object Init : Intent
        data object Refresh : Intent
        data class SwitchPackType(val type: PackType) : Intent
        data class OpenDetails(val packId: String) : Intent
        data object OpenCreate : Intent
        data object GoBack : Intent
    }

    data class State(
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val activeType: PackType = PackType.Memes,
        val memePacks: List<MemePack> = emptyList(),
        val situationPacks: List<SituationPack> = emptyList(),
        val error: String? = null,
    )

    sealed interface Effect {
        data class NavigateToDetails(val packId: String) : Effect
        data object NavigateToCreate : Effect
        data class ShowError(val message: String) : Effect
    }
}
