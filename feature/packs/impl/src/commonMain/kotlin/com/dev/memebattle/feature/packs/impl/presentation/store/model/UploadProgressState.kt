package com.dev.memebattle.feature.packs.impl.presentation.store.model

data class UploadProgressState(
    val current: Int = 0,
    val total: Int = 0,
) {
    val progressFraction: Float
        get() = if (total > 0) (current.toFloat() / total).coerceIn(0f, 1f) else 0f

    val percentage: Int get() = (progressFraction * 100).toInt().coerceIn(0, 100)
}
