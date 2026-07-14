package com.dev.network.game.current.dto

import kotlin.Long
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackMemeDetailsDto(
  @SerialName("id")
  val id: String,
  @SerialName("media_id")
  val media_id: Long? = null,
  @SerialName("media_url")
  val media_url: String,
  @SerialName("pack_id")
  val pack_id: String,
)
