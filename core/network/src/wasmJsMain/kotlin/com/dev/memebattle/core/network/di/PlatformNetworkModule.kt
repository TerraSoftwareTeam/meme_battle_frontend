package com.dev.memebattle.core.network.di

import com.dev.memebattle.core.network.auth.TokenStorage
import com.dev.memebattle.core.network.auth.WebTokenStorage
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.dsl.module
import org.koin.core.module.dsl.singleOf

import org.koin.dsl.bind

actual fun platformNetworkModule(): Module = module {
    singleOf(::WebTokenStorage) bind TokenStorage::class
}
