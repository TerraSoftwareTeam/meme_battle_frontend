package com.dev.memebattle.core.network.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EncryptedTokenStorage(context: Context) : TokenStorage {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _authOrigin = MutableStateFlow(
        AuthOrigin.valueOf(
            prefs.getString(KEY_ORIGIN, AuthOrigin.NONE.name) ?: AuthOrigin.NONE.name
        )
    )
    override val authOrigin: StateFlow<AuthOrigin> = _authOrigin.asStateFlow()

    override fun getAccessToken(): String? = prefs.getString(KEY_ACCESS, null)
    override fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH, null)

    override fun saveTokens(accessToken: String, refreshToken: String, origin: AuthOrigin) {
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .putString(KEY_REFRESH, refreshToken)
            .putString(KEY_ORIGIN, origin.name)
            .apply()
        _authOrigin.value = origin
    }

    override fun clear() {
        prefs.edit().clear().apply()
        _authOrigin.value = AuthOrigin.NONE
    }

    private companion object {
        const val PREFS_NAME = "secure_token_prefs"
        const val KEY_ACCESS  = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_ORIGIN  = "auth_origin"
    }
}
