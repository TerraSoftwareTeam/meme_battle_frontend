package com.dev.memebattle.feature.gameplay.impl.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.dev.memebattle.feature.gameplay.impl.presentation.component.GameplayComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.view.game.GameplayGameScreen
import com.dev.memebattle.feature.gameplay.impl.presentation.view.info.GameplayInfoScreen
import com.dev.memebattle.feature.gameplay.impl.presentation.view.players.GameplayPlayersScreen

private val SIDE_PANEL_WIDTH = 280.dp
private val PLAYERS_PANEL_WIDTH = 260.dp

/**
 * Адаптивный Layout для игрового экрана.
 *
 * Три режима задаются через [windowWidthClass]:
 * - Large  (≥ 1200dp): все три панели одновременно
 * - Medium (≥ 600dp) : GameScreen по центру + шторки Info/Players по краям
 * - Small  (< 600dp) : только GameScreen; боковая панель выезжает из-за правого края
 *
 * @param gameContent    слот для GameScreen
 * @param infoContent    слот для InfoScreen
 * @param playersContent слот для PlayersScreen
 * @param windowWidthClass пиксельная ширина контейнера (передаётся из WindowSizeClass)
 */
@Composable
fun GameplayAdaptiveLayout(
    gameContent: @Composable () -> Unit,
    infoContent: @Composable () -> Unit,
    playersContent: @Composable () -> Unit,
    windowWidthClass: WindowWidthClass,
    modifier: Modifier = Modifier,
) {
    when (windowWidthClass) {
        WindowWidthClass.LARGE -> GameplayLayoutLarge(
            gameContent = gameContent,
            infoContent = infoContent,
            playersContent = playersContent,
            modifier = modifier,
        )
        WindowWidthClass.MEDIUM -> GameplayLayoutMedium(
            gameContent = gameContent,
            infoContent = infoContent,
            playersContent = playersContent,
            modifier = modifier,
        )
        WindowWidthClass.SMALL -> GameplayLayoutSmall(
            gameContent = gameContent,
            infoContent = infoContent,
            playersContent = playersContent,
            modifier = modifier,
        )
    }
}

enum class WindowWidthClass { SMALL, MEDIUM, LARGE }

// ── Large ────────────────────────────────────────────────────────────────────

@Composable
private fun GameplayLayoutLarge(
    gameContent: @Composable () -> Unit,
    infoContent: @Composable () -> Unit,
    playersContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxSize()) {
        Box(Modifier.width(PLAYERS_PANEL_WIDTH).fillMaxHeight()) { playersContent() }
        Box(Modifier.weight(1f).fillMaxHeight()) { gameContent() }
        Box(Modifier.width(SIDE_PANEL_WIDTH).fillMaxHeight()) { infoContent() }
    }
}

// ── Medium ───────────────────────────────────────────────────────────────────

@Composable
private fun GameplayLayoutMedium(
    gameContent: @Composable () -> Unit,
    infoContent: @Composable () -> Unit,
    playersContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    var sidePanelsVisible by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize()) {
            // Левая шторка — Players
            AnimatedVisibility(
                visible = sidePanelsVisible,
                enter = slideInHorizontally { -it },
                exit = slideOutHorizontally { -it },
            ) {
                Box(Modifier.width(PLAYERS_PANEL_WIDTH).fillMaxHeight()) { playersContent() }
            }

            // Центр — Game (занимает всё оставшееся)
            Box(Modifier.weight(1f).fillMaxHeight()) {
                gameContent()
                // Кнопка toggle шторок — вверху по центру
                GameplayPanelToggleButton(
                    isOpen = sidePanelsVisible,
                    onToggle = { sidePanelsVisible = !sidePanelsVisible },
                )
            }

            // Правая шторка — Info
            AnimatedVisibility(
                visible = sidePanelsVisible,
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it },
            ) {
                Box(Modifier.width(SIDE_PANEL_WIDTH).fillMaxHeight()) { infoContent() }
            }
        }
    }
}

// ── Small ────────────────────────────────────────────────────────────────────

@Composable
private fun GameplayLayoutSmall(
    gameContent: @Composable () -> Unit,
    infoContent: @Composable () -> Unit,
    playersContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    var drawerVisible by remember { mutableStateOf(false) }
    var drawerTab by remember { mutableStateOf(SideDrawerTab.PLAYERS) }

    Box(modifier.fillMaxSize()) {
        // Основной экран
        Box(Modifier.fillMaxSize()) {
            gameContent()
            GameplaySmallTopBar(
                onOpenPlayers = { drawerTab = SideDrawerTab.PLAYERS; drawerVisible = true },
                onOpenInfo = { drawerTab = SideDrawerTab.INFO; drawerVisible = true },
            )
        }

        // Боковая панель — выезжает поверх с правой стороны
        AnimatedVisibility(
            visible = drawerVisible,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it },
        ) {
            GameplaySideDrawer(
                activeTab = drawerTab,
                onTabChange = { drawerTab = it },
                onClose = { drawerVisible = false },
                playersContent = playersContent,
                infoContent = infoContent,
            )
        }
    }
}
