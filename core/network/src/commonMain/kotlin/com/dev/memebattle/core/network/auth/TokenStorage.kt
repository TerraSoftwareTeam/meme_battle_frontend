package com.dev.memebattle.core.network.auth

import kotlinx.coroutines.flow.StateFlow

interface TokenStorage {
    val authOrigin: StateFlow<AuthOrigin>
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun saveTokens(accessToken: String, refreshToken: String, origin: AuthOrigin)
    fun clear()
}
