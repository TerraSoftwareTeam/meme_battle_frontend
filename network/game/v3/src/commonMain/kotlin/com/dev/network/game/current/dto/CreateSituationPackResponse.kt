package com.dev.network.game.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSituationPackResponse(
  @SerialName("id")
  val id: String,
)
