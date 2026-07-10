package com.dev.memebattle.feature.packs.impl.presentation.store.create

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.memebattle.core.domain.packs.model.SafetyLevel

interface PacksCreateStore : Store<PacksCreateStore.Intent, PacksCreateStore.State, PacksCreateStore.Effect> {

    enum class PackType { Memes, Situations }

    sealed interface Intent {
        data object Close : Intent
        data class UpdateName(val name: String) : Intent
        data class UpdateDescription(val description: String) : Intent
        data class UpdateType(val type: PackType) : Intent
        data class UpdateIsPublic(val isPublic: Boolean) : Intent
        data class UpdateLanguage(val languageCode: String) : Intent
        data class UpdateSafetyLevel(val safetyLevel: SafetyLevel) : Intent
        data class AddPrompt(val prompt: String) : Intent
        data class RemovePrompt(val index: Int) : Intent
        data object AddMemePlaceholder : Intent
        data object RemoveMemePlaceholder : Intent
        data class UpdateSelectedFiles(val files: List<io.github.vinceglb.filekit.core.PlatformFile>) : Intent
        data object Create : Intent
    }

    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val type: PackType = PackType.Memes,
        val name: String = "",
        val description: String = "",
        val isPublic: Boolean = true,
        val languageCode: String = "ru",
        val safetyLevel: SafetyLevel = SafetyLevel.FAMILY_FRIENDLY,
        val prompts: List<String> = emptyList(),
        val selectedFiles: List<io.github.vinceglb.filekit.core.PlatformFile> = emptyList()
    ) {
        val isCreateEnabled: Boolean
            get() = name.isNotBlank() && !isLoading &&
                (if (type == PackType.Memes) selectedFiles.isNotEmpty() else prompts.isNotEmpty())
    }

    sealed interface Effect {
        data object NavigateBack : Effect
        data class Created(val packId: String) : Effect
        data class ShowError(val message: String) : Effect
    }
}
