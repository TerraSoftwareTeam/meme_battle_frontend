package com.dev.memebattle.feature.packs.impl.presentation.view.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.feature.packs.impl.presentation.component.catalog.PacksCatalogComponent
import com.dev.memebattle.feature.packs.impl.presentation.store.catalog.PacksCatalogStore
import com.dev.memebattle.feature.packs.impl.presentation.view.catalog.widgets.PacksCatalogBottomBar
import com.dev.memebattle.feature.packs.impl.presentation.view.catalog.widgets.PacksList
import com.dev.memebattle.feature.packs.impl.presentation.view.catalog.widgets.AccentPrimary
import com.dev.memebattle.feature.packs.impl.presentation.view.catalog.widgets.BackgroundBottom
import com.dev.memebattle.feature.packs.impl.presentation.view.catalog.widgets.BackgroundTop
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.packs_title


@Composable
fun PacksCatalogView(
    component: PacksCatalogComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()

    LaunchedEffect(component) {
        component.effects.collectLatest { _ ->

        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom))
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { component.onIntent(PacksCatalogStore.Intent.GoBack) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.packs_title),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }


            PacksList(
                state = state,
                onPackClick = { packId ->
                    component.onIntent(PacksCatalogStore.Intent.OpenDetails(packId))
                },
                modifier = Modifier.weight(1f),
            )


            PacksCatalogBottomBar(
                activeType = state.activeType,
                onSwitchType = { type ->
                    component.onIntent(PacksCatalogStore.Intent.SwitchPackType(type))
                },
                onCreateClick = {
                    component.onIntent(PacksCatalogStore.Intent.OpenCreate)
                },
            )
        }


        AnimatedVisibility(
            visible = state.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            CircularProgressIndicator(color = AccentPrimary)
        }
    }
}
