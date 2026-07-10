package com.dev.memebattle.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.memebattle.feature.home.impl.di.homeModule
import com.dev.memebattle.feature.packs.impl.di.packsModule
import com.dev.memebattle.feature.gameSetup.impl.di.gameSetupModule
import com.dev.memebattle.feature.gameplay.impl.di.gameplayModule
import com.dev.memebattle.host.root.di.rootHostModule
import com.dev.memebattle.core.network.di.networkModule
import com.dev.network.game.current.di.gameNetworkModule
import com.dev.network.media.current.di.mediaNetworkModule
import com.dev.network.user.current.di.userNetworkModule
import com.dev.memebattle.core.data.packs.di.packsDataModule
import com.dev.network.user_auth.current.di.user_authNetworkModule
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {
    single<StoreFactory> { LoggingStoreFactory(DefaultStoreFactory()) }
}

fun initKoin() {
    startKoin {
        modules(
            sharedModule,
            rootHostModule,
            networkModule,
            user_authNetworkModule,
            userNetworkModule,
            mediaNetworkModule,
            gameNetworkModule,
            packsDataModule,
            homeModule,
            packsModule,
            gameSetupModule,
            gameplayModule
        )
    }
}
