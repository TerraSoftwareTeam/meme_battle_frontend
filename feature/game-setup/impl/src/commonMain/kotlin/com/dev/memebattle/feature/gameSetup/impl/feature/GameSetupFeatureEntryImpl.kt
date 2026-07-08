package com.dev.memebattle.feature.gameSetup.impl.feature

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.dev.memebattle.core.navigation.entry.FeatureComponent
import com.dev.memebattle.core.navigation.entry.TypedFeatureEntry
import com.dev.memebattle.feature.gameSetup.api.entry.GameSetupFeatureEntry
import com.dev.memebattle.feature.gameSetup.api.route.GameSetupRoute
import com.dev.memebattle.feature.gameSetup.impl.presentation.component.GameSetupComponent
import com.dev.memebattle.feature.gameSetup.impl.presentation.component.GameSetupComponentImpl
import com.dev.memebattle.feature.gameSetup.impl.presentation.view.GameSetupView
import org.koin.mp.KoinPlatform.getKoin

class GameSetupFeatureEntryImpl : TypedFeatureEntry<GameSetupComponent, GameSetupRoute>(), GameSetupFeatureEntry {
    override val routeClass = GameSetupRoute::class
    override val baseRoute: GameSetupRoute = GameSetupRoute

    override fun createTyped(route: GameSetupRoute, componentContext: ComponentContext): GameSetupComponent {
        val koin = getKoin()
        return GameSetupComponentImpl(
            componentContext = componentContext,
            storeFactory = koin.get()
        )
    }

    @Composable
    override fun RenderTyped(component: GameSetupComponent) {
        GameSetupView(component = component)
    }
}
