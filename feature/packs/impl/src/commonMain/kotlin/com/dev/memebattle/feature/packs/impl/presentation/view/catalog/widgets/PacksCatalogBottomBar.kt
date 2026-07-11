package com.dev.memebattle.feature.packs.impl.presentation.view.catalog.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.feature.packs.impl.presentation.store.catalog.PacksCatalogStore
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.packs_create
import com.dev.memebattle.core.localization.packs_type_memes
import com.dev.memebattle.core.localization.packs_type_situations

@Composable
internal fun PacksCatalogBottomBar(
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
