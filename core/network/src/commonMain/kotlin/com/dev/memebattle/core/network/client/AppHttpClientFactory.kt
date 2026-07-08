package com.dev.memebattle.core.network.client

import com.dev.memebattle.core.network.auth.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object AppHttpClientFactory {

    private fun buildJsonConfig() = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun createUnauthenticated(baseUrl: String): HttpClient = HttpClient {
        expectSuccess = true
        
        install(ContentNegotiation) { json(buildJsonConfig()) }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = println("Ktor[unauth]: $message")
            }
            level = LogLevel.BODY
        }
        defaultRequest { 
            url(baseUrl)
            contentType(ContentType.Application.Json) 
        }
    }

    fun createAuthenticated(
        baseUrl: String,
        tokenStorage: TokenStorage,
        unauthenticatedClientProvider: () -> HttpClient
    ): HttpClient = HttpClient {
        expectSuccess = true
        
        install(ContentNegotiation) { json(buildJsonConfig()) }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = println("Ktor[auth]: $message")
            }
            level = LogLevel.BODY
        }
        install(AppAuthPlugin) {
            this.tokenStorage = tokenStorage
            this.unauthenticatedClientProvider = unauthenticatedClientProvider
            this.baseUrl = baseUrl
        }
        defaultRequest { 
            url(baseUrl)
            contentType(ContentType.Application.Json) 
        }
    }
}
