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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
 * [closePanelsSignal] — целое число, инкрементируется при событии GameStarted.
 * При изменении — закрывает все открытые боковые панели.
 */
@Composable
fun GameplayAdaptiveLayout(
    gameContent: @Composable () -> Unit,
    infoContent: @Composable () -> Unit,
    playersContent: @Composable () -> Unit,
    windowWidthClass: WindowWidthClass,
    closePanelsSignal: Int = 0,
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
            closePanelsSignal = closePanelsSignal,
            modifier = modifier,
        )
        WindowWidthClass.SMALL -> GameplayLayoutSmall(
            gameContent = gameContent,
            infoContent = infoContent,
            playersContent = playersContent,
            closePanelsSignal = closePanelsSignal,
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
    // На большом экране панели всегда открыты — сигнал не нужен
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
    closePanelsSignal: Int,
    modifier: Modifier = Modifier,
) {
    var sidePanelsVisible by remember { mutableStateOf(false) }

    // Закрыть панели при GameStarted
    LaunchedEffect(closePanelsSignal) {
        if (closePanelsSignal > 0) sidePanelsVisible = false
    }

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

            // Центр — Game
            Box(Modifier.weight(1f).fillMaxHeight()) {
                gameContent()
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
    closePanelsSignal: Int,
    modifier: Modifier = Modifier,
) {
    var drawerVisible by remember { mutableStateOf(false) }
    var drawerTab by remember { mutableStateOf(SideDrawerTab.PLAYERS) }

    // Закрыть drawer при GameStarted
    LaunchedEffect(closePanelsSignal) {
        if (closePanelsSignal > 0) drawerVisible = false
    }

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
