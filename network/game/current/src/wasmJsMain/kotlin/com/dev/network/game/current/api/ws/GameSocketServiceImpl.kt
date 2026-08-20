@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.dev.network.game.current.api.ws

import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.network.game.current.api.GameApiService
import com.dev.network.game.current.dto.ws.GameEvent
import com.dev.network.game.current.dto.ws.LobbyEvent
import com.dev.network.game.current.dto.ws.PersonalEvent
import io.ktor.client.HttpClient
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * WebSocket реализация для WasmJs используя нативный браузерный WebSocket.
 * Поддерживает Centrifugo ping/pong и подписки.
 */

private external class WebSocket(url: String) {
    var onopen: (() -> Unit)?
    var onmessage: ((MessageEvent) -> Unit)?
    var onerror: ((JsAny) -> Unit)?
    var onclose: ((CloseEvent) -> Unit)?
    val readyState: Short
    fun send(data: String)
    fun close(code: Short, reason: String)

    companion object {
        val CONNECTING: Short
        val OPEN: Short
        val CLOSING: Short
        val CLOSED: Short
    }
}

private external class MessageEvent : JsAny {
    val data: String
}

private external class CloseEvent : JsAny {
    val code: Short
    val reason: String
    val wasClean: Boolean
}

internal class GameSocketServiceImpl(
    @Suppress("UNUSED_PARAMETER") private val httpClient: HttpClient,
    private val gameApiService: GameApiService,
    private val wsBaseUrl: String,
) : GameSocketService {

    private val _gameEvents = MutableSharedFlow<GameEvent>(extraBufferCapacity = 64)
    override val gameEvents = _gameEvents.asSharedFlow()

    private val _personalEvents = MutableSharedFlow<PersonalEvent>(extraBufferCapacity = 64)
    override val personalEvents = _personalEvents.asSharedFlow()

    private val _lobbyEvents = MutableSharedFlow<LobbyEvent>(extraBufferCapacity = 64)
    override val lobbyEvents = _lobbyEvents.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.Main)
    private var connectionJob: kotlinx.coroutines.Job? = null
    private var ws: WebSocket? = null
    private var connectionToken: String? = null
    private var lobbiesSubscriptionToken: String? = null
    private var commandId = 0
    private val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

    // Состояние активных подписок — для восстановления после реконнекта
    private var activeGameSub: Pair<String, String>? = null
    private var activePersonalSub: Pair<String, String>? = null
    private var isLobbiesSubscribed = false
    private var reconnectDelayMs = 2000L

    override suspend fun connect() {
        if (connectionJob?.isActive == true) {
            println("[WS] Already connected")
            return
        }
        connectionJob = scope.launch {
            connectionLoop()
        }
    }

    private suspend fun connectionLoop() {
        println("[WS] Connection loop started")
        while (true) {
            try {
                val token = fetchConnectionToken()
                if (token == null) {
                    println("[WS] Cannot get token, retrying in ${reconnectDelayMs}ms")
                    delay(reconnectDelayMs.toLong())
                    reconnectDelayMs = (reconnectDelayMs * 2).coerceAtMost(30_000L)
                    continue
                }
                connectionToken = token
                reconnectDelayMs = 2000L // сброс backoff при успехе

                val wsUrl = "$wsBaseUrl/connection/websocket?token=$token"
                println("[WS] Connecting to: $wsUrl")
                val connected = connectWebSocket(wsUrl, token)

                if (connected) {
                    // Восстанавливаем подписки после реконнекта
                    restoreSubscriptions()
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                println("[WS] Connection loop cancelled")
                throw e
            } catch (e: Exception) {
                println("[WS] Error in connection loop: ${e.message}")
            }
            // Ждём перед следующей попыткой
            println("[WS] Reconnecting in ${reconnectDelayMs}ms...")
            delay(reconnectDelayMs.toLong())
            reconnectDelayMs = (reconnectDelayMs * 2).coerceAtMost(30_000L)
        }
    }

    private fun restoreSubscriptions() {
        val socket = ws ?: return
        if (socket.readyState != WebSocket.OPEN) return

        activeGameSub?.let { (gameId, token) ->
            println("[WS] Restoring game subscription: $gameId")
            commandId++
            socket.send("""{"id":$commandId,"subscribe":{"channel":"game:$gameId","token":"$token"}}""")
        }
        activePersonalSub?.let { (userId, token) ->
            println("[WS] Restoring personal subscription: $userId")
            commandId++
            socket.send("""{"id":$commandId,"subscribe":{"channel":"personal:#$userId","token":"$token"}}""")
        }
        if (isLobbiesSubscribed) {
            subscribeToLobbiesIfConnected(socket)
        }
    }

    /**
     * Подключается к WebSocket, возвращает true если соединение было открыто успешно.
     */
    private suspend fun connectWebSocket(url: String, token: String): Boolean {
        
        val socket = WebSocket(url)
        ws = socket
        
        // Ждём подключения
        suspendCancellableCoroutine { cont ->
            var resumed = false
            
            socket.onopen = {
                println("[WS] WebSocket opened!")
                if (!resumed) {
                    resumed = true
                    cont.resume(Unit)
                }
            }
            
            socket.onerror = { error ->
                println("[WS] WebSocket error: $error")
                if (!resumed) {
                    resumed = true
                    cont.resume(Unit)
                }
            }
            
            socket.onclose = { event ->
                println("[WS] WebSocket closed: code=${event.code}, reason=${event.reason}")
                if (!resumed) {
                    resumed = true
                    cont.resume(Unit)
                }
            }
        }
        
        if (socket.readyState != WebSocket.OPEN) {
            println("[WS] Failed to open WebSocket")
            return false
        }
        
        // Отправляем команду connect
        val connectCommand = """{"id":1,"connect":{"token":"$token"}}"""
        println("[WS] Sending connect command: $connectCommand")
        socket.send(connectCommand)
        
        // Настраиваем обработчик сообщений
        var connected = false
        socket.onmessage = { event ->
            val data = event.data
            println("[WS] Received: $data")
            
            if (data == "{}") {
                println("[WS] Received ping, sending pong")
                socket.send("{}")
            } else if (data.contains(""""id":1,"connect""")) {
                connected = true
                println("[WS] Connected to Centrifugo!")
                subscribeToLobbiesIfConnected(socket)
            } else if (data.contains(""""push"""")) {
                try {
                    val push = json.decodeFromString<com.dev.network.game.current.dto.ws.CentrifugoPush>(data)
                    val channel = push.push.channel
                    val payload = push.push.pub.data.payload
                    when {
                        channel.startsWith("game:") -> {
                            val event = json.decodeFromJsonElement<GameEvent>(payload)
                            scope.launch { _gameEvents.emit(event) }
                        }
                        channel.startsWith("personal:") -> {
                            val event = json.decodeFromJsonElement<PersonalEvent>(payload)
                            scope.launch { _personalEvents.emit(event) }
                        }
                        channel == "lobbies" -> {
                            val event = json.decodeFromJsonElement<LobbyEvent>(payload)
                            scope.launch { _lobbyEvents.emit(event) }
                        }
                    }
                } catch (e: Exception) {
                    println("[WS] Parse error (ignoring): ${e.message}")
                }
            }
        }
        
        // Ждём пока соединение открыто
        while (socket.readyState == WebSocket.OPEN) {
            delay(100.milliseconds)
        }
        
        println("[WS] Connection ended")
        return connected
    }

    private fun subscribeToLobbiesIfConnected(socket: WebSocket) {
        val subToken = lobbiesSubscriptionToken
        if (subToken != null) {
            commandId++
            val subCmd = """{"id":$commandId,"subscribe":{"channel":"lobbies","token":"$subToken"}}"""
            println("[WS] Sending subscribe to lobbies: $subCmd")
            socket.send(subCmd)
        }
    }

    private suspend fun fetchConnectionToken(): String? {
        repeat(3) { attempt ->
            println("[WS] Fetching token, attempt ${attempt + 1}")
            try {
                val result = gameApiService.getLobbiesWsToken()
                if (result is NetworkResult.Success) {
                    println("[WS] Got token successfully")
                    connectionToken = result.data.connection_token
                    lobbiesSubscriptionToken = result.data.lobbies_subscription_token
                    return result.data.connection_token
                } else {
                    println("[WS] Token request failed: $result")
                }
            } catch (e: Exception) {
                println("[WS] Token fetch error: ${e.message}")
            }
            if (attempt < 2) {
                println("[WS] Retrying in 2 seconds...")
                delay(2.seconds)
            }
        }
        return null
    }

    override suspend fun disconnect() {
        println("[WS] Disconnecting")
        ws?.close(1000, "Client disconnect")
        ws = null
        connectionJob?.cancel()
        connectionJob = null
        connectionToken = null
        lobbiesSubscriptionToken = null
        activeGameSub = null
        activePersonalSub = null
        isLobbiesSubscribed = false
        reconnectDelayMs = 2000L
    }

    override suspend fun subscribeToGame(gameId: String, token: String) {
        println("[WS] Subscribe to game: $gameId")
        activeGameSub = gameId to token  // сохраняем для реконнекта
        val socket = ws
        if (socket != null && socket.readyState == WebSocket.OPEN) {
            commandId++
            val subCmd = """{"id":$commandId,"subscribe":{"channel":"game:$gameId","token":"$token"}}"""
            println("[WS] Sending subscribe: $subCmd")
            socket.send(subCmd)
        }
    }

    override suspend fun unsubscribeFromGame(gameId: String) {
        println("[WS] Unsubscribe from game: $gameId")
        val socket = ws
        if (socket != null && socket.readyState == WebSocket.OPEN) {
            commandId++
            val unsubCmd = """{"id":$commandId,"unsubscribe":{"channel":"game:$gameId"}}"""
            println("[WS] Sending unsubscribe: $unsubCmd")
            socket.send(unsubCmd)
        }
    }

    override suspend fun subscribeToPersonal(userId: String, token: String) {
        println("[WS] Subscribe to personal: $userId")
        activePersonalSub = userId to token  // сохраняем для реконнекта
        val socket = ws
        if (socket != null && socket.readyState == WebSocket.OPEN) {
            commandId++
            val subCmd = """{"id":$commandId,"subscribe":{"channel":"personal:#$userId","token":"$token"}}"""
            println("[WS] Sending subscribe: $subCmd")
            socket.send(subCmd)
        }
    }

    override suspend fun unsubscribeFromPersonal(userId: String) {
        println("[WS] Unsubscribe from personal: $userId")
        val socket = ws
        if (socket != null && socket.readyState == WebSocket.OPEN) {
            commandId++
            val unsubCmd = """{"id":$commandId,"unsubscribe":{"channel":"personal:#$userId"}}"""
            println("[WS] Sending unsubscribe: $unsubCmd")
            socket.send(unsubCmd)
        }
    }

    override suspend fun subscribeToLobbies() {
        println("[WS] Subscribe to lobbies called")
        val socket = ws
        if (socket != null && socket.readyState == WebSocket.OPEN) {
            subscribeToLobbiesIfConnected(socket)
        } else {
            println("[WS] Socket not open, subscription will happen automatically on connect")
        }
    }
}
