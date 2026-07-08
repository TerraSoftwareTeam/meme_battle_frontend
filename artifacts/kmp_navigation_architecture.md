# Архитектура Навигации KMP (Decompose + Clean Architecture)

Этот документ содержит полные реализации всех слоев навигации согласно требованиям. 
Архитектура построена на 3 слоях:
1. **Root** — управляет `ChildStack` и делегирует события навигации.
2. **Host Layer** — абстрактные UI-обёртки (с BottomBar, без бара и т.д.).
3. **Feature** — изолированные фичи со своими точками входа и `NavigationOutputHandler`.

---

## 1. Контракты (Модуль `:core:navigation`)

### `core/navigation/output/NavigationOutput.kt`
```kotlin
package com.dev.core.navigation.output

import com.dev.core.navigation.route.AppRoute

/**
 * Маркерный интерфейс для передачи данных между экранами
 */
interface NavigationPayload

/**
 * События навигации, которые могут эмиттить фичи
 */
sealed interface NavigationOutput {
    data object Back : NavigationOutput
    data class NavigateTo(val route: AppRoute) : NavigationOutput
    data class BringToFront(val route: AppRoute, val payload: NavigationPayload? = null) : NavigationOutput
    data class PopAndBringToFront(val route: AppRoute, val payload: NavigationPayload? = null) : NavigationOutput
    data class ReplaceAll(val stack: List<AppRoute>) : NavigationOutput
}
```

### `core/navigation/output/FeatureComponent.kt`
```kotlin
package com.dev.core.navigation.output

import kotlinx.coroutines.flow.Flow

/**
 * Базовый компонент фичи.
 * Все фичи должны возвращать поток событий навигации (или emptyFlow).
 */
interface FeatureComponent {
    val output: Flow<NavigationOutput>
}
```

### `core/navigation/output/NavigationContext.kt`
```kotlin
package com.dev.core.navigation.output

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.value.Value
import com.dev.core.navigation.route.AppRoute

/**
 * Контекст навигации, передаваемый в обработчики.
 */
interface NavigationContext {
    val navigation: StackNavigation<AppRoute>
    val stack: Value<ChildStack<out AppRoute, FeatureComponent>>
    
    /**
     * Поиск активного компонента по роуту в текущем стеке.
     */
    fun findComponent(route: AppRoute): FeatureComponent?
}
```

### `core/navigation/output/NavigationOutputHandler.kt`
```kotlin
package com.dev.core.navigation.output

/**
 * Обработчик навигации (Chain of Responsibility).
 * Каждая фича может зарегистрировать свой хендлер.
 */
interface NavigationOutputHandler {
    fun canHandle(output: NavigationOutput, ctx: NavigationContext): Boolean
    fun handle(output: NavigationOutput, ctx: NavigationContext)
}
```

### `core/navigation/entry/FeatureEntry.kt`
```kotlin
package com.dev.core.navigation.entry

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.dev.core.navigation.output.FeatureComponent
import com.dev.core.navigation.route.AppRoute
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
```

### `core/navigation/entry/TypedFeatureEntry.kt`
```kotlin
package com.dev.core.navigation.entry

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.dev.core.navigation.output.FeatureComponent
import com.dev.core.navigation.route.AppRoute

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
```

### `core/navigation/layer/LayerFeature.kt`
```kotlin
import org.jetbrains.compose.resources.StringResource

interface GlobalLayerFeature<R : AppRoute> : FeatureEntry<R>

interface MainLayerFeature<R : AppRoute> : FeatureEntry<R> {
    val tabIcon: ImageVector
    val tabLabel: StringResource
    val tabOrder: Int
}
```

### `core/navigation/host/HostLayer.kt`
```kotlin
package com.dev.core.navigation.host

import androidx.compose.runtime.Composable
import com.dev.core.navigation.entry.FeatureEntry
import com.dev.core.navigation.output.FeatureComponent
import com.dev.core.navigation.route.AppRoute

/**
 * Интерфейс UI-обёртки слоя.
 */
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
```

