package com.dev.memebattle.core.network.di

import org.koin.core.module.Module
import org.koin.dsl.module

expect fun platformNetworkModule(): Module

val networkModule = module {
    includes(platformNetworkModule())
}

