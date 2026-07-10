package com.dev.memebattle.feature.packs.impl.presentation.store.create

actual suspend fun compressImageIfNeeded(byteArray: ByteArray, maxSizeBytes: Long): ByteArray {
    // Return original array in Web/WasmJS environment for simplicity
    return byteArray
}
