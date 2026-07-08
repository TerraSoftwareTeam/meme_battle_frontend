package com.dev.memebattle.core.navigation.output

import com.dev.memebattle.core.navigation.route.AppRoute

/**
 * Маркерный интерфейс для передачи данных между экранами
 */
interface NavigationPayload

/**
 * События навигации, которые могут эмиттить фичи
 */
sealed interface NavigationOutput {
    data object Back : NavigationOutput
    data class NavigateTo(val route: AppRoute) : NavigationOutput
    data class BringToFront(val route: AppRoute, val payload: NavigationPayload? = null) : NavigationOutput
    data class PopAndBringToFront(val route: AppRoute, val payload: NavigationPayload? = null) : NavigationOutput
    data class ReplaceAll(val stack: List<AppRoute>) : NavigationOutput
}
