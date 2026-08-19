package com.dev.memebattle.feature.home.api.route

import com.dev.memebattle.core.navigation.route.AppRoute
import kotlinx.serialization.Serializable

@Serializable
data class HomeRoute(
    /** Если задан — при открытии сразу показать диалог подключения к лобби с этим ID */
    val openLobbyId: String? = null,
) : AppRoute
