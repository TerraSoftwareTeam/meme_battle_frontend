package com.dev.memebattle.feature.packs.impl.presentation.store.create

import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.dataWithBytes
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.memcpy
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual suspend fun compressImageIfNeeded(byteArray: ByteArray, maxSizeBytes: Long): ByteArray {
    if (byteArray.size <= maxSizeBytes) return byteArray

    try {
        val nsData = byteArray.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), byteArray.size.toULong())
        }
        val image = UIImage.imageWithData(nsData) ?: return byteArray

        var quality = 0.9
        var compressedData = UIImageJPEGRepresentation(image, quality)
        
        while (compressedData != null && compressedData.length > maxSizeBytes.toULong() && quality > 0.2) {
            quality -= 0.1
            compressedData = UIImageJPEGRepresentation(image, quality)
        }

        if (compressedData == null) return byteArray

        val bytes = ByteArray(compressedData.length.toInt())
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), compressedData.bytes, compressedData.length)
        }
        return bytes
    } catch (e: Exception) {
        println("[ImageCompressor] iOS error: ${e.message}")
        return byteArray
    }
}
