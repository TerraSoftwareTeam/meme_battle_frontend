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
import io.ktor.client.plugins.websocket.wss
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
    private val gameApiService: GameApiService
) : GameSocketService {

    private val _gameEvents = MutableSharedFlow<GameEvent>(extraBufferCapacity = 64)
    override val gameEvents = _gameEvents.asSharedFlow()

    private val _personalEvents = MutableSharedFlow<PersonalEvent>(extraBufferCapacity = 64)
    override val personalEvents = _personalEvents.asSharedFlow()

    private val _lobbyEvents = MutableSharedFlow<LobbyEvent>(extraBufferCapacity = 64)
    override val lobbyEvents = _lobbyEvents.asSharedFlow()

    private val commandFlow = MutableSharedFlow<CentrifugoCommand>(extraBufferCapacity = 64)

    private var currentOffset: Long? = null
    private var currentEpoch: String? = null
    private var isConnected = false
    private var connectionJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private val json = Json { ignoreUnknownKeys = true }
    
    private var connectionToken: String? = null

    override suspend fun connect() {
        if (isConnected) return
        
        connectionJob?.cancel()
        connectionJob = scope.launch {
            // First we need a connection token. We'll use getLobbiesWsToken for the connection token since it gives a valid one.
            if (connectionToken == null) {
                val tokensResult = gameApiService.getLobbiesWsToken()
                if (tokensResult is NetworkResult.Success) {
                    connectionToken = tokensResult.data.connection_token
                } else {
                    return@launch
                }
            }

            while (isActive) {
                try {
                    httpClient.wss(urlString = "${com.dev.memebattle.core.network.BuildKonfig.WS_BASE_URL}/connection/websocket") {
                        isConnected = true
                        
                        // 1. Connect
                        val connectCmd = CentrifugoCommand(
                            id = 1,
                            connect = ConnectData(token = connectionToken!!)
                        )
                        sendSerialized(connectCmd)

                        // 2. Launch command writer
                        val writerJob = launch {
                            commandFlow.collect { cmd ->
                                sendSerialized(cmd)
                            }
                        }

                        // 3. Listen for events with Heartbeat
                        while (isActive) {
                            val frame = withTimeoutOrNull(35_000L.milliseconds) {
                                incoming.receive()
                            }
                            if (frame == null) {
                                break // Timeout
                            }

                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                if (text == "{}" || text.isBlank()) {
                                    send(Frame.Text("{}"))
                                    continue
                                }
                                
                                try {
                                    val pushObj = json.decodeFromString<CentrifugoPush>(text)
                                    val channelName = pushObj.push.channel
                                    val pubData = pushObj.push.pub

                                    pubData.offset?.let { currentOffset = it }
                                    pubData.epoch?.let { currentEpoch = it }
                                    
                                    val payload = pubData.data.payload
                                    
                                    when {
                                        channelName.startsWith("game:") -> {
                                            val gameEvent = json.decodeFromJsonElement<GameEvent>(payload)
                                            _gameEvents.emit(gameEvent)
                                        }
                                        channelName.startsWith("personal:") -> {
                                            val personalEvent = json.decodeFromJsonElement<PersonalEvent>(payload)
                                            _personalEvents.emit(personalEvent)
                                        }
                                        channelName == "lobbies" -> {
                                            val lobbyEvent = json.decodeFromJsonElement<LobbyEvent>(payload)
                                            _lobbyEvents.emit(lobbyEvent)
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Ignore non-push messages or malformed JSON
                                }
                            }
                        }
                        
                        writerJob.cancel()
                    }
                } catch (e: Exception) {
                    isConnected = false
                    if (e is CancellationException) throw e
                    delay(3000L.milliseconds)
                }
            }
        }
    }

    override suspend fun disconnect() {
        isConnected = false
        connectionJob?.cancel()
        connectionJob = null
    }

    override suspend fun subscribeToGame(gameId: String, token: String) {
        val subscribeCmd = CentrifugoCommand(
            id = 2,
            subscribe = SubscribeData(
                channel = "game:$gameId",
                token = token,
                recover = if (currentOffset != null && currentEpoch != null) true else null,
                offset = currentOffset,
                epoch = currentEpoch
            )
        )
        commandFlow.emit(subscribeCmd)
    }

    override suspend fun subscribeToPersonal(userId: String, token: String) {
        val personalCmd = CentrifugoCommand(
            id = 3,
            subscribe = SubscribeData(
                channel = "personal:#$userId",
                token = token
            )
        )
        commandFlow.emit(personalCmd)
    }

    override suspend fun subscribeToLobbies(token: String) {
        val subscribeCmd = CentrifugoCommand(
            id = 4,
            subscribe = SubscribeData(
                channel = "lobbies",
                token = token
            )
        )
        commandFlow.emit(subscribeCmd)
    }
}
