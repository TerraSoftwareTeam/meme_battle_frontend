package com.dev.network.media.current.api

import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.memebattle.core.network.call.safeCall
import com.dev.network.media.current.dto.MediaAssetDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class MediaApiServiceImpl(
  private val client: HttpClient,
) : MediaApiService {
  override suspend fun uploadImageMedia(): NetworkResult<MediaAssetDto> = safeCall {
    client.post("/media/upload/image") {
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<MediaAssetDto>>().data
  }
}
