package com.dev.memebattle.core.network.di

import com.dev.memebattle.core.network.BuildKonfig
import com.dev.memebattle.core.network.WebApiConfig
import com.dev.memebattle.core.network.auth.TokenStorage
import com.dev.memebattle.core.network.auth.WebTokenStorage
import com.dev.memebattle.core.network.client.AppHttpClientFactory
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.core.module.dsl.singleOf

import org.koin.dsl.bind

actual fun platformNetworkModule(): Module = module {
    singleOf(::WebTokenStorage) bind TokenStorage::class

    // On WasmJs we route API calls through the local /api-proxy dev-server proxy
    // so that CORS preflight is never triggered (same-origin from browser POV).
    // If apiBaseUrl is not set (e.g. production build), fall back to BuildKonfig default.
    single<HttpClient>(named("unauthenticated")) {
        val base = WebApiConfig.apiBaseUrl ?: BuildKonfig.API_BASE_URL
        AppHttpClientFactory.createUnauthenticated(base)
    }

    single<HttpClient>(named("authenticated")) {
        val base = WebApiConfig.apiBaseUrl ?: BuildKonfig.API_BASE_URL
        AppHttpClientFactory.createAuthenticated(
            baseUrl = base,
            tokenStorage = get(),
            unauthenticatedClientProvider = { get(named("unauthenticated")) }
        )
    }
}
