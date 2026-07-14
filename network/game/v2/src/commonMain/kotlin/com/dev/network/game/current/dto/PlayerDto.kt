package com.dev.network.game.current.dto

import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerDto(
  @SerialName("has_submitted")
  val has_submitted: Boolean,
  @SerialName("is_ready")
  val is_ready: Boolean,
  @SerialName("score")
  val score: Int,
  @SerialName("user_id")
  val user_id: String,
)
