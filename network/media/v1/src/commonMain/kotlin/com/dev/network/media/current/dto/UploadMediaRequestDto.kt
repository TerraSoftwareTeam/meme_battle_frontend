package com.dev.network.media.current.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadMediaRequestDto(
  @SerialName("file")
  val `file`: String,
)
