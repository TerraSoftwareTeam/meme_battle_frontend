package com.dev.memebattle.feature.packs.impl.presentation.store.create

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

actual suspend fun compressImageIfNeeded(byteArray: ByteArray, maxSizeBytes: Long): ByteArray {
    if (byteArray.size <= maxSizeBytes) return byteArray

    try {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
        }
        var bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options) ?: return byteArray

        // 1. Resize if image dimensions are extremely large to save heap memory and file size
        val maxDimension = 2048
        if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val newWidth = if (bitmap.width > bitmap.height) maxDimension else (maxDimension * aspectRatio).toInt()
            val newHeight = if (bitmap.width > bitmap.height) (maxDimension / aspectRatio).toInt() else maxDimension
            
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            if (scaledBitmap != bitmap) {
                bitmap.recycle()
                bitmap = scaledBitmap
            }
        }

        // 2. Compress JPEG quality progressively
        var quality = 90
        var outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        
        while (outputStream.size() > maxSizeBytes && quality > 20) {
            outputStream.reset()
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }

        // 3. If still too large, downscale size progressively
        var scaleFactor = 0.8f
        while (outputStream.size() > maxSizeBytes && scaleFactor > 0.2f) {
            val newWidth = (bitmap.width * scaleFactor).toInt()
            val newHeight = (bitmap.height * scaleFactor).toInt()
            if (newWidth < 100 || newHeight < 100) break

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            outputStream.reset()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            scaledBitmap.recycle()
            scaleFactor -= 0.2f
        }

        bitmap.recycle()
        return outputStream.toByteArray()
    } catch (e: Exception) {
        println("[ImageCompressor] Error compressing image: ${e.message}")
        return byteArray
    }
}
