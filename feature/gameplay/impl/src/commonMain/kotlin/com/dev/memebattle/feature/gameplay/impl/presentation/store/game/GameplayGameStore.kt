package com.dev.memebattle.feature.gameplay.impl.presentation.store.game

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.network.game.current.dto.GameCard
import com.dev.network.game.current.dto.RoundPhase
import com.dev.network.game.current.dto.ws.ScoreboardEntry

/**
 * Store для GameScreen — управляет всеми под-фазами игрового экрана.
 *
 * Жизненный цикл фаз:
 * HandleInput → Lobby → Submitting → Voting → RoundResult → (Submitting...) → GameFinished
 */
interface GameplayGameStore : Store<GameplayGameStore.Intent, GameplayGameStore.State, GameplayGameStore.Effect> {

    /**
     * Внутренняя фаза UI — определяет, какой composable рендерит GameplayGameScreen.
     * Не совпадает один-в-один с [RoundPhase] — включает специфические UI-состояния.
     */
    enum class UiPhase {
        /** Ожидание в лобби: список игроков, кнопка "Готов". */
        Lobby,
        /** Выбор карты из руки для текущего раунда. */
        Submitting,
        /** Просмотр submission-ов других игроков и голосование. */
        Voting,
        /** Оверлей с итогами раунда (~3 сек, затем автопереход). */
        RoundResult,
        /** Финальный full-screen с пьедесталом и кнопкой выхода. */
        GameFinished,
    }

    /** Данные для оверлея итогов раунда. */
    data class RoundResultData(
        val roundNumber: Int,
        val winnerUserId: String?,
        val winnerHandle: String?,
        /** Изменения очков за раунд (userId → delta). */
        val roundScoreboard: List<ScoreboardEntry>,
    )

    sealed interface Intent {
        /** Инициализация с snapshot - вызывается после загрузки данных игры. */
        data class Initialize(val snapshot: com.dev.network.game.current.dto.GameStateDto?) : Intent
        /** Листать карты в руке / submission-ы стрелками. */
        data class SelectCard(val index: Int) : Intent
        /** Подать выбранную карту (фаза Submitting). */
        data object Submit : Intent
        /** Проголосовать за submission (фаза Voting). */
        data class Vote(val submissionId: String) : Intent
        /** Small-экран: переключить между промтом и картой из руки. */
        data object TogglePromptVisible : Intent
        /** Загрузить карты для голосования (вызывается ComponentImpl при переходе в Voting). */
        data class LoadSubmissions(val cards: List<com.dev.network.game.current.dto.GameCard>, val ids: List<String>) : Intent
        /** Пользователь нажал "Выйти" на экране GameFinished. */
        data object ExitGame : Intent
    }

    data class State(
        // ── Фаза UI ─────────────────────────────────────────────────────────
        val uiPhase: UiPhase = UiPhase.Lobby,

        // ── Общие игровые данные ─────────────────────────────────────────────
        val isLoading: Boolean = true,
        val promptCard: GameCard? = null,
        /** ID активного раунда (нужен для submit/vote API-вызовов). */
        val roundId: String? = null,

        // ── Submitting ───────────────────────────────────────────────────────
        /** Карты в руке (фаза Submitting). */
        val handCards: List<GameCard> = emptyList(),
        val selectedCardIndex: Int = 0,
        val mySubmissionCard: GameCard? = null,
        val isSubmitting: Boolean = false,

        // ── Voting ───────────────────────────────────────────────────────────
        /**
         * Анонимные submission-карты для голосования.
         * Индекс соответствует submissionIds — для отправки Vote.
         */
        val submissionCards: List<GameCard> = emptyList(),
        val submissionIds: List<String> = emptyList(),
        val selectedSubmissionIndex: Int = 0,
        val hasVoted: Boolean = false,
        val isVoting: Boolean = false,

        // ── Small-экран toggle ───────────────────────────────────────────────
        /** true — показываем промт, false — карту из руки/submission. */
        val showPrompt: Boolean = true,

        // ── RoundResult ──────────────────────────────────────────────────────
        val roundResult: RoundResultData? = null,

        // ── GameFinished ─────────────────────────────────────────────────────
        val finalScoreboard: List<ScoreboardEntry> = emptyList(),
        val gameWinnerUserId: String? = null,
    ) {
        // ── Computed ─────────────────────────────────────────────────────────
        val selectedHandCard: GameCard? get() = handCards.getOrNull(selectedCardIndex)
        val selectedSubmissionCard: GameCard? get() = submissionCards.getOrNull(selectedSubmissionIndex)
        val selectedSubmissionId: String? get() = submissionIds.getOrNull(selectedSubmissionIndex)

        val canSubmit: Boolean
            get() = uiPhase == UiPhase.Submitting && mySubmissionCard == null && !isSubmitting

        val canVote: Boolean
            get() = uiPhase == UiPhase.Voting && !hasVoted && selectedSubmissionId != null && !isVoting

        /** Отображаемая "активная" карта справа: рука или submission. */
        val activeRightCard: GameCard?
            get() = when (uiPhase) {
                UiPhase.Submitting -> selectedHandCard
                UiPhase.Voting -> selectedSubmissionCard
                else -> null
            }

        val canNavigatePrev: Boolean
            get() = when (uiPhase) {
                UiPhase.Submitting -> selectedCardIndex > 0
                UiPhase.Voting -> selectedSubmissionIndex > 0
                else -> false
            }

        val canNavigateNext: Boolean
            get() = when (uiPhase) {
                UiPhase.Submitting -> selectedCardIndex < handCards.lastIndex
                UiPhase.Voting -> selectedSubmissionIndex < submissionCards.lastIndex
                else -> false
            }
    }

    sealed interface Effect {
        /** Отображение ошибки (toast / snackbar). */
        data class ShowError(val message: String) : Effect
        /** После RoundResult: автоматический переход к следующему раунду. */
        data object RoundResultDismissed : Effect
        /** Пользователь нажал "Выйти" на GameFinished экране. */
        data object ExitGame : Effect
    }
}
