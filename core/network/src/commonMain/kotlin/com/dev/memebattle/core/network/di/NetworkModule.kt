package com.dev.memebattle.core.network.di

import com.dev.memebattle.core.network.auth.TokenStorage
import com.dev.memebattle.core.network.client.AppHttpClientFactory
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

import com.dev.memebattle.core.network.BuildKonfig

expect fun platformNetworkModule(): Module

val networkModule = module {
    includes(platformNetworkModule())

    single<HttpClient>(named("unauthenticated")) {
        AppHttpClientFactory.createUnauthenticated(BuildKonfig.API_BASE_URL)
    }

    single<HttpClient>(named("authenticated")) {
        AppHttpClientFactory.createAuthenticated(
            baseUrl = BuildKonfig.API_BASE_URL,
            tokenStorage = get(),
            unauthenticatedClientProvider = { get(named("unauthenticated")) }
        )
    }
}
