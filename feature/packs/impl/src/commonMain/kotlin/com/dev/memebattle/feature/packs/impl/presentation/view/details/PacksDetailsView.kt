package com.dev.memebattle.feature.packs.impl.presentation.view.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.dev.memebattle.feature.packs.impl.presentation.component.details.PacksDetailsComponent
import com.dev.memebattle.feature.packs.impl.presentation.store.details.PacksDetailsStore
import kotlinx.coroutines.flow.collectLatest

import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.*

private val BackgroundTop = Color(0xFF1A1035)
private val BackgroundBottom = Color(0xFF08040F)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFB0A2C7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacksDetailsView(
    component: PacksDetailsComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()

    LaunchedEffect(component) {
        component.effects.collectLatest { effect ->
            when (effect) {
                is PacksDetailsStore.Effect.NavigateBack -> component.onIntent(PacksDetailsStore.Intent.Close)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Топ-бар с кнопкой назад ───────────────────────────────────
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.packs_details_title),
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { component.onIntent(PacksDetailsStore.Intent.Close) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )

            // ── Содержимое (заглушка) ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Pack ID: ${state.packId ?: "—"}",
                        color = TextSecondary,
                        fontSize = 16.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.packs_details_coming_soon),
                        color = TextSecondary.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}
