package com.dev.memebattle.feature.gameplay.impl.presentation.store.info

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.network.game.current.dto.GameMode
import com.dev.network.game.current.dto.RoundPhase

/**
 * Store для InfoScreen — таймер, статистика раунда, кнопки хоста.
 */
interface GameplayInfoStore : Store<GameplayInfoStore.Intent, GameplayInfoStore.State, GameplayInfoStore.Effect> {

    sealed interface Intent {
        data class Initialize(val snapshot: com.dev.network.game.current.dto.GameStateDto?) : Intent
        data object Init : Intent
        /** Хост нажал "Начать игру" (только в фазе Lobby). */
        data object StartGame : Intent
        /** Переключить статус готовности (только в фазе Lobby). */
        data class SetReady(val isReady: Boolean) : Intent
    }

    data class State(
        val isLoading: Boolean = true,
        val mode: GameMode? = null,
        /** Текущая фаза раунда. Lobby = RoundPhase.WAITING. */
        val phase: RoundPhase = RoundPhase.WAITING,
        val roundNumber: Int = 0,
        val totalRounds: Int = 0,
        /** ISO-строка дедлайна фазы; View строит обратный отсчёт. null — таймера нет. */
        val phaseExpiresAt: String? = null,
        val playerCount: Int = 0,
        val readyCount: Int = 0,
        /** Кол-во игроков, подавших карту (из submission_received событий). */
        val submittedCount: Int = 0,
        /** Кол-во проголосовавших в текущем раунде (из vote_received событий). */
        val votedCount: Int = 0,
        /** Является ли текущий пользователь хостом игры. */
        val isHost: Boolean = false,
        val isStartingGame: Boolean = false,
        val isSettingReady: Boolean = false,
        /** Текущий статус готовности «я». */
        val amIReady: Boolean = false,
    ) {
        /** Кнопка "Начать игру" доступна: хост, все готовы, минимум 3 игрока. */
        val canStartGame: Boolean
            get() = isHost
                && phase == RoundPhase.WAITING
                && playerCount >= 3
                && readyCount == playerCount
                && !isStartingGame

        val modeLabel: String
            get() = when (mode) {
                GameMode.SITUATION_TO_MEME -> "Ситуация - Мем"
                GameMode.MEME_TO_SITUATION -> "Мем - Ситуация"
                null -> "—"
            }

        val phaseLabel: String
            get() = when (phase) {
                RoundPhase.WAITING -> "Ожидание"
                RoundPhase.SUBMITTING -> "Выбор карты"
                RoundPhase.VOTING -> "Голосование"
                RoundPhase.FINISHED -> "Завершено"
            }
    }

    sealed interface Effect {
        data class ShowError(val message: String) : Effect
    }
}
