package com.dev.memebattle.core.network.utils

import com.dev.memebattle.core.network.BuildKonfig

object MediaUrlEnv {
    var webOrigin: String? = null
}

/**
 * Нормализует URL медиа-файлов (мемы, обложки паков и т.д.):
 * - Относительные пути (например `/media/download/...`) преобразуются в абсолютные URL с префиксом API_BASE_URL.
 * - В web-окружении относительные пути и CDN URL маршрутизируются через прокси (`/api-proxy` или `/cdn-proxy`), чтобы избежать CORS.
 */
fun normalizeMediaUrl(url: String?): String {
    if (url.isNullOrBlank()) return ""
    val trimmed = url.trim()
    return when {
        // Относительный путь → добавляем префикс API base (или api-proxy в вебе)
        trimmed.startsWith("/") -> {
            val origin = MediaUrlEnv.webOrigin
            if (origin != null) "$origin/api-proxy$trimmed"
            else "${BuildKonfig.API_BASE_URL}$trimmed"
        }
        // CDN URL → проксируем через /cdn-proxy в вебе для обхода CORS
        trimmed.contains("cdn.hackclub.com") || trimmed.contains("user-cdn.hackclub-assets.com") -> {
            val prefix = MediaUrlEnv.webOrigin
            if (prefix != null) {
                trimmed
                    .replace("https://user-cdn.hackclub-assets.com", "$prefix/cdn-proxy")
                    .replace("http://user-cdn.hackclub-assets.com", "$prefix/cdn-proxy")
                    .replace("https://cdn.hackclub.com", "$prefix/cdn-proxy")
                    .replace("http://cdn.hackclub.com", "$prefix/cdn-proxy")
            } else {
                trimmed
            }
        }
        else -> trimmed
    }
}
