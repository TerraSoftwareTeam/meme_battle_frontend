package com.dev.network.user.current.di

import org.koin.dsl.module
import org.koin.core.qualifier.named
import com.dev.network.user.current.api.UserApiService
import com.dev.network.user.current.api.UserApiServiceImpl

val userNetworkModule = module {
    single<UserApiService> { UserApiServiceImpl(get(named("authenticated"))) }
}