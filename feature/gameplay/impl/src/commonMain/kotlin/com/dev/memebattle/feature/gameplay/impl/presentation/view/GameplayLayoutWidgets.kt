package com.dev.memebattle.feature.gameplay.impl.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person

enum class SideDrawerTab(val label: String) {
    PLAYERS("Игроки"),
    INFO("Инфо"),
}

/**
 * Кнопка-шторка для medium-режима — вверху по центру GameScreen.
 */
@Composable
fun GameplayPanelToggleButton(
    isOpen: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = if (isOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isOpen) "Скрыть панели" else "Показать панели",
            )
        }
    }
}

/**
 * Кнопки верхнего бара для small-режима (открыть игроков / инфо).
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onOpenPlayers) {
            Icon(Icons.Default.Person, contentDescription = "Игроки")
        }
        IconButton(onClick = onOpenInfo) {
            Icon(Icons.Default.Info, contentDescription = "Инфо")
        }
    }
}

/**
 * Боковая панель для small-режима — выезжает справа, содержит TabRow для переключения.
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
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // Заголовок с табами и кнопкой закрытия
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TabRow(
                selectedTabIndex = SideDrawerTab.entries.indexOf(activeTab),
                modifier = Modifier.weight(1f),
            ) {
                SideDrawerTab.entries.forEach { tab ->
                    Tab(
                        selected = activeTab == tab,
                        onClick = { onTabChange(tab) },
                        text = { Text(tab.label) },
                    )
                }
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Контент активного таба
        when (activeTab) {
            SideDrawerTab.PLAYERS -> playersContent()
            SideDrawerTab.INFO -> infoContent()
        }
    }
}
