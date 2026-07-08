package com.dev.network.game.current.dto

import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateMemePackRequest(
  @SerialName("description")
  val description: String? = null,
  @SerialName("is_public")
  val is_public: Boolean,
  @SerialName("language_code")
  val language_code: String,
  @SerialName("media_ids")
  val media_ids: List<Long>,
  @SerialName("name")
  val name: String,
  @SerialName("safety_level")
  val safety_level: ContentSafetyLevel,
)
