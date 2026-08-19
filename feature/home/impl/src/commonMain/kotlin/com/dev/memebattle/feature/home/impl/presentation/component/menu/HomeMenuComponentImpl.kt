package com.dev.memebattle.feature.home.impl.presentation.component.menu

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.dev.memebattle.core.network.auth.TokenStorage
import com.dev.memebattle.feature.home.impl.presentation.store.auth.AuthStore
import com.dev.memebattle.feature.home.impl.presentation.store.auth.AuthStoreFactory
import com.dev.memebattle.feature.home.impl.presentation.store.menu.HomeMenuStore
import com.dev.memebattle.feature.home.impl.presentation.store.menu.HomeMenuStoreFactory
import com.dev.network.game.current.api.GameApiService
import com.dev.network.game.current.api.ws.GameSocketService
import com.dev.network.user.current.api.UserApiService
import com.dev.network.user_auth.current.api.User_authApiService

class HomeMenuComponentImpl(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val gameSocketService: GameSocketService,
    private val gameApiService: GameApiService,
    private val tokenStorage: TokenStorage,
    private val userAuthService: User_authApiService,
    private val userApiService: UserApiService,
    private val onNavigateToCreateLobby: () -> Unit,
    private val onNavigateToStore: () -> Unit,
    private val onNavigateToGame: (String) -> Unit,
    /** Если задан (из диплинка) — авто-откроет Join диалог при старте */
    private val initialLobbyId: String? = null,
) : HomeMenuComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()

    // --- Lobby store (existing) ---
    private val menuStore = HomeMenuStoreFactory(
        storeFactory = storeFactory,
        gameSocketService = gameSocketService,
        gameApiService = gameApiService,
        onNavigateToCreateLobby = onNavigateToCreateLobby,
        onNavigateToStore = onNavigateToStore,
        onNavigateToGame = onNavigateToGame,
    ).create()

    private val menuLabelsFlow = menuStore.labels.shareIn(scope, SharingStarted.Eagerly, replay = 0)
    override val effects: SharedFlow<HomeMenuStore.Effect> = menuLabelsFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<HomeMenuStore.State> = menuStore.stateFlow(scope)

    // --- Auth store (new) ---
    private val authStore = AuthStoreFactory(
        storeFactory = storeFactory,
        tokenStorage = tokenStorage,
        userAuthService = userAuthService,
        userApiService = userApiService,
    ).create()

    private val authLabelsFlow = authStore.labels.shareIn(scope, SharingStarted.Eagerly, replay = 0)
    override val authEffects: SharedFlow<AuthStore.Effect> = authLabelsFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    override val authState: StateFlow<AuthStore.State> = authStore.stateFlow(scope)

    init {
        // Deep link: если пришли по ссылке /lobby/{id} — сразу открываем диалог Join
        if (initialLobbyId != null) {
            menuStore.accept(HomeMenuStore.Intent.OnPlayClicked)
            menuStore.accept(HomeMenuStore.Intent.OnJoinLobbyClicked(initialLobbyId))
        }
    }

    override fun onIntent(intent: HomeMenuStore.Intent) = menuStore.accept(intent)
    override fun onAuthIntent(intent: AuthStore.Intent) = authStore.accept(intent)
}
