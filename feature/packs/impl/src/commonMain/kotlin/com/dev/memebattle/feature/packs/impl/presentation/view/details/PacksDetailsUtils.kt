package com.dev.memebattle.feature.packs.impl.presentation.view.details

internal fun formatDate(raw: String): String = try {
    val d = raw.split("T").first().split("-")
    "${d[2]}.${d[1]}.${d[0]}"
} catch (_: Exception) { raw }