---

## 2. Роуты (`:core:navigation:route`)

### `core/navigation/route/AppRoute.kt`
```kotlin
package com.dev.core.navigation.route

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {
    @Serializable
    sealed interface Map : AppRoute {
        @Serializable
        data object Free : Map
    }
    
    @Serializable
    data object Catalog : AppRoute
    
    @Serializable
    data object Auth : AppRoute
    
    @Serializable
    data object Placeholder : AppRoute
}
```

---

## 3. UI Слои (Host Layers)

### `hosts/global/GlobalHostLayer.kt`
```kotlin
package com.dev.hosts.global

import androidx.compose.runtime.Composable
import com.dev.core.navigation.entry.FeatureEntry
import com.dev.core.navigation.host.HostLayer
import com.dev.core.navigation.layer.GlobalLayerFeature
import com.dev.core.navigation.output.FeatureComponent
import com.dev.core.navigation.route.AppRoute

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
        // Прямой рендер без обёрток
        entry.Render(component)
    }
}
```

### `hosts/main/LocalMainLayerEntries.kt`
```kotlin
package com.dev.hosts.main

import androidx.compose.runtime.compositionLocalOf
import com.dev.core.navigation.layer.MainLayerFeature

val LocalMainLayerEntries = compositionLocalOf<List<MainLayerFeature<*>>> { 
    error("MainLayerEntries not provided") 
}
```

### `hosts/main/MainHostLayer.kt`
```kotlin
package com.dev.hosts.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import com.dev.core.navigation.entry.FeatureEntry
import com.dev.core.navigation.host.HostLayer
import com.dev.core.navigation.layer.MainLayerFeature
import com.dev.core.navigation.output.FeatureComponent
import com.dev.core.navigation.route.AppRoute

class MainHostLayer : HostLayer {
    override val key: String = "main_layer"

    override fun supports(entry: FeatureEntry<*>): Boolean {
        return entry is MainLayerFeature<*>
    }

    @Composable
    override fun Render(
        entry: FeatureEntry<*>,
        component: FeatureComponent,
        onNavigate: (AppRoute) -> Unit
    ) {
        val tabEntries = LocalMainLayerEntries.current

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    tabEntries.forEach { tab ->
                        val isSelected = entry.routeClass == tab.routeClass
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { onNavigate(tab.baseRoute) },
                            icon = { Icon(tab.tabIcon, contentDescription = null) },
                            label = { Text(stringResource(tab.tabLabel)) } 
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                entry.Render(component)
            }
        }
    }
}
```

---

## 4. Диспетчер (Root)

### `root/RootComponent.kt`
```kotlin
package com.dev.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.dev.core.navigation.output.FeatureComponent
import com.dev.core.navigation.route.AppRoute

interface RootComponent {
    val childStack: Value<ChildStack<AppRoute, Child>>
    fun onNavigate(route: AppRoute)

    data class Child(
        val route: AppRoute,
        val entry: FeatureEntry<*>,
        val component: FeatureComponent,
        val hostLayer: HostLayer,
    )
}
```

