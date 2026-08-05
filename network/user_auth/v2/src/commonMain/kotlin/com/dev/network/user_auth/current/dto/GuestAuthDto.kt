package com.dev.network.user_auth.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GuestAuthDto(
  @SerialName("username")
  val username: String? = null,
)
