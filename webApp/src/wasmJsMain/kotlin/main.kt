import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import com.dev.memebattle.core.navigation.route.AppRoute
import com.dev.memebattle.feature.home.api.route.HomeRoute
import com.dev.memebattle.feature.packs.api.route.PacksRoute
import com.dev.memebattle.host.root.presentation.component.RootComponentImpl
import com.dev.memebattle.host.root.presentation.view.RootScreen
import kotlinx.browser.document
import kotlinx.browser.window
import com.dev.memebattle.di.initKoin
import com.dev.memebattle.core.network.WebApiConfig
import com.dev.memebattle.core.network.utils.MediaUrlEnv
import com.arkivanov.decompose.value.subscribe
import org.w3c.dom.events.Event


private fun isDocumentVisible(): Boolean = js("document.visibilityState === 'visible'")

/**
 * Парсит текущий URL браузера и возвращает соответствующий AppRoute.
 * - /lobby/{id}           → HomeRoute(openLobbyId = id)
 * - /pack/{id}?kind=meme  → PacksRoute(openPackId = id, openPackKind = kind)
 * - всё остальное         → HomeRoute()
 */
private fun parseDeepLink(pathname: String, search: String): AppRoute {
    val cleanPath = pathname.trimEnd('/')
    return when {
        cleanPath.startsWith("/lobby/") -> {
            val lobbyId = cleanPath.removePrefix("/lobby/").trim()
            if (lobbyId.isNotEmpty()) HomeRoute(openLobbyId = lobbyId) else HomeRoute()
        }
        cleanPath.startsWith("/pack/") -> {
            val packId = cleanPath.removePrefix("/pack/").trim()
            val kind = parseQueryParam(search, "kind") ?: "meme"
            if (packId.isNotEmpty()) PacksRoute(openPackId = packId, openPackKind = kind) else PacksRoute()
        }
        else -> HomeRoute()
    }
}

/** Минимальный парсер query-строки — достаёт значение одного параметра */
private fun parseQueryParam(search: String, key: String): String? {
    if (search.isEmpty()) return null
    val query = if (search.startsWith("?")) search.drop(1) else search
    return query.split("&")
        .map { it.split("=", limit = 2) }
        .firstOrNull { it.size == 2 && it[0] == key }
        ?.get(1)
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val origin = window.location.origin
    println("webOrigin initialized to: $origin")
    
    // Route all API calls and CDN requests through local dev-server proxy to avoid CORS preflight
    // ONLY when running locally. In production, use real CDN and API directly.
    if (origin.contains("localhost") || origin.contains("127.0.0.1")) {
        MediaUrlEnv.webOrigin = origin
        WebApiConfig.apiBaseUrl = "$origin/api-proxy"
        // Route WebSocket through local dev-server proxy to avoid CORS for ws/wss
        val wsOrigin = origin.replace(Regex("^http"), "ws")
        WebApiConfig.wsBaseUrl = "$wsOrigin/ws-proxy"
    }
    com.dev.memebattle.core.localization.initAppLanguage()
    initKoin()

    val lifecycle = LifecycleRegistry()
    val backDispatcher = BackDispatcher()

    // Manage lifecycle via page visibility (mirrors Android onResume/onStop)
    lifecycle.resume()
    window.addEventListener("visibilitychange", callback = { _: Event ->
        if (isDocumentVisible()) {
            lifecycle.resume()
        } else {
            lifecycle.stop()
        }
    })

    // Push a dummy history entry so the browser has something to go "back" from,
    // then intercept popstate (browser back/forward) and dispatch it to Decompose.
    window.history.pushState(data = null, title = "", url = null)
    window.addEventListener("popstate", callback = { _: Event ->
        if (backDispatcher.back()) {
            // Intercepted by Decompose — re-push history state so browser Back remains active for future clicks
            window.history.pushState(data = null, title = "", url = null)
        }
    })

    // Парсим диплинк из текущего URL перед созданием компонента
    val initialRoute = parseDeepLink(
        pathname = window.location.pathname,
        search = window.location.search
    )
    println("Deep link parsed: $initialRoute")

    val rootComponent = RootComponentImpl(
        componentContext = DefaultComponentContext(
            lifecycle = lifecycle,
            backHandler = backDispatcher,
        ),
        initialRoute = initialRoute,
    )

    // Синхронизируем URL браузера с текущим активным роутом в Decompose
    rootComponent.childStack.subscribe { stack ->
        val route = stack.active.configuration
        val targetPath = when (route) {
            is HomeRoute -> if (route.openLobbyId != null) "/lobby/${route.openLobbyId}" else "/"
            is PacksRoute -> if (route.openPackId != null) "/pack/${route.openPackId}?kind=${route.openPackKind}" else "/"
            else -> "/"
        }
        window.history.replaceState(null, "", targetPath)
    }

    ComposeViewport(document.body!!) {
        RootScreen(
            component = rootComponent
        )
    }
    hideLoadingScreen()
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun hideLoadingScreen() {
    js("window.hideLoadingScreen && window.hideLoadingScreen()")
}
