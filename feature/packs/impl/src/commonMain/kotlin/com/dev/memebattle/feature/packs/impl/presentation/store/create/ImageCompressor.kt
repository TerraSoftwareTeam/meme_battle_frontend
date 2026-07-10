package com.dev.memebattle.feature.packs.impl.presentation.store.create

expect suspend fun compressImageIfNeeded(byteArray: ByteArray, maxSizeBytes: Long): ByteArray
