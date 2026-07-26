package com.dev.memebattle.feature.packs.impl.presentation.view.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.feature.packs.impl.presentation.component.catalog.PacksCatalogComponent
import com.dev.memebattle.feature.packs.impl.presentation.store.catalog.PacksCatalogStore
import com.dev.memebattle.feature.packs.impl.presentation.view.catalog.widgets.PacksCatalogBottomBar
import com.dev.memebattle.feature.packs.impl.presentation.view.catalog.widgets.PacksList
import com.dev.memebattle.core.ui.components.pack.AccentPrimary
import com.dev.memebattle.core.ui.components.pack.BackgroundBottom
import com.dev.memebattle.core.ui.components.pack.BackgroundTop
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.packs_title
import com.dev.memebattle.core.localization.packs_filter_all
import com.dev.memebattle.core.localization.packs_filter_personal
import com.dev.memebattle.core.localization.packs_filter_liked
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import com.dev.memebattle.core.ui.components.pack.TextSecondary


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

                Spacer(Modifier.weight(1f))

                val infiniteTransition = rememberInfiniteTransition()
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                IconButton(
                    onClick = { component.onIntent(PacksCatalogStore.Intent.Refresh) },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White,
                        modifier = Modifier.graphicsLayer {
                            rotationZ = if (state.isLoading || state.isRefreshing) rotation else 0f
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterTab(
                        text = stringResource(Res.string.packs_filter_all),
                        isSelected = state.activeFilter == PacksCatalogStore.PackFilter.All,
                        onClick = { component.onIntent(PacksCatalogStore.Intent.SwitchPackFilter(PacksCatalogStore.PackFilter.All)) }
                    )
                    FilterTab(
                        text = stringResource(Res.string.packs_filter_personal),
                        isSelected = state.activeFilter == PacksCatalogStore.PackFilter.Personal,
                        onClick = { component.onIntent(PacksCatalogStore.Intent.SwitchPackFilter(PacksCatalogStore.PackFilter.Personal)) }
                    )
                    FilterTab(
                        text = stringResource(Res.string.packs_filter_liked),
                        isSelected = state.activeFilter == PacksCatalogStore.PackFilter.Liked,
                        onClick = { component.onIntent(PacksCatalogStore.Intent.SwitchPackFilter(PacksCatalogStore.PackFilter.Liked)) }
                    )
                }
            }


            PacksList(
                state = state,
                onPackClick = { packId ->
                    component.onIntent(PacksCatalogStore.Intent.OpenDetails(packId))
                },
                onEditClick = { packId ->
                    component.onIntent(PacksCatalogStore.Intent.OpenEdit(packId))
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



    }
}

@Composable
private fun FilterTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) AccentPrimary else Color.Transparent
    val textColor = if (isSelected) Color.White else TextSecondary

    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}
