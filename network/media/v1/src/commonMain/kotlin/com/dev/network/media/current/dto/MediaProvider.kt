package com.dev.network.media.current.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MediaProvider {
  @SerialName("hack_club_cdn")
  HACK_CLUB_CDN,
}
