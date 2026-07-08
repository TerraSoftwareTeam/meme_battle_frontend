package com.dev.memebattle.core.navigation.entry

import com.dev.memebattle.core.navigation.output.NavigationOutput
import kotlinx.coroutines.flow.Flow

/**
 * Базовый интерфейс для всех Decompose-компонентов.
 */
interface FeatureComponent {
    val output: Flow<NavigationOutput>
}
