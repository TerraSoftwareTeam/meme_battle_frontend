package com.dev.network.user_auth.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordDto(
  @SerialName("new_password")
  val new_password: String,
)
