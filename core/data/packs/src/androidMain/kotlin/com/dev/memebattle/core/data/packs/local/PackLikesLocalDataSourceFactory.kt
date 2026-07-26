package com.dev.memebattle.core.data.packs.local

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun createPackLikesLocalDataSource(): PackLikesLocalDataSource {
    throw IllegalStateException("Use packLikesPlatformModule with Koin")
}

actual val packLikesPlatformModule: Module = module {
    single<PackLikesLocalDataSource> { AndroidPackLikesLocalDataSource(androidContext()) }
}
