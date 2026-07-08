package com.dev.memebattle.core.navigation.output

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.value.Value
import com.dev.memebattle.core.navigation.entry.FeatureComponent
import com.dev.memebattle.core.navigation.route.AppRoute

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
