package com.dev.network.media.current.di

import org.koin.dsl.module
import org.koin.core.qualifier.named
import com.dev.network.media.current.api.MediaApiService
import com.dev.network.media.current.api.MediaApiServiceImpl

val mediaNetworkModule = module {
    single<MediaApiService> { MediaApiServiceImpl(get(named("authenticated"))) }
}