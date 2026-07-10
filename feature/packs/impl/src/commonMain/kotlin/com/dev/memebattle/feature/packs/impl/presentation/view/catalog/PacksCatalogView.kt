package com.dev.memebattle.feature.packs.impl.presentation.view.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.feature.packs.impl.presentation.component.catalog.PacksCatalogComponent
import com.dev.memebattle.feature.packs.impl.presentation.store.catalog.PacksCatalogStore
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.*

// ─── Цветовая схема каталога ────────────────────────────────────────────────

private val BackgroundTop = Color(0xFF1A1035)
private val BackgroundBottom = Color(0xFF08040F)
private val AccentPrimary = Color(0xFF7C5DFA)
private val AccentSecondary = Color(0xFF6C47FF)
private val CardBackground = Color(0xFF211640)
private val CardBorder = Color(0xFF3A2860)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFB0A2C7)

@Composable
fun PacksCatalogView(
    component: PacksCatalogComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()

    LaunchedEffect(component) {
        component.effects.collectLatest { effect ->
            // Навигационные эффекты уже обработаны в PacksCatalogComponentImpl через колбэки
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
            // ── Top Bar ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { component.onIntent(PacksCatalogStore.Intent.GoBack) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
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

            // ── Список паков ─────────────────────────────────────────────
            PacksList(
                state = state,
                onPackClick = { packId ->
                    component.onIntent(PacksCatalogStore.Intent.OpenDetails(packId))
                },
                modifier = Modifier.weight(1f),
            )

            // ── Нижний бар с действиями ───────────────────────────────────
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

        // ── Индикатор загрузки ────────────────────────────────────────────
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

// ─── Список карточек паков ───────────────────────────────────────────────────

private data class PackUiModel(val id: String, val name: String)

@Composable
private fun PacksList(
    state: PacksCatalogStore.State,
    onPackClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiPacks = when (state.activeType) {
        PacksCatalogStore.PackType.Memes -> state.memePacks.map { PackUiModel(it.id, it.name) }
        PacksCatalogStore.PackType.Situations -> state.situationPacks.map { PackUiModel(it.id, it.name) }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 8.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (uiPacks.isEmpty() && !state.isLoading) {
            item {
                EmptyPacksPlaceholder(
                    type = state.activeType,
                    modifier = Modifier.fillParentMaxSize(),
                )
            }
        } else {
            itemsIndexed(uiPacks, key = { _, pack -> pack.id }) { _, pack ->
                PackCard(
                    name = pack.name,
                    cardCount = 0, // Placeholder until domain model supports counting
                    onClick = { onPackClick(pack.id) },
                )
            }
        }
    }
}

// ─── Карточка пака ───────────────────────────────────────────────────────────

@Composable
private fun PackCard(
    name: String,
    cardCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── Иконка-заглушка пака ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentSecondary.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = name.take(2).uppercase(),
                    color = AccentPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.packs_cards_count, cardCount),
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

// ─── Заглушка пустого списка ─────────────────────────────────────────────────

@Composable
private fun EmptyPacksPlaceholder(
    type: PacksCatalogStore.PackType,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when (type) {
                    PacksCatalogStore.PackType.Memes -> stringResource(Res.string.packs_no_memes)
                    PacksCatalogStore.PackType.Situations -> stringResource(Res.string.packs_no_situations)
                },
                color = TextSecondary,
                fontSize = 16.sp,
            )
        }
    }
}

// ─── Нижний бар с действиями ─────────────────────────────────────────────────

@Composable
private fun PacksCatalogBottomBar(
    activeType: PacksCatalogStore.PackType,
    onSwitchType: (PacksCatalogStore.PackType) -> Unit,
    onCreateClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Выбор типа пака (Табы) ──────────────────────────────────
        Row(
            modifier = Modifier
                .weight(1f)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PackTypeTab(
                text = stringResource(Res.string.packs_type_memes),
                isSelected = activeType == PacksCatalogStore.PackType.Memes,
                onClick = { onSwitchType(PacksCatalogStore.PackType.Memes) },
                modifier = Modifier.weight(1f)
            )
            PackTypeTab(
                text = stringResource(Res.string.packs_type_situations),
                isSelected = activeType == PacksCatalogStore.PackType.Situations,
                onClick = { onSwitchType(PacksCatalogStore.PackType.Situations) },
                modifier = Modifier.weight(1f)
            )
        }

        // ── Кнопка создания пака ──────────────────────────────────────
        Button(
            onClick = onCreateClick,
            modifier = Modifier.height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentPrimary,
                contentColor = Color.White,
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 10.dp,
            ),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(Res.string.packs_create),
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.packs_create),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun PackTypeTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) AccentPrimary else Color.Transparent
    val textColor = if (isSelected) Color.White else TextSecondary

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}
