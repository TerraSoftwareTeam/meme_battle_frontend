package com.dev.memebattle.feature.packs.impl.presentation.view.create.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.memebattle.feature.packs.impl.presentation.store.create.PacksCreateStore
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.packs_type_memes
import com.dev.memebattle.core.localization.packs_type_situations

@Composable
internal fun PackTypeSelector(
    selectedType: PacksCreateStore.PackType,
    onTypeSelected: (PacksCreateStore.PackType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(SurfaceColor, RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        val types = PacksCreateStore.PackType.entries
        types.forEach { type ->
            val isSelected = selectedType == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) AccentColor else Color.Transparent)
                    .clickable { onTypeSelected(type) },
                contentAlignment = Alignment.Center
            ) {
                val textRes = if (type == PacksCreateStore.PackType.Memes) Res.string.packs_type_memes else Res.string.packs_type_situations
                Text(
                    text = stringResource(textRes),
                    color = if (isSelected) Color.White else TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
