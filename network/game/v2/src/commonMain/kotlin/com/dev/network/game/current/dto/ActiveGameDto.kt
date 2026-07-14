package com.dev.network.game.current.dto

import kotlin.Int
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActiveGameDto(
  @SerialName("created_at")
  val created_at: String,
  @SerialName("hand_size")
  val hand_size: Int,
  @SerialName("host_id")
  val host_id: String,
  @SerialName("id")
  val id: String,
  @SerialName("max_rounds")
  val max_rounds: Int,
  @SerialName("mode")
  val mode: GameMode,
  @SerialName("players_count")
  val players_count: Int,
)
