package com.dev.network.game.current.api.ws

import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.network.game.current.api.GameApiService
import com.dev.network.game.current.dto.ws.CentrifugoCommand
import com.dev.network.game.current.dto.ws.CentrifugoPush
import com.dev.network.game.current.dto.ws.ConnectData
import com.dev.network.game.current.dto.ws.GameEvent
import com.dev.network.game.current.dto.ws.LobbyEvent
import com.dev.network.game.current.dto.ws.PersonalEvent
import com.dev.network.game.current.dto.ws.SubscribeData
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.takeFrom
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

internal class GameSocketServiceImpl(
    private val httpClient: HttpClient,
    private val gameApiService: GameApiService,
    private val wsBaseUrl: String,
) : GameSocketService {

    private val _gameEvents = MutableSharedFlow<GameEvent>(extraBufferCapacity = 64)
    override val gameEvents = _gameEvents.asSharedFlow()

    private val _personalEvents = MutableSharedFlow<PersonalEvent>(extraBufferCapacity = 64)
    override val personalEvents = _personalEvents.asSharedFlow()

    private val _lobbyEvents = MutableSharedFlow<LobbyEvent>(extraBufferCapacity = 64)
    override val lobbyEvents = _lobbyEvents.asSharedFlow()

    private val commandFlow = MutableSharedFlow<CentrifugoCommand>(extraBufferCapacity = 64)

    // FIX 1: Per-channel recovery state instead of a single global offset/epoch
    private data class ChannelState(val offset: Long, val epoch: String)
    private val channelStates = mutableMapOf<String, ChannelState>()

    // FIX 2: Use connectionJob.isActive as the guard — no stale isConnected flag
    private var connectionJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private val json = Json { ignoreUnknownKeys = true }

    private var connectionToken: String? = null

    // FIX 3: Cache lobbies subscription token to avoid re-fetching on every reconnect
    private var lobbiesSubscriptionToken: String? = null

    // FIX 4: Monotonically increasing command IDs
    private var commandIdCounter = 0
    private fun nextId() = ++commandIdCounter

    // Subscriptions to restore after reconnect
    private var activeGameSub: Pair<String, String>? = null
    private var activePersonalSub: Pair<String, String>? = null
    private var isLobbiesSubscribed = false

    override suspend fun connect() {
        // FIX 2: Correct guard — check job liveness, not a stale Boolean
        if (connectionJob?.isActive == true) return

        connectionJob?.cancel()
        connectionJob = scope.launch {
            while (isActive) {
                try {
                    if (connectionToken == null) {
                        val result = gameApiService.getLobbiesWsToken()
                        if (result is NetworkResult.Success) {
                            connectionToken = result.data.connection_token
                            // Cache lobbies token on initial fetch (FIX 3)
                            if (lobbiesSubscriptionToken == null) {
                                lobbiesSubscriptionToken = result.data.lobbies_subscription_token
                            }
                        } else {
                            delay(2000.milliseconds)
                            continue
                        }
                    }

                    httpClient.webSocket({
                        url.takeFrom("$wsBaseUrl/connection/websocket")
                    }) {
                        // 1. Connect
                        sendSerialized(
                            CentrifugoCommand(
                                id = nextId(),
                                connect = ConnectData(token = connectionToken!!)
                            )
                        )

                        // 2. Launch command writer
                        val writerJob = launch {
                            commandFlow.collect { cmd -> sendSerialized(cmd) }
                        }

                        // 3. Restore subscriptions after reconnect
                        activeGameSub?.let { (gameId, token) -> subscribeToGame(gameId, token) }
                        activePersonalSub?.let { (userId, token) -> subscribeToPersonal(userId, token) }
                        if (isLobbiesSubscribed) subscribeToLobbies()

                        // 4. Read loop
                        while (isActive) {
                            val frame = withTimeoutOrNull(35_000.milliseconds) { incoming.receive() }
                            if (frame == null) break // Timeout → reconnect

                            if (frame is Frame.Text) {
                                val text = frame.readText()

                                // FIX 5: Centrifugo JSON ping is exactly "{}"; respond with pong
                                if (text == "{}") {
                                    send(Frame.Text("{}"))
                                    continue
                                }

                                try {
                                    val pushObj = json.decodeFromString<CentrifugoPush>(text)
                                    val channel = pushObj.push.channel
                                    val pubData = pushObj.push.pub

                                    // FIX 1: Store recovery state keyed by channel name
                                    val offset = pubData.offset
                                    val epoch = pubData.epoch
                                    if (offset != null && epoch != null) {
                                        channelStates[channel] = ChannelState(offset, epoch)
                                    } else if (offset != null) {
                                        channelStates[channel]?.let {
                                            channelStates[channel] = it.copy(offset = offset)
                                        }
                                    }

                                    val payload = pubData.data.payload

                                    when {
                                        channel.startsWith("game:") -> {
                                            val event = json.decodeFromJsonElement<GameEvent>(payload)
                                            _gameEvents.emit(event)
                                        }
                                        channel.startsWith("personal:") -> {
                                            val event = json.decodeFromJsonElement<PersonalEvent>(payload)
                                            _personalEvents.emit(event)
                                        }
                                        channel == "lobbies" -> {
                                            val event = json.decodeFromJsonElement<LobbyEvent>(payload)
                                            _lobbyEvents.emit(event)
                                        }
                                    }
                                } catch (_: Exception) {
                                    // Ignore non-push messages (ack frames, etc.)
                                }
                            }
                        }

                        writerJob.cancel()
                    }
                } catch (e: Exception) {
                    // Force connection token refresh on next attempt
                    connectionToken = null
                    if (e is CancellationException) throw e
                    delay(3000.milliseconds)
                }
            }
        }
    }

    override suspend fun disconnect() {
        connectionJob?.cancel()
        connectionJob = null
        activeGameSub = null
        activePersonalSub = null
        isLobbiesSubscribed = false
        connectionToken = null
        lobbiesSubscriptionToken = null
        channelStates.clear()
    }

    override suspend fun subscribeToGame(gameId: String, token: String) {
        activeGameSub = gameId to token
        val channel = "game:$gameId"
        // FIX 1: Use per-channel state for recovery
        val state = channelStates[channel]
        commandFlow.emit(
            CentrifugoCommand(
                id = nextId(), // FIX 4: unique ID
                subscribe = SubscribeData(
                    channel = channel,
                    token = token,
                    recover = if (state != null) true else null,
                    offset = state?.offset,
                    epoch = state?.epoch
                )
            )
        )
    }

    override suspend fun subscribeToPersonal(userId: String, token: String) {
        activePersonalSub = userId to token
        val channel = "personal:#$userId"
        // FIX 1: Per-channel recovery for personal channel too
        val state = channelStates[channel]
        commandFlow.emit(
            CentrifugoCommand(
                id = nextId(), // FIX 4
                subscribe = SubscribeData(
                    channel = channel,
                    token = token,
                    recover = if (state != null) true else null,
                    offset = state?.offset,
                    epoch = state?.epoch
                )
            )
        )
    }

    override suspend fun subscribeToLobbies() {
        isLobbiesSubscribed = true
        // FIX 3: Use cached token; only fetch if not yet available
        val token = lobbiesSubscriptionToken ?: run {
            val result = gameApiService.getLobbiesWsToken()
            if (result is NetworkResult.Success) {
                lobbiesSubscriptionToken = result.data.lobbies_subscription_token
                result.data.lobbies_subscription_token
            } else return
        }
        commandFlow.emit(
            CentrifugoCommand(
                id = nextId(), // FIX 4
                subscribe = SubscribeData(channel = "lobbies", token = token)
            )
        )
    }
}
