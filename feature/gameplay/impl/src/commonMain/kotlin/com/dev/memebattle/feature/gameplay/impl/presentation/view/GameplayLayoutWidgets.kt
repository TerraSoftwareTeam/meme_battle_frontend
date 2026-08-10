package com.dev.memebattle.feature.gameplay.impl.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SideDrawerTab(val label: String) {
    PLAYERS("Игроки"),
    INFO("Инфо"),
}

// ── Кнопка переключения панелей (Medium layout) ──────────────────────────────

/**
 * Компактная кнопка в верхней части GameScreen для Medium-режима.
 * Показывает/прячет боковые панели.
 */
@Composable
fun GameplayPanelToggleButton(
    isOpen: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .clickable(onClick = onToggle)
                .padding(horizontal = 14.dp, vertical = 7.dp),
        ) {
            Text(
                text = if (isOpen) "Свернуть" else "Панели",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

// ── Топ-бар для Small layout ──────────────────────────────────────────────────

/**
 * Две компактные кнопки «Игроки» / «Инфо» поверх GameScreen.
 * Расположены по краям экрана — не перекрывают центральный контент.
 */
@Composable
fun GameplaySmallTopBar(
    onOpenPlayers: () -> Unit,
    onOpenInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmallTabButton(label = "Игроки", onClick = onOpenPlayers)
        SmallTabButton(label = "Инфо", onClick = onOpenInfo)
    }
}

@Composable
private fun SmallTabButton(
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1A0F38).copy(alpha = 0.75f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.9f),
        )
    }
}

// ── Боковой drawer для Small layout ──────────────────────────────────────────

/**
 * Полноширинная боковая панель (Small-режим).
 * Содержит TabRow для переключения между Игроки / Инфо.
 */
@Composable
fun GameplaySideDrawer(
    activeTab: SideDrawerTab,
    onTabChange: (SideDrawerTab) -> Unit,
    onClose: () -> Unit,
    playersContent: @Composable () -> Unit,
    infoContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A0F38), Color(0xFF100820), Color(0xFF080412))
                )
            ),
    ) {
        // ── Шапка с табами ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Tab Pills
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SideDrawerTab.entries.forEach { tab ->
                    val selected = tab == activeTab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (selected) Color(0xFF7C5DFA)
                                else Color.White.copy(alpha = 0.08f)
                            )
                            .clickable { onTabChange(tab) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tab.label,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
                        )
                    }
                }
            }

            // Кнопка закрытия
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable(onClick = onClose)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "✕",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        }

        // ── Контент ───────────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (activeTab) {
                SideDrawerTab.PLAYERS -> playersContent()
                SideDrawerTab.INFO -> infoContent()
            }
        }
    }
}
