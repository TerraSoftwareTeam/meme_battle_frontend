package com.dev.network.game.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WsTokenDto(
  @SerialName("connection_token")
  val connection_token: String,
  @SerialName("game_subscription_token")
  val game_subscription_token: String,
  @SerialName("personal_subscription_token")
  val personal_subscription_token: String,
)
