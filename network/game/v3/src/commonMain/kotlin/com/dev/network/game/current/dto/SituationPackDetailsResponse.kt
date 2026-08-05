package com.dev.network.game.current.dto

import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SituationPackDetailsResponse(
  @SerialName("pack")
  val pack: SituationPackDto,
  @SerialName("situations")
  val situations: List<PackSituationDto>,
)