### `root/DefaultRootComponent.kt`
```kotlin
package com.dev.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.dev.core.navigation.entry.FeatureEntry
import com.dev.core.navigation.output.FeatureComponent
import com.dev.core.navigation.output.NavigationContext
import com.dev.core.navigation.output.NavigationOutput
import com.dev.core.navigation.output.NavigationOutputHandler
import com.dev.core.navigation.route.AppRoute
import com.dev.core.navigation.host.HostLayer
import com.dev.hosts.global.GlobalHostLayer
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val featureEntries: List<FeatureEntry<*>>,
    private val hostLayers: List<HostLayer>,
    private val handlers: List<NavigationOutputHandler>
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<AppRoute>()
    private val scope = coroutineScope()

    override val childStack: Value<ChildStack<AppRoute, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = AppRoute.serializer(),
        initialConfiguration = AppRoute.Catalog,
        handleBackButton = true,
        childFactory = ::createChild
    )

    private val navContext = object : NavigationContext {
        override val navigation: StackNavigation<AppRoute> get() = this@DefaultRootComponent.navigation
        override val stack: Value<ChildStack<out AppRoute, FeatureComponent>> get() = TODO("Адаптировать если нужно")
        
        override fun findComponent(route: AppRoute): FeatureComponent? {
            return childStack.value.items.find { it.configuration == route }?.instance?.component
        }
    }

    private fun createChild(config: AppRoute, context: ComponentContext): RootComponent.Child {
        val entry = featureEntries.firstOrNull { it.routeClass.isInstance(config) }
            ?: PlaceholderEntry // Fallback
            
        @Suppress("UNCHECKED_CAST")
        val typedEntry = entry as FeatureEntry<AppRoute>
        val component = typedEntry.create(config, context)
        val hostLayer = hostLayers.firstOrNull { it.supports(entry) } ?: GlobalHostLayer()

        // Подписываемся на события навигации от компонента
        component.output.onEach { output ->
            handleNavigationOutput(output)
        }.launchIn(scope)

        return RootComponent.Child(
            route = config,
            entry = entry,
            component = component,
            hostLayer = hostLayer
        )
    }

    private fun handleNavigationOutput(output: NavigationOutput) {
        // Проверяем кастомные хендлеры из фич
        for (handler in handlers) {
            if (handler.canHandle(output, navContext)) {
                handler.handle(output, navContext)
                return
            }
        }

        // Дефолтная логика
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
```

### `root/RootScreen.kt`
```kotlin
package com.dev.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.dev.core.navigation.layer.MainLayerFeature
import com.dev.hosts.main.LocalMainLayerEntries

@Composable
fun RootScreen(
    component: RootComponent,
    mainTabs: List<MainLayerFeature<*>> // Получаем один раз из DI
) {
    CompositionLocalProvider(
        LocalMainLayerEntries provides mainTabs
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Children(
                stack = component.childStack,
                animation = stackAnimation(fade())
            ) { child ->
                // Полностью тупой рендер — всё уже решено в RootComponent
                child.instance.hostLayer.Render(
                    entry = child.instance.entry,
                    component = child.instance.component,
                    onNavigate = component::onNavigate
                )
            }
            
            // Глобальные overlay (например, ErrorChip) рендерятся поверх ChildStack
            // ErrorChipOverlay()
        }
    }
}
```

---

## 5. Пример Фичи (`:feature:map`)

### `feature/map/navigation/MapFeatureEntry.kt`
```kotlin
package com.dev.feature.map.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.arkivanov.decompose.ComponentContext
import com.dev.core.navigation.entry.TypedFeatureEntry
import com.dev.core.navigation.layer.MainLayerFeature
import com.dev.core.navigation.route.AppRoute
import com.dev.feature.map.component.MapComponent
import com.dev.feature.map.component.DefaultMapComponent
import com.dev.feature.map.ui.MapScreen // Ваш UI
import kotlin.reflect.KClass

class MapFeatureEntry : TypedFeatureEntry<MapComponent, AppRoute.Map.Free>(), 
    MainLayerFeature<AppRoute.Map.Free> {

    override val routeClass: KClass<AppRoute.Map.Free> = AppRoute.Map.Free::class
    override val baseRoute: AppRoute.Map.Free = AppRoute.Map.Free

    override val tabIcon: ImageVector = Icons.Default.Place
    override val tabLabel: org.jetbrains.compose.resources.StringResource = Res.string.map_tab
    override val tabOrder: Int = 2

    override fun createTyped(route: AppRoute.Map.Free, componentContext: ComponentContext): MapComponent {
        return DefaultMapComponent(componentContext)
    }

    @Composable
    override fun RenderTyped(component: MapComponent) {
        MapScreen(component)
    }
}
```

### `feature/map/navigation/MapPayload.kt`
```kotlin
package com.dev.feature.map.navigation

import com.dev.core.navigation.output.NavigationPayload

data class MapPayload(val poiId: String) : NavigationPayload
```

