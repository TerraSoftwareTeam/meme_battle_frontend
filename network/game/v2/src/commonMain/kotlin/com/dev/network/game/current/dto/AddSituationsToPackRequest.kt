package com.dev.network.game.current.dto

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddSituationsToPackRequest(
  @SerialName("prompts")
  val prompts: List<String>,
)
