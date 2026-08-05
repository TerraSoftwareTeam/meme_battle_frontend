package com.dev.network.game.current.di

import org.koin.dsl.module
import org.koin.core.qualifier.named
import org.koin.core.module.Module
import com.dev.network.game.current.api.GameApiService
import com.dev.network.game.current.api.GameApiServiceImpl
import com.dev.network.game.current.api.ws.GameSocketService
import com.dev.network.game.current.api.ws.GameSocketServiceImpl

actual val gameNetworkModule: Module = module {
    single<GameApiService> { GameApiServiceImpl(get(named("authenticated"))) }
    // WebSocket uses native JS WebSocket API on WasmJs
    single<GameSocketService> { GameSocketServiceImpl(get(named("unauthenticated")), get(), get(named("wsBaseUrl"))) }
}
