package com.dev.memebattle.feature.packs.impl.presentation.store

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.SituationPack

interface PacksStore : Store<PacksStore.Intent, PacksStore.State, PacksStore.Effect> {

    sealed interface Intent {
        data object Init : Intent
        data object RefreshMemePacks : Intent
        data object RefreshSituationPacks : Intent
        data class DeleteMemePack(val id: String) : Intent
        data class DeleteSituationPack(val id: String) : Intent
    }

    data class State(
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val memePacks: List<MemePack> = emptyList(),
        val situationPacks: List<SituationPack> = emptyList(),
        val error: String? = null,
    )

    sealed interface Effect {
        data object NavigateBack : Effect
        data class ShowError(val message: String) : Effect
    }
}

