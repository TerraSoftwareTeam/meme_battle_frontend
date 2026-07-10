package com.dev.memebattle.core.domain.packs.model

data class MemePack(
    val id: String,
    val name: String,
    val description: String?,
    val isPublic: Boolean,
    val authorId: String,
    val languageCode: String,
    val createdAt: String,
    val safetyLevel: SafetyLevel,
)

data class MemeCard(
    val id: String,
    val packId: String,
    val mediaId: Long?,
    val mediaUrl: String,
)

data class MemePackDetails(
    val pack: MemePack,
    val memes: List<MemeCard>,
)
