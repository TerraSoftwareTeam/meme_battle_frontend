package com.dev.memebattle.core.network.client

import com.dev.memebattle.core.network.auth.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.http.encodedPath

object AppHttpClientFactory {

    private class BaseUrlConfig {
        var baseUrl: String = ""
    }

    private val BaseUrlPlugin = createClientPlugin("BaseUrlPlugin", ::BaseUrlConfig) {
        val parsedBase = io.ktor.http.Url(pluginConfig.baseUrl)
        val basePath = parsedBase.encodedPath.trimEnd('/')

        onRequest { request, _ ->
            request.url.protocol = parsedBase.protocol
            request.url.host = parsedBase.host
            if (parsedBase.port != 0 && parsedBase.port != 80 && parsedBase.port != 443) {
                request.url.port = parsedBase.port
            }
            
            val currentPath = request.url.encodedPath
            if (basePath.isNotEmpty() && !currentPath.startsWith(basePath)) {
                request.url.encodedPath = basePath + if (currentPath.startsWith("/")) currentPath else "/$currentPath"
            }
        }
    }

    private val JsonContentTypePlugin = createClientPlugin("JsonContentTypePlugin") {
        onRequest { request, content ->
            if (content !is OutgoingContent && content != Unit) {
                if (request.contentType() == null) {
                    request.contentType(ContentType.Application.Json)
                }
            }
        }
    }

    private fun buildJsonConfig() = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun createUnauthenticated(baseUrl: String): HttpClient = HttpClient {
        // NOTE: expectSuccess must be false so that auth responses (token refresh, etc.)
        // are not thrown as exceptions before we can read their body.
        expectSuccess = false
        
        install(ContentNegotiation) { json(buildJsonConfig()) }
        install(JsonContentTypePlugin)
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = println("Ktor[unauth]: $message")
            }
            level = LogLevel.HEADERS
        }
        install(BaseUrlPlugin) { this.baseUrl = baseUrl }
    }

    fun createAuthenticated(
        baseUrl: String,
        tokenStorage: TokenStorage,
        unauthenticatedClientProvider: () -> HttpClient
    ): HttpClient = HttpClient {
        // expectSuccess = true so that 4xx/5xx become ClientRequestException in safeCall.
        // The AppAuthPlugin uses HttpSend-level interception (on(Send)) which runs BEFORE
        // Ktor's expectSuccess check, so our 401-retry logic executes first.
        expectSuccess = true
        
        install(ContentNegotiation) { json(buildJsonConfig()) }
        install(JsonContentTypePlugin)
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = println("Ktor[auth]: $message")
            }
            level = LogLevel.HEADERS
        }
        install(AppAuthPlugin) {
            this.tokenStorage = tokenStorage
            this.unauthenticatedClientProvider = unauthenticatedClientProvider
            this.baseUrl = baseUrl
        }
        install(BaseUrlPlugin) { this.baseUrl = baseUrl }
    }
}

