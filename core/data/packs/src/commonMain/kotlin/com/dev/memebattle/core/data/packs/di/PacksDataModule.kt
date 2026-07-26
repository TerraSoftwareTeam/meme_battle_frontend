package com.dev.memebattle.core.data.packs.di

import com.dev.memebattle.core.data.packs.local.packLikesPlatformModule
import com.dev.memebattle.core.data.packs.repository.PackRepositoryImpl
import com.dev.memebattle.core.domain.packs.repository.PackRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val packsDataModule = module {
    includes(packLikesPlatformModule)
    singleOf(::PackRepositoryImpl) bind PackRepository::class
}
