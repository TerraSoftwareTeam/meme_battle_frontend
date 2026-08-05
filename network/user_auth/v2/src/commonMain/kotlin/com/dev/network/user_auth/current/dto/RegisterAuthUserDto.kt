package com.dev.network.user_auth.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterAuthUserDto(
  @SerialName("password")
  val password: String? = null,
  @SerialName("username")
  val username: String,
)
