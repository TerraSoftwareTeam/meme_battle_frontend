import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.dev.memebattle.core.navigation.entry.FeatureEntry
import com.dev.memebattle.host.root.di.rootHostModule
import com.dev.memebattle.host.root.presentation.component.RootComponentImpl
import com.dev.memebattle.host.root.presentation.view.RootScreen
import kotlinx.browser.document
import com.dev.memebattle.di.initKoin
import com.dev.memebattle.core.network.WebApiConfig
import org.koin.mp.KoinPlatform.getKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val origin = kotlinx.browser.window.location.origin
    println("webOrigin initialized to: $origin")
    com.dev.memebattle.core.data.packs.mapper.PlatformEnv.webOrigin = origin
    // Route all API calls through local dev-server proxy to avoid CORS preflight for PATCH/DELETE
    WebApiConfig.apiBaseUrl = "$origin/api-proxy"
    // Route WebSocket through local dev-server proxy to avoid CORS for ws/wss
    val wsOrigin = origin.replace(Regex("^http"), "ws")
    WebApiConfig.wsBaseUrl = "$wsOrigin/ws-proxy"
    initKoin()


    val lifecycle = LifecycleRegistry()
    val rootComponent = RootComponentImpl(
        componentContext = DefaultComponentContext(lifecycle = lifecycle)
    )

    ComposeViewport(document.body!!) {
        RootScreen(
            component = rootComponent
        )
    }
}
