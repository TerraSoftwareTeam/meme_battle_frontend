package com.dev.network.game.current.dto.ws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CentrifugoCommand(
    @SerialName("id") val id: Int,
    @SerialName("connect") val connect: ConnectData? = null,
    @SerialName("subscribe") val subscribe: SubscribeData? = null,
    @SerialName("unsubscribe") val unsubscribe: UnsubscribeData? = null
)

@Serializable
data class ConnectData(
    @SerialName("token") val token: String
)

@Serializable
data class SubscribeData(
    @SerialName("channel") val channel: String,
    @SerialName("token") val token: String,
    @SerialName("recover") val recover: Boolean? = null,
    @SerialName("offset") val offset: Long? = null,
    @SerialName("epoch") val epoch: String? = null
)

@Serializable
data class UnsubscribeData(
    @SerialName("channel") val channel: String
)
