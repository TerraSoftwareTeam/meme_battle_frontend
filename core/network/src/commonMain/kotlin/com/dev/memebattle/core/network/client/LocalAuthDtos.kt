package com.dev.memebattle.core.network.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalAuthBody(
    @SerialName("access_token")  val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("token_type")    val tokenType: String = "Bearer"
)

@Serializable
data class LocalRefreshSessionDto(
    @SerialName("refresh_token") val refreshToken: String
)
