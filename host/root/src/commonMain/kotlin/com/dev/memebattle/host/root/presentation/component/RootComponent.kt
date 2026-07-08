package com.dev.memebattle.host.root.presentation.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.dev.memebattle.core.navigation.entry.FeatureComponent
import com.dev.memebattle.core.navigation.entry.FeatureEntry
import com.dev.memebattle.core.navigation.route.AppRoute

interface RootComponent {
    val childStack: Value<ChildStack<AppRoute, Child>>
    
    fun onNavigate(route: AppRoute)

    data class Child(
        val route: AppRoute,
        val entry: FeatureEntry<*>,
        val component: FeatureComponent,
        val hostLayer: com.dev.memebattle.core.navigation.layer.HostLayer,
    )
}
