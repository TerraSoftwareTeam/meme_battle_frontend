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
    val payloadEncoded = jwt.split(".").getOrNull(1) ?: return null
    val json = Base64.UrlSafe.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL)
        .decode(payloadEncoded)
        .decodeToString()

    Regex(""""sub"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.getOrNull(1)
}.getOrNull()
