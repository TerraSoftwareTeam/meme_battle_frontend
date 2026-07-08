package com.dev.memebattle.feature.packs.impl.feature

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.dev.memebattle.core.navigation.entry.FeatureComponent
import com.dev.memebattle.core.navigation.entry.TypedFeatureEntry
import com.dev.memebattle.feature.packs.api.entry.PacksFeatureEntry
import com.dev.memebattle.feature.packs.api.route.PacksRoute
import com.dev.memebattle.feature.packs.impl.presentation.component.PacksComponent
import com.dev.memebattle.feature.packs.impl.presentation.component.PacksComponentImpl
import com.dev.memebattle.feature.packs.impl.presentation.view.PacksView
import org.koin.mp.KoinPlatform.getKoin

class PacksFeatureEntryImpl : TypedFeatureEntry<PacksComponent, PacksRoute>(), PacksFeatureEntry {
    override val routeClass = PacksRoute::class
    override val baseRoute: PacksRoute = PacksRoute

    override fun createTyped(route: PacksRoute, componentContext: ComponentContext): PacksComponent {
        val koin = getKoin()
        return PacksComponentImpl(
            componentContext = componentContext,
            storeFactory = koin.get()
        )
    }

    @Composable
    override fun RenderTyped(component: PacksComponent) {
        PacksView(component = component)
    }
}
