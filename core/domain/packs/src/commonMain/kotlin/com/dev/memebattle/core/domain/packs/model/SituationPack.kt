package com.dev.memebattle.core.domain.packs.model

data class SituationPack(
    val id: String,
    val name: String,
    val description: String?,
    val isPublic: Boolean,
    val authorId: String,
    val languageCode: String,
    val createdAt: String,
    val safetyLevel: SafetyLevel,
)

data class SituationCard(
    val id: String,
    val packId: String,
    val promptText: String,
)

data class SituationPackDetails(
    val pack: SituationPack,
    val situations: List<SituationCard>,
)
