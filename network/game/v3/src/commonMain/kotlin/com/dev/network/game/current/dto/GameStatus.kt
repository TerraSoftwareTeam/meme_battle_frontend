package com.dev.network.game.current.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class GameStatus {
  @SerialName("lobby")
  LOBBY,
  @SerialName("playing")
  PLAYING,
  @SerialName("finished")
  FINISHED,
}
