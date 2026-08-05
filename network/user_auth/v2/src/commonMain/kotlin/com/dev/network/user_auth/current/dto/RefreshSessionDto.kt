package com.dev.network.user_auth.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshSessionDto(
  @SerialName("refresh_token")
  val refresh_token: String,
)
