package com.dev.network.user_auth.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthUserDto(
  @SerialName("handle")
  val handle: String,
  @SerialName("password")
  val password: String? = null,
)
