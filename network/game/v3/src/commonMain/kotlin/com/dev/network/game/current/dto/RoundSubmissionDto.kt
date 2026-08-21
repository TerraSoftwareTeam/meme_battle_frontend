package com.dev.network.game.current.dto

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoundSubmissionDto(
  @SerialName("card")
  val card: GameCard,
  @SerialName("id")
  val id: String,
  @SerialName("is_mine")
  val is_mine: Boolean = false,
)
