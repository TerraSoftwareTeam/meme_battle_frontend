package com.dev.memebattle.feature.packs.impl.presentation.store.details

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.memebattle.core.domain.packs.model.MemeCard
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.MemePackDetails
import com.dev.memebattle.core.domain.packs.model.SituationCard
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.core.domain.packs.model.SituationPackDetails

interface PacksDetailsStore : Store<PacksDetailsStore.Intent, PacksDetailsStore.State, PacksDetailsStore.Effect> {

    enum class PackKind { Meme, Situation }

    sealed interface Intent {
        data class Load(val packId: String, val kind: PackKind) : Intent
        data object Close : Intent
        data object ToggleLike : Intent
    }

    data class State(
        val isLoading: Boolean = false,
        val packId: String? = null,
        val kind: PackKind = PackKind.Meme,

        // Meme pack details
        val memePack: MemePack? = null,
        val memeCards: List<MemeCard> = emptyList(),

        // Situation pack details
        val situationPack: SituationPack? = null,
        val situationCards: List<SituationCard> = emptyList(),

        val isLiked: Boolean = false,
        val isLikeLoading: Boolean = false,
        val error: String? = null,
    )

    sealed interface Effect {
        data object NavigateBack : Effect
    }
}
