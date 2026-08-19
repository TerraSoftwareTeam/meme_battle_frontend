package com.dev.memebattle.feature.home.impl.presentation.store.auth

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.memebattle.feature.home.impl.domain.UserIdentity

interface AuthStore : Store<AuthStore.Intent, AuthStore.State, AuthStore.Effect> {

    sealed interface Intent {
        // --- Login / Register form ---
        data class UpdateLoginUsername(val value: String) : Intent
        data class UpdateLoginPassword(val value: String) : Intent
        data object SubmitLogin : Intent
        data object SubmitRegister : Intent

        // --- Guest username ---
        data class UpdateGuestUsername(val value: String) : Intent
        /** Calls authAsGuest(username) — creates a new guest session with a chosen name. */
        data object ConfirmGuestUsername : Intent
        /** Calls authAsGuest(null) — immediately starts anonymous guest session. */
        data object ContinueAsGuest : Intent

        // --- Common ---
        data object DismissError : Intent
        data object LogOut : Intent
        /** Re-fetches /user/me to refresh profile data (e.g. after username update). */
        data object RefreshProfile : Intent
    }

    data class State(
        val identity: UserIdentity = UserIdentity.Unknown,
        val isLoading: Boolean = false,
        val error: String? = null,
        // Login / Register form fields
        val loginUsername: String = "",
        val loginPassword: String = "",
        // Guest name input
        val guestUsernameInput: String = "",
    )

    sealed interface Effect {
        /** Authentication succeeded — UI should close the auth sheet. */
        data object AuthSuccess : Effect
        /** User logged out — identity is now Unknown/Guest. */
        data object LoggedOut : Effect
    }
}
