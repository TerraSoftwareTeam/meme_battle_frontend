package com.dev.network.media.current.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MediaStatus {
  @SerialName("pending")
  PENDING,
  @SerialName("attached")
  ATTACHED,
  @SerialName("deleted")
  DELETED,
}
