package com.dev.memebattle.core.data.packs.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidPackLikesLocalDataSource(
    context: Context
) : PackLikesLocalDataSource {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, 
        Context.MODE_PRIVATE
    )
    
    override suspend fun getLikedMemePackIds(): Set<String> = withContext(Dispatchers.IO) {
        prefs.getStringSet(KEY_MEME_PACKS, emptySet()) ?: emptySet()
    }
    
    override suspend fun getLikedSituationPackIds(): Set<String> = withContext(Dispatchers.IO) {
        prefs.getStringSet(KEY_SITUATION_PACKS, emptySet()) ?: emptySet()
    }
    
    override suspend fun addLikedMemePack(id: String) = withContext(Dispatchers.IO) {
        val current = getLikedMemePackIds().toMutableSet()
        current.add(id)
        prefs.edit().putStringSet(KEY_MEME_PACKS, current).apply()
    }
    
    override suspend fun removeLikedMemePack(id: String) = withContext(Dispatchers.IO) {
        val current = getLikedMemePackIds().toMutableSet()
        current.remove(id)
        prefs.edit().putStringSet(KEY_MEME_PACKS, current).apply()
    }
    
    override suspend fun addLikedSituationPack(id: String) = withContext(Dispatchers.IO) {
        val current = getLikedSituationPackIds().toMutableSet()
        current.add(id)
        prefs.edit().putStringSet(KEY_SITUATION_PACKS, current).apply()
    }
    
    override suspend fun removeLikedSituationPack(id: String) = withContext(Dispatchers.IO) {
        val current = getLikedSituationPackIds().toMutableSet()
        current.remove(id)
        prefs.edit().putStringSet(KEY_SITUATION_PACKS, current).apply()
    }
    
    override suspend fun setLikedMemePackIds(ids: Set<String>) = withContext(Dispatchers.IO) {
        prefs.edit().putStringSet(KEY_MEME_PACKS, ids).apply()
    }
    
    override suspend fun setLikedSituationPackIds(ids: Set<String>) = withContext(Dispatchers.IO) {
        prefs.edit().putStringSet(KEY_SITUATION_PACKS, ids).apply()
    }
    
    override suspend fun clear() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
    }
    
    companion object {
        private const val PREFS_NAME = "pack_likes_prefs"
        private const val KEY_MEME_PACKS = "liked_meme_pack_ids"
        private const val KEY_SITUATION_PACKS = "liked_situation_pack_ids"
    }
}
