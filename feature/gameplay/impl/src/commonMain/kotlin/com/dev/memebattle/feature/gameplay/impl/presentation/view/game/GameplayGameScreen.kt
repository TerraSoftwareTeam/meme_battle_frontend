package com.dev.memebattle.feature.gameplay.impl.presentation.view.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.feature.gameplay.impl.presentation.component.game.GameplayGameComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStore
import com.dev.memebattle.feature.gameplay.impl.presentation.store.info.GameplayInfoStore
import com.dev.memebattle.feature.gameplay.impl.presentation.store.players.GameplayPlayersStore
import com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets.GameFinishedContent
import com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets.LobbyContent
import com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets.PhaseTimerHud
import com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets.RoundResultOverlay
import com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets.SubmittingContent
import com.dev.memebattle.feature.gameplay.impl.presentation.view.game.widgets.VotingContent
import kotlinx.coroutines.delay

import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.gameplay_connection_lost
import org.jetbrains.compose.resources.stringResource

/**
 * Корневой экран игры — маршрутизирует по [GameplayGameStore.UiPhase].
 */
@Composable
fun GameplayGameScreen(
    component: GameplayGameComponent,
    myUserId: String = "",
    isConnected: Boolean = true,
    lobbyPlayersState: GameplayPlayersStore.State? = null,
    infoState: GameplayInfoStore.State? = null,
    onToggleReady: () -> Unit = {},
    onStartGame: () -> Unit = {},
    /** Резолвер handle по userId — пробрасывается из ComponentImpl */
    getPlayerHandle: (String) -> String? = { null },
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Отслеживание ошибок от бэка / стора
    LaunchedEffect(component) {
        component.effects.collect { effect ->
            when (effect) {
                is GameplayGameStore.Effect.ShowError -> {
                    errorMessage = effect.message
                    delay(4000)
                    errorMessage = null
                }
                else -> Unit
            }
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E1035),
            Color(0xFF0F081D),
            Color(0xFF08040F),
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
                strokeWidth = 2.dp,
            )
            return@Box
        }

        Crossfade(
            targetState = state.uiPhase,
            animationSpec = tween(300),
            label = "uiPhase",
        ) { phase ->
            when (phase) {
                GameplayGameStore.UiPhase.Lobby -> {
                    val isHost = infoState?.isHost ?: false
                    val playersCount = lobbyPlayersState?.players?.size ?: infoState?.playerCount ?: 0
                    val readyCount = infoState?.readyCount ?: 0
                    val maxPlayers = infoState?.maxPlayers
                    val isTooManyPlayersError = infoState?.isTooManyPlayersError == true
                            || (infoState?.blockedAtPlayerCount != null && playersCount >= infoState.blockedAtPlayerCount!!)
                    val isMaxExceeded = isTooManyPlayersError || (maxPlayers != null && maxPlayers > 0 && playersCount > maxPlayers)

                    val canStartGame = isHost
                            && playersCount >= 3
                            && !isMaxExceeded
                            && readyCount == playersCount
                            && infoState?.isStartingGame != true

                    LobbyContent(
                        gameId = component.gameId,
                        players = lobbyPlayersState?.players ?: emptyList(),
                        readyCount = readyCount,
                        amIReady = infoState?.amIReady ?: false,
                        isSettingReady = infoState?.isSettingReady ?: false,
                        isHost = isHost,
                        canStartGame = canStartGame,
                        isStartingGame = infoState?.isStartingGame ?: false,
                        isTooManyPlayersError = isTooManyPlayersError,
                        maxPlayers = maxPlayers,
                        onToggleReady = onToggleReady,
                        onStartGame = onStartGame,
                    )
                }

                GameplayGameStore.UiPhase.Submitting -> SubmittingContent(
                    state = state,
                    onSelectCard = { component.onIntent(GameplayGameStore.Intent.SelectCard(it)) },
                    onSubmit = { component.onIntent(GameplayGameStore.Intent.Submit) },
                )

                GameplayGameStore.UiPhase.Voting -> VotingContent(
                    state = state,
                    onSelectSubmission = { component.onIntent(GameplayGameStore.Intent.SelectCard(it)) },
                    onVote = { submissionId ->
                        component.onIntent(GameplayGameStore.Intent.Vote(submissionId))
                    },
                )

                GameplayGameStore.UiPhase.RoundResult -> {
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
                    getPlayerHandle = getPlayerHandle,
                    onExit = { component.onIntent(GameplayGameStore.Intent.ExitGame) },
                )
            }
        }

        // ── Предупреждающие баннеры и ошибки (смещены под верхнюю панель/таймер) ──
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 56.dp)
                .widthIn(max = 500.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Баннер отсутствия WebSocket соединения
            AnimatedVisibility(
                visible = !isConnected,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFAB00).copy(alpha = 0.18f))
                        .border(1.dp, Color(0xFFFFAB00).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFAB00)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = stringResource(Res.string.gameplay_connection_lost),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFAB00),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // Баннер ошибки
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            ) {
                errorMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFD32F2F))
                            .border(1.dp, Color(0xFFFF6B6B), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFD32F2F),
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }

        // RoundResult оверлей поверх всего
        RoundResultOverlay(
            visible = state.uiPhase == GameplayGameStore.UiPhase.RoundResult,
            result = state.roundResult,
        )

        // Таймер раунда
        if (state.uiPhase != GameplayGameStore.UiPhase.GameFinished && state.uiPhase != GameplayGameStore.UiPhase.Lobby) {
            PhaseTimerHud(
                phaseExpiresAt = infoState?.phaseExpiresAt,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}
