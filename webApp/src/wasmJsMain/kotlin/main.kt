import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import com.dev.memebattle.host.root.presentation.component.RootComponentImpl
import com.dev.memebattle.host.root.presentation.view.RootScreen
import kotlinx.browser.document
import kotlinx.browser.window
import com.dev.memebattle.di.initKoin
import com.dev.memebattle.core.network.WebApiConfig
import org.w3c.dom.events.Event


private fun isDocumentVisible(): Boolean = js("document.visibilityState === 'visible'")

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val origin = window.location.origin
    println("webOrigin initialized to: $origin")
    com.dev.memebattle.core.data.packs.mapper.PlatformEnv.webOrigin = origin
    // Route all API calls through local dev-server proxy to avoid CORS preflight for PATCH/DELETE
    // ONLY when running locally. In production, use the real API directly.
    if (origin.contains("localhost") || origin.contains("127.0.0.1")) {
        WebApiConfig.apiBaseUrl = "$origin/api-proxy"
        // Route WebSocket through local dev-server proxy to avoid CORS for ws/wss
        val wsOrigin = origin.replace(Regex("^http"), "ws")
        WebApiConfig.wsBaseUrl = "$wsOrigin/ws-proxy"
    }
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
        if (!backDispatcher.back()) {
            // Decompose stack is empty — nothing to pop, re-push so user stays in the app
            window.history.pushState(data = null, title = "", url = null)
        }
    })

    val rootComponent = RootComponentImpl(
        componentContext = DefaultComponentContext(
            lifecycle = lifecycle,
            backHandler = backDispatcher,
        )
    )

    ComposeViewport(document.body!!) {
        RootScreen(
            component = rootComponent
        )
    }
}
