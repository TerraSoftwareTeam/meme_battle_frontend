package com.dev.network.game.current.dto

import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActiveGamesResponseDto(
  @SerialName("games")
  val games: List<ActiveGameDto>,
)
