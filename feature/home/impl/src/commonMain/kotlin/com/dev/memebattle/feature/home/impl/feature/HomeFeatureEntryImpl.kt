package com.dev.memebattle.feature.home.impl.feature

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.dev.memebattle.core.navigation.entry.FeatureComponent
import com.dev.memebattle.core.navigation.entry.TypedFeatureEntry
import com.dev.memebattle.feature.home.api.entry.HomeFeatureEntry
import com.dev.memebattle.feature.home.api.route.HomeRoute
import com.dev.memebattle.feature.home.impl.presentation.component.HomeComponent
import com.dev.memebattle.feature.home.impl.presentation.component.HomeComponentImpl
import com.dev.memebattle.feature.home.impl.presentation.view.HomeView
import org.koin.mp.KoinPlatform.getKoin
import com.dev.memebattle.core.network.auth.TokenStorage
import com.dev.network.user_auth.current.api.User_authApiService
import com.dev.network.user.current.api.UserApiService

class HomeFeatureEntryImpl : TypedFeatureEntry<HomeComponent, HomeRoute>(), HomeFeatureEntry {
    override val routeClass = HomeRoute::class
    override val baseRoute: HomeRoute = HomeRoute()

    override fun createTyped(route: HomeRoute, componentContext: ComponentContext): HomeComponent {
        val koin = getKoin()
        return HomeComponentImpl(
            componentContext = componentContext,
            storeFactory = koin.get(),
            gameSocketService = koin.get(),
            gameApiService = koin.get(),
            tokenStorage = koin.get(),
            userAuthService = koin.get(),
            userApiService = koin.get(),
            initialLobbyId = route.openLobbyId,
        )
    }

    @Composable
    override fun RenderTyped(component: HomeComponent) {
        HomeView(component = component)
    }
}
