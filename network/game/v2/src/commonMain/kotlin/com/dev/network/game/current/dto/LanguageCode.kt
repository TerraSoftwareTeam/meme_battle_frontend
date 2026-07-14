package com.dev.network.game.current.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LanguageCode {
  @SerialName("ru")
  RU,
  @SerialName("en")
  EN,
  @SerialName("und")
  UND,
}
