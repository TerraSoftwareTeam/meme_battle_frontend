package com.dev.memebattle.feature.home.impl.presentation.store.menu

import com.arkivanov.mvikotlin.core.store.Store

import com.dev.network.game.current.dto.ws.LobbyEvent

interface HomeMenuStore : Store<HomeMenuStore.Intent, HomeMenuStore.State, HomeMenuStore.Effect> {
    sealed interface Intent {
        data object OnPlayClicked : Intent
        data object OnCloseLobbiesClicked : Intent
        data object OnStoreClicked : Intent
        data object OnCreateLobbyClicked : Intent
        data class OnJoinLobbyClicked(val gameId: String) : Intent
        data class UpdateJoinHandleInput(val handle: String) : Intent
        data object ConfirmJoin : Intent
        data object CancelJoin : Intent
    }
    data class State(
        val isLoading: Boolean = false,
        val isLobbyListVisible: Boolean = false,
        val lobbies: List<LobbyEvent.LobbyCreated> = emptyList(),
        val joinGameId: String? = null,
        val joinHandleInput: String = "",
        val isJoining: Boolean = false,
        val joinError: String? = null
    )
    sealed interface Effect
}
