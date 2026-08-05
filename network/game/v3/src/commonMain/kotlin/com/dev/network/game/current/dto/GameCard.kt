package com.dev.network.game.current.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class GameCard

@Serializable
@SerialName("Meme")
data class MemeGameCard(
    @SerialName("data")
    val data: MemeCardData
) : GameCard()

@Serializable
data class MemeCardData(
    @SerialName("id")
    val id: String,
    @SerialName("media_url")
    val mediaUrl: String
)

@Serializable
@SerialName("Situation")
data class SituationGameCard(
    @SerialName("data")
    val data: SituationCardData
) : GameCard()

@Serializable
data class SituationCardData(
    @SerialName("id")
    val id: String,
    @SerialName("prompt_text")
    val promptText: String
)
