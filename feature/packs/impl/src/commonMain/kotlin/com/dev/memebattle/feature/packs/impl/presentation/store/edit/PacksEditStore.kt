package com.dev.memebattle.feature.packs.impl.presentation.store.edit

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.memebattle.core.domain.packs.model.MemeCard
import com.dev.memebattle.core.domain.packs.model.SafetyLevel
import com.dev.memebattle.core.domain.packs.model.SituationCard

interface PacksEditStore : Store<PacksEditStore.Intent, PacksEditStore.State, PacksEditStore.Effect> {

    sealed interface Intent {
        data object Close : Intent
        data class Load(val packId: String, val kind: String) : Intent
        data class UpdateName(val name: String) : Intent
        data class UpdateDescription(val description: String) : Intent
        data class UpdateIsPublic(val isPublic: Boolean) : Intent
        data class UpdateSafetyLevel(val safetyLevel: SafetyLevel) : Intent
        data class AddPrompt(val prompt: String) : Intent
        data class RemovePrompt(val index: Int) : Intent
        data class UpdateSelectedFiles(val files: List<io.github.vinceglb.filekit.core.PlatformFile>) : Intent
        data class DeleteMemeCard(val cardId: String) : Intent
        data class DeleteSituationCard(val cardId: String) : Intent
        data class UpdateLanguage(val languageCode: String) : Intent
        data object DeletePack : Intent
        data object Save : Intent
    }

    data class State(
        val packId: String = "",
        val kind: String = "meme",
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,
        val name: String = "",
        val description: String = "",
        val isPublic: Boolean = true,
        val languageCode: String = "ru",
        val safetyLevel: SafetyLevel = SafetyLevel.FAMILY_FRIENDLY,
        
        val memeCards: List<MemeCard> = emptyList(),
        val situationCards: List<SituationCard> = emptyList(),
        
        val selectedFiles: List<io.github.vinceglb.filekit.core.PlatformFile> = emptyList(),
        val promptsToAdd: List<String> = emptyList()
    ) {
        val isSaveEnabled: Boolean
            get() = name.isNotBlank() && !isSaving && !isLoading
    }

    sealed interface Effect {
        data object NavigateBack : Effect
        data class Saved(val packId: String, val kind: String) : Effect
        data class ShowNotification(val message: String, val isError: Boolean = false) : Effect
        data object Deleted : Effect
    }
}
