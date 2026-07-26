package com.dev.memebattle.core.data.packs.local

import platform.Foundation.NSUserDefaults

class IosPackLikesLocalDataSource : PackLikesLocalDataSource {
    
    private val defaults = NSUserDefaults.standardUserDefaults
    
    override suspend fun getLikedMemePackIds(): Set<String> {
        val array = defaults.arrayForKey(KEY_MEME_PACKS) as? List<String>
        return array?.toSet() ?: emptySet()
    }
    
    override suspend fun getLikedSituationPackIds(): Set<String> {
        val array = defaults.arrayForKey(KEY_SITUATION_PACKS) as? List<String>
        return array?.toSet() ?: emptySet()
    }
    
    override suspend fun addLikedMemePack(id: String) {
        val current = getLikedMemePackIds().toMutableSet()
        current.add(id)
        defaults.setObject(current.toList(), forKey = KEY_MEME_PACKS)
    }
    
    override suspend fun removeLikedMemePack(id: String) {
        val current = getLikedMemePackIds().toMutableSet()
        current.remove(id)
        defaults.setObject(current.toList(), forKey = KEY_MEME_PACKS)
    }
    
    override suspend fun addLikedSituationPack(id: String) {
        val current = getLikedSituationPackIds().toMutableSet()
        current.add(id)
        defaults.setObject(current.toList(), forKey = KEY_SITUATION_PACKS)
    }
    
    override suspend fun removeLikedSituationPack(id: String) {
        val current = getLikedSituationPackIds().toMutableSet()
        current.remove(id)
        defaults.setObject(current.toList(), forKey = KEY_SITUATION_PACKS)
    }
    
    override suspend fun setLikedMemePackIds(ids: Set<String>) {
        defaults.setObject(ids.toList(), forKey = KEY_MEME_PACKS)
    }
    
    override suspend fun setLikedSituationPackIds(ids: Set<String>) {
        defaults.setObject(ids.toList(), forKey = KEY_SITUATION_PACKS)
    }
    
    override suspend fun clear() {
        defaults.removeObjectForKey(KEY_MEME_PACKS)
        defaults.removeObjectForKey(KEY_SITUATION_PACKS)
    }
    
    companion object {
        private const val KEY_MEME_PACKS = "liked_meme_pack_ids"
        private const val KEY_SITUATION_PACKS = "liked_situation_pack_ids"
    }
}
