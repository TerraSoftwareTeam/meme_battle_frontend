package com.dev.network.game.current.dto

import kotlin.Int
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoundDto(
  @SerialName("id")
  val id: String,
  @SerialName("phase")
  val phase: RoundPhase,
  @SerialName("phase_expires_at")
  val phase_expires_at: String? = null,
  @SerialName("prompt")
  val prompt: GameCard? = null,
  @SerialName("round_number")
  val round_number: Int,
)
