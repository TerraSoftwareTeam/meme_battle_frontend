package com.dev.memebattle.core.data.packs.local

import org.koin.core.module.Module

/**
 * Фабрика для создания PackLikesLocalDataSource на разных платформах
 */
expect fun createPackLikesLocalDataSource(): PackLikesLocalDataSource

/** Platform-specific module для PackLikes */
expect val packLikesPlatformModule: Module
