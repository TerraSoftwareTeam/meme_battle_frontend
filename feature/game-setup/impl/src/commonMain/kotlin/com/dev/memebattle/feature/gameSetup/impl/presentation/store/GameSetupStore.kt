package com.dev.memebattle.feature.gameSetup.impl.presentation.store

import com.arkivanov.mvikotlin.core.store.Store

interface GameSetupStore : Store<GameSetupStore.Intent, GameSetupStore.State, GameSetupStore.Effect> {
    sealed interface Intent { data object Init : Intent }
    data class State(val isLoading: Boolean = false)
    sealed interface Effect { data object NavigateBack : Effect }
}
