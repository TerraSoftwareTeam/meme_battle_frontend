package com.dev.network.game.current.dto

import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSituationPackRequest(
  @SerialName("description")
  val description: String? = null,
  @SerialName("is_public")
  val is_public: Boolean,
  @SerialName("language_code")
  val language_code: LanguageCode,
  @SerialName("name")
  val name: String,
  @SerialName("prompts")
  val prompts: List<String>,
  @SerialName("safety_level")
  val safety_level: ContentSafetyLevel,
)
