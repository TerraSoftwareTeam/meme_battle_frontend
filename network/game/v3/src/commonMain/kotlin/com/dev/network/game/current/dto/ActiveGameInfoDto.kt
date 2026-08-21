package com.dev.network.game.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActiveGameInfoDto(
  @SerialName("game_id")
  val game_id: String,
  @SerialName("status")
  val status: GameStatus,
)
