package com.dev.memebattle.feature.home.impl.presentation.store

import com.arkivanov.mvikotlin.core.store.Store

interface HomeStore : Store<HomeStore.Intent, HomeStore.State, HomeStore.Effect> {
    sealed interface Intent { data object Init : Intent }
    data class State(val isLoading: Boolean = false)
    sealed interface Effect { data object NavigateBack : Effect }
}
