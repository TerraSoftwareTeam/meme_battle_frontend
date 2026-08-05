package com.dev.network.game.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubmitCardRequest(
  @SerialName("card_id")
  val card_id: String,
)
