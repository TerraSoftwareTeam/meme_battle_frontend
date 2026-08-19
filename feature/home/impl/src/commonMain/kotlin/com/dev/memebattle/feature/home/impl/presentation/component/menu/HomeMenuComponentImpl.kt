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
import com.dev.memebattle.feature.home.impl.presentation.store.menu.HomeMenuStore
import com.dev.memebattle.feature.home.impl.presentation.store.menu.HomeMenuStoreFactory
import com.dev.network.game.current.api.GameApiService
import com.dev.network.game.current.api.ws.GameSocketService

class HomeMenuComponentImpl(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val gameSocketService: GameSocketService,
    private val gameApiService: GameApiService,
    private val onNavigateToCreateLobby: () -> Unit,
    private val onNavigateToStore: () -> Unit,
    private val onNavigateToGame: (String) -> Unit,
    /** Если задан (из диплинка) — авто-откроет Join диалог при старте */
    private val initialLobbyId: String? = null,
) : HomeMenuComponent, ComponentContext by componentContext {
    
    private val scope = coroutineScope()
    private val store = HomeMenuStoreFactory(
        storeFactory = storeFactory,
        gameSocketService = gameSocketService,
        gameApiService = gameApiService,
        onNavigateToCreateLobby = onNavigateToCreateLobby,
        onNavigateToStore = onNavigateToStore,
        onNavigateToGame = onNavigateToGame,
    ).create()

    private val labelsFlow = store.labels.shareIn(scope, SharingStarted.Eagerly, replay = 0)
    override val effects: SharedFlow<HomeMenuStore.Effect> = labelsFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<HomeMenuStore.State> = store.stateFlow(scope)

    init {
        // Deep link: если пришли по ссылке /lobby/{id} — сразу открываем диалог Join
        if (initialLobbyId != null) {
            // Сначала запускаем загрузку лобби (для отображения списка), потом показываем диалог
            store.accept(HomeMenuStore.Intent.OnPlayClicked)
            store.accept(HomeMenuStore.Intent.OnJoinLobbyClicked(initialLobbyId))
        }
    }

    override fun onIntent(intent: HomeMenuStore.Intent) = store.accept(intent)
}
