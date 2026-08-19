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
import com.dev.network.game.current.dto.LanguageCode
internal fun MemePackDto.toDomain(): MemePack = MemePack(
    id = id,
    name = name,
    description = description,
    isPublic = is_public,
    authorId = author_id,
    languageCode = language_code.toDomain(),
    createdAt = created_at,
    safetyLevel = safety_level.toDomain(),
)

object PlatformEnv {
    var webOrigin: String? = null
}

internal fun PackMemeDetailsDto.toDomain(): MemeCard {
    val fullUrl = when {
        // Relative path → prepend API base (use local proxy on web to avoid CORS)
        media_url.startsWith("/") -> {
            val origin = PlatformEnv.webOrigin
            if (origin != null) "$origin/api-proxy$media_url"
            else "${com.dev.memebattle.core.network.BuildKonfig.API_BASE_URL}$media_url"
        }
        // CDN URL → rewrite through local /cdn-proxy only on Web to fix CORS ("*, *" header bug).
        // On Android/native webOrigin is null, so we load the CDN URL directly.
        media_url.contains("cdn.hackclub.com") || media_url.contains("user-cdn.hackclub-assets.com") -> {
            val prefix = PlatformEnv.webOrigin
            if (prefix != null) {
                media_url
                    .replace("https://user-cdn.hackclub-assets.com", "$prefix/cdn-proxy")
                    .replace("http://user-cdn.hackclub-assets.com", "$prefix/cdn-proxy")
                    .replace("https://cdn.hackclub.com", "$prefix/cdn-proxy")
                    .replace("http://cdn.hackclub.com", "$prefix/cdn-proxy")
            } else {
                // Native platform (Android/iOS): load CDN directly, no CORS restriction
                media_url
            }
        }
        else -> media_url
    }
    println("PackMemeDetailsDto.toDomain: mapping '$media_url' -> '$fullUrl' (webOrigin=${PlatformEnv.webOrigin})")
    return MemeCard(
        id = id,
        packId = pack_id,
        mediaId = media_id,
        mediaUrl = fullUrl,
    )
}

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
    languageCode = language_code.toDomain(),
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

internal fun LanguageCode.toDomain(): String = when (this) {
    LanguageCode.RU -> "ru"
    LanguageCode.EN -> "en"
    LanguageCode.UND -> "und"
}

internal fun String.toLanguageCodeDto(): LanguageCode = when (this.lowercase()) {
    "ru" -> LanguageCode.RU
    "en" -> LanguageCode.EN
    else -> LanguageCode.UND
}
