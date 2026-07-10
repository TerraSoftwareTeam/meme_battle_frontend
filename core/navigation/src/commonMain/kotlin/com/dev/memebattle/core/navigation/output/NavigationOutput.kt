package com.dev.memebattle.core.navigation.output

import com.dev.memebattle.core.navigation.route.AppRoute

interface NavigationPayload

enum class NotificationType {
    Positive,
    Neutral,
    Negative,
}

/**
 * События навигации, которые могут эмиттить фичи
 */
sealed interface NavigationOutput {
    data object Back : NavigationOutput
    data class NavigateTo(val route: AppRoute) : NavigationOutput
    data class BringToFront(val route: AppRoute, val payload: NavigationPayload? = null) : NavigationOutput
    data class PopAndBringToFront(val route: AppRoute, val payload: NavigationPayload? = null) : NavigationOutput
    data class ReplaceAll(val stack: List<AppRoute>) : NavigationOutput
    data class ShowNotification(
        val message: String,
        val type: NotificationType = NotificationType.Neutral,
        val actionLabel: String? = null,
        val onAction: (() -> Unit)? = null,
    ) : NavigationOutput
}
