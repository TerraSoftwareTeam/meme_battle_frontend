package com.dev.memebattle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.retainedComponent
import com.dev.memebattle.di.initKoin
import com.dev.memebattle.host.root.presentation.component.RootComponentImpl
import com.dev.memebattle.host.root.presentation.view.RootScreen
import org.koin.android.ext.koin.androidContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initKoinIfNeeded()

        val rootComponent = retainedComponent { componentContext ->
            RootComponentImpl(componentContext = componentContext)
        }

        setContent {
            RootScreen(component = rootComponent)
        }
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
