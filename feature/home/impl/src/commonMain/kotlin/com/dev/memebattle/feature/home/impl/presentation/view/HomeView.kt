package com.dev.memebattle.feature.home.impl.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import com.dev.memebattle.feature.home.impl.presentation.component.HomeComponent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeView(component: HomeComponent) {
    val state by component.state.collectAsState()
    
    LaunchedEffect(component) {
        component.effects.collectLatest { effect ->
            // Handle effects
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Home Feature")
    }
}
