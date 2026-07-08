package com.dev.network.media.current.dto

import kotlin.Long
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaAssetDto(
  @SerialName("content_type")
  val content_type: String,
  @SerialName("created_at")
  val created_at: String,
  @SerialName("filename")
  val filename: String,
  @SerialName("id")
  val id: Long,
  @SerialName("owner_user_id")
  val owner_user_id: String,
  @SerialName("provider")
  val provider: MediaProvider,
  @SerialName("provider_file_id")
  val provider_file_id: String,
  @SerialName("size_bytes")
  val size_bytes: Long,
  @SerialName("status")
  val status: MediaStatus,
  @SerialName("url")
  val url: String,
)
