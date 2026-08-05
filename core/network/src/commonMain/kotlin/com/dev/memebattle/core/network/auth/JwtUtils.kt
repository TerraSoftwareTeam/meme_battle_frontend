package com.dev.memebattle.core.network.auth

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Decodes the `sub` (subject / userId) claim from a JWT access token without
 * verifying the signature.  Used client-side only for display / routing —
 * server always re-validates the token.
 */
@OptIn(ExperimentalEncodingApi::class)
fun decodeJwtSub(jwt: String): String? = runCatching {
    // JWT = header.payload.signature  (all Base64url-encoded)
    val payloadEncoded = jwt.split(".").getOrNull(1) ?: return null

    // Base64url → Base64: replace URL-safe chars and add padding
    val base64 = payloadEncoded
        .replace('-', '+')
        .replace('_', '/')
        .let { it + "=".repeat((4 - it.length % 4) % 4) }

    val json = Base64.decode(base64).decodeToString()

    // Simple regex extraction — no full JSON parser needed here
    Regex(""""sub"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.getOrNull(1)
}.getOrNull()
