package com.dev.memebattle.feature.home.impl.presentation.store

import com.arkivanov.mvikotlin.core.store.Store

interface HomeStore : Store<HomeStore.Intent, HomeStore.State, HomeStore.Effect> {
    sealed interface Intent {
        data object Init : Intent
        data object OnPlayClicked : Intent
        data object OnStoreClicked : Intent
    }
    data class State(val isLoading: Boolean = false)
    sealed interface Effect {
        data object NavigateToPlay : Effect
        data object NavigateToStore : Effect
    }
}
