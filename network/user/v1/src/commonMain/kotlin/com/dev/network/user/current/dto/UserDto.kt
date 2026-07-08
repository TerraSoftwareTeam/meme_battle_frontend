package com.dev.network.user.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
  @SerialName("avatar_url")
  val avatar_url: String? = null,
  @SerialName("created_at")
  val created_at: String? = null,
  @SerialName("handle")
  val handle: String,
  @SerialName("id")
  val id: String,
  @SerialName("modified_at")
  val modified_at: String? = null,
  @SerialName("username")
  val username: String,
)
