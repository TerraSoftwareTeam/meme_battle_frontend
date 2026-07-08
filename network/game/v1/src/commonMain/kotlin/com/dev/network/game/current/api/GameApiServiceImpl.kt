package com.dev.network.game.current.api

import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.memebattle.core.network.call.safeCall
import com.dev.network.game.current.dto.AddMemesToPackRequest
import com.dev.network.game.current.dto.AddSituationsToPackRequest
import com.dev.network.game.current.dto.CreateGameRequest
import com.dev.network.game.current.dto.CreateMemePackRequest
import com.dev.network.game.current.dto.CreateMemePackResponse
import com.dev.network.game.current.dto.CreateSituationPackRequest
import com.dev.network.game.current.dto.CreateSituationPackResponse
import com.dev.network.game.current.dto.GameDto
import com.dev.network.game.current.dto.GameStateDto
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
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlin.String
import kotlin.Unit
import kotlin.collections.List

class GameApiServiceImpl(
  private val client: HttpClient,
) : GameApiService {
  override suspend fun createGame(body: CreateGameRequest): NetworkResult<GameDto> = safeCall {
    client.post("/games") {
      setBody(body)
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<GameDto>>().data
  }

  override suspend fun listMemePacks(): NetworkResult<List<MemePackDto>> = safeCall {
    client.get("/games/packs/memes") {
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<List<MemePackDto>>>().data
  }

  override suspend fun createMemePack(body: CreateMemePackRequest):
      NetworkResult<CreateMemePackResponse> = safeCall {
    client.post("/games/packs/memes") {
      setBody(body)
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<CreateMemePackResponse>>().data
  }

  override suspend fun getMemePack(id: String): NetworkResult<MemePackDetailsResponse> = safeCall {
    client.get("/games/packs/memes/$id") {
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<MemePackDetailsResponse>>().data
  }

  override suspend fun deleteMemePack(id: String): NetworkResult<Unit> = safeCall {
    client.delete("/games/packs/memes/$id") {
    }
    .let { Unit }
  }

  override suspend fun updateMemePack(id: String, body: UpdateMemePackRequest): NetworkResult<Unit>
      = safeCall {
    client.patch("/games/packs/memes/$id") {
      setBody(body)
    }
    .let { Unit }
  }

  override suspend fun addMemesToPack(id: String, body: AddMemesToPackRequest): NetworkResult<Unit>
      = safeCall {
    client.post("/games/packs/memes/$id/memes") {
      setBody(body)
    }
    .let { Unit }
  }

  override suspend fun deletePackMeme(id: String, meme_id: String): NetworkResult<Unit> = safeCall {
    client.delete("/games/packs/memes/$id/memes/$meme_id") {
    }
    .let { Unit }
  }

  override suspend fun listSituationPacks(): NetworkResult<List<SituationPackDto>> = safeCall {
    client.get("/games/packs/situations") {
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<List<SituationPackDto>>>().data
  }

  override suspend fun createSituationPack(body: CreateSituationPackRequest):
      NetworkResult<CreateSituationPackResponse> = safeCall {
    client.post("/games/packs/situations") {
      setBody(body)
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<CreateSituationPackResponse>>().data
  }

  override suspend fun getSituationPack(id: String): NetworkResult<SituationPackDetailsResponse> =
      safeCall {
    client.get("/games/packs/situations/$id") {
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<SituationPackDetailsResponse>>().data
  }

  override suspend fun deleteSituationPack(id: String): NetworkResult<Unit> = safeCall {
    client.delete("/games/packs/situations/$id") {
    }
    .let { Unit }
  }

  override suspend fun updateSituationPack(id: String, body: UpdateSituationPackRequest):
      NetworkResult<Unit> = safeCall {
    client.patch("/games/packs/situations/$id") {
      setBody(body)
    }
    .let { Unit }
  }

  override suspend fun addSituationsToPack(id: String, body: AddSituationsToPackRequest):
      NetworkResult<Unit> = safeCall {
    client.post("/games/packs/situations/$id/situations") {
      setBody(body)
    }
    .let { Unit }
  }

  override suspend fun deletePackSituation(id: String, situation_id: String): NetworkResult<Unit> =
      safeCall {
    client.delete("/games/packs/situations/$id/situations/$situation_id") {
    }
    .let { Unit }
  }

  override suspend fun updateGame(id: String, body: UpdateGameRequest): NetworkResult<GameDto> =
      safeCall {
    client.patch("/games/$id") {
      setBody(body)
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<GameDto>>().data
  }

  override suspend fun joinGame(id: String): NetworkResult<Unit> = safeCall {
    client.post("/games/$id/join") {
    }
    .let { Unit }
  }

  override suspend fun setReady(id: String, body: ReadyRequest): NetworkResult<Unit> = safeCall {
    client.post("/games/$id/ready") {
      setBody(body)
    }
    .let { Unit }
  }

  override suspend fun submitCard(
    id: String,
    round_id: String,
    body: SubmitCardRequest,
  ): NetworkResult<Unit> = safeCall {
    client.post("/games/$id/rounds/$round_id/submit") {
      setBody(body)
    }
    .let { Unit }
  }

  override suspend fun voteCard(
    id: String,
    round_id: String,
    body: VoteRequest,
  ): NetworkResult<Unit> = safeCall {
    client.post("/games/$id/rounds/$round_id/vote") {
      setBody(body)
    }
    .let { Unit }
  }

  override suspend fun startGameSession(id: String): NetworkResult<Unit> = safeCall {
    client.post("/games/$id/start") {
    }
    .let { Unit }
  }

  override suspend fun getGameState(id: String): NetworkResult<GameStateDto> = safeCall {
    client.get("/games/$id/state") {
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<GameStateDto>>().data
  }

  override suspend fun getWsToken(id: String): NetworkResult<WsTokenDto> = safeCall {
    client.get("/games/$id/ws-token") {
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<WsTokenDto>>().data
  }
}
