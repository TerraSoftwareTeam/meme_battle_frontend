package com.dev.network.game.current.dto

import kotlin.Long
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameDto(
  @SerialName("id")
  val id: String,
  @SerialName("mode")
  val mode: GameMode,
  @SerialName("status")
  val status: GameStatus,
  @SerialName("version")
  val version: Long,
  @SerialName("host_id")
  val host_id: String? = null,
  @SerialName("host_user_id")
  val host_user_id: String? = null,
)
