package com.dev.network.user.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateMeDto(
  @SerialName("password")
  val password: String? = null,
  @SerialName("username")
  val username: String? = null,
)
