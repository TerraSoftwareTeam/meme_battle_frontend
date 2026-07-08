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
import org.koin.mp.KoinPlatform.getKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
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
