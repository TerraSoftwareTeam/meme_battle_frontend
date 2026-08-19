package com.dev.network.game.current.dto

import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemePackDetailsResponse(
  @SerialName("memes")
  val memes: List<PackMemeDetailsDto>,
  @SerialName("pack")
  val pack: MemePackDto,
)
