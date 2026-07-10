package com.dev.memebattle.core.data.packs.mapper

import com.dev.memebattle.core.domain.packs.model.MemeCard
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.MemePackDetails
import com.dev.memebattle.core.domain.packs.model.SafetyLevel
import com.dev.memebattle.core.domain.packs.model.SituationCard
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.core.domain.packs.model.SituationPackDetails
import com.dev.network.game.current.dto.ContentSafetyLevel
import com.dev.network.game.current.dto.MemePackDetailsResponse
import com.dev.network.game.current.dto.MemePackDto
import com.dev.network.game.current.dto.PackMemeDetailsDto
import com.dev.network.game.current.dto.PackSituationDto
import com.dev.network.game.current.dto.SituationPackDetailsResponse
import com.dev.network.game.current.dto.SituationPackDto

internal fun MemePackDto.toDomain(): MemePack = MemePack(
    id = id,
    name = name,
    description = description,
    isPublic = is_public,
    authorId = author_id,
    languageCode = language_code,
    createdAt = created_at,
    safetyLevel = safety_level.toDomain(),
)

internal fun PackMemeDetailsDto.toDomain(): MemeCard = MemeCard(
    id = id,
    packId = pack_id,
    mediaId = media_id,
    mediaUrl = media_url,
)

internal fun MemePackDetailsResponse.toDomain(): MemePackDetails = MemePackDetails(
    pack = pack.toDomain(),
    memes = memes.map { it.toDomain() },
)

internal fun SituationPackDto.toDomain(): SituationPack = SituationPack(
    id = id,
    name = name,
    description = description,
    isPublic = is_public,
    authorId = author_id,
    languageCode = language_code,
    createdAt = created_at,
    safetyLevel = safety_level.toDomain(),
)

internal fun PackSituationDto.toDomain(): SituationCard = SituationCard(
    id = id,
    packId = pack_id,
    promptText = prompt_text,
)

internal fun SituationPackDetailsResponse.toDomain(): SituationPackDetails = SituationPackDetails(
    pack = pack.toDomain(),
    situations = situations.map { it.toDomain() },
)

internal fun ContentSafetyLevel.toDomain(): SafetyLevel = when (this) {
    ContentSafetyLevel.FAMILY_FRIENDLY -> SafetyLevel.FAMILY_FRIENDLY
    ContentSafetyLevel.SPICY -> SafetyLevel.SPICY
    ContentSafetyLevel.EXPLICIT -> SafetyLevel.EXPLICIT
}

internal fun SafetyLevel.toDto(): ContentSafetyLevel = when (this) {
    SafetyLevel.FAMILY_FRIENDLY -> ContentSafetyLevel.FAMILY_FRIENDLY
    SafetyLevel.SPICY -> ContentSafetyLevel.SPICY
    SafetyLevel.EXPLICIT -> ContentSafetyLevel.EXPLICIT
}
