package com.dev.network.game.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JoinGameRequest(
  @SerialName("handle")
  val handle: String? = null,
)
