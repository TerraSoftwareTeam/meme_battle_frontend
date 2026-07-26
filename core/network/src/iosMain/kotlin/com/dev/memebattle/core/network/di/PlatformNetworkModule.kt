package com.dev.memebattle.core.network.di

import com.dev.memebattle.core.network.BuildKonfig
import com.dev.memebattle.core.network.auth.IosTokenStorage
import com.dev.memebattle.core.network.auth.TokenStorage
import com.dev.memebattle.core.network.client.AppHttpClientFactory
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.bind

actual fun platformNetworkModule(): Module = module {
    singleOf(::IosTokenStorage) { bind<TokenStorage>() }

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

    single(named("wsBaseUrl")) { BuildKonfig.WS_BASE_URL }
}
