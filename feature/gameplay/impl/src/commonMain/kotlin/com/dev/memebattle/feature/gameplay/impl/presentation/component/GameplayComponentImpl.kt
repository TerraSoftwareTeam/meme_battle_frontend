package com.dev.memebattle.feature.gameplay.impl.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.router.panels.Panels
import com.arkivanov.decompose.router.panels.PanelsNavigation
import com.arkivanov.decompose.router.panels.childPanels
import com.arkivanov.decompose.router.panels.setMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.memebattle.core.navigation.output.NavigationOutput
import com.dev.memebattle.feature.gameplay.impl.presentation.component.game.GameplayGameComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.component.game.GameplayGameComponentImpl
import com.dev.memebattle.feature.gameplay.impl.presentation.component.info.GameplayInfoComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.component.info.GameplayInfoComponentImpl
import com.dev.memebattle.feature.gameplay.impl.presentation.component.players.GameplayPlayersComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.component.players.GameplayPlayersComponentImpl
import com.dev.memebattle.feature.gameplay.impl.presentation.store.GameplayStore
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStore
import com.dev.memebattle.feature.gameplay.impl.presentation.store.game.GameplayGameStoreFactory
import com.dev.memebattle.feature.gameplay.impl.presentation.store.info.GameplayInfoStore
import com.dev.memebattle.feature.gameplay.impl.presentation.store.players.GameplayPlayersStore
import com.dev.memebattle.feature.gameplay.impl.presentation.store.players.GameplayPlayersStoreFactory
import com.dev.network.game.current.api.GameApiService
import com.dev.network.game.current.api.ws.GameSocketService
import com.dev.network.game.current.dto.GameStateDto
import com.dev.network.game.current.dto.MemeGameCard
import com.dev.network.game.current.dto.VoteRequest
import com.dev.network.game.current.dto.ws.GameEvent
import com.dev.network.game.current.dto.ws.PersonalEvent
import com.dev.memebattle.core.network.utils.normalizeMediaUrl
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@OptIn(ExperimentalDecomposeApi::class)
class GameplayComponentImpl(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val gameSocketService: GameSocketService,
    private val gameApiService: GameApiService,
    private val gameId: String,
    private val myUserId: String,
) : GameplayComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val outputChannel = Channel<NavigationOutput>(Channel.BUFFERED)

    // ── WS Fan-out ──────────────────────────────────────────────────────────

    private val _gameEventsForGame = MutableSharedFlow<GameEvent>(extraBufferCapacity = 64)
    private val _gameEventsForInfo = MutableSharedFlow<GameEvent>(extraBufferCapacity = 64)
    private val _gameEventsForPlayers = MutableSharedFlow<GameEvent>(extraBufferCapacity = 64)
    private val _personalEventsForGame = MutableSharedFlow<PersonalEvent>(extraBufferCapacity = 64)

    /** Снимок состояния — загружается после WS-подключения (до создания дочерних компонентов). */
    private var cachedSnapshot: GameStateDto? = null

    /**
     * Кеш userId → handle. Заполняется из snapshot и обновляется при PlayerJoined.
     * Используется для обогащения WS-событий, которые не содержат handle.
     * Internal — доступен из GameplayView через safe cast.
     */
    internal val playerHandleCache = mutableMapOf<String, String>()

    /**
     * Сигнал для UI — закрыть все боковые панели.
     * Публикуется при получении GameStarted.
     */
    val closePanelsSignal = MutableStateFlow(0)

    /**
     * Прямая ссылка на PlayersComponent — нужна для обогащения handles в GameStore.
     * Заполняется при создании PlayersComponent через extraFactory.
     */
    private var playersComponentRef: GameplayPlayersComponent? = null

    // ── Конфигурации панелей ─────────────────────────────────────────────────

    @Serializable sealed interface MainConfig    { @Serializable data object Game    : MainConfig    }
    @Serializable sealed interface DetailsConfig { @Serializable data object Info    : DetailsConfig }
    @Serializable sealed interface ExtraConfig   { @Serializable data object Players : ExtraConfig   }

    // ── ChildPanels ──────────────────────────────────────────────────────────

    private val panelsNavigation = PanelsNavigation<MainConfig, DetailsConfig, ExtraConfig>()

    override val panels: Value<ChildPanels<MainConfig, GameplayGameComponent, DetailsConfig, GameplayInfoComponent, ExtraConfig, GameplayPlayersComponent>> =
        childPanels(
            source = panelsNavigation,
            serializers = Triple(MainConfig.serializer(), DetailsConfig.serializer(), ExtraConfig.serializer()),
            initialPanels = { Panels(main = MainConfig.Game, details = DetailsConfig.Info, extra = ExtraConfig.Players) },
            handleBackButton = false,
            mainFactory    = { _, ctx -> createGameComponent(ctx) },
            detailsFactory = { _, ctx -> createInfoComponent(ctx) },
            extraFactory   = { _, ctx -> createPlayersComponent(ctx) },
        )

    // ── Фабрики дочерних компонентов ─────────────────────────────────────────

    private fun createGameComponent(ctx: ComponentContext): GameplayGameComponent {
        val component = GameplayGameComponentImpl(
            componentContext = ctx,
            storeFactory = storeFactory,
            gameApiService = gameApiService,
            gameId = gameId,
            myUserId = myUserId,
            gameEvents = _gameEventsForGame,
            personalEvents = _personalEventsForGame,
            initialSnapshot = cachedSnapshot,
            // Резолвим handle из кеша или из живого PlayersStore
            getPlayerHandle = { userId ->
                playerHandleCache[userId]
                    ?: playersComponentRef?.state?.value?.players
                        ?.firstOrNull { it.userId == userId }
                        ?.handle
                        ?.takeIf { it.isNotBlank() }
            },
        )
        // Слушаем ExitGame Effect — выходим из игры и перенаправляем в навигацию
        component.effects.onEach { effect ->
            if (effect is GameplayGameStore.Effect.ExitGame) {
                scope.launch {
                    try {
                        gameApiService.leaveCurrentGame()
                    } catch (_: Exception) {}
                    outputChannel.trySend(NavigationOutput.Back)
                }
            }
        }.launchIn(scope)
        return component
    }

    private fun createInfoComponent(ctx: ComponentContext) = GameplayInfoComponentImpl(
        componentContext = ctx,
        storeFactory = storeFactory,
        gameApiService = gameApiService,
        gameId = gameId,
        myUserId = myUserId,
        gameEvents = _gameEventsForInfo,
        initialSnapshot = cachedSnapshot,
    )

    private fun createPlayersComponent(ctx: ComponentContext): GameplayPlayersComponent {
        val component = GameplayPlayersComponentImpl(
            componentContext = ctx,
            storeFactory = storeFactory,
            myUserId = myUserId,
            gameEvents = _gameEventsForPlayers,
            initialSnapshot = cachedSnapshot,
        )
        // Сохраняем ссылку — через неё GameStore будет резолвить handles
        playersComponentRef = component
        // Ловим VoteRequested из PlayersStore → вызываем API vote здесь
        component.effects.onEach { effect ->
            when (effect) {
                is GameplayPlayersStore.Effect.VoteRequested -> handleVoteFromPlayers(effect.submissionId)
            }
        }.launchIn(scope)
        return component
    }

    // ── Инициализация WS (БЕЗ joinGame — его вызывает GameStore через JoinLobby) ──

    private val backCallback = BackCallback {
        val gameComponent = panels.value.main.instance
        val currentPhase = gameComponent.state.value.uiPhase
        when (currentPhase) {
            GameplayGameStore.UiPhase.Lobby -> {
                scope.launch {
                    try {
                        gameApiService.leaveCurrentGame()
                    } catch (_: Exception) {}
                    outputChannel.trySend(NavigationOutput.Back)
                }
            }
            GameplayGameStore.UiPhase.GameFinished -> {
                outputChannel.trySend(NavigationOutput.Back)
            }
            else -> {
                // В самой игре (Submitting, Voting, RoundResult):
                // Назад запрещён — не позволяем уйти с экрана во время процесса игры!
            }
        }
    }

    init {
        backHandler.register(backCallback)
        lifecycle.doOnDestroy {
            @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
            kotlinx.coroutines.GlobalScope.launch {
                try {
                    gameApiService.leaveGame(gameId)
                } catch (_: Exception) {}
                try {
                    gameSocketService.unsubscribeFromGame(gameId)
                    gameSocketService.unsubscribeFromPersonal(myUserId)
                } catch (_: Exception) {}
            }
        }
        gameSocketService.reconnectedEvents.onEach {
            println("[GameplayComponentImpl] Socket reconnected -> refreshing snapshot")
            refreshSnapshotAndStores()
        }.launchIn(scope)

        scope.launch { initializeWsSession() }
    }

    private suspend fun refreshSnapshotAndStores() {
        val snapshotResult = gameApiService.getGameState(gameId)
        if (snapshotResult is NetworkResult.Success) {
            val snapshot = snapshotResult.data
            cachedSnapshot = snapshot
            snapshot.players.forEach { player ->
                if (player.handle.isNotBlank()) {
                    playerHandleCache[player.user_id] = player.handle
                }
            }
            val panelsValue = panels.value
            panelsValue.main.instance.onIntent(GameplayGameStore.Intent.Initialize(snapshot))
            panelsValue.details?.instance?.onIntent(GameplayInfoStore.Intent.Initialize(snapshot))
            panelsValue.extra?.instance?.onIntent(GameplayPlayersStore.Intent.Initialize(snapshot))

            if (snapshot.round?.phase == com.dev.network.game.current.dto.RoundPhase.VOTING) {
                loadSubmissionsForVoting()
            }
        }
    }

    private suspend fun initializeWsSession() {
        // 1. Получить токены
        val tokenResult = gameApiService.getWsToken(gameId)
        val tokenDto = when (tokenResult) {
            is NetworkResult.Success -> tokenResult.data
            else -> return
        }

        // 2. Подключить WebSocket
        gameSocketService.connect()
        gameSocketService.subscribeToGame(gameId, tokenDto.game_subscription_token)
        gameSocketService.subscribeToPersonal(myUserId, tokenDto.personal_subscription_token)

        // 3. Получить снимок и инициализировать дочерние сторы
        refreshSnapshotAndStores()

        // 4. Запустить fan-out и проверку таймера
        startFanOut()
        startTimerFallbackCheck()
    }

    private var lastFetchedExpiresAt: String? = null

    private fun startTimerFallbackCheck() {
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(2000)
                val infoComponent = panels.value.details?.instance ?: continue
                val phaseExpiresAt = infoComponent.state.value.phaseExpiresAt ?: run {
                    lastFetchedExpiresAt = null
                    continue
                }

                val secondsLeft = computeSecondsLeft(phaseExpiresAt)
                if (secondsLeft <= 0 && lastFetchedExpiresAt != phaseExpiresAt) {
                    lastFetchedExpiresAt = phaseExpiresAt

                    // Если сокет отвалился, пытаемся переподключиться
                    if (!gameSocketService.isConnected.value) {
                        scope.launch {
                            try {
                                gameSocketService.reconnect()
                            } catch (e: Exception) {
                                println("[Gameplay] Socket reconnect error: ${e.message}")
                            }
                        }
                    }

                    val result = gameApiService.getGameState(gameId)
                    if (result is NetworkResult.Success) {
                        val snapshot = result.data
                        panels.value.main.instance.onIntent(GameplayGameStore.Intent.Initialize(snapshot))
                        panels.value.details?.instance?.onIntent(GameplayInfoStore.Intent.Initialize(snapshot))
                        panels.value.extra?.instance?.onIntent(GameplayPlayersStore.Intent.Initialize(snapshot))

                        if (snapshot.round?.phase == com.dev.network.game.current.dto.RoundPhase.VOTING) {
                            loadSubmissionsForVoting()
                        }
                    } else {
                        // Если REST запрос не удался, форсируем реконект сокета
                        scope.launch {
                            try {
                                gameSocketService.reconnect()
                            } catch (e: Exception) {
                                println("[Gameplay] Socket reconnect error: ${e.message}")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun computeSecondsLeft(isoString: String): Int {
        return try {
            val clean = isoString.trimEnd('Z').substringBefore('.')
            val parts = clean.split('T')
            val dateParts = parts[0].split('-').map { it.toInt() }
            val timeParts = parts[1].split(':').map { it.toInt() }

            val year = dateParts[0]
            val month = dateParts[1]
            val day = dateParts[2]
            val hour = timeParts[0]
            val min = timeParts[1]
            val sec = timeParts[2]

            val y = if (month <= 2) year - 1 else year
            val m = if (month <= 2) month + 12 else month
            val A = y / 100
            val B = 2 - A + A / 4

            val jdn = (365.25 * (y + 4716)).toLong() +
                    (30.6001 * (m + 1)).toLong() +
                    day + B - 1524

            val daysSinceEpoch = jdn - 2440588L
            val secondsSinceEpoch = daysSinceEpoch * 86400L + hour * 3600L + min * 60L + sec
            val deadlineMs = secondsSinceEpoch * 1000L
            val nowMs = kotlin.time.Clock.System.now().toEpochMilliseconds()
            ((deadlineMs - nowMs) / 1000L).toInt().coerceIn(0, 300)
        } catch (_: Exception) {
            300
        }
    }

    private fun startFanOut() {
        gameSocketService.gameEvents.onEach { event ->
            when (event) {
                is GameEvent.GameStarted -> {
                    // Закрываем все боковые панели — игра началась!
                    closePanelsSignal.value += 1
                    _gameEventsForInfo.emit(event)
                }
                is GameEvent.RoundStarted,
                is GameEvent.RoundFinished,
                is GameEvent.GameFinished -> {
                    _gameEventsForGame.emit(event)
                    _gameEventsForInfo.emit(event)
                    _gameEventsForPlayers.emit(event)
                }
                is GameEvent.RoundPhaseChanged -> {
                    _gameEventsForGame.emit(event)
                    _gameEventsForInfo.emit(event)
                    // При переходе в Voting — загружаем submission-карты для голосования
                    if (event.phase == "voting") {
                        loadSubmissionsForVoting()
                    }
                }
                is GameEvent.VoteReceived -> _gameEventsForInfo.emit(event)
                is GameEvent.SubmissionReceived -> {
                    // PlayersStore: отмечаем игрока как подавшего
                    _gameEventsForPlayers.emit(event)
                    // InfoStore: инкрементируем submittedCount
                    _gameEventsForInfo.emit(event)
                }
                is GameEvent.PlayerJoined -> {
                    // Кешируем handle если пришёл
                    if (event.handle.isNotBlank()) {
                        playerHandleCache[event.userId] = event.handle
                    }
                    _gameEventsForInfo.emit(event)
                    _gameEventsForPlayers.emit(event)
                }
                is GameEvent.PlayerLeft -> {
                    playerHandleCache.remove(event.userId)
                    _gameEventsForInfo.emit(event)
                    _gameEventsForPlayers.emit(event)
                }
                is GameEvent.PlayerReadyChanged -> {
                    _gameEventsForInfo.emit(event)
                    _gameEventsForPlayers.emit(event)
                }
            }
        }.launchIn(scope)

        gameSocketService.personalEvents.onEach { event ->
            _personalEventsForGame.emit(event)
        }.launchIn(scope)
    }

    /** Загружаем submission-карты с бэкенда при переходе фазы в Voting */
    private fun loadSubmissionsForVoting() {
        scope.launch {
            val result = gameApiService.getGameState(gameId)
            if (result is NetworkResult.Success) {
                val snapshot = result.data
                val round = snapshot.round ?: return@launch
                val submissions = round.submissions ?: return@launch
                if (submissions.isEmpty()) return@launch

                val cards = submissions.map { sub ->
                    val card = sub.card
                    if (card is MemeGameCard) {
                        MemeGameCard(card.data.copy(mediaUrl = normalizeMediaUrl(card.data.mediaUrl)))
                    } else {
                        card
                    }
                }
                val ids   = submissions.map { it.id }

                val mySubmissionNormalized = (round.my_submission ?: submissions.firstOrNull { it.is_mine || it.id == round.my_submission_id }?.card)?.let { card ->
                    if (card is MemeGameCard) {
                        MemeGameCard(card.data.copy(mediaUrl = normalizeMediaUrl(card.data.mediaUrl)))
                    } else {
                        card
                    }
                }

                panels.value.main.instance.onIntent(
                    GameplayGameStore.Intent.LoadSubmissions(
                        cards = cards,
                        ids = ids,
                        mySubmissionCard = mySubmissionNormalized,
                        hasVoted = round.has_voted,
                    )
                )
            }
        }
    }

    // ── Голосование из PlayersScreen (через Effect маршрутизация) ────────────

    private fun handleVoteFromPlayers(submissionId: String) {
        // Vote через PlayersScreen — направляем в GameplayGameStore для согласованного стейта и обработки ошибок
        panels.value.main.instance.onIntent(GameplayGameStore.Intent.Vote(submissionId))
    }

    // ── Public API ────────────────────────────────────────────────────────────

    @OptIn(ExperimentalDecomposeApi::class)
    override fun setAdaptiveMode(mode: ChildPanelsMode) = panelsNavigation.setMode(mode)

    override val isConnected: StateFlow<Boolean> = gameSocketService.isConnected

    override val output: Flow<NavigationOutput> = outputChannel.receiveAsFlow()

    // Transitional stub — kept while GameplayComponent interface still exposes old store API
    override val state: StateFlow<GameplayStore.State> = MutableStateFlow(GameplayStore.State(gameId = gameId))
    override val effects: SharedFlow<GameplayStore.Effect> = MutableSharedFlow()
    override fun onIntent(intent: GameplayStore.Intent) = Unit
}
