package com.dev.memebattle.feature.packs.impl.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.dev.memebattle.feature.packs.impl.presentation.component.PacksComponent
import com.dev.memebattle.feature.packs.impl.presentation.component.PacksComponentImpl
import com.dev.memebattle.feature.packs.impl.presentation.view.catalog.PacksCatalogView
import com.dev.memebattle.feature.packs.impl.presentation.view.create.PacksCreateView
import com.dev.memebattle.feature.packs.impl.presentation.view.details.PacksDetailsView
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.*

@OptIn(com.arkivanov.decompose.ExperimentalDecomposeApi::class)
@Composable
fun PacksView(component: PacksComponent) {
    val panels by component.panels.subscribeAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLargeScreen = maxWidth >= 1200.dp
        val isMediumScreen = maxWidth >= 800.dp && maxWidth < 1200.dp
        val isMultiPane = isLargeScreen || isMediumScreen
        
        LaunchedEffect(isMultiPane) {
            val mode = if (isMultiPane) ChildPanelsMode.DUAL else ChildPanelsMode.SINGLE
            component.setAdaptiveMode(mode)
        }

        if (isMultiPane) {
            // ══════════════════════════════════════════════════════════════════════
            // DUAL / TRIPLE-PANE: каталог слева, детали/создание справа
            // ══════════════════════════════════════════════════════════════════════
            Row(modifier = Modifier.fillMaxSize()) {
                val catalogWeight = if (isLargeScreen) 0.75f else 0.5f
                val extraWeight = 1f - catalogWeight

                // ── Левая панель: каталог (всегда видим) ─────────────────────────
                PacksCatalogView(
                    component = panels.main.instance,
                    modifier = Modifier
                        .weight(catalogWeight)
                        .fillMaxHeight(),
                )

                // ── Вертикальный разделитель ──────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.08f))
                )

                // ── Правая панель: Extra (Create) → Details → пустой placeholder ─
                Box(modifier = Modifier.weight(extraWeight).fillMaxHeight()) {
                    when {
                        panels.extra != null -> {
                            PacksCreateView(
                                component = panels.extra!!.instance,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        panels.details != null -> {
                            PacksDetailsView(
                                component = panels.details!!.instance,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        else -> {
                            RightPanelPlaceholder(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        } else {
            // ══════════════════════════════════════════════════════════════════════
            // SINGLE-PANE: один экран за раз
            // ══════════════════════════════════════════════════════════════════════
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    panels.extra != null -> {
                        PacksCreateView(
                            component = panels.extra!!.instance,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    panels.details != null -> {
                        PacksDetailsView(
                            component = panels.details!!.instance,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    else -> {
                        PacksCatalogView(
                            component = panels.main.instance,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

// ─── Placeholder для правой панели когда нет активного контента ───────────────

@Composable
private fun RightPanelPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFF0F0820))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            androidx.compose.material3.Text(
                text = stringResource(Res.string.packs_select_pack),
                color = Color(0xFFB0A2C7),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
