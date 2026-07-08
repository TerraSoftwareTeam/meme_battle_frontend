package com.dev.network.game.current.dto

import kotlinx.serialization.Serializable

@Serializable
data class GameCard(
    val id: String,
    val text: String? = null,
    val imageUrl: String? = null
)
