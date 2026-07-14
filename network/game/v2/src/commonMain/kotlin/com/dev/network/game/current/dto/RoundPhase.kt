package com.dev.network.game.current.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RoundPhase {
  @SerialName("waiting")
  WAITING,
  @SerialName("submitting")
  SUBMITTING,
  @SerialName("voting")
  VOTING,
  @SerialName("finished")
  FINISHED,
}
