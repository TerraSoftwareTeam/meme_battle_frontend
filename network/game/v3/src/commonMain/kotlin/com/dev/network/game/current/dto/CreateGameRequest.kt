package com.dev.network.game.current.dto

import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateGameRequest(
  @SerialName("hand_size")
  val hand_size: Int? = null,
  @SerialName("handle")
  val handle: String? = null,
  @SerialName("max_rounds")
  val max_rounds: Int? = null,
  @SerialName("mode")
  val mode: GameMode,
  @SerialName("selected_meme_pack_ids")
  val selected_meme_pack_ids: List<String>,
  @SerialName("selected_situation_pack_ids")
  val selected_situation_pack_ids: List<String>,
)
