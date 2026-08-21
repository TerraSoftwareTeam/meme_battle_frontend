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
  @SerialName("name")
  val name: String? = null,
  @SerialName("status")
  val status: GameStatus,
  @SerialName("version")
  val version: Long,
  @SerialName("max_players")
  val max_players: Int? = null,
)
