package com.dev.network.user_auth.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthBody(
  @SerialName("access_token")
  val access_token: String,
  @SerialName("refresh_token")
  val refresh_token: String,
  @SerialName("token_type")
  val token_type: String,
)
