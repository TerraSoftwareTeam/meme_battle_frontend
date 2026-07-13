package com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.core.domain.packs.model.SafetyLevel
import com.dev.memebattle.feature.packs.impl.presentation.view.details.DeckTextPri
import com.dev.memebattle.feature.packs.impl.presentation.view.details.DeckTextSec
import com.dev.memebattle.feature.packs.impl.presentation.view.details.formatDate

@Composable
internal fun PackInfoRow(
    name: String, description: String?,
    safetyLevel: SafetyLevel, isPublic: Boolean,
    languageCode: String, createdAt: String,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(name.ifBlank { "—" }, color = DeckTextPri, fontWeight = FontWeight.Bold, fontSize = 19.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (!description.isNullOrBlank()) {
            Text(description, color = DeckTextSec, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            val (sl, sb, sf) = when (safetyLevel) {
                SafetyLevel.FAMILY_FRIENDLY -> Triple("0+",  Color(0xFF43A047), Color.White)
                SafetyLevel.SPICY           -> Triple("16+", Color(0xFFFFD54F), Color(0xFF3E2723))
                SafetyLevel.EXPLICIT        -> Triple("18+", Color(0xFFE53935), Color.White)
            }
            Box(Modifier.clip(RoundedCornerShape(6.dp)).background(sb).padding(horizontal = 7.dp, vertical = 3.dp)) {
                Text(sl, color = sf, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Box(Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFF2E2452)).padding(horizontal = 7.dp, vertical = 3.dp)) {
                Text(languageCode.uppercase(), color = DeckTextSec, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.weight(1f))
            Text(formatDate(createdAt), color = DeckTextSec.copy(0.6f), fontSize = 11.sp)
        }
    }
}
