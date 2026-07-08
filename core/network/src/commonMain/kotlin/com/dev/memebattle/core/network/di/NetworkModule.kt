package com.dev.memebattle.core.network.di

import com.dev.memebattle.core.network.auth.TokenStorage
import com.dev.memebattle.core.network.client.AppHttpClientFactory
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

expect fun platformNetworkModule(): Module

val networkModule = module {
    includes(platformNetworkModule())

    single<HttpClient>(named("unauthenticated")) {
        AppHttpClientFactory.createUnauthenticated("https://api.example.com") // TODO: replace with your BuildKonfig base url
    }

    single<HttpClient>(named("authenticated")) {
        AppHttpClientFactory.createAuthenticated(
            baseUrl = "https://api.example.com", // TODO: replace with your BuildKonfig base url
            tokenStorage = get(),
            unauthenticatedClientProvider = { get(named("unauthenticated")) }
        )
    }
}
