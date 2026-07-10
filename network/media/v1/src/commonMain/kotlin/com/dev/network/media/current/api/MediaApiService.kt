package com.dev.network.media.current.api

import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.network.media.current.dto.MediaAssetDto

interface MediaApiService {
  suspend fun uploadImageMedia(byteArray: ByteArray, fileName: String): NetworkResult<MediaAssetDto>
}
