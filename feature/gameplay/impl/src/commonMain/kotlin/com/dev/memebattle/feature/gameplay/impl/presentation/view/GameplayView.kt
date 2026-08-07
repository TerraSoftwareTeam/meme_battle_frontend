package com.dev.memebattle.feature.gameplay.impl.presentation.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.dev.memebattle.feature.gameplay.impl.presentation.component.GameplayComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.view.game.GameplayGameScreen
import com.dev.memebattle.feature.gameplay.impl.presentation.view.info.GameplayInfoScreen
import com.dev.memebattle.feature.gameplay.impl.presentation.view.players.GameplayPlayersScreen

/**
 * Корневой View для всей геймплейной фичи.
 *
 * Получает состояния всех трёх компонентов и делегирует рендеринг
 * в [GameplayAdaptiveLayout], который самостоятельно выбирает компоновку
 * на основе [WindowWidthClass].
 *
 * Сам по себе является тонким слоем без бизнес-логики:
 * он только извлекает компоненты из ChildPanels и передаёт слоты-лямбды.
 */
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun GameplayView(
    component: GameplayComponent,
    windowWidthClass: WindowWidthClass = WindowWidthClass.SMALL, // TODO: определять через WindowSizeClass
    modifier: Modifier = Modifier,
) {
    val panels by component.panels.subscribeAsState()

    val gameComponent = panels.main.instance
    val infoComponent = panels.details?.instance
    val playersComponent = panels.extra?.instance

    GameplayAdaptiveLayout(
        windowWidthClass = windowWidthClass,
        gameContent = {
            gameComponent?.let { gameCtx ->
                val infoState = infoComponent?.state?.collectAsState()?.value
                val lobbyPlayersState = playersComponent?.state?.collectAsState()?.value
                val myUserId = lobbyPlayersState?.players?.find { it.isMe }?.userId ?: ""
                val amIReady = infoState?.amIReady ?: false
                
                GameplayGameScreen(
                    component = gameCtx,
                    myUserId = myUserId,
                    lobbyPlayersState = lobbyPlayersState,
                    infoState = infoState,
                    onToggleReady = {
                        infoComponent?.onIntent(com.dev.memebattle.feature.gameplay.impl.presentation.store.info.GameplayInfoStore.Intent.SetReady(!amIReady))
                    }
                )
            }
        },
        infoContent = {
            infoComponent?.let { infoCtx ->
                val lobbyPlayersState = playersComponent?.state?.collectAsState()?.value
                val myUserId = lobbyPlayersState?.players?.find { it.isMe }?.userId ?: ""
                GameplayInfoScreen(
                    component = infoCtx,
                    lobbyPlayersState = lobbyPlayersState,
                    myUserId = myUserId
                )
            }
        },
        playersContent = {
            playersComponent?.let { GameplayPlayersScreen(it) }
        },
        modifier = modifier,
    )
}
