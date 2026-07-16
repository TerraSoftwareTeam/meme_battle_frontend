package com.dev.memebattle.core.data.game.di

import com.dev.memebattle.core.data.game.GameRepositoryImpl
import com.dev.memebattle.core.domain.game.GameRepository
import org.koin.dsl.module

val dataGameModule = module {
    single<GameRepository> { GameRepositoryImpl(get(), get()) }
}
