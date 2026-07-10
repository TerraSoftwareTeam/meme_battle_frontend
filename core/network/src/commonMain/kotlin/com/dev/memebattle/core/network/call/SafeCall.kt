package com.dev.memebattle.core.network.call

import com.dev.memebattle.core.network.error.NetworkError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.io.IOException

/**
 * Executes a network call and safely maps Ktor exceptions to NetworkResult.
 */
inline suspend fun <T> safeCall(block: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(block())
    } catch (e: ClientRequestException) {
        val status = e.response.status.value
        val message = try {
            val text = e.response.bodyAsText()
            val json = Json.parseToJsonElement(text).jsonObject
            json["message"]?.jsonPrimitive?.content
        } catch (ex: Exception) {
            null
        }
        
        if (message != null) {
            NetworkResult.Error(NetworkError.ApiException(status, message))
        } else {
            when (status) {
                401 -> NetworkResult.Error(NetworkError.Unauthorized)
                403 -> NetworkResult.Error(NetworkError.Forbidden)
                404 -> NetworkResult.Error(NetworkError.NotFound)
                else -> NetworkResult.Error(NetworkError.Unknown)
            }
        }
    } catch (e: ServerResponseException) {
        NetworkResult.Error(NetworkError.ServerError(e.response.status.value))
    } catch (e: IOException) {
        NetworkResult.Error(NetworkError.Timeout)
    } catch (e: Exception) {
        NetworkResult.Error(NetworkError.Exception(e))
    }
}
