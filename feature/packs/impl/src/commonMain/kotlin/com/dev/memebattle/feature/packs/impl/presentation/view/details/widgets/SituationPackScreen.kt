package com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.core.domain.packs.model.SituationCard
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.packs_details_cards
import com.dev.memebattle.core.localization.packs_details_empty_cards
import com.dev.memebattle.feature.packs.impl.presentation.view.shared.StaticAdaptiveCardGrid
import com.dev.memebattle.feature.packs.impl.presentation.view.details.DeckTextSec
import com.dev.memebattle.feature.packs.impl.presentation.view.details.SituationAccents
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SituationPackScreen(pack: SituationPack, cards: List<SituationCard>) {
    var selectedIdx by remember { mutableStateOf(0) }
    val safeIdx = selectedIdx.coerceIn(0, (cards.size - 1).coerceAtLeast(0))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(3f / 4f)
            ) {
                LargeSituationPreview(text = cards.getOrNull(safeIdx)?.promptText ?: "", idx = safeIdx)
            }
        }

        PackInfoRow(pack.name, pack.description, pack.safetyLevel, pack.isPublic, pack.languageCode, pack.createdAt)

        if (cards.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(Res.string.packs_details_cards), color = DeckTextSec, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("${cards.size}", color = DeckTextSec.copy(0.5f), fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))

            StaticAdaptiveCardGrid(
                cards = cards,
                selectedIdx = safeIdx,
                onSelect = { selectedIdx = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) { card, isSelected ->
                val cardIdx = cards.indexOf(card).let { if (it < 0) 0 else it }
                SituationCardFace(
                    text = card.promptText,
                    accent = SituationAccents[cardIdx % SituationAccents.size],
                    isSelected = isSelected,
                )
            }
        } else {
            Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                Text(stringResource(Res.string.packs_details_empty_cards), color = DeckTextSec.copy(0.6f))
            }
        }
    }
}
