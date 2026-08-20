package com.dev.memebattle.feature.home.impl.presentation.view.packpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.pack_picker_confirm
import com.dev.memebattle.core.localization.pack_picker_confirm_empty
import com.dev.memebattle.core.localization.pack_picker_tab_memes
import com.dev.memebattle.core.localization.pack_picker_tab_situations
import com.dev.memebattle.core.localization.pack_picker_title
import com.dev.memebattle.feature.home.impl.presentation.component.packpicker.PackPickerComponent
import com.dev.memebattle.feature.home.impl.presentation.store.packpicker.PackPickerStore.PackPickerTab
import org.jetbrains.compose.resources.stringResource

private val AccentColor = Color(0xFF7C5DFA)
private val SurfaceColor = Color(0xFF2A1F44)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFB0A2C7)

@Composable
fun PackPickerView(
    component: PackPickerComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { component.onClose() }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = TextPrimary
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(Res.string.pack_picker_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf(
                    PackPickerTab.MEMES to stringResource(Res.string.pack_picker_tab_memes),
                    PackPickerTab.SITUATIONS to stringResource(Res.string.pack_picker_tab_situations)
                )
                tabs.forEach { (tab, label) ->
                    val isSelected = state.activeTab == tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { component.onSelectTab(tab) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) AccentColor else SurfaceColor
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 10.dp),
                            alignment = Alignment.Center
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AccentColor
                    )
                } else {
                    when (state.activeTab) {
                        PackPickerTab.MEMES -> MemePackGrid(
                            packs = state.memePacks,
                            selectedIds = state.selectedMemePackIds,
                            onToggle = { component.onTogglePack(it) },
                        )
                        PackPickerTab.SITUATIONS -> SituationPackGrid(
                            packs = state.situationPacks,
                            selectedIds = state.selectedSituationPackIds,
                            onToggle = { component.onTogglePack(it) },
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                val count = state.totalSelectedCount
                Button(
                    onClick = { if (count > 0) component.onConfirm() },
                    enabled = count > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        disabledContainerColor = SurfaceColor
                    )
                ) {
                    if (count > 0) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.pack_picker_confirm),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.pack_picker_confirm_empty),
                            fontSize = 15.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
