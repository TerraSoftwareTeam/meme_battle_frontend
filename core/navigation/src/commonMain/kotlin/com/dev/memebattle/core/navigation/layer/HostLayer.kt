package com.dev.memebattle.core.navigation.layer

import androidx.compose.runtime.Composable
import com.dev.memebattle.core.navigation.entry.FeatureEntry
import com.dev.memebattle.core.navigation.entry.FeatureComponent
import com.dev.memebattle.core.navigation.route.AppRoute

interface HostLayer {
    val key: String
    
    fun supports(entry: FeatureEntry<*>): Boolean
    
    @Composable
    fun Render(
        entry: FeatureEntry<*>,
        component: FeatureComponent,
        onNavigate: (AppRoute) -> Unit
    )
}
