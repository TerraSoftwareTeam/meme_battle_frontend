package com.dev.memebattle.core.navigation.entry

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.dev.memebattle.core.navigation.route.AppRoute
import kotlin.reflect.KClass

/**
 * Базовый контракт для регистрации фичи в графе навигации.
 */
interface FeatureEntry<R : AppRoute> {
    val routeClass: KClass<R>
    val baseRoute: R

    fun create(route: R, componentContext: ComponentContext): FeatureComponent
    
    @Composable
    fun Render(component: FeatureComponent)
}

/**
 * Типизированная база, убирающая необходимость кастов внутри фич.
 */
abstract class TypedFeatureEntry<C : FeatureComponent, R : AppRoute> : FeatureEntry<R> {
    
    abstract fun createTyped(route: R, componentContext: ComponentContext): C
    
    @Composable
    abstract fun RenderTyped(component: C)

    @Suppress("UNCHECKED_CAST")
    final override fun create(route: R, componentContext: ComponentContext): FeatureComponent {
        return createTyped(route, componentContext)
    }

    @Suppress("UNCHECKED_CAST")
    @Composable
    final override fun Render(component: FeatureComponent) {
        RenderTyped(component as C)
    }
}
