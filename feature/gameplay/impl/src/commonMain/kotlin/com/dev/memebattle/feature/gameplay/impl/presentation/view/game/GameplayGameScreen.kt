package com.dev.memebattle.feature.gameplay.impl.presentation.view.game

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.dev.memebattle.feature.gameplay.impl.presentation.component.game.GameplayGameComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStore
import com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets.GameFinishedContent

import com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets.LobbyContent
import com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets.RoundResultOverlay
import com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets.SubmittingContent
import com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets.VotingContent

/**
 * Корневой экран игры — маршрутизирует по [GameplayGameStore.UiPhase].
 *
 * Содержит ТОЛЬКО `when(state.uiPhase)` — вся логика в Store.
 * Каждый под-экран — отдельный файл в пакете `widgets/`.
 *
 * @param myUserId передаётся в GameFinishedContent для выделения текущего игрока
 * @param lobbyPlayersState внешние данные для LobbyContent из PlayersStore (список + ready-статус)
 */
@Composable
fun GameplayGameScreen(
    component: GameplayGameComponent,
    myUserId: String = "",
    lobbyPlayersState: com.dev.memebattle.feature.gameplay.impl.presentation.store.players.GameplayPlayersStore.State? = null,
    infoState: com.dev.memebattle.feature.gameplay.impl.presentation.store.info.GameplayInfoStore.State? = null,
    onToggleReady: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E1035),
            Color(0xFF0F081D),
            Color(0xFF08040F)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF7C5DFA),
            )
            return@Box
        }

        Crossfade(
            targetState = state.uiPhase,
            animationSpec = tween(300),
            label = "uiPhase",
        ) { phase ->
            when (phase) {


                GameplayGameStore.UiPhase.Lobby -> LobbyContent(
                    players = lobbyPlayersState?.players ?: emptyList(),
                    readyCount = infoState?.readyCount ?: 0,
                    amIReady = infoState?.amIReady ?: false,
                    isSettingReady = infoState?.isSettingReady ?: false,
                    onToggleReady = onToggleReady,
                )

                GameplayGameStore.UiPhase.Submitting -> SubmittingContent(
                    state = state,
                    onSelectCard = { component.onIntent(GameplayGameStore.Intent.SelectCard(it)) },
                    onSubmit = { component.onIntent(GameplayGameStore.Intent.Submit) },
                )

                GameplayGameStore.UiPhase.Voting -> VotingContent(
                    state = state,
                    onSelectSubmission = { component.onIntent(GameplayGameStore.Intent.SelectCard(it)) },
                    onVote = { submissionId -> component.onIntent(GameplayGameStore.Intent.Vote(submissionId)) },
                )

                GameplayGameStore.UiPhase.RoundResult -> {
                    // Под фазой RoundResult продолжаем показывать submitting-layout + оверлей
                    SubmittingContent(
                        state = state,
                        onSelectCard = {},
                        onSubmit = {},
                    )
                }

                GameplayGameStore.UiPhase.GameFinished -> GameFinishedContent(
                    winnerUserId = state.gameWinnerUserId,
                    finalScoreboard = state.finalScoreboard,
                    myUserId = myUserId,
                    onExit = { component.onIntent(GameplayGameStore.Intent.ExitGame) },
                )
            }
        }

        // RoundResult оверлей поверх всего
        RoundResultOverlay(
            visible = state.uiPhase == GameplayGameStore.UiPhase.RoundResult,
            result = state.roundResult,
        )
    }
}
