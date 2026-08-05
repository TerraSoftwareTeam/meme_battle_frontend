package com.dev.network.game.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LobbiesWsTokenDto(
  @SerialName("connection_token")
  val connection_token: String,
  @SerialName("lobbies_subscription_token")
  val lobbies_subscription_token: String,
)
