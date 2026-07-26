package com.dev.memebattle.core.data.packs.local

/**
 * Локальное хранилище ID лайкнутых паков.
 * Сохраняет только ID, не полные объекты.
 */
interface PackLikesLocalDataSource {
    
    /** Получить сохраненные ID лайкнутых мем-паков */
    suspend fun getLikedMemePackIds(): Set<String>
    
    /** Получить сохраненные ID лайкнутых ситуационных паков */
    suspend fun getLikedSituationPackIds(): Set<String>
    
    /** Добавить ID мем-пака в избранное */
    suspend fun addLikedMemePack(id: String)
    
    /** Удалить ID мем-пака из избранного */
    suspend fun removeLikedMemePack(id: String)
    
    /** Добавить ID ситуационного пака в избранное */
    suspend fun addLikedSituationPack(id: String)
    
    /** Удалить ID ситуационного пака из избранного */
    suspend fun removeLikedSituationPack(id: String)
    
    /** Заменить все ID лайкнутых мем-паков (используется при синхронизации с сервером) */
    suspend fun setLikedMemePackIds(ids: Set<String>)
    
    /** Заменить все ID лайкнутых ситуационных паков (используется при синхронизации с сервером) */
    suspend fun setLikedSituationPackIds(ids: Set<String>)
    
    /** Очистить все локальные данные */
    suspend fun clear()
}
