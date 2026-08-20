package com.dev.network.media.current.api

import com.dev.memebattle.core.network.call.BaseResponse
import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.memebattle.core.network.call.safeCall
import com.dev.network.media.current.dto.MediaAssetDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class MediaApiServiceImpl(
    private val client: HttpClient,
) : MediaApiService {

    override suspend fun uploadImageMedia(
        byteArray: ByteArray,
        fileName: String,
    ): NetworkResult<MediaAssetDto> = safeCall {
        val mimeType = getMimeType(fileName)
        println("MediaApiServiceImpl: Starting upload of image '$fileName', size: ${byteArray.size} bytes, determined MIME-type: $mimeType")

        val response = client.submitFormWithBinaryData(
            url = "/media/upload/image",
            formData = formData {
                append(
                    key = "file",
                    value = byteArray,
                    headers = Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        append(HttpHeaders.ContentType, mimeType)
                    }
                )
            }
        )
        val data = response.body<BaseResponse<MediaAssetDto>>().data
        println("MediaApiServiceImpl: Upload completed successfully. Asset data: $data")
        data
    }

    private fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "heic" -> "image/heic"
            "heif" -> "image/heif"
            "svg" -> "image/svg+xml"
            else -> "image/jpeg"
        }
    }
}