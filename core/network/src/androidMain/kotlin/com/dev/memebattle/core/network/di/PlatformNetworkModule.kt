package com.dev.memebattle.core.network.di

import com.dev.memebattle.core.network.auth.EncryptedTokenStorage
import com.dev.memebattle.core.network.auth.TokenStorage
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.bind

actual fun platformNetworkModule(): Module = module {
    singleOf(::EncryptedTokenStorage) { bind<TokenStorage>() }
}
