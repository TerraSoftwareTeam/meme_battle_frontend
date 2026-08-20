package com.dev.memebattle.feature.home.impl.presentation.store.auth

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.core.utils.ExperimentalMviKotlinApi
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.dev.memebattle.core.network.auth.AuthOrigin
import com.dev.memebattle.core.network.auth.TokenStorage
import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.memebattle.core.network.error.NetworkError
import com.dev.memebattle.feature.home.impl.domain.UserIdentity
import com.dev.memebattle.feature.home.impl.domain.isAuthorized
import com.dev.network.user.current.api.UserApiService
import com.dev.network.user_auth.current.api.User_authApiService
import com.dev.network.user.current.dto.UpdateMeDto
import com.dev.network.user_auth.current.dto.AuthUserDto
import com.dev.network.user_auth.current.dto.GuestAuthDto
import com.dev.network.user_auth.current.dto.RegisterAuthUserDto
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AuthStoreFactory(
    private val storeFactory: StoreFactory,
    private val tokenStorage: TokenStorage,
    private val userAuthService: User_authApiService,
    private val userApiService: UserApiService,
) {
    @OptIn(ExperimentalMviKotlinApi::class)
    fun create(): AuthStore =
        object : AuthStore,
            Store<AuthStore.Intent, AuthStore.State, AuthStore.Effect> by storeFactory.create(
                name = "AuthStore",
                initialState = AuthStore.State(),
                bootstrapper = coroutineBootstrapper {
                    // Reacts to changes in tokenStorage.authOrigin reactively.
                    // This covers cases when AuthPlugin updates the token autonomously.
                    launch {
                        tokenStorage.authOrigin.collectLatest { origin ->
                            when (origin) {
                                AuthOrigin.NONE -> dispatch(Action.SetIdentity(UserIdentity.Unknown))
                                AuthOrigin.GUEST -> {
                                    dispatch(Action.LoadingStarted)
                                    val identity = tryLoadGuestProfile()
                                    dispatch(Action.SetIdentity(identity))
                                    dispatch(Action.LoadingFinished)
                                }
                                AuthOrigin.USER -> {
                                    dispatch(Action.LoadingStarted)
                                    val identity = tryLoadAuthorizedProfile()
                                    dispatch(Action.SetIdentity(identity))
                                    dispatch(Action.LoadingFinished)
                                }
                            }
                        }
                    }
                },
                executorFactory = ::ExecutorImpl,
                reducer = { msg: Msg ->
                    when (msg) {
                        is Msg.SetIdentity -> copy(identity = msg.identity, error = null)
                        is Msg.SetLoading -> copy(isLoading = msg.isLoading)
                        is Msg.SetError -> copy(error = msg.error, isLoading = false)
                        is Msg.UpdateLoginUsername -> copy(loginUsername = msg.value)
                        is Msg.UpdateLoginPassword -> copy(loginPassword = msg.value)
                        is Msg.UpdateGuestUsername -> copy(guestUsernameInput = msg.value)
                        Msg.ClearError -> copy(error = null)
                    }
                }
            ) {}

    // ──────────────────────────────────────────────
    // Bootstrapper helpers (run in coroutine scope)
    // ──────────────────────────────────────────────

    private suspend fun tryLoadAuthorizedProfile(): UserIdentity {
        return when (val result = userApiService.getMe()) {
            is NetworkResult.Success -> {
                val user = result.data
                if (user.is_guest) {
                    val customName = user.username.takeIf { it.isNotBlank() && !it.startsWith("player-") }
                    UserIdentity.Guest(name = customName)
                } else {
                    UserIdentity.Authorized(
                        id = user.id,
                        username = user.username
                    )
                }
            }
            is NetworkResult.Error -> {
                UserIdentity.Guest(name = null)
            }
        }
    }

    private suspend fun tryLoadGuestProfile(): UserIdentity {
        return when (val result = userApiService.getMe()) {
            is NetworkResult.Success -> {
                val user = result.data
                val customName = user.username.takeIf { it.isNotBlank() && !it.startsWith("player-") }
                UserIdentity.Guest(name = customName)
            }
            is NetworkResult.Error -> UserIdentity.Guest(name = null)
        }
    }

    // ──────────────────────────────────────────────
    // Internal messages
    // ──────────────────────────────────────────────

    private sealed interface Action {
        data class SetIdentity(val identity: UserIdentity) : Action
        data object LoadingStarted : Action
        data object LoadingFinished : Action
    }

    private sealed interface Msg {
        data class SetIdentity(val identity: UserIdentity) : Msg
        data class SetLoading(val isLoading: Boolean) : Msg
        data class SetError(val error: String) : Msg
        data class UpdateLoginUsername(val value: String) : Msg
        data class UpdateLoginPassword(val value: String) : Msg
        data class UpdateGuestUsername(val value: String) : Msg
        data object ClearError : Msg
    }

    // ──────────────────────────────────────────────
    // Executor
    // ──────────────────────────────────────────────

    private inner class ExecutorImpl :
        CoroutineExecutor<AuthStore.Intent, Action, AuthStore.State, Msg, AuthStore.Effect>() {

        override fun executeAction(action: Action) {
            when (action) {
                is Action.SetIdentity -> dispatch(Msg.SetIdentity(action.identity))
                Action.LoadingStarted -> dispatch(Msg.SetLoading(true))
                Action.LoadingFinished -> dispatch(Msg.SetLoading(false))
            }
        }

        override fun executeIntent(intent: AuthStore.Intent) {
            when (intent) {
                is AuthStore.Intent.UpdateLoginUsername ->
                    dispatch(Msg.UpdateLoginUsername(intent.value))

                is AuthStore.Intent.UpdateLoginPassword ->
                    dispatch(Msg.UpdateLoginPassword(intent.value))

                is AuthStore.Intent.UpdateGuestUsername ->
                    dispatch(Msg.UpdateGuestUsername(intent.value))

                AuthStore.Intent.DismissError ->
                    dispatch(Msg.ClearError)

                AuthStore.Intent.SubmitLogin -> login()
                AuthStore.Intent.SubmitRegister -> register()
                AuthStore.Intent.ConfirmGuestUsername -> confirmGuestUsername()
                AuthStore.Intent.ContinueAsGuest -> continueAsGuest()
                AuthStore.Intent.LogOut -> logOut()
                AuthStore.Intent.RefreshProfile -> refreshProfile()
            }
        }

        // --- Login ---

        private fun login() {
            val st = state()
            val username = st.loginUsername.trim()
            val password = st.loginPassword.trim()
            if (username.isBlank()) {
                dispatch(Msg.SetError("Username cannot be empty"))
                return
            }
            scope.launch {
                dispatch(Msg.SetLoading(true))
                dispatch(Msg.ClearError)
                val result = userAuthService.loginUser(
                    AuthUserDto(username = username, password = password.takeIf { it.isNotBlank() })
                )
                when (result) {
                    is NetworkResult.Success -> {
                        val body = result.data
                        tokenStorage.saveTokens(
                            accessToken = body.access_token,
                            refreshToken = body.refresh_token,
                            origin = AuthOrigin.USER
                        )
                        val profile = tryLoadAuthorizedProfile()
                        dispatch(Msg.SetIdentity(profile))
                        dispatch(Msg.SetLoading(false))
                        publish(AuthStore.Effect.AuthSuccess)
                    }
                    is NetworkResult.Error -> {
                        dispatch(Msg.SetError(result.error.toUserMessage()))
                    }
                }
            }
        }

        // --- Register ---

        private fun register() {
            val st = state()
            val username = st.loginUsername.trim()
            val password = st.loginPassword.trim()
            if (username.isBlank()) {
                dispatch(Msg.SetError("Username cannot be empty"))
                return
            }
            scope.launch {
                dispatch(Msg.SetLoading(true))
                dispatch(Msg.ClearError)

                val updateResult = userApiService.updateMe(
                    UpdateMeDto(
                        username = username,
                        password = password.takeIf { it.isNotBlank() }
                    )
                )
                when (updateResult) {
                    is NetworkResult.Success -> {
                        val profile = tryLoadAuthorizedProfile()
                        dispatch(Msg.SetIdentity(profile))
                        dispatch(Msg.SetLoading(false))
                        publish(AuthStore.Effect.AuthSuccess)
                    }
                    is NetworkResult.Error -> {
                        dispatch(Msg.SetError(updateResult.error.toUserMessage()))
                    }
                }
            }
        }

        // --- Guest ---

        private fun continueAsGuest() {
            scope.launch {
                dispatch(Msg.SetLoading(true))
                dispatch(Msg.ClearError)
                val result = userAuthService.authAsGuest(GuestAuthDto(username = null))
                when (result) {
                    is NetworkResult.Success -> {
                        val body = result.data
                        tokenStorage.saveTokens(
                            accessToken = body.access_token,
                            refreshToken = body.refresh_token,
                            origin = AuthOrigin.GUEST
                        )
                        dispatch(Msg.SetIdentity(UserIdentity.Guest(name = null)))
                        dispatch(Msg.SetLoading(false))
                        publish(AuthStore.Effect.AuthSuccess)
                    }
                    is NetworkResult.Error -> {
                        dispatch(Msg.SetError(result.error.toUserMessage()))
                    }
                }
            }
        }

        private fun confirmGuestUsername() {
            val st = state()
            val username = st.guestUsernameInput.trim().takeIf { it.isNotBlank() }
            scope.launch {
                dispatch(Msg.SetLoading(true))
                dispatch(Msg.ClearError)
                val result = userAuthService.authAsGuest(GuestAuthDto(username = username))
                when (result) {
                    is NetworkResult.Success -> {
                        val body = result.data
                        tokenStorage.saveTokens(
                            accessToken = body.access_token,
                            refreshToken = body.refresh_token,
                            origin = AuthOrigin.GUEST
                        )
                        dispatch(Msg.SetIdentity(UserIdentity.Guest(name = username)))
                        dispatch(Msg.SetLoading(false))
                        publish(AuthStore.Effect.AuthSuccess)
                    }
                    is NetworkResult.Error -> {
                        dispatch(Msg.SetError(result.error.toUserMessage()))
                    }
                }
            }
        }

        // --- LogOut ---

        private fun logOut() {
            scope.launch {
                dispatch(Msg.SetLoading(true))
                tokenStorage.clear()
                val result = userAuthService.authAsGuest(GuestAuthDto(username = null))
                when (result) {
                    is NetworkResult.Success -> {
                        val body = result.data
                        tokenStorage.saveTokens(
                            accessToken = body.access_token,
                            refreshToken = body.refresh_token,
                            origin = AuthOrigin.GUEST
                        )
                        dispatch(Msg.SetIdentity(UserIdentity.Guest(name = null)))
                    }
                    is NetworkResult.Error -> {
                        dispatch(Msg.SetIdentity(UserIdentity.Guest(name = null)))
                    }
                }
                dispatch(Msg.SetLoading(false))
                publish(AuthStore.Effect.LoggedOut)
            }
        }

        // --- Refresh profile ---

        private fun refreshProfile() {
            val st = state()
            if (!st.identity.isAuthorized) return
            scope.launch {
                dispatch(Msg.SetLoading(true))
                val identity = tryLoadAuthorizedProfile()
                dispatch(Msg.SetIdentity(identity))
                dispatch(Msg.SetLoading(false))
            }
        }
    }
}

private fun NetworkError.toUserMessage(): String = when (this) {
    is NetworkError.ApiException -> message ?: "Error $code"
    is NetworkError.ServerError -> "Server error ($code)"
    NetworkError.Unauthorized -> "Unauthorized"
    NetworkError.Forbidden -> "Access forbidden"
    NetworkError.NotFound -> "Resource not found"
    NetworkError.Timeout -> "Request timed out"
    NetworkError.NoInternet -> "No internet connection"
    NetworkError.Unknown -> "Unknown network error"
    is NetworkError.Exception -> cause.message ?: "An unexpected error occurred"
}
