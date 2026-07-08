package com.dev.network.user_auth.current.di

import org.koin.dsl.module
import org.koin.core.qualifier.named
import com.dev.network.user_auth.current.api.User_authApiService
import com.dev.network.user_auth.current.api.User_authApiServiceImpl

val user_authNetworkModule = module {
    single<User_authApiService> { User_authApiServiceImpl(get(named("authenticated"))) }
}