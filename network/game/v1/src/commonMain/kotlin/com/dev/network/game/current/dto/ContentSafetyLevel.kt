package com.dev.network.game.current.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ContentSafetyLevel {
  @SerialName("family_friendly")
  FAMILY_FRIENDLY,
  @SerialName("spicy")
  SPICY,
  @SerialName("explicit")
  EXPLICIT,
}
