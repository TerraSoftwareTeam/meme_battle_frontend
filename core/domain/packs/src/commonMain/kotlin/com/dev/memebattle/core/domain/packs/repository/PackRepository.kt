package com.dev.memebattle.core.domain.packs.repository

import com.dev.memebattle.core.domain.packs.model.MemeCard
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.MemePackDetails
import com.dev.memebattle.core.domain.packs.model.SafetyLevel
import com.dev.memebattle.core.domain.packs.model.SituationCard
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.core.domain.packs.model.SituationPackDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Состояние загрузки лайкнутых паков.
 * Используется для отображения skeleton/placeholder пока грузятся данные.
 */
sealed class LikedPacksState<out T> {
    /**
     * Загрузка. Содержит ID liked паков для отображения placeholder'ов.
     * [count] - количество liked паков (размер списка ID)
     */
    data class Loading(val count: Int) : LikedPacksState<Nothing>()
    
    /**
     * Успешно загружены данные.
     */
    data class Success<T>(val packs: List<T>) : LikedPacksState<T>()
}

interface PackRepository {

    // ─── Реактивные списки (Single Source of Truth) ───────────────────────────

    /** Список мем-паков. Обновляется при любых мутациях автоматически. */
    val memePacks: StateFlow<List<MemePack>>

    /** Список ситуационных паков. Обновляется при любых мутациях автоматически. */
    val situationPacks: StateFlow<List<SituationPack>>

    /** Список личных мем-паков пользователя. */
    val myMemePacks: StateFlow<List<MemePack>>

    /** Список личных ситуационных паков пользователя. */
    val mySituationPacks: StateFlow<List<SituationPack>>
    
    /** Поток событий обновления паков (выдает id пака при изменении его деталей) */
    val packUpdates: kotlinx.coroutines.flow.SharedFlow<String>

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

    suspend fun refreshMyMemePacks(): Result<Unit>

    suspend fun refreshMySituationPacks(): Result<Unit>

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

    // ─── Pack Likes ───────────────────────────────────────────────────────────

    /** Список лайкнутых мем-паков. */
    val likedMemePacks: StateFlow<List<MemePack>>

    /** Список лайкнутых ситуационных паков. */
    val likedSituationPacks: StateFlow<List<SituationPack>>

    suspend fun refreshLikedMemePacks(): Result<Unit>

    suspend fun refreshLikedSituationPacks(): Result<Unit>

    suspend fun likeMemePack(id: String): Result<Unit>

    suspend fun unlikeMemePack(id: String): Result<Unit>

    suspend fun likeSituationPack(id: String): Result<Unit>

    suspend fun unlikeSituationPack(id: String): Result<Unit>

    // ─── Lazy Loading Liked Packs with State Flow ─────────────────────────────

    /**
     * Поток состояний liked мем-паков.
     * Сначала эмитит Loading(count), потом Success(packs).
     * Если данных нет в кэше - автоматически загружает с бэкенда.
     */
    fun observeLikedMemePacks(): Flow<LikedPacksState<MemePack>>

    /**
     * Поток состояний liked ситуационных паков.
     * Сначала эмитит Loading(count), потом Success(packs).
     * Если данных нет в кэше - автоматически загружает с бэкенда.
     */
    fun observeLikedSituationPacks(): Flow<LikedPacksState<SituationPack>>
}
