package com.dev.memebattle.feature.gameplay.impl.presentation.store

import com.arkivanov.mvikotlin.core.store.Store

/**
 * MVI Store для всей игровой сессии.
 *
 * Охватывает все фазы одной партии:
 *   Lobby (ожидание готовности) → Submitting (выбор карты) → Voting (голосование) → Results (итоги раунда/игры)
 *
 * Заполняется в будущем по мере реализации каждой фазы.
 */
interface GameplayStore : Store<GameplayStore.Intent, GameplayStore.State, GameplayStore.Effect> {

    sealed interface Intent {
        /** Вызывается сразу при создании компонента — инициирует загрузку снимка состояния и WS-подписку. */
        data object Init : Intent
    }

    data class State(
        val gameId: String = "",
        val isLoading: Boolean = false,
    )

    sealed interface Effect {
        data object NavigateBack : Effect
    }
}
