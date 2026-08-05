package com.dev.network.game.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackSituationDto(
  @SerialName("id")
  val id: String,
  @SerialName("pack_id")
  val pack_id: String,
  @SerialName("prompt_text")
  val prompt_text: String,
)
