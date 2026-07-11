package com.dev.memebattle.feature.packs.impl.presentation.view.catalog.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.dev.memebattle.feature.packs.impl.presentation.store.catalog.PacksCatalogStore
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.packs_no_memes
import com.dev.memebattle.core.localization.packs_no_situations

@Composable
internal fun EmptyPacksPlaceholder(
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
