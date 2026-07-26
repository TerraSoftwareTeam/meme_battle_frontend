package com.dev.network.game.current.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Тело запроса при входе в лобби.
 *
 * @param handle Желаемый игровой ник в рамках этой партии.
 *   - Если null — бэкенд использует users.nickname как default.
 *   - Если занят другим игроком — возвращается 409 Conflict.
 *   - Если nickname (default) конфликтует — бэкенд автоматически подставит player_id.
 */
@Serializable
data class JoinGameRequest(
    @SerialName("handle") val handle: String? = null
)
