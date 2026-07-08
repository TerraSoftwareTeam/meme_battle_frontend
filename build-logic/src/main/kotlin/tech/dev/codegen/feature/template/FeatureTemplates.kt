package tech.dev.codegen.feature.template

object FeatureTemplates {
    fun apiRoute(pkg: String, pascal: String) = """
        package $pkg.api.route

        import com.dev.memebattle.core.navigation.route.AppRoute
        import kotlinx.serialization.Serializable

        @Serializable
        data object ${pascal}Route : AppRoute
    """.trimIndent()

    fun apiFeatureEntry(pkg: String, pascal: String) = """
        package $pkg.api.entry

        import com.dev.memebattle.core.navigation.entry.FeatureComponent
        import com.dev.memebattle.core.navigation.entry.FeatureEntry
        import $pkg.api.route.${pascal}Route

        interface ${pascal}FeatureEntry : FeatureEntry<${pascal}Route>
    """.trimIndent()

    fun implFeatureEntry(pkg: String, pascal: String) = """
        package $pkg.impl.feature

        import androidx.compose.runtime.Composable
        import com.arkivanov.decompose.ComponentContext
        import com.dev.memebattle.core.navigation.entry.FeatureComponent
        import com.dev.memebattle.core.navigation.entry.TypedFeatureEntry
        import $pkg.api.entry.${pascal}FeatureEntry
        import $pkg.api.route.${pascal}Route
        import $pkg.impl.presentation.component.${pascal}Component
        import $pkg.impl.presentation.component.${pascal}ComponentImpl
        import $pkg.impl.presentation.view.${pascal}View
        import org.koin.mp.KoinPlatform.getKoin

        class ${pascal}FeatureEntryImpl : TypedFeatureEntry<${pascal}Component, ${pascal}Route>(), ${pascal}FeatureEntry {
            override val routeClass = ${pascal}Route::class
            override val baseRoute: ${pascal}Route = ${pascal}Route

            override fun createTyped(route: ${pascal}Route, componentContext: ComponentContext): ${pascal}Component {
                val koin = getKoin()
                return ${pascal}ComponentImpl(
                    componentContext = componentContext,
                    storeFactory = koin.get()
                )
            }

            @Composable
            override fun RenderTyped(component: ${pascal}Component) {
                ${pascal}View(component = component)
            }
        }
    """.trimIndent()

    fun component(pkg: String, pascal: String) = """
        package $pkg.impl.presentation.component

        import com.dev.memebattle.core.navigation.entry.FeatureComponent
        import kotlinx.coroutines.flow.StateFlow
        import kotlinx.coroutines.flow.SharedFlow
        import $pkg.impl.presentation.store.${pascal}Store

        interface ${pascal}Component : FeatureComponent {
            val state: StateFlow<${pascal}Store.State>
            val effects: SharedFlow<${pascal}Store.Effect>
            fun onIntent(intent: ${pascal}Store.Intent)
        }
    """.trimIndent()

    fun componentImpl(pkg: String, pascal: String, withDomain: Boolean) = """
        package $pkg.impl.presentation.component

        import com.arkivanov.decompose.ComponentContext
        import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
        import com.arkivanov.mvikotlin.core.store.StoreFactory
        import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
        import com.arkivanov.mvikotlin.extensions.coroutines.labels
        import kotlinx.coroutines.flow.StateFlow
        import kotlinx.coroutines.flow.SharedFlow
        import kotlinx.coroutines.flow.shareIn
        import kotlinx.coroutines.flow.SharingStarted
        import kotlinx.coroutines.ExperimentalCoroutinesApi
        import $pkg.impl.presentation.store.${pascal}Store
        import $pkg.impl.presentation.store.${pascal}StoreFactory
        ${if (withDomain) "import $pkg.impl.domain.${pascal}Interactor" else ""}

        class ${pascal}ComponentImpl(
            componentContext: ComponentContext,
            private val storeFactory: StoreFactory${if (withDomain) ",\n            private val interactor: ${pascal}Interactor" else ""}
        ) : ${pascal}Component, ComponentContext by componentContext {
            
            private val scope = coroutineScope()
            private val store = ${pascal}StoreFactory(storeFactory${if (withDomain) ", interactor" else ""}).create()

            private val labelsFlow = store.labels.shareIn(scope, SharingStarted.Eagerly, replay = 0)
            override val effects: SharedFlow<${pascal}Store.Effect> = labelsFlow

            @OptIn(ExperimentalCoroutinesApi::class)
            override val state: StateFlow<${pascal}Store.State> = store.stateFlow(scope)

            override val output: kotlinx.coroutines.flow.Flow<com.dev.memebattle.core.navigation.output.NavigationOutput> = kotlinx.coroutines.flow.emptyFlow()

            override fun onIntent(intent: ${pascal}Store.Intent) = store.accept(intent)
        }
    """.trimIndent()

