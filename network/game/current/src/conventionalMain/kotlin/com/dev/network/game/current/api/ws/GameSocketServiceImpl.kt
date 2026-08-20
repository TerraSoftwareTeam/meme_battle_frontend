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
import com.dev.network.game.current.dto.ws.UnsubscribeData
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.takeFrom
import io.ktor.websocket.Frame
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.time.Duration.Companion.seconds

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

    // Pending commands are buffered here before/during a session
    private val pendingCommands = kotlinx.coroutines.channels.Channel<CentrifugoCommand>(kotlinx.coroutines.channels.Channel.UNLIMITED)

    // Heartbeat pong (literal "{}"): tracked as a simple counter so we respond promptly
    private val pendingPongs = MutableSharedFlow<Unit>(extraBufferCapacity = 32)

    // Recovery state: channel -> (offset, epoch)
    private data class ChannelState(val offset: Long, val epoch: String)
    private val channelStates = mutableMapOf<String, ChannelState>()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    private var connectionJob: Job? = null
    private var connectionToken: String? = null
    private var lobbiesSubscriptionToken: String? = null
    private var commandIdCounter = 0

    // Subscriptions to restore after reconnect
    private var activeGameSub: Pair<String, String>? = null
    private var activePersonalSub: Pair<String, String>? = null
    private var isLobbiesSubscribed = false

    private fun nextId() = ++commandIdCounter

    override suspend fun connect() {
        mutex.withLock {
            if (connectionJob?.isActive == true) {
                println("[WS] Already connected, skipping")
                return
            }
            connectionJob?.cancel()
            connectionJob = scope.launch { connectionLoop() }
        }
    }

    private suspend fun connectionLoop() {
        var reconnectDelay = 2.seconds
        while (currentCoroutineContext().isActive) {
            try {
                ensureConnectionToken()

                println("[WS] Connecting to $wsBaseUrl/connection/websocket?token=${connectionToken}")
                httpClient.webSocket({
                    url.takeFrom("$wsBaseUrl/connection/websocket?token=${connectionToken}")
                }) {
                    println("[WS] Session opened")
                    reconnectDelay = 2.seconds // reset backoff on success

                    sendConnectCommand()
                    restoreSubscriptions()

                    // Run writer and reader concurrently
                    val writerJob = launch { writerLoop(this@webSocket) }
                    try {
                        readerLoop()
                    } finally {
                        writerJob.cancel()
                    }
                    println("[WS] Session ended")
                }
            } catch (e: CancellationException) {
                println("[WS] Cancelled")
                throw e
            } catch (e: Exception) {
                // Invalidate token on error so it's refreshed on next attempt
                connectionToken = null
                println("[WS] Error: ${e::class.simpleName}: ${e.message}")
                delay(reconnectDelay)
                reconnectDelay = (reconnectDelay * 2).coerceAtMost(30.seconds)
            }
        }
    }

    private suspend fun ensureConnectionToken() {
        if (connectionToken != null) return
        println("[WS] Fetching connection token...")
        var attempts = 0
        while (currentCoroutineContext().isActive) {
            val result = gameApiService.getLobbiesWsToken()
            if (result is NetworkResult.Success) {
                connectionToken = result.data.connection_token
                if (lobbiesSubscriptionToken == null) {
                    lobbiesSubscriptionToken = result.data.lobbies_subscription_token
                }
                println("[WS] Got token: ${connectionToken?.take(20)}...")
                return
            }
            attempts++
            println("[WS] Token fetch failed (attempt $attempts), retrying in 2s...")
            delay(2.seconds)
        }
    }

    private suspend fun DefaultWebSocketSession.sendConnectCommand() {
        val cmd = CentrifugoCommand(
            id = nextId(),
            connect = ConnectData(token = connectionToken!!)
        )
        val json2 = json.encodeToString(cmd)
        println("[WS] -> connect: $json2")
        send(Frame.Text(json2))
    }

    private suspend fun restoreSubscriptions() {
        activeGameSub?.let { (gameId, token) ->
            println("[WS] Restoring game subscription: $gameId")
            emitSubscribeCommand("game:$gameId", token)
        }
        activePersonalSub?.let { (userId, token) ->
            println("[WS] Restoring personal subscription: $userId")
            emitSubscribeCommand("personal:#$userId", token)
        }
        if (isLobbiesSubscribed) {
            println("[WS] Restoring lobbies subscription")
            subscribeToLobbies()
        }
    }

    private suspend fun writerLoop(session: DefaultWebSocketSession) {
        // Merge pong and command flows
        val pongJob = scope.launch {
            pendingPongs.collect {
                try { session.send(Frame.Text("{}")) } catch (e: Exception) {
                    if (e is CancellationException) throw e
                }
            }
        }
        try {
            for (cmd in pendingCommands) {
                if (!session.isActive) break
                val encoded = json.encodeToString(cmd)
                println("[WS] cmd -> $encoded")
                try {
                    session.send(Frame.Text(encoded))
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    println("[WS] Send error: ${e.message}")
                }
            }
        } finally {
            pongJob.cancel()
        }
    }

    private suspend fun DefaultWebSocketSession.readerLoop() {
        while (isActive) {
            val frame = try {
                incoming.receive()
            } catch (e: ClosedReceiveChannelException) {
                println("[WS] Channel closed by server")
                break
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("[WS] Receive error: ${e.message}")
                break
            }

            when (frame) {
                is Frame.Text -> processText(frame.readText())
                is Frame.Close -> {
                    println("[WS] Received Close frame")
                    break
                }
                is Frame.Ping -> send(Frame.Pong(frame.readBytes()))
                else -> {} // ignore
            }
        }
    }

    private suspend fun processText(text: String) {
        println("[WS] <- $text")
        // Centrifugo heartbeat — respond with literal "{}"
        if (text == "{}") {
            pendingPongs.emit(Unit)
            return
        }
        try {
            val push = json.decodeFromString<CentrifugoPush>(text)
            val channel = push.push.channel
            val pub = push.push.pub

            // Update recovery state
            val offset = pub.offset
            val epoch = pub.epoch
            when {
                offset != null && epoch != null -> channelStates[channel] = ChannelState(offset, epoch)
                offset != null -> channelStates[channel]?.let { channelStates[channel] = it.copy(offset = offset) }
            }

            val payload = pub.data.payload
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
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            println("[WS] Parse error (ignoring): ${e.message}")
        }
    }

    override suspend fun disconnect() {
        println("[WS] disconnect()")
        connectionJob?.cancel()
        connectionJob = null
        connectionToken = null
        lobbiesSubscriptionToken = null
        activeGameSub = null
        activePersonalSub = null
        isLobbiesSubscribed = false
        channelStates.clear()
    }

    private suspend fun emitSubscribeCommand(channel: String, token: String) {
        val state = channelStates[channel]
        pendingCommands.send(
            CentrifugoCommand(
                id = nextId(),
                subscribe = SubscribeData(
                    channel = channel,
                    token = token,
                    recover = state != null,
                    offset = state?.offset,
                    epoch = state?.epoch
                )
            )
        )
    }
    private suspend fun emitUnsubscribeCommand(channel: String) {
        pendingCommands.send(
            CentrifugoCommand(
                id = nextId(),
                unsubscribe = UnsubscribeData(channel = channel)
            )
        )
    }

    override suspend fun subscribeToGame(gameId: String, token: String) {
        activeGameSub = gameId to token
        emitSubscribeCommand("game:$gameId", token)
    }

    override suspend fun unsubscribeFromGame(gameId: String) {
        if (activeGameSub?.first == gameId) {
            activeGameSub = null
            emitUnsubscribeCommand("game:$gameId")
        }
    }

    override suspend fun subscribeToPersonal(userId: String, token: String) {
        activePersonalSub = userId to token
        emitSubscribeCommand("personal:#$userId", token)
    }

    override suspend fun unsubscribeFromPersonal(userId: String) {
        if (activePersonalSub?.first == userId) {
            activePersonalSub = null
            emitUnsubscribeCommand("personal:#$userId")
        }
    }

    override suspend fun subscribeToLobbies() {
        isLobbiesSubscribed = true
        val token = lobbiesSubscriptionToken ?: run {
            val result = gameApiService.getLobbiesWsToken()
            if (result is NetworkResult.Success) {
                lobbiesSubscriptionToken = result.data.lobbies_subscription_token
                result.data.lobbies_subscription_token
            } else {
                println("[WS] Failed to get lobbies token")
                return
            }
        }
        emitSubscribeCommand("lobbies", token)
    }
}
