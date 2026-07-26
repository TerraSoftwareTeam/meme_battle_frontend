package com.dev.memebattle.feature.home.impl.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.router.panels.Panels
import com.arkivanov.decompose.router.panels.PanelsNavigation
import com.arkivanov.decompose.router.panels.activateDetails
import com.arkivanov.decompose.router.panels.childPanels
import com.arkivanov.decompose.router.panels.dismissDetails
import com.arkivanov.decompose.router.panels.setMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.dev.memebattle.core.navigation.output.NavigationOutput
import com.dev.memebattle.feature.gameplay.api.route.GameplayRoute
import com.dev.memebattle.feature.packs.api.route.PacksRoute
import com.dev.memebattle.feature.home.impl.presentation.component.menu.HomeMenuComponent
import com.dev.memebattle.feature.home.impl.presentation.component.menu.HomeMenuComponentImpl
import com.dev.memebattle.feature.home.impl.presentation.component.create.CreateLobbyComponent
import com.dev.memebattle.feature.home.impl.presentation.component.create.CreateLobbyComponentImpl
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Serializable

import com.dev.network.game.current.api.ws.GameSocketService

class HomeComponentImpl(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val gameSocketService: GameSocketService
) : HomeComponent, ComponentContext by componentContext {

    private val _output = MutableSharedFlow<NavigationOutput>(extraBufferCapacity = 64)
    override val output: Flow<NavigationOutput> = _output.asSharedFlow()

    @Serializable
    sealed interface MainConfig {
        @Serializable
        data object Menu : MainConfig
    }

    @Serializable
    sealed interface DetailsConfig {
        @Serializable
        data object CreateLobby : DetailsConfig
    }

    @OptIn(ExperimentalDecomposeApi::class)
    private val panelsNavigation = PanelsNavigation<MainConfig, DetailsConfig, Nothing>()

    @OptIn(ExperimentalDecomposeApi::class)
    override val panels: Value<ChildPanels<MainConfig, HomeMenuComponent, DetailsConfig, CreateLobbyComponent, Nothing, Nothing>> =
        childPanels(
            source = panelsNavigation,
            serializers = Triple(
                MainConfig.serializer(),
                DetailsConfig.serializer(),
                kotlinx.serialization.builtins.NothingSerializer()
            ),
            initialPanels = { Panels(main = MainConfig.Menu) },
            handleBackButton = true,
            mainFactory = { config, context ->
                when (config) {
                    is MainConfig.Menu -> HomeMenuComponentImpl(
                        componentContext = context,
                        storeFactory = storeFactory,
                        gameSocketService = gameSocketService,
                        onNavigateToCreateLobby = {
                            panelsNavigation.activateDetails(DetailsConfig.CreateLobby)
                        },
                        onNavigateToStore = {
                            _output.tryEmit(NavigationOutput.NavigateTo(PacksRoute))
                        }
                    )
                }
            },
            detailsFactory = { config, context ->
                when (config) {
                    is DetailsConfig.CreateLobby -> CreateLobbyComponentImpl(
                        componentContext = context,
                        onCloseClicked = {
                            panelsNavigation.dismissDetails()
                        },
                        onGameCreatedCallback = { gameId ->
                            _output.tryEmit(NavigationOutput.NavigateTo(GameplayRoute(gameId = gameId)))
                        }
                    )
                }
            },
            extraFactory = { _, _ -> error("Not supported") }
        )

    @OptIn(ExperimentalDecomposeApi::class)
    override fun setAdaptiveMode(mode: ChildPanelsMode) {
        panelsNavigation.setMode(mode)
    }
}
