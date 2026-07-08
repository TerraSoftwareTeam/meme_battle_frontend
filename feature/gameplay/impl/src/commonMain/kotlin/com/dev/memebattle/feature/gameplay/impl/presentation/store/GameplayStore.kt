package com.dev.memebattle.feature.gameplay.impl.presentation.store

import com.arkivanov.mvikotlin.core.store.Store

interface GameplayStore : Store<GameplayStore.Intent, GameplayStore.State, GameplayStore.Effect> {
    sealed interface Intent { data object Init : Intent }
    data class State(val isLoading: Boolean = false)
    sealed interface Effect { data object NavigateBack : Effect }
}
