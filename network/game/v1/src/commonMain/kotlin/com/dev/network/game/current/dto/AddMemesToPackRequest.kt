package com.dev.network.game.current.dto

import kotlin.Long
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddMemesToPackRequest(
  @SerialName("media_ids")
  val media_ids: List<Long>,
)
