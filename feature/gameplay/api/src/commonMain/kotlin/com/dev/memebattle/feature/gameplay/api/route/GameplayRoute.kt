package com.dev.memebattle.feature.gameplay.api.route

import com.dev.memebattle.core.navigation.route.AppRoute
import kotlinx.serialization.Serializable

@Serializable
data class GameplayRoute(val gameId: String) : AppRoute
