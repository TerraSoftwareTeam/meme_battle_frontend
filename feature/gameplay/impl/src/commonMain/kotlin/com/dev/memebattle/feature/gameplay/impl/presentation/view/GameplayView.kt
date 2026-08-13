package com.dev.memebattle.feature.gameplay.impl.presentation.view

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.dev.memebattle.feature.gameplay.impl.presentation.component.GameplayComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.component.GameplayComponentImpl
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStore
import com.dev.memebattle.feature.gameplay.impl.presentation.store.info.GameplayInfoStore
import com.dev.memebattle.feature.gameplay.impl.presentation.view.game.GameplayGameScreen
import com.dev.memebattle.feature.gameplay.impl.presentation.view.info.GameplayInfoScreen
import com.dev.memebattle.feature.gameplay.impl.presentation.view.players.GameplayPlayersScreen
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Корневой View для всей геймплейной фичи.
 * Использует [BoxWithConstraints] для авто-определения категории ширины экрана ([WindowWidthClass]).
 */
@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun GameplayView(
    component: GameplayComponent,
    overrideWindowWidthClass: WindowWidthClass? = null,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val windowWidthClass = overrideWindowWidthClass ?: when {
            maxWidth >= 1100.dp -> WindowWidthClass.LARGE
            maxWidth >= 700.dp  -> WindowWidthClass.MEDIUM
            else                -> WindowWidthClass.SMALL
        }

        val panels by component.panels.subscribeAsState()

        val gameComponent = panels.main.instance
        val infoComponent = panels.details?.instance
        val playersComponent = panels.extra?.instance

        // Сигнал закрытия панелей при GameStarted
        val signalFlow = (component as? GameplayComponentImpl)?.closePanelsSignal
            ?: MutableStateFlow(0)
        val closePanelsSignal by signalFlow.collectAsState()

        // Резолвер handle — приоритет: кеш ComponentImpl → PlayersStore live state
        val getPlayerHandle: (String) -> String? = { userId ->
            (component as? GameplayComponentImpl)?.playerHandleCache?.get(userId)
                ?: playersComponent?.state?.value?.players
                    ?.firstOrNull { it.userId == userId }
                    ?.handle
                    ?.takeIf { it.isNotBlank() }
        }

        // Общие состояния используемые в нескольких слотах
        val infoState = infoComponent?.state?.collectAsState()?.value
        val lobbyPlayersState = playersComponent?.state?.collectAsState()?.value
        val gameState = gameComponent?.state?.collectAsState()?.value
        val myUserId = lobbyPlayersState?.players?.find { it.isMe }?.userId ?: ""
        val amIReady = infoState?.amIReady ?: false
        val uiPhase = gameState?.uiPhase ?: GameplayGameStore.UiPhase.Lobby
        val hasVoted = gameState?.hasVoted ?: false
        val isVoting = gameState?.isVoting ?: false

        GameplayAdaptiveLayout(
            windowWidthClass = windowWidthClass,
            closePanelsSignal = closePanelsSignal,
            gameContent = {
                gameComponent?.let { gameCtx ->
                    GameplayGameScreen(
                        component = gameCtx,
                        myUserId = myUserId,
                        lobbyPlayersState = lobbyPlayersState,
                        infoState = infoState,
                        onToggleReady = {
                            infoComponent?.onIntent(GameplayInfoStore.Intent.SetReady(!amIReady))
                        },
                        getPlayerHandle = getPlayerHandle,
                    )
                }
            },
            infoContent = {
                infoComponent?.let { infoCtx ->
                    GameplayInfoScreen(
                        component = infoCtx,
                        lobbyPlayersState = lobbyPlayersState,
                        myUserId = myUserId,
                    )
                }
            },
            playersContent = {
                playersComponent?.let {
                    GameplayPlayersScreen(
                        component = it,
                        uiPhase = uiPhase,
                        hasVoted = hasVoted,
                        isVoting = isVoting,
                    )
                }
            },
        )
    }
}
