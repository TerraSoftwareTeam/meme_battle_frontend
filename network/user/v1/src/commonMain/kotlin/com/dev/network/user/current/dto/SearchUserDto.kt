package com.dev.network.user.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchUserDto(
  @SerialName("handle")
  val handle: String? = null,
  @SerialName("id")
  val id: String? = null,
  @SerialName("username")
  val username: String? = null,
)
