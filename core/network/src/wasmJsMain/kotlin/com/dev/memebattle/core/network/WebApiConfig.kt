package com.dev.memebattle.core.network

/**
 * Holds the effective API base URL for the WasmJs target.
 * Set this BEFORE calling initKoin() so the Koin HTTP client singletons
 * pick up the proxy URL instead of the BuildKonfig default.
 */
object WebApiConfig {
    /**
     * If non-null, overrides [BuildKonfig.API_BASE_URL] for all HTTP clients.
     * On dev: set to "$windowOrigin/api-proxy" to bypass CORS preflight.
     */
    var apiBaseUrl: String? = null

    /**
     * If non-null, overrides [BuildKonfig.WS_BASE_URL] for WebSocket connections.
     * On dev: set to "$windowOrigin/ws-proxy" to bypass CORS via webpack dev-server ws proxy.
     */
    var wsBaseUrl: String? = null
}
