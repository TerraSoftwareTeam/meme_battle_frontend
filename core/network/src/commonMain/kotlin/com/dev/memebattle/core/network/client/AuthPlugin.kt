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

    suspend fun requestGuestToken(): String? {
        return try {
            val client = unauthenticatedClientProvider()
            val response = client.post("$baseUrl/auth/guest")
            if (response.status.isSuccess()) {
                val body = response.body<BaseResponse<LocalAuthBody>>().data
                tokenStorage.saveTokens(body.accessToken, body.refreshToken, AuthOrigin.GUEST)
                body.accessToken
            } else null
        } catch (e: Exception) {
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
                val body = resp.body<BaseResponse<LocalAuthBody>>().data
                val currentOrigin = tokenStorage.authOrigin.value
                tokenStorage.saveTokens(
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    origin = if (currentOrigin == AuthOrigin.NONE) AuthOrigin.GUEST else currentOrigin
                )
                body
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun requestGuestTokensBody(): LocalAuthBody? {
         return try {
            val client = unauthenticatedClientProvider()
            val resp = client.post("$baseUrl/auth/guest")
            if (resp.status.isSuccess()) {
                val body = resp.body<BaseResponse<LocalAuthBody>>().data
                tokenStorage.saveTokens(body.accessToken, body.refreshToken, AuthOrigin.GUEST)
                body
            } else null
        } catch (e: Exception) {
            null
        }
    }

    on(io.ktor.client.plugins.api.Send) { request ->
        if (request.url.toString().contains("/auth/guest") || request.url.toString().contains("/auth/refresh")) {
            return@on proceed(request)
        }
        
        var accessToken = tokenStorage.getAccessToken()

        if (accessToken.isNullOrBlank()) {
            mutex.withLock {
                accessToken = tokenStorage.getAccessToken()
                if (accessToken.isNullOrBlank()) {
                    accessToken = requestGuestToken()
                }
            }
        }

        if (!accessToken.isNullOrBlank()) {
            request.headers.remove(HttpHeaders.Authorization)
            request.headers.append(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        var call = proceed(request)
        
        if (call.response.status.value == 401) {
            val refreshToken = tokenStorage.getRefreshToken()
            val newTokens = mutex.withLock {
                if (!refreshToken.isNullOrBlank()) {
                    refreshTokens(refreshToken) ?: requestGuestTokensBody()
                } else {
                    requestGuestTokensBody()
                }
            }

            if (newTokens == null) {
                tokenStorage.clear()
            } else {
                request.headers.remove(HttpHeaders.Authorization)
                request.headers.append(HttpHeaders.Authorization, "Bearer ${newTokens.accessToken}")
                call = proceed(request)
            }
        }
        
        call
    }
}
