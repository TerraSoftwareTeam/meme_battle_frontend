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
            gameComponent?.let { GameplayGameScreen(it) }
        },
        infoContent = {
            infoComponent?.let { GameplayInfoScreen(it) }
        },
        playersContent = {
            playersComponent?.let { GameplayPlayersScreen(it) }
        },
        modifier = modifier,
    )
}
