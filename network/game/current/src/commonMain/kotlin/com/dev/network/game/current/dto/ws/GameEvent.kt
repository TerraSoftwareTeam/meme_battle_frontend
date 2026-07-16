package com.dev.network.game.current.dto.ws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface GameEvent {

    @Serializable
    @SerialName("player_joined")
    data class PlayerJoined(
        @SerialName("user_id") val userId: String,
        @SerialName("players_count") val playersCount: Int
    ) : GameEvent

    @Serializable
    @SerialName("player_ready_changed")
    data class PlayerReadyChanged(
        @SerialName("user_id") val userId: String,
        @SerialName("is_ready") val isReady: Boolean
    ) : GameEvent

    @Serializable
    @SerialName("game_started")
    data class GameStarted(
        @SerialName("rounds_count") val roundsCount: Int,
        @SerialName("hand_size") val handSize: Int,
        @SerialName("current_round_number") val currentRoundNumber: Int
    ) : GameEvent

    @Serializable
    @SerialName("round_started")
    data class RoundStarted(
        @SerialName("round_id") val roundId: String,
        @SerialName("round_number") val roundNumber: Int,
        @SerialName("phase") val phase: String,
        @SerialName("prompt_kind") val promptKind: String,
        @SerialName("prompt_content") val promptContent: String,
        @SerialName("phase_expires_at") val phaseExpiresAt: String
    ) : GameEvent

    @Serializable
    @SerialName("submission_received")
    data class SubmissionReceived(
        @SerialName("round_id") val roundId: String,
        @SerialName("user_id") val userId: String
    ) : GameEvent

    @Serializable
    @SerialName("round_phase_changed")
    data class RoundPhaseChanged(
        @SerialName("round_id") val roundId: String,
        @SerialName("phase") val phase: String,
        @SerialName("phase_expires_at") val phaseExpiresAt: String
    ) : GameEvent

    @Serializable
    @SerialName("vote_received")
    data class VoteReceived(
        @SerialName("round_id") val roundId: String,
        @SerialName("voter_id") val voterId: String
    ) : GameEvent

    @Serializable
    @SerialName("round_finished")
    data class RoundFinished(
        @SerialName("round_id") val roundId: String,
        @SerialName("round_number") val roundNumber: Int,
        @SerialName("winner_user_id") val winnerUserId: String?,
        @SerialName("scoreboard") val scoreboard: List<ScoreboardEntry>,
        @SerialName("round_scoreboard") val roundScoreboard: List<ScoreboardEntry>
    ) : GameEvent

    @Serializable
    @SerialName("game_finished")
    data class GameFinished(
        @SerialName("winner_user_id") val winnerUserId: String?,
        @SerialName("final_scoreboard") val finalScoreboard: List<ScoreboardEntry>
    ) : GameEvent

}

@Serializable
data class HandCard(
    @SerialName("id") val id: String,
    @SerialName("kind") val kind: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("text") val text: String? = null
)

@Serializable
data class ScoreboardEntry(
    @SerialName("user_id") val userId: String,
    @SerialName("score") val score: Int
)
