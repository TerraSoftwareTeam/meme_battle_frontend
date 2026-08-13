package com.dev.memebattle.feature.gameplay.impl.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SIDE_PANEL_WIDTH = 300.dp
private val PLAYERS_PANEL_WIDTH = 280.dp

/**
 * Адаптивный Layout для игрового экрана.
 *
 * Три режима:
 * - Large  (≥ 1100dp): 3-колоночный интерфейс — Игроки слева, Игра по центру, Инфо справа (всегда открыты на веб/десктоп).
 * - Medium (700..1099dp): Игра по центру, панели выезжают по нажатию на кнопки сверху.
 * - Small  (< 700dp) : Игра на весь экран; нижняя выезжающая шторка для карточек Инфо/Игроки.
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

// ── Large (Desktop / Web ≥ 1100dp) ──────────────────────────────────────────

@Composable
private fun GameplayLayoutLarge(
    gameContent: @Composable () -> Unit,
    infoContent: @Composable () -> Unit,
    playersContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    // На большом экране боковые панели Игроки и Инфо расположены с двух сторон постоянным HUD
    Row(modifier = modifier.fillMaxSize()) {
        // Левая панель — Игроки
        Box(
            modifier = Modifier
                .width(PLAYERS_PANEL_WIDTH)
                .fillMaxHeight()
                .background(Color(0xFF140B2E))
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.08f)),
        ) {
            playersContent()
        }

        // Центр — Игровой стол
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            gameContent()
        }

        // Правая панель — Информация
        Box(
            modifier = Modifier
                .width(SIDE_PANEL_WIDTH)
                .fillMaxHeight()
                .background(Color(0xFF140B2E))
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.08f)),
        ) {
            infoContent()
        }
    }
}

// ── Medium (Планшеты / Небольшие окна 700..1099dp) ──────────────────────────

@Composable
private fun GameplayLayoutMedium(
    gameContent: @Composable () -> Unit,
    infoContent: @Composable () -> Unit,
    playersContent: @Composable () -> Unit,
    closePanelsSignal: Int,
    modifier: Modifier = Modifier,
) {
    var showPlayers by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }

    LaunchedEffect(closePanelsSignal) {
        if (closePanelsSignal > 0) {
            showPlayers = false
            showInfo = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        gameContent()

        GameplaySmallTopBar(
            onOpenPlayers = {
                showPlayers = !showPlayers
                showInfo = false
            },
            onOpenInfo = {
                showInfo = !showInfo
                showPlayers = false
            },
        )

        // Затеменение под панелями при открытии
        if (showPlayers || showInfo) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable {
                        showPlayers = false
                        showInfo = false
                    },
            )
        }

        // Выезжающая панель игроков слева
        AnimatedVisibility(
            visible = showPlayers,
            enter = slideInHorizontally { -it } + fadeIn(),
            exit = slideOutHorizontally { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Box(
                modifier = Modifier
                    .width(PLAYERS_PANEL_WIDTH)
                    .fillMaxHeight()
                    .background(Color(0xFF160C33))
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.1f)),
            ) {
                playersContent()
            }
        }

        // Выезжающая панель инфо справа
        AnimatedVisibility(
            visible = showInfo,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            Box(
                modifier = Modifier
                    .width(SIDE_PANEL_WIDTH)
                    .fillMaxHeight()
                    .background(Color(0xFF160C33))
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.1f)),
            ) {
                infoContent()
            }
        }
    }
}

// ── Small (Мобильные экраны < 700dp) ────────────────────────────────────────

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

    LaunchedEffect(closePanelsSignal) {
        if (closePanelsSignal > 0) drawerVisible = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            gameContent()
            GameplaySmallTopBar(
                onOpenPlayers = { drawerTab = SideDrawerTab.PLAYERS; drawerVisible = true },
                onOpenInfo = { drawerTab = SideDrawerTab.INFO; drawerVisible = true },
            )
        }

        // Шторка с затеменением
        if (drawerVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { drawerVisible = false },
            )
        }

        // Нижниe/боковые выезжающие панели для мобильных
        AnimatedVisibility(
            visible = drawerVisible,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
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
