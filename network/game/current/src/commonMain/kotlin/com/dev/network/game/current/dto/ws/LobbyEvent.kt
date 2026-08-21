package com.dev.network.game.current.dto.ws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface LobbyEvent {

    @Serializable
    @SerialName("lobby_created")
    data class LobbyCreated(
        @SerialName("id") val id: String,
        @SerialName("name") val name: String? = null,
        @SerialName("host_id") val hostId: String,
        @SerialName("mode") val mode: String,
        @SerialName("max_rounds") val maxRounds: Int,
        @SerialName("hand_size") val handSize: Int,
        @SerialName("players_count") val playersCount: Int,
        @SerialName("max_players") val maxPlayers: Int? = null,
        @SerialName("created_at") val createdAt: String
    ) : LobbyEvent

    @Serializable
    @SerialName("lobby_updated")
    data class LobbyUpdated(
        @SerialName("id") val id: String,
        @SerialName("players_count") val playersCount: Int
    ) : LobbyEvent

    @Serializable
    @SerialName("lobby_removed")
    data class LobbyRemoved(
        @SerialName("id") val id: String
    ) : LobbyEvent
}
