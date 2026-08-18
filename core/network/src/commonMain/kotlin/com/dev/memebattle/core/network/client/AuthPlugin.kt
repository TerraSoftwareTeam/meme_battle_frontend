package com.dev.memebattle.core.network.client

import com.dev.memebattle.core.network.auth.AuthOrigin
import com.dev.memebattle.core.network.auth.TokenStorage
import com.dev.memebattle.core.network.call.BaseResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

import io.ktor.client.statement.bodyAsText

class AuthPluginConfig {
    lateinit var tokenStorage: TokenStorage
    lateinit var unauthenticatedClientProvider: () -> HttpClient
    lateinit var baseUrl: String
}

val AppAuthPlugin = createClientPlugin("AppAuthPlugin", ::AuthPluginConfig) {
    val tokenStorage = pluginConfig.tokenStorage
    val unauthenticatedClientProvider = pluginConfig.unauthenticatedClientProvider
    val baseUrl = pluginConfig.baseUrl
    
    val mutex = Mutex()

    val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

    suspend fun requestGuestToken(): String? {
        return try {
            val client = unauthenticatedClientProvider()
            val response = client.post("$baseUrl/auth/guest")
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                val body = json.decodeFromString<BaseResponse<LocalAuthBody>>(text).data
                tokenStorage.saveTokens(body.accessToken, body.refreshToken ?: "", AuthOrigin.GUEST)
                body.accessToken
            } else {
                println("[AuthPlugin] requestGuestToken status failed: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("[AuthPlugin] requestGuestToken exception: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun refreshTokens(refreshToken: String): LocalAuthBody? {
        return try {
            val client = unauthenticatedClientProvider()
            val resp = client.post("$baseUrl/auth/refresh") {
                setBody(LocalRefreshSessionDto(refreshToken = refreshToken))
            }
            if (resp.status.isSuccess()) {
                val text = resp.bodyAsText()
                val body = json.decodeFromString<BaseResponse<LocalAuthBody>>(text).data
                val currentOrigin = tokenStorage.authOrigin.value
                val newRefreshToken = body.refreshToken.takeIf { !it.isNullOrBlank() } ?: refreshToken
                tokenStorage.saveTokens(
                    accessToken = body.accessToken,
                    refreshToken = newRefreshToken,
                    origin = if (currentOrigin == AuthOrigin.NONE) AuthOrigin.GUEST else currentOrigin
                )
                body.copy(refreshToken = newRefreshToken)
            } else {
                println("[AuthPlugin] refreshTokens status failed: ${resp.status}")
                null
            }
        } catch (e: Exception) {
            println("[AuthPlugin] refreshTokens exception: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun requestGuestTokensBody(): LocalAuthBody? {
          return try {
            val client = unauthenticatedClientProvider()
            val resp = client.post("$baseUrl/auth/guest")
            if (resp.status.isSuccess()) {
                val text = resp.bodyAsText()
                val body = json.decodeFromString<BaseResponse<LocalAuthBody>>(text).data
                tokenStorage.saveTokens(body.accessToken, body.refreshToken ?: "", AuthOrigin.GUEST)
                body
            } else {
                println("[AuthPlugin] requestGuestTokensBody status failed: ${resp.status}")
                null
            }
        } catch (e: Exception) {
            println("[AuthPlugin] requestGuestTokensBody exception: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    on(io.ktor.client.plugins.api.Send) { request ->
        if (request.url.toString().contains("/auth/guest") || request.url.toString().contains("/auth/refresh")) {
            return@on proceed(request)
        }
        
        var accessToken = tokenStorage.getAccessToken()
        println("[AuthPlugin] Request to ${request.url}, token=${accessToken?.take(20)}...")

        if (accessToken.isNullOrBlank()) {
            mutex.withLock {
                accessToken = tokenStorage.getAccessToken()
                if (accessToken.isNullOrBlank()) {
                    println("[AuthPlugin] No token, requesting guest token...")
                    accessToken = requestGuestToken()
                    println("[AuthPlugin] Got guest token: ${accessToken?.take(20)}...")
                }
            }
        }

        if (!accessToken.isNullOrBlank()) {
            request.headers.remove(HttpHeaders.Authorization)
            request.headers.append(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        val call = try {
            proceed(request)
        } catch (e: io.ktor.client.plugins.ResponseException) {
            val statusCode = e.response.status.value
            println("[AuthPlugin] Caught ResponseException with status: $statusCode for ${request.url}")
            
            if (statusCode == 401) {
                println("[AuthPlugin] Got 401, attempting refresh or guest tokens...")
                
                val newTokens = mutex.withLock {
                    val currentRefreshToken = tokenStorage.getRefreshToken()
                    if (!currentRefreshToken.isNullOrBlank()) {
                        println("[AuthPlugin] Attempting token refresh...")
                        refreshTokens(currentRefreshToken)?.also {
                            println("[AuthPlugin] Token refresh successful")
                        } ?: run {
                            println("[AuthPlugin] Token refresh failed, clearing tokens and trying guest tokens...")
                            tokenStorage.clear()
                            requestGuestTokensBody()
                        }
                    } else {
                        println("[AuthPlugin] No refresh token, clearing tokens and requesting guest tokens...")
                        tokenStorage.clear()
                        requestGuestTokensBody()
                    }
                }

                if (newTokens == null) {
                    println("[AuthPlugin] All token methods failed")
                    throw e
                } else {
                    println("[AuthPlugin] Got new token, retrying request...")
                    request.headers.remove(HttpHeaders.Authorization)
                    request.headers.append(HttpHeaders.Authorization, "Bearer ${newTokens.accessToken}")
                    return@on proceed(request)
                }
            }
            throw e
        }

        call
    }
}
