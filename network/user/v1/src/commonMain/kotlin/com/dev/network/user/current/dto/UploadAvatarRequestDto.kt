package com.dev.network.user.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadAvatarRequestDto(
  @SerialName("file")
  val `file`: String,
)
