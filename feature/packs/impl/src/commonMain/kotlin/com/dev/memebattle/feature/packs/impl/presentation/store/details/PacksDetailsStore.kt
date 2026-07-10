package com.dev.memebattle.feature.packs.impl.presentation.store.details

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.SituationPack

interface PacksDetailsStore : Store<PacksDetailsStore.Intent, PacksDetailsStore.State, PacksDetailsStore.Effect> {

    sealed interface Intent {
        data class Load(val packId: String) : Intent
        data object Close : Intent
        // TODO: добавить интенты редактирования
    }

    data class State(
        val isLoading: Boolean = false,
        val packId: String? = null,
        // TODO: добавить модель деталей пака
    )

    sealed interface Effect {
        data object NavigateBack : Effect
    }
}
