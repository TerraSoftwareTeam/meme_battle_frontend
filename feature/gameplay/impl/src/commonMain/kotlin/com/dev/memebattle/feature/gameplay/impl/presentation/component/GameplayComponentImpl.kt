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
import com.dev.network.game.current.dto.VoteRequest
import com.dev.network.game.current.dto.ws.GameEvent
import com.dev.network.game.current.dto.ws.PersonalEvent
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
        )
        // Слушаем ExitGame Effect — перенаправляем в навигацию
        component.effects.onEach { effect ->
            if (effect is GameplayGameStore.Effect.ExitGame) {
                outputChannel.trySend(NavigationOutput.Back)
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
        // Ловим VoteRequested из PlayersStore → вызываем API vote здесь
        component.effects.onEach { effect ->
            when (effect) {
                is GameplayPlayersStore.Effect.VoteRequested -> handleVoteFromPlayers(effect.submissionId)
            }
        }.launchIn(scope)
        return component
    }

    // ── Инициализация WS (БЕЗ joinGame — его вызывает GameStore через JoinLobby) ──

    init {
        lifecycle.doOnDestroy {
            // Отменяем корутину WS через отмену scope, не GlobalScope
            scope.launch {
                try {
                    gameSocketService.unsubscribeFromGame(gameId)
                    gameSocketService.unsubscribeFromPersonal(myUserId)
                } catch (_: Exception) {}
            }
        }
        scope.launch { initializeWsSession() }
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

        // 3. Получить снимок (если игра уже в процессе — для reconnect)
        val snapshotResult = gameApiService.getGameState(gameId)
        if (snapshotResult is NetworkResult.Success) {
            cachedSnapshot = snapshotResult.data
        }

        // 4. Инициализировать GameStore с snapshot
        val panelsValue = panels.value
        panelsValue.main.instance?.let { gameComponent ->
            gameComponent.onIntent(GameplayGameStore.Intent.Initialize(cachedSnapshot))
        }
        panelsValue.details?.instance?.onIntent(GameplayInfoStore.Intent.Initialize(cachedSnapshot))
        panelsValue.extra?.instance?.onIntent(GameplayPlayersStore.Intent.Initialize(cachedSnapshot))

        // 5. Запустить fan-out
        startFanOut()
    }

    private fun startFanOut() {
        gameSocketService.gameEvents.onEach { event ->
            when (event) {
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
                    if ((event as GameEvent.RoundPhaseChanged).phase == "voting") {
                        loadSubmissionsForVoting()
                    }
                }
                is GameEvent.GameStarted,
                is GameEvent.VoteReceived -> _gameEventsForInfo.emit(event)
                is GameEvent.SubmissionReceived -> {
                    // PlayersStore: отмечаем игрока как подавшего
                    _gameEventsForPlayers.emit(event)
                    // InfoStore: инкрементируем submittedCount
                    _gameEventsForInfo.emit(event)
                }
                is GameEvent.PlayerJoined,
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

                // card уже является GameCard (sealed interface) — берём напрямую
                val cards = submissions.map { it.card }
                val ids   = submissions.map { it.id }

                panels.value.main.instance?.onIntent(
                    GameplayGameStore.Intent.LoadSubmissions(cards, ids)
                )
            }
        }
    }

    // ── Голосование из PlayersScreen (через Effect маршрутизация) ────────────

    private fun handleVoteFromPlayers(submissionId: String) {
        // Vote через PlayersScreen
        scope.launch {
            gameApiService.voteCard(gameId, VoteRequest(submission_id = submissionId))
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    @OptIn(ExperimentalDecomposeApi::class)
    override fun setAdaptiveMode(mode: ChildPanelsMode) = panelsNavigation.setMode(mode)

    override val output: Flow<NavigationOutput> = outputChannel.receiveAsFlow()

    // Transitional stub — kept while GameplayComponent interface still exposes old store API
    override val state: StateFlow<GameplayStore.State> = MutableStateFlow(GameplayStore.State(gameId = gameId))
    override val effects: SharedFlow<GameplayStore.Effect> = MutableSharedFlow()
    override fun onIntent(intent: GameplayStore.Intent) = Unit
}
