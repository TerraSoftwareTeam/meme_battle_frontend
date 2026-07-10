package com.dev.network.media.current.api

import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.memebattle.core.network.call.safeCall
import com.dev.network.media.current.dto.MediaAssetDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.forms.formData
import io.ktor.http.contentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class MediaApiServiceImpl(
  private val client: HttpClient,
) : MediaApiService {
  override suspend fun uploadImageMedia(byteArray: ByteArray, fileName: String): NetworkResult<MediaAssetDto> = safeCall {
    client.submitFormWithBinaryData(
        url = "/media/upload/image",
        formData = formData {
            append("file", byteArray, Headers.build {
                append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"$fileName\"")
                append(HttpHeaders.ContentType, "image/jpeg")
            })
        }
    ) {
        contentType(io.ktor.http.ContentType.MultiPart.FormData)
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<MediaAssetDto>>().data
  }
}
