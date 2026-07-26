package com.dev.memebattle.feature.gameplay.impl.presentation.store.players

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.network.game.current.dto.GameCard

/**
 * Store для PlayersScreen — список участников с их статусами и механика голосования.
 *
 * В фазе Voting каждый игрок может нести [PlayerUiModel.submissionCard] —
 * карту которую он подал (если бэкенд отдаёт user_id в submissions).
 * Пока это TODO — UI уже готов к такому поведению.
 */
interface GameplayPlayersStore : Store<GameplayPlayersStore.Intent, GameplayPlayersStore.State, GameplayPlayersStore.Effect> {

    /**
     * UI-модель одного игрока.
     * [handle] — итоговый (resolved бэкендом после joinGame).
     */
    data class PlayerUiModel(
        val userId: String,
        val handle: String,
        val score: Int,
        val isReady: Boolean,
        val hasSubmitted: Boolean,
        val hasVoted: Boolean = false,
        /** true — текущий авторизованный пользователь. */
        val isMe: Boolean = false,
        /**
         * Карта игрока для фазы Voting.
         * null — не известна (анонимное голосование или данные ещё не загружены).
         */
        val submissionCard: GameCard? = null,
        /**
         * ID submission этого игрока (для отправки Vote через Effect.VoteRequested).
         * null — не известен или голосование анонимное.
         */
        val submissionId: String? = null,
    )

    sealed interface Intent {
        data object Init : Intent
        /**
         * Пользователь нажал "👁 Карта" на карточке игрока — показать диалог с картой.
         * [userId] — игрок, чью карту хотим посмотреть.
         */
        data class ShowSubmissionPreview(val userId: String) : Intent
        /** Закрыть диалог предпросмотра карты. */
        data object HideSubmissionPreview : Intent
        /**
         * Пользователь нажал "Проголосовать" в диалоге предпросмотра.
         * Действие уходит через [Effect.VoteRequested] → перехватывает GameplayComponentImpl.
         */
        data class VoteForPlayer(val submissionId: String) : Intent
    }

    data class State(
        val isLoading: Boolean = true,
        val players: List<PlayerUiModel> = emptyList(),
        /**
         * userId игрока, чья карта показывается в диалоге.
         * null — диалог закрыт.
         */
        val previewingSubmissionForUserId: String? = null,
    ) {
        /** Данные игрока для диалога (null если диалог закрыт или игрок не найден). */
        val previewPlayer: PlayerUiModel?
            get() = previewingSubmissionForUserId?.let { uid ->
                players.firstOrNull { it.userId == uid }
            }
    }

    sealed interface Effect {
        /**
         * Пользователь нажал "Голосовать" в диалоге.
         * [GameplayComponentImpl] перехватывает и отправляет POST /vote.
         */
        data class VoteRequested(val submissionId: String) : Effect
    }
}
