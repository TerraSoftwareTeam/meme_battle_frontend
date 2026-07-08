package com.dev.memebattle.core.network.auth

import kotlinx.cinterop.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*

@OptIn(ExperimentalForeignApi::class)
class IosTokenStorage : TokenStorage {

    private val _authOrigin = MutableStateFlow(
        AuthOrigin.valueOf(getString(KEY_ORIGIN) ?: AuthOrigin.NONE.name)
    )
    override val authOrigin: StateFlow<AuthOrigin> = _authOrigin.asStateFlow()

    override fun getAccessToken(): String? = getString(KEY_ACCESS)

    override fun getRefreshToken(): String? = getString(KEY_REFRESH)

    override fun saveTokens(accessToken: String, refreshToken: String, origin: AuthOrigin) {
        putString(KEY_ACCESS, accessToken)
        putString(KEY_REFRESH, refreshToken)
        putString(KEY_ORIGIN, origin.name)
        _authOrigin.value = origin
    }

    override fun clear() {
        deleteString(KEY_ACCESS)
        deleteString(KEY_REFRESH)
        deleteString(KEY_ORIGIN)
        _authOrigin.value = AuthOrigin.NONE
    }

    private fun putString(key: String, value: String) {
        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val query = mutableMapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key,
            kSecAttrService to SERVICE_NAME
        )
        
        SecItemDelete(query as NSDictionary as CFDictionaryRef)
        
        query[kSecValueData] = data
        SecItemAdd(query as NSDictionary as CFDictionaryRef, null)
    }

    private fun getString(key: String): String? {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key,
            kSecAttrService to SERVICE_NAME,
            kSecReturnData to true,
            kSecMatchLimit to kSecMatchLimitOne
        )
        
        var result: String? = null
        memScoped {
            val resultPtr = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as NSDictionary as CFDictionaryRef, resultPtr.ptr)
            if (status == errSecSuccess) {
                val data = bridge<NSData>(resultPtr.value)
                result = data?.let { NSString.create(data = it, encoding = NSUTF8StringEncoding) as String? }
            }
        }
        return result
    }

    private fun deleteString(key: String) {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key,
            kSecAttrService to SERVICE_NAME
        )
        SecItemDelete(query as NSDictionary as CFDictionaryRef)
    }

    // Workaround for CoreFoundation casting in Kotlin/Native
    @OptIn(kotlinx.cinterop.BetaInteropApi::class)
    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> bridge(cfTypeRef: CFTypeRef?): T? {
        if (cfTypeRef == null) return null
        return CFBridgingRelease(cfTypeRef) as T?
    }

    private companion object {
        const val SERVICE_NAME = "com.dev.memebattle.secure_token_prefs"
        const val KEY_ACCESS  = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_ORIGIN  = "auth_origin"
    }
}
