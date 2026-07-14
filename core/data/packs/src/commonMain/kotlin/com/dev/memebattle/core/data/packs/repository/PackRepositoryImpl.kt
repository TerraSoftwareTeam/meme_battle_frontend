package com.dev.memebattle.core.data.packs.repository

import com.dev.memebattle.core.data.packs.mapper.toDomain
import com.dev.memebattle.core.data.packs.mapper.toDto
import com.dev.memebattle.core.data.packs.mapper.toLanguageCodeDto
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.MemePackDetails
import com.dev.memebattle.core.domain.packs.model.SafetyLevel
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.core.domain.packs.model.SituationPackDetails
import com.dev.memebattle.core.domain.packs.repository.PackRepository
import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.memebattle.core.network.error.NetworkError
import com.dev.network.game.current.api.GameApiService
import com.dev.network.game.current.dto.AddMemesToPackRequest
import com.dev.network.game.current.dto.AddSituationsToPackRequest
import com.dev.network.game.current.dto.CreateMemePackRequest
import com.dev.network.game.current.dto.CreateSituationPackRequest
import com.dev.network.game.current.dto.UpdateMemePackRequest
import com.dev.network.game.current.dto.UpdateSituationPackRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class PackRepositoryImpl(
    private val api: GameApiService,
) : PackRepository {

    // ─── Реактивные источники истины ──────────────────────────────────────────

    private val _memePacks = MutableStateFlow<List<MemePack>>(emptyList())
    override val memePacks: StateFlow<List<MemePack>> = _memePacks.asStateFlow()

    private val _situationPacks = MutableStateFlow<List<SituationPack>>(emptyList())
    override val situationPacks: StateFlow<List<SituationPack>> = _situationPacks.asStateFlow()

    private val _myMemePacks = MutableStateFlow<List<MemePack>>(emptyList())
    override val myMemePacks: StateFlow<List<MemePack>> = _myMemePacks.asStateFlow()

    private val _mySituationPacks = MutableStateFlow<List<SituationPack>>(emptyList())
    override val mySituationPacks: StateFlow<List<SituationPack>> = _mySituationPacks.asStateFlow()

    private val _packUpdates = kotlinx.coroutines.flow.MutableSharedFlow<String>(extraBufferCapacity = 10)
    override val packUpdates: kotlinx.coroutines.flow.SharedFlow<String> = _packUpdates.asSharedFlow()

    // ─── In-memory кэш деталей ────────────────────────────────────────────────

    private val memeDetailsCache = mutableMapOf<String, MemePackDetails>()
    private val situationDetailsCache = mutableMapOf<String, SituationPackDetails>()

    // ─── Детали (cache-first) ─────────────────────────────────────────────────

    override suspend fun getMemePackDetails(id: String): Result<MemePackDetails> {
        memeDetailsCache[id]?.let { return Result.success(it) }
        return api.getMemePack(id).toResult { dto ->
            dto.toDomain().also { memeDetailsCache[id] = it }
        }
    }

    override suspend fun getSituationPackDetails(id: String): Result<SituationPackDetails> {
        situationDetailsCache[id]?.let { return Result.success(it) }
        return api.getSituationPack(id).toResult { dto ->
            dto.toDomain().also { situationDetailsCache[id] = it }
        }
    }

    // ─── Обновление списков ───────────────────────────────────────────────────

    override suspend fun refreshMemePacks(): Result<Unit> =
        api.listMemePacks().toResult { dtos ->
            _memePacks.value = dtos.map { it.toDomain() }
        }

    override suspend fun refreshSituationPacks(): Result<Unit> =
        api.listSituationPacks().toResult { dtos ->
            _situationPacks.value = dtos.map { it.toDomain() }
        }

    override suspend fun refreshMyMemePacks(): Result<Unit> =
        api.listUserMemePacks().toResult { dtos ->
            _myMemePacks.value = dtos.map { it.toDomain().pack }
        }

    override suspend fun refreshMySituationPacks(): Result<Unit> =
        api.listUserSituationPacks().toResult { dtos ->
            _mySituationPacks.value = dtos.map { it.toDomain().pack }
        }

    // ─── Мутации мем-паков ────────────────────────────────────────────────────

    override suspend fun createMemePack(
        name: String,
        description: String?,
        isPublic: Boolean,
        languageCode: String,
        safetyLevel: SafetyLevel,
        mediaIds: List<Long>,
    ): Result<MemePack> {
        val body = CreateMemePackRequest(
            name = name,
            description = description,
            is_public = isPublic,
            language_code = languageCode.toLanguageCodeDto(),
            safety_level = safetyLevel.toDto(),
            media_ids = mediaIds,
        )
        return api.createMemePack(body).toResult { response ->
            // После создания — обновляем весь список для получения полного объекта
            val newPack = MemePack(
                id = response.id,
                name = name,
                description = description,
                isPublic = isPublic,
                authorId = "",   // будет перезаписан при следующем refresh
                languageCode = languageCode,
                createdAt = "",
                safetyLevel = safetyLevel,
            )
            _memePacks.update { it + newPack }
            _myMemePacks.update { it + newPack }
            newPack
        }
    }

    override suspend fun updateMemePack(
        id: String,
        name: String,
        description: String?,
        isPublic: Boolean,
        languageCode: String,
        safetyLevel: SafetyLevel,
    ): Result<Unit> {
        val body = UpdateMemePackRequest(
            name = name,
            description = description,
            is_public = isPublic,
            language_code = languageCode.toLanguageCodeDto(),
            safety_level = safetyLevel.toDto(),
        )
        return api.updateMemePack(id, body).toResult {
            // Обновляем элемент в кэше списка in-place
            _memePacks.update { list ->
                list.map { pack ->
                    if (pack.id == id) pack.copy(
                        name = name,
                        description = description,
                        isPublic = isPublic,
                        languageCode = languageCode,
                        safetyLevel = safetyLevel,
                    ) else pack
                }
            }
            _myMemePacks.update { list ->
                list.map { pack ->
                    if (pack.id == id) pack.copy(
                        name = name,
                        description = description,
                        isPublic = isPublic,
                        languageCode = languageCode,
                        safetyLevel = safetyLevel,
                    ) else pack
                }
            }
            // Инвалидируем кэш деталей — при следующем открытии перезагрузится
            memeDetailsCache.remove(id)
            _packUpdates.tryEmit(id)
        }
    }

    override suspend fun deleteMemePack(id: String): Result<Unit> =
        api.deleteMemePack(id).toResult {
            _memePacks.update { list -> list.filter { it.id != id } }
            _myMemePacks.update { list -> list.filter { it.id != id } }
            memeDetailsCache.remove(id)
            _packUpdates.tryEmit(id)
        }

    override suspend fun addMemesToPack(packId: String, mediaIds: List<Long>): Result<Unit> =
        api.addMemesToPack(packId, AddMemesToPackRequest(media_ids = mediaIds)).toResult {
            // Инвалидируем детали пака — карточки изменились
            memeDetailsCache.remove(packId)
            _packUpdates.tryEmit(packId)
        }

    override suspend fun deleteMemeFromPack(packId: String, memeId: String): Result<Unit> =
        api.deletePackMeme(packId, memeId).toResult {
            // Инвалидируем детали и обновляем локально если уже загружены
            memeDetailsCache[packId]?.let { cached ->
                memeDetailsCache[packId] = cached.copy(
                    memes = cached.memes.filter { it.id != memeId }
                )
            }
            _packUpdates.tryEmit(packId)
        }

    // ─── Мутации ситуационных паков ───────────────────────────────────────────

    override suspend fun createSituationPack(
        name: String,
        description: String?,
        isPublic: Boolean,
        languageCode: String,
        safetyLevel: SafetyLevel,
        prompts: List<String>,
    ): Result<SituationPack> {
        val body = CreateSituationPackRequest(
            name = name,
            description = description,
            is_public = isPublic,
            language_code = languageCode.toLanguageCodeDto(),
            safety_level = safetyLevel.toDto(),
            prompts = prompts,
        )
        return api.createSituationPack(body).toResult { response ->
            val newPack = SituationPack(
                id = response.id,
                name = name,
                description = description,
                isPublic = isPublic,
                authorId = "",
                languageCode = languageCode,
                createdAt = "",
                safetyLevel = safetyLevel,
            )
            _situationPacks.update { it + newPack }
            _mySituationPacks.update { it + newPack }
            newPack
        }
    }

    override suspend fun updateSituationPack(
        id: String,
        name: String,
        description: String?,
        isPublic: Boolean,
        languageCode: String,
        safetyLevel: SafetyLevel,
    ): Result<Unit> {
        val body = UpdateSituationPackRequest(
            name = name,
            description = description,
            is_public = isPublic,
            language_code = languageCode.toLanguageCodeDto(),
            safety_level = safetyLevel.toDto(),
        )
        return api.updateSituationPack(id, body).toResult {
            _situationPacks.update { list ->
                list.map { pack ->
                    if (pack.id == id) pack.copy(
                        name = name,
                        description = description,
                        isPublic = isPublic,
                        languageCode = languageCode,
                        safetyLevel = safetyLevel,
                    ) else pack
                }
            }
            _mySituationPacks.update { list ->
                list.map { pack ->
                    if (pack.id == id) pack.copy(
                        name = name,
                        description = description,
                        isPublic = isPublic,
                        languageCode = languageCode,
                        safetyLevel = safetyLevel,
                    ) else pack
                }
            }
            situationDetailsCache.remove(id)
            _packUpdates.tryEmit(id)
        }
    }

    override suspend fun deleteSituationPack(id: String): Result<Unit> =
        api.deleteSituationPack(id).toResult {
            _situationPacks.update { list -> list.filter { it.id != id } }
            _mySituationPacks.update { list -> list.filter { it.id != id } }
            situationDetailsCache.remove(id)
            _packUpdates.tryEmit(id)
        }

    override suspend fun addSituationsToPack(packId: String, prompts: List<String>): Result<Unit> =
        api.addSituationsToPack(packId, AddSituationsToPackRequest(prompts = prompts)).toResult {
            situationDetailsCache.remove(packId)
            _packUpdates.tryEmit(packId)
        }

    override suspend fun deleteSituationFromPack(packId: String, situationId: String): Result<Unit> =
        api.deletePackSituation(packId, situationId).toResult {
            situationDetailsCache[packId]?.let { cached ->
                situationDetailsCache[packId] = cached.copy(
                    situations = cached.situations.filter { it.id != situationId }
                )
            }
            _packUpdates.tryEmit(packId)
        }

    // ─── Вспомогательный маппинг NetworkResult → Result ──────────────────────

    private inline fun <T, R> NetworkResult<T>.toResult(transform: (T) -> R): Result<R> =
        when (this) {
            is NetworkResult.Success -> runCatching { transform(data) }
            is NetworkResult.Error -> Result.failure(error.toException())
        }

    private fun NetworkError.toException(): Throwable = when (this) {
        is NetworkError.Unauthorized -> Exception("Unauthorized (401)")
        is NetworkError.Forbidden -> Exception("Forbidden (403)")
        is NetworkError.NotFound -> Exception("Not found (404)")
        is NetworkError.Timeout -> Exception("Request timeout")
        is NetworkError.NoInternet -> Exception("No internet connection")
        is NetworkError.ServerError -> Exception("Server error ($code)")
        is NetworkError.ApiException -> Exception("API error $code: $message")
        is NetworkError.Exception -> cause
        is NetworkError.Unknown -> Exception("Unknown network error")
    }
}
