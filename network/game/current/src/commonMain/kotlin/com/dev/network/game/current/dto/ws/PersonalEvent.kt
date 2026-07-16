package com.dev.network.game.current.dto.ws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface PersonalEvent {
    @Serializable
    @SerialName("hand_updated")
    data class HandUpdated(
        @SerialName("round_id") val roundId: String,
        @SerialName("cards") val cards: List<HandCard>
    ) : PersonalEvent
}
