package com.dev.memebattle.host.root.di

import com.dev.memebattle.core.navigation.layer.HostLayer
import com.dev.memebattle.host.root.presentation.layer.GlobalHostLayer
import org.koin.dsl.bind
import org.koin.dsl.module

val rootHostModule = module {
    single { GlobalHostLayer() } bind HostLayer::class
}
