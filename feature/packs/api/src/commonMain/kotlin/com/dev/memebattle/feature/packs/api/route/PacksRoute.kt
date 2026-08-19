package com.dev.memebattle.feature.packs.api.route

import com.dev.memebattle.core.navigation.route.AppRoute
import kotlinx.serialization.Serializable

@Serializable
data class PacksRoute(
    /** Если задан — при открытии сразу открыть экран деталей этого пака */
    val openPackId: String? = null,
    /** "meme" или "situation" — тип пака для деталей */
    val openPackKind: String? = null,
) : AppRoute
