package com.dev.memebattle.host.root.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.dev.memebattle.core.navigation.entry.FeatureComponent
import com.dev.memebattle.core.navigation.entry.FeatureEntry
import com.dev.memebattle.core.navigation.output.NavigationContext
import com.dev.memebattle.core.navigation.output.NavigationOutput
import com.dev.memebattle.core.navigation.output.NavigationOutputHandler
import com.dev.memebattle.core.navigation.route.AppRoute
import com.dev.memebattle.feature.home.api.route.HomeRoute
import com.dev.memebattle.host.root.presentation.layer.GlobalHostLayer
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent

class RootComponentImpl(
    componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext, KoinComponent {

    private val hostLayers: List<com.dev.memebattle.core.navigation.layer.HostLayer> = getKoin().getAll()
    private val featureEntries: List<FeatureEntry<*>> = getKoin().getAll()
    private val handlers: List<NavigationOutputHandler> = getKoin().getAll()

    private val navigation = StackNavigation<AppRoute>()
    private val scope = coroutineScope()

    private val initialRoute: AppRoute = HomeRoute

    override val childStack: Value<ChildStack<AppRoute, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = null, // Временно без сериализации, так как AppRoute - interface без @Serializable
        initialConfiguration = initialRoute,
        handleBackButton = true,
        childFactory = ::createChild
    )

    private val navContext = object : NavigationContext {
        override val navigation: StackNavigation<AppRoute> get() = this@RootComponentImpl.navigation
        override val stack: Value<ChildStack<out AppRoute, FeatureComponent>> get() = TODO("Not fully mapped")
        
        override fun findComponent(route: AppRoute): FeatureComponent? {
            return childStack.value.items.find { it.configuration == route }?.instance?.component
        }
    }

    private fun createChild(config: AppRoute, context: ComponentContext): RootComponent.Child {
        val entry = featureEntries.firstOrNull { it.routeClass.isInstance(config) }
            ?: throw IllegalStateException("No FeatureEntry found for route $config")
            
        @Suppress("UNCHECKED_CAST")
        val typedEntry = entry as FeatureEntry<AppRoute>
        val component = typedEntry.create(config, context)

        component.output.onEach { output ->
            handleNavigationOutput(output)
        }.launchIn(scope)

        val hostLayer = hostLayers.firstOrNull { it.supports(entry) } ?: GlobalHostLayer()

        return RootComponent.Child(
            route = config,
            entry = entry,
            component = component,
            hostLayer = hostLayer
        )
    }

    private fun handleNavigationOutput(output: NavigationOutput) {
        for (handler in handlers) {
            if (handler.canHandle(output, navContext)) {
                handler.handle(output, navContext)
                return
            }
        }

        when (output) {
            is NavigationOutput.Back -> navigation.pop()
            is NavigationOutput.BringToFront -> navigation.bringToFront(output.route)
            is NavigationOutput.NavigateTo -> navigation.bringToFront(output.route)
            is NavigationOutput.PopAndBringToFront -> {
                navigation.pop()
                navigation.bringToFront(output.route)
            }
            is NavigationOutput.ReplaceAll -> navigation.replaceAll(*output.stack.toTypedArray())
        }
    }

    override fun onNavigate(route: AppRoute) {
        navigation.bringToFront(route)
    }
}
