package com.dev.memebattle.feature.packs.impl.presentation.store

import com.arkivanov.mvikotlin.core.store.Store

interface PacksStore : Store<PacksStore.Intent, PacksStore.State, PacksStore.Effect> {
    sealed interface Intent { data object Init : Intent }
    data class State(val isLoading: Boolean = false)
    sealed interface Effect { data object NavigateBack : Effect }
}
