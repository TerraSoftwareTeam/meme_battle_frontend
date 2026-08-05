package com.dev.network.game.current.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class GameMode {
  @SerialName("situation_to_meme")
  SITUATION_TO_MEME,
  @SerialName("meme_to_situation")
  MEME_TO_SITUATION,
}