    fun store(pkg: String, pascal: String) = """
        package $pkg.impl.presentation.store

        import com.arkivanov.mvikotlin.core.store.Store

        interface ${pascal}Store : Store<${pascal}Store.Intent, ${pascal}Store.State, ${pascal}Store.Effect> {
            sealed interface Intent { data object Init : Intent }
            data class State(val isLoading: Boolean = false)
            sealed interface Effect { data object NavigateBack : Effect }
        }
    """.trimIndent()

    fun storeFactory(pkg: String, pascal: String, withDomain: Boolean) = """
        package $pkg.impl.presentation.store

        import com.arkivanov.mvikotlin.core.store.Reducer
        import com.arkivanov.mvikotlin.core.store.Store
        import com.arkivanov.mvikotlin.core.store.StoreFactory
        import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
        ${if (withDomain) "import $pkg.impl.domain.${pascal}Interactor" else ""}

        internal class ${pascal}StoreFactory(
            private val storeFactory: StoreFactory${if (withDomain) ",\n            private val interactor: ${pascal}Interactor" else ""}
        ) {
            fun create(): ${pascal}Store = object : ${pascal}Store, Store<${pascal}Store.Intent, ${pascal}Store.State, ${pascal}Store.Effect> by storeFactory.create(
                name = "${pascal}Store", initialState = ${pascal}Store.State(), executorFactory = ::${pascal}Executor, reducer = ${pascal}Reducer
            ) {}

            private inner class ${pascal}Executor : CoroutineExecutor<${pascal}Store.Intent, Nothing, ${pascal}Store.State, Message, ${pascal}Store.Effect>() {
                override fun executeIntent(intent: ${pascal}Store.Intent) {
                    when (intent) { is ${pascal}Store.Intent.Init -> {} }
                }
            }
            private sealed interface Message { data class Loading(val isLoading: Boolean) : Message }
            private object ${pascal}Reducer : Reducer<${pascal}Store.State, Message> {
                override fun ${pascal}Store.State.reduce(msg: Message): ${pascal}Store.State = when (msg) { is Message.Loading -> copy(isLoading = msg.isLoading) }
            }
        }
    """.trimIndent()

    fun view(pkg: String, pascal: String) = """
        package $pkg.impl.presentation.view

        import androidx.compose.foundation.layout.Box
        import androidx.compose.foundation.layout.fillMaxSize
        import androidx.compose.material3.Text
        import androidx.compose.runtime.Composable
        import androidx.compose.runtime.LaunchedEffect
        import androidx.compose.runtime.getValue
        import androidx.compose.ui.Alignment
        import androidx.compose.ui.Modifier
        import androidx.compose.runtime.collectAsState
        import $pkg.impl.presentation.component.${pascal}Component
        import kotlinx.coroutines.flow.collectLatest

        @Composable
        fun ${pascal}View(component: ${pascal}Component) {
            val state by component.state.collectAsState()
            
            LaunchedEffect(component) {
                component.effects.collectLatest { effect ->
                    // Handle effects
                }
            }

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "${pascal} Feature")
            }
        }
    """.trimIndent()
    
    fun diModule(pkg: String, pascal: String, withDomain: Boolean, withData: Boolean) = """
        package $pkg.impl.di

        import $pkg.api.entry.${pascal}FeatureEntry
        import $pkg.impl.feature.${pascal}FeatureEntryImpl
        ${if (withDomain) "import $pkg.impl.domain.${pascal}Interactor" else ""}
        ${if (withData) "import $pkg.impl.domain.${pascal}Repository\nimport $pkg.impl.data.${pascal}RepositoryImpl" else ""}
        import com.dev.memebattle.core.navigation.entry.FeatureEntry
        import org.koin.dsl.bind
        import org.koin.core.module.dsl.factoryOf
        import org.koin.core.module.dsl.singleOf
        import org.koin.dsl.module

        val ${pascal.replaceFirstChar { it.lowercase() }}Module = module {
            ${if (withData) "singleOf(::${pascal}RepositoryImpl) bind ${pascal}Repository::class" else ""}
            ${if (withDomain) "factoryOf(::${pascal}Interactor)" else ""}
            factoryOf(::${pascal}FeatureEntryImpl) bind FeatureEntry::class
        }
    """.trimIndent()
}
