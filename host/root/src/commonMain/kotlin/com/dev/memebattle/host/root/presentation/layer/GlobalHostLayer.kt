package com.dev.memebattle.host.root.presentation.layer

import androidx.compose.runtime.Composable
import com.dev.memebattle.core.navigation.layer.HostLayer
import com.dev.memebattle.core.navigation.entry.FeatureEntry
import com.dev.memebattle.core.navigation.entry.FeatureComponent
import com.dev.memebattle.core.navigation.layer.GlobalLayerFeature
import com.dev.memebattle.core.navigation.route.AppRoute

class GlobalHostLayer : HostLayer {
    override val key: String = "global_layer"

    override fun supports(entry: FeatureEntry<*>): Boolean {
        return entry is GlobalLayerFeature<*>
    }

    @Composable
    override fun Render(
        entry: FeatureEntry<*>,
        component: FeatureComponent,
        onNavigate: (AppRoute) -> Unit
    ) {
        entry.Render(component)
    }
}
