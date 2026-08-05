package com.dev.network.game.current.dto

import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoundDto(
  @SerialName("has_voted")
  val has_voted: Boolean,
  @SerialName("id")
  val id: String,
  @SerialName("my_submission")
  val my_submission: GameCard? = null,
  @SerialName("phase")
  val phase: RoundPhase,
  @SerialName("phase_expires_at")
  val phase_expires_at: String? = null,
  @SerialName("prompt")
  val prompt: GameCard? = null,
  @SerialName("round_number")
  val round_number: Int,
  @SerialName("submissions")
  val submissions: List<RoundSubmissionDto>? = null,
)
