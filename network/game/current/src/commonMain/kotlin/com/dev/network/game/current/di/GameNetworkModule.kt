package com.dev.network.game.current.di

import org.koin.dsl.module
import org.koin.core.qualifier.named
import com.dev.network.game.current.api.GameApiService
import com.dev.network.game.current.api.GameApiServiceImpl

import com.dev.network.game.current.api.ws.GameSocketService
import com.dev.network.game.current.api.ws.GameSocketServiceImpl

val gameNetworkModule = module {
    single<GameApiService> { GameApiServiceImpl(get(named("authenticated"))) }
    single<GameSocketService> { GameSocketServiceImpl(get(named("authenticated")), get()) }
}