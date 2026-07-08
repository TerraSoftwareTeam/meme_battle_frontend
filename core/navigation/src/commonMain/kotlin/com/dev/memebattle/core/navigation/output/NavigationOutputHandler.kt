package com.dev.memebattle.core.navigation.output

/**
 * Обработчик навигации (Chain of Responsibility).
 * Каждая фича может зарегистрировать свой хендлер.
 */
interface NavigationOutputHandler {
    fun canHandle(output: NavigationOutput, ctx: NavigationContext): Boolean
    fun handle(output: NavigationOutput, ctx: NavigationContext)
}
