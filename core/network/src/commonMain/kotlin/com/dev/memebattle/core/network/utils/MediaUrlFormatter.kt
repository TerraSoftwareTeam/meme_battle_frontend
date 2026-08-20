package com.dev.memebattle.core.network.utils

import com.dev.memebattle.core.network.BuildKonfig

object MediaUrlEnv {
    var webOrigin: String? = null
}

private const val CDN_HOST = "https://user-cdn.hackclub-assets.com"

/**
 * Нормализует URL медиа-файлов (мемы, обложки паков и т.д.):
 * - Если путь начинается с `/cdn-proxy/` или содержит `user-cdn.hackclub-assets.com`/`cdn.hackclub.com`:
 *   - В локальном dev-вебе (`webOrigin != null`) -> преобразуется в `$webOrigin/cdn-proxy/...`
 *   - В продакшене / на мобилке -> преобразуется в прямой CDN URL `https://user-cdn.hackclub-assets.com/...`
 * - Иные относительные пути (напр. `/media/download/...`):
 *   - В локальном dev-вебе -> `$webOrigin/api-proxy/...`
 *   - В продакшене / на мобилке -> `${BuildKonfig.API_BASE_URL}/...`
 */
fun normalizeMediaUrl(url: String?): String {
    if (url.isNullOrBlank()) return ""
    val trimmed = url.trim()

    // 1. Если бэкенд возвращает относительный путь, начинающийся с /cdn-proxy/
    if (trimmed.startsWith("/cdn-proxy/")) {
        val pathWithoutProxy = trimmed.removePrefix("/cdn-proxy")
        val origin = MediaUrlEnv.webOrigin
        return if (origin != null) {
            "$origin/cdn-proxy$pathWithoutProxy"
        } else {
            "$CDN_HOST$pathWithoutProxy"
        }
    }

    // 2. Если URL указывает на абсолютный CDN (user-cdn.hackclub-assets.com или cdn.hackclub.com)
    if (trimmed.contains("cdn.hackclub.com") || trimmed.contains("user-cdn.hackclub-assets.com")) {
        val origin = MediaUrlEnv.webOrigin
        return if (origin != null) {
            trimmed
                .replace("https://user-cdn.hackclub-assets.com", "$origin/cdn-proxy")
                .replace("http://user-cdn.hackclub-assets.com", "$origin/cdn-proxy")
                .replace("https://cdn.hackclub.com", "$origin/cdn-proxy")
                .replace("http://cdn.hackclub.com", "$origin/cdn-proxy")
        } else {
            trimmed
        }
    }

    // 3. Другие относительные пути (начинаются с /)
    if (trimmed.startsWith("/")) {
        val origin = MediaUrlEnv.webOrigin
        return if (origin != null) {
            "$origin/api-proxy$trimmed"
        } else {
            "${BuildKonfig.API_BASE_URL}$trimmed"
        }
    }

    // 4. Любые другие абсолютные URL
    return trimmed
}
