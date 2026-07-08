package com.dev.network.game.current.dto

import kotlin.Boolean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReadyRequest(
  @SerialName("is_ready")
  val is_ready: Boolean,
)
