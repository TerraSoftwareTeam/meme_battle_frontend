package com.dev.memebattle.core.data.packs.local

import kotlinx.browser.localStorage

class WebPackLikesLocalDataSource : PackLikesLocalDataSource {
    
    override suspend fun getLikedMemePackIds(): Set<String> {
        val json = localStorage.getItem(KEY_MEME_PACKS) ?: return emptySet()
        return json.removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotEmpty() }
            .toSet()
    }
    
    override suspend fun getLikedSituationPackIds(): Set<String> {
        val json = localStorage.getItem(KEY_SITUATION_PACKS) ?: return emptySet()
        return json.removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotEmpty() }
            .toSet()
    }
    
    override suspend fun addLikedMemePack(id: String) {
        val current = getLikedMemePackIds().toMutableSet()
        current.add(id)
        localStorage.setItem(KEY_MEME_PACKS, current.toJsonArray())
    }
    
    override suspend fun removeLikedMemePack(id: String) {
        val current = getLikedMemePackIds().toMutableSet()
        current.remove(id)
        localStorage.setItem(KEY_MEME_PACKS, current.toJsonArray())
    }
    
    override suspend fun addLikedSituationPack(id: String) {
        val current = getLikedSituationPackIds().toMutableSet()
        current.add(id)
        localStorage.setItem(KEY_SITUATION_PACKS, current.toJsonArray())
    }
    
    override suspend fun removeLikedSituationPack(id: String) {
        val current = getLikedSituationPackIds().toMutableSet()
        current.remove(id)
        localStorage.setItem(KEY_SITUATION_PACKS, current.toJsonArray())
    }
    
    override suspend fun setLikedMemePackIds(ids: Set<String>) {
        localStorage.setItem(KEY_MEME_PACKS, ids.toJsonArray())
    }
    
    override suspend fun setLikedSituationPackIds(ids: Set<String>) {
        localStorage.setItem(KEY_SITUATION_PACKS, ids.toJsonArray())
    }
    
    override suspend fun clear() {
        localStorage.removeItem(KEY_MEME_PACKS)
        localStorage.removeItem(KEY_SITUATION_PACKS)
    }
    
    private fun Set<String>.toJsonArray(): String {
        if (isEmpty()) return "[]"
        return joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
    }
    
    companion object {
        private const val KEY_MEME_PACKS = "pack_likes_meme"
        private const val KEY_SITUATION_PACKS = "pack_likes_situation"
    }
}
