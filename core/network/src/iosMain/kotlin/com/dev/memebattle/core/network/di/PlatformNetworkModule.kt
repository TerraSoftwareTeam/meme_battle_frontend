package com.dev.memebattle.core.network.di

import com.dev.memebattle.core.network.auth.IosTokenStorage
import com.dev.memebattle.core.network.auth.TokenStorage
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.bind

actual fun platformNetworkModule(): Module = module {
    singleOf(::IosTokenStorage) { bind<TokenStorage>() }
}
