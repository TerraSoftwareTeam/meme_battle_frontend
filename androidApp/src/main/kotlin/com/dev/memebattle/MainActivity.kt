package com.dev.memebattle

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.retainedComponent
import com.dev.memebattle.core.navigation.route.AppRoute
import com.dev.memebattle.di.initKoin
import com.dev.memebattle.feature.home.api.route.HomeRoute
import com.dev.memebattle.feature.packs.api.route.PacksRoute
import com.dev.memebattle.host.root.presentation.component.RootComponentImpl
import com.dev.memebattle.host.root.presentation.view.RootScreen
import org.koin.android.ext.koin.androidContext
import okio.Path.Companion.toPath
import com.dev.memebattle.core.localization.initAndroidLocalization

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initAndroidLocalization(applicationContext)
        initKoinIfNeeded()

        val initialRoute = parseDeepLink(intent)

        val rootComponent = retainedComponent { componentContext ->
            RootComponentImpl(
                componentContext = componentContext,
                initialRoute = initialRoute,
            )
        }

        setContent {
            coil3.compose.setSingletonImageLoaderFactory { context ->
                coil3.ImageLoader.Builder(context)
                    .components {
                        add(coil3.network.ktor3.KtorNetworkFetcherFactory())
                    }
                    .memoryCache {
                        coil3.memory.MemoryCache.Builder()
                            .maxSizePercent(context, 0.25)
                            .build()
                    }
                    .diskCache {
                        coil3.disk.DiskCache.Builder()
                            .directory(context.cacheDir.resolve("image_cache").absolutePath.toPath())
                            .maxSizeBytes(100L * 1024 * 1024)
                            .build()
                    }
                    .build()
            }
            RootScreen(component = rootComponent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Приложение уже запущено — navigating через существующий rootComponent невозможно здесь
        // напрямую (retainedComponent не переотдаёт ссылку). Перезапускаем Activity:
        setIntent(intent)
        recreate()
    }

    private fun initKoinIfNeeded() {
        try {
            initKoin {
                androidContext(this@MainActivity)
            }
        } catch (_: Exception) {
            // Koin already started
        }
    }
}

/**
 * Парсит android Intent и возвращает соответствующий AppRoute.
 * - https://meme.skyfly.hackclub.app/lobby/{id} → HomeRoute(openLobbyId = id)
 * - https://meme.skyfly.hackclub.app/pack/{id}  → PacksRoute(openPackId = id, openPackKind = kind)
 * - всё остальное (или null)                   → HomeRoute()
 */
private fun parseDeepLink(intent: Intent): AppRoute {
    if (intent.action != Intent.ACTION_VIEW) return HomeRoute()
    val uri: Uri = intent.data ?: return HomeRoute()
    val path = uri.path ?: return HomeRoute()
    return when {
        path.startsWith("/lobby/") -> {
            val lobbyId = uri.lastPathSegment?.takeIf { it.isNotEmpty() }
            if (lobbyId != null) HomeRoute(openLobbyId = lobbyId) else HomeRoute()
        }
        path.startsWith("/pack/") -> {
            val packId = uri.lastPathSegment?.takeIf { it.isNotEmpty() }
            val kind = uri.getQueryParameter("kind") ?: "meme"
            if (packId != null) PacksRoute(openPackId = packId, openPackKind = kind) else PacksRoute()
        }
        else -> HomeRoute()
    }
}