### `feature/map/navigation/MapOutputHandler.kt`
```kotlin
package com.dev.feature.map.navigation

import com.arkivanov.decompose.router.stack.bringToFront
import com.dev.core.navigation.output.NavigationContext
import com.dev.core.navigation.output.NavigationOutput
import com.dev.core.navigation.output.NavigationOutputHandler
import com.dev.core.navigation.route.AppRoute
import com.dev.feature.map.component.MapComponent

class MapOutputHandler : NavigationOutputHandler {

    override fun canHandle(output: NavigationOutput, ctx: NavigationContext): Boolean {
        return output is NavigationOutput.BringToFront && output.route is AppRoute.Map.Free
    }

    override fun handle(output: NavigationOutput, ctx: NavigationContext) {
        if (output !is NavigationOutput.BringToFront) return
        val payload = output.payload as? MapPayload

        // Выполняем переход
        ctx.navigation.bringToFront(output.route)

        // Ищем компонент в стеке
        val component = ctx.findComponent(output.route) as? MapComponent
        
        if (component != null && payload != null) {
            // Компонент уже жив — передаем данные напрямую
            component.handlePayload(payload)
        } else if (payload != null) {
            // Компонент еще не создан или восстанавливается. 
            // Гарантированная доставка может быть реализована через 
            // обновление конфигурации роута (добавление payload прямо в AppRoute.Map.Free)
            // или через отдельный PayloadStore в DI, к которому компонент обратится при onResume.
            // В рамках данной архитектуры рекомендуется добавлять payload в AppRoute:
            // ctx.navigation.bringToFront(AppRoute.Map.Free(payload = payload))
            println("Payload delivery fallback: Store payload globally or in Route config.")
        }
    }
}
```

### `feature/map/component/MapComponent.kt`
```kotlin
package com.dev.feature.map.component

import com.arkivanov.decompose.ComponentContext
import com.dev.core.navigation.output.FeatureComponent
import com.dev.core.navigation.output.NavigationOutput
import com.dev.feature.map.navigation.MapPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

interface MapComponent : FeatureComponent {
    fun handlePayload(payload: MapPayload)
}

class DefaultMapComponent(
    componentContext: ComponentContext
) : MapComponent, ComponentContext by componentContext {

    private val _output = MutableSharedFlow<NavigationOutput>()
    override val output: Flow<NavigationOutput> = _output

    override fun handlePayload(payload: MapPayload) {
        println("Map received payload: ${payload.poiId}")
        // Обновление стейта карты...
    }
}
```

---

## 6. Пример Unit-Теста

```kotlin
package com.dev.feature.map.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.dev.core.navigation.output.FeatureComponent
import com.dev.core.navigation.output.NavigationContext
import com.dev.core.navigation.output.NavigationOutput
import com.dev.core.navigation.route.AppRoute
import com.dev.feature.map.component.MapComponent
import kotlin.test.Test
import kotlin.test.assertTrue

class MapOutputHandlerTest {

    @Test
    fun `when BringToFront MapFree is emitted, it handles payload`() {
        val handler = MapOutputHandler()
        val payload = MapPayload("poi-123")
        val output = NavigationOutput.BringToFront(AppRoute.Map.Free, payload)
        
        var handledPayload: MapPayload? = null
        val fakeComponent = object : MapComponent {
            override val output = kotlinx.coroutines.flow.emptyFlow<NavigationOutput>()
            override fun handlePayload(p: MapPayload) {
                handledPayload = p
            }
        }
        
        val fakeCtx = object : NavigationContext {
            override val navigation = StackNavigation<AppRoute>()
            override val stack: Value<ChildStack<out AppRoute, FeatureComponent>> = 
                MutableValue(ChildStack(configuration = AppRoute.Map.Free, instance = fakeComponent))
                
            override fun findComponent(route: AppRoute): FeatureComponent = fakeComponent
        }

        assertTrue(handler.canHandle(output, fakeCtx))
        handler.handle(output, fakeCtx)
        
        assertTrue(handledPayload == payload)
    }
}
```
