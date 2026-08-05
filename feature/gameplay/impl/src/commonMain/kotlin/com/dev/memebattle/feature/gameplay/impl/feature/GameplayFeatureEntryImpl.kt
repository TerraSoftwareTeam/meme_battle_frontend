package com.dev.memebattle.feature.gameplay.impl.feature

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.dev.memebattle.core.navigation.entry.TypedFeatureEntry
import com.dev.memebattle.core.network.auth.TokenStorage
import com.dev.memebattle.core.network.auth.decodeJwtSub
import com.dev.memebattle.feature.gameplay.api.entry.GameplayFeatureEntry
import com.dev.memebattle.feature.gameplay.api.route.GameplayRoute
import com.dev.memebattle.feature.gameplay.impl.presentation.component.GameplayComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.component.GameplayComponentImpl
import com.dev.memebattle.feature.gameplay.impl.presentation.view.GameplayView
import com.dev.network.game.current.api.GameApiService
import com.dev.network.game.current.api.ws.GameSocketService
import org.koin.mp.KoinPlatform.getKoin

class GameplayFeatureEntryImpl : TypedFeatureEntry<GameplayComponent, GameplayRoute>(), GameplayFeatureEntry {
    override val routeClass = GameplayRoute::class
    // baseRoute is never read at runtime for parameterized routes — placeholder gameId is fine
    override val baseRoute: GameplayRoute = GameplayRoute(gameId = "")

    override fun createTyped(route: GameplayRoute, componentContext: ComponentContext): GameplayComponent {
        val koin = getKoin()
        // myUserId — декодируем sub-claim из JWT access-токена (не проверяем подпись,
        // сервер всё равно валидирует токен самостоятельно на каждом запросе)
        val tokenStorage = koin.get<TokenStorage>()
        val myUserId = decodeJwtSub(tokenStorage.getAccessToken() ?: "") ?: ""
        return GameplayComponentImpl(
            componentContext = componentContext,
            storeFactory = koin.get(),
            gameSocketService = koin.get<GameSocketService>(),
            gameApiService = koin.get<GameApiService>(),
            gameId = route.gameId,
            myUserId = myUserId,
        )
    }

    @Composable
    override fun RenderTyped(component: GameplayComponent) {
        GameplayView(component = component)
    }
}
