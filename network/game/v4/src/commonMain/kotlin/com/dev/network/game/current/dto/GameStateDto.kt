package com.dev.network.game.current.dto

import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameStateDto(
  @SerialName("game")
  val game: GameDto,
  @SerialName("my_hand")
  val my_hand: List<GameCard>,
  @SerialName("players")
  val players: List<PlayerDto>,
  @SerialName("round")
  val round: RoundDto? = null,
)
