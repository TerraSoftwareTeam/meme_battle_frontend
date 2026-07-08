package com.dev.memebattle.core.network.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Helper functions for localStorage in WasmJs
internal fun setItem(key: String, value: String): Unit = js("window.localStorage.setItem(key, value)")

internal fun getItem(key: String): String? = js("window.localStorage.getItem(key)")

internal fun removeItem(key: String): Unit = js("window.localStorage.removeItem(key)")

class WebTokenStorage : TokenStorage {

    private val _authOrigin = MutableStateFlow(AuthOrigin.NONE)
    override val authOrigin: StateFlow<AuthOrigin> = _authOrigin.asStateFlow()

    init {
        val originStr = getItem("auth_origin")
        if (originStr != null) {
            try {
                _authOrigin.value = AuthOrigin.valueOf(originStr)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    override fun getAccessToken(): String? {
        return getItem("access_token")
    }

    override fun getRefreshToken(): String? {
        return getItem("refresh_token")
    }

    override fun saveTokens(accessToken: String, refreshToken: String, origin: AuthOrigin) {
        setItem("access_token", accessToken)
        setItem("refresh_token", refreshToken)
        setItem("auth_origin", origin.name)
        _authOrigin.value = origin
    }

    override fun clear() {
        removeItem("access_token")
        removeItem("refresh_token")
        removeItem("auth_origin")
        _authOrigin.value = AuthOrigin.NONE
    }
}
