package com.dev.memebattle.feature.gameplay.impl.feature

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.dev.memebattle.core.navigation.entry.FeatureComponent
import com.dev.memebattle.core.navigation.entry.TypedFeatureEntry
import com.dev.memebattle.feature.gameplay.api.entry.GameplayFeatureEntry
import com.dev.memebattle.feature.gameplay.api.route.GameplayRoute
import com.dev.memebattle.feature.gameplay.impl.presentation.component.GameplayComponent
import com.dev.memebattle.feature.gameplay.impl.presentation.component.GameplayComponentImpl
import com.dev.memebattle.feature.gameplay.impl.presentation.view.GameplayView
import org.koin.mp.KoinPlatform.getKoin

class GameplayFeatureEntryImpl : TypedFeatureEntry<GameplayComponent, GameplayRoute>(), GameplayFeatureEntry {
    override val routeClass = GameplayRoute::class
    override val baseRoute: GameplayRoute = GameplayRoute

    override fun createTyped(route: GameplayRoute, componentContext: ComponentContext): GameplayComponent {
        val koin = getKoin()
        return GameplayComponentImpl(
            componentContext = componentContext,
            storeFactory = koin.get()
        )
    }

    @Composable
    override fun RenderTyped(component: GameplayComponent) {
        GameplayView(component = component)
    }
}
