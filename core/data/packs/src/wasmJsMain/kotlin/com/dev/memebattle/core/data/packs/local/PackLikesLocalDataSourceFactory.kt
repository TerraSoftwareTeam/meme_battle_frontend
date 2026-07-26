package com.dev.memebattle.core.data.packs.local

import org.koin.core.module.Module
import org.koin.dsl.module

actual fun createPackLikesLocalDataSource(): PackLikesLocalDataSource {
    return WebPackLikesLocalDataSource()
}

actual val packLikesPlatformModule: Module = module {
    single<PackLikesLocalDataSource> { WebPackLikesLocalDataSource() }
}
