package com.dev.memebattle.core.domain.packs.repository

import com.dev.memebattle.core.domain.packs.model.MemeCard
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.MemePackDetails
import com.dev.memebattle.core.domain.packs.model.SafetyLevel
import com.dev.memebattle.core.domain.packs.model.SituationCard
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.core.domain.packs.model.SituationPackDetails
import kotlinx.coroutines.flow.StateFlow

interface PackRepository {

    // ─── Реактивные списки (Single Source of Truth) ───────────────────────────

    /** Список мем-паков. Обновляется при любых мутациях автоматически. */
    val memePacks: StateFlow<List<MemePack>>

    /** Список ситуационных паков. Обновляется при любых мутациях автоматически. */
    val situationPacks: StateFlow<List<SituationPack>>

    // ─── Детали паков (cache-first) ───────────────────────────────────────────

    /**
     * Возвращает детали мем-пака с карточками.
     * Сначала ищет в кэше, при отсутствии — загружает из сети.
     */
    suspend fun getMemePackDetails(id: String): Result<MemePackDetails>

    /**
     * Возвращает детали ситуационного пака с карточками.
     * Сначала ищет в кэше, при отсутствии — загружает из сети.
     */
    suspend fun getSituationPackDetails(id: String): Result<SituationPackDetails>

    // ─── Обновление списков (pull-to-refresh) ─────────────────────────────────

    /** Принудительно загружает список мем-паков из сети и обновляет [memePacks]. */
    suspend fun refreshMemePacks(): Result<Unit>

    /** Принудительно загружает список ситуационных паков из сети и обновляет [situationPacks]. */
    suspend fun refreshSituationPacks(): Result<Unit>

    // ─── Мутации мем-паков ────────────────────────────────────────────────────

    suspend fun createMemePack(
        name: String,
        description: String?,
        isPublic: Boolean,
        languageCode: String,
        safetyLevel: SafetyLevel,
        mediaIds: List<Long>,
    ): Result<MemePack>

    suspend fun updateMemePack(
        id: String,
        name: String,
        description: String?,
        isPublic: Boolean,
        languageCode: String,
        safetyLevel: SafetyLevel,
    ): Result<Unit>

    suspend fun deleteMemePack(id: String): Result<Unit>

    suspend fun addMemesToPack(packId: String, mediaIds: List<Long>): Result<Unit>

    suspend fun deleteMemeFromPack(packId: String, memeId: String): Result<Unit>

    // ─── Мутации ситуационных паков ───────────────────────────────────────────

    suspend fun createSituationPack(
        name: String,
        description: String?,
        isPublic: Boolean,
        languageCode: String,
        safetyLevel: SafetyLevel,
        prompts: List<String>,
    ): Result<SituationPack>

    suspend fun updateSituationPack(
        id: String,
        name: String,
        description: String?,
        isPublic: Boolean,
        languageCode: String,
        safetyLevel: SafetyLevel,
    ): Result<Unit>

    suspend fun deleteSituationPack(id: String): Result<Unit>

    suspend fun addSituationsToPack(packId: String, prompts: List<String>): Result<Unit>

    suspend fun deleteSituationFromPack(packId: String, situationId: String): Result<Unit>
}
