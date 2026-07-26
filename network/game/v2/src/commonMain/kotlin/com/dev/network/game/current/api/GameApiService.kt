package com.dev.network.game.current.api

import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.network.game.current.dto.ActiveGamesResponseDto
import com.dev.network.game.current.dto.AddMemesToPackRequest
import com.dev.network.game.current.dto.AddSituationsToPackRequest
import com.dev.network.game.current.dto.CreateGameRequest
import com.dev.network.game.current.dto.CreateMemePackRequest
import com.dev.network.game.current.dto.CreateMemePackResponse
import com.dev.network.game.current.dto.CreateSituationPackRequest
import com.dev.network.game.current.dto.CreateSituationPackResponse
import com.dev.network.game.current.dto.GameDto
import com.dev.network.game.current.dto.GameStateDto
import com.dev.network.game.current.dto.LobbiesWsTokenDto
import com.dev.network.game.current.dto.MemePackDetailsResponse
import com.dev.network.game.current.dto.MemePackDto
import com.dev.network.game.current.dto.ReadyRequest
import com.dev.network.game.current.dto.SituationPackDetailsResponse
import com.dev.network.game.current.dto.SituationPackDto
import com.dev.network.game.current.dto.SubmitCardRequest
import com.dev.network.game.current.dto.UpdateGameRequest
import com.dev.network.game.current.dto.UpdateMemePackRequest
import com.dev.network.game.current.dto.UpdateSituationPackRequest
import com.dev.network.game.current.dto.VoteRequest
import com.dev.network.game.current.dto.WsTokenDto
import kotlin.String
import kotlin.Unit
import kotlin.collections.List

interface GameApiService {
  suspend fun listActiveGames(): NetworkResult<ActiveGamesResponseDto>

  suspend fun createGame(body: CreateGameRequest): NetworkResult<GameDto>

  suspend fun getLobbiesWsToken(): NetworkResult<LobbiesWsTokenDto>

  suspend fun getWsToken(id: String): NetworkResult<WsTokenDto>

  suspend fun listMemePacks(): NetworkResult<List<MemePackDto>>

  suspend fun createMemePack(body: CreateMemePackRequest):
      NetworkResult<CreateMemePackResponse>

  suspend fun listUserMemePacks(): NetworkResult<List<MemePackDetailsResponse>>

  suspend fun getMemePack(id: String): NetworkResult<MemePackDetailsResponse>

  suspend fun deleteMemePack(id: String): NetworkResult<Unit>

  suspend fun updateMemePack(id: String, body: UpdateMemePackRequest): NetworkResult<Unit>

  suspend fun addMemesToPack(id: String, body: AddMemesToPackRequest): NetworkResult<Unit>

  suspend fun deletePackMeme(id: String, meme_id: String): NetworkResult<Unit>

  suspend fun listSituationPacks(): NetworkResult<List<SituationPackDto>>

  suspend fun createSituationPack(body: CreateSituationPackRequest):
      NetworkResult<CreateSituationPackResponse>

  suspend fun listUserSituationPacks(): NetworkResult<List<SituationPackDetailsResponse>>

  suspend fun getSituationPack(id: String): NetworkResult<SituationPackDetailsResponse>

  suspend fun deleteSituationPack(id: String): NetworkResult<Unit>

  suspend fun updateSituationPack(id: String, body: UpdateSituationPackRequest):
      NetworkResult<Unit>

  suspend fun addSituationsToPack(id: String, body: AddSituationsToPackRequest):
      NetworkResult<Unit>

  suspend fun deletePackSituation(id: String, situation_id: String): NetworkResult<Unit>

  suspend fun updateGame(id: String, body: UpdateGameRequest): NetworkResult<GameDto>

  suspend fun joinGame(id: String, body: com.dev.network.game.current.dto.JoinGameRequest): NetworkResult<Unit>

  suspend fun setReady(id: String, body: ReadyRequest): NetworkResult<Unit>

  suspend fun submitCard(
    id: String,
    round_id: String,
    body: SubmitCardRequest,
  ): NetworkResult<Unit>

  suspend fun voteCard(
    id: String,
    round_id: String,
    body: VoteRequest,
  ): NetworkResult<Unit>

  suspend fun startGameSession(id: String): NetworkResult<Unit>

  suspend fun getGameState(id: String): NetworkResult<GameStateDto>

  // ─── Pack Likes ───────────────────────────────────────────────────────────

  suspend fun likeMemePack(id: String): NetworkResult<Unit>

  suspend fun unlikeMemePack(id: String): NetworkResult<Unit>

  suspend fun getLikedMemePacks(): NetworkResult<List<MemePackDto>>

  suspend fun likeSituationPack(id: String): NetworkResult<Unit>

  suspend fun unlikeSituationPack(id: String): NetworkResult<Unit>

  suspend fun getLikedSituationPacks(): NetworkResult<List<SituationPackDto>>
}
