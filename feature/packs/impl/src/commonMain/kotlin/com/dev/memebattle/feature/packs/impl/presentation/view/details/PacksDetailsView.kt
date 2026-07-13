package com.dev.memebattle.feature.packs.impl.presentation.view.details

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.dev.memebattle.core.domain.packs.model.MemeCard
import com.dev.memebattle.core.domain.packs.model.MemePack
import com.dev.memebattle.core.domain.packs.model.SafetyLevel
import com.dev.memebattle.core.domain.packs.model.SituationCard
import com.dev.memebattle.core.domain.packs.model.SituationPack
import com.dev.memebattle.feature.packs.impl.presentation.component.details.PacksDetailsComponent
import com.dev.memebattle.feature.packs.impl.presentation.store.details.PacksDetailsStore
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.*

private fun formatDate(raw: String): String = try {
    val d = raw.split("T").first().split("-")
    "${d[2]}.${d[1]}.${d[0]}"
} catch (_: Exception) { raw }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacksDetailsView(component: PacksDetailsComponent, modifier: Modifier = Modifier) {
    val state by component.state.collectAsState()
    LaunchedEffect(component) {
        component.effects.collectLatest { effect ->
            when (effect) {
                is PacksDetailsStore.Effect.NavigateBack -> component.onIntent(PacksDetailsStore.Intent.Close)
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A1035), Color(0xFF110D28), Color(0xFF08040F))))
    ) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    val name = when (state.kind) {
                        PacksDetailsStore.PackKind.Meme -> state.memePack?.name
                        PacksDetailsStore.PackKind.Situation -> state.situationPack?.name
                    }
                    Text(name?.ifBlank { null } ?: stringResource(Res.string.packs_details_title),
                        color = DeckTextPri, fontWeight = FontWeight.SemiBold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = { component.onIntent(PacksDetailsStore.Intent.Close) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DeckTextPri)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = DeckAccent)
                }
                state.kind == PacksDetailsStore.PackKind.Meme && state.memePack != null ->
                    MemePackScreen(state.memePack!!, state.memeCards)
                state.kind == PacksDetailsStore.PackKind.Situation && state.situationPack != null ->
                    SituationPackScreen(state.situationPack!!, state.situationCards)
                state.error != null ->
                    Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
                        Text(state.error!!, color = Color(0xFFE57373), textAlign = TextAlign.Center)
                    }
                else -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = DeckAccent)
                }
            }
        }
    }
}

// ── Meme Pack Screen ──────────────────────────────────────────────────────────
@Composable
private fun MemePackScreen(pack: MemePack, cards: List<MemeCard>) {
    var selectedIdx by remember { mutableStateOf(0) }
    val safeIdx = selectedIdx.coerceIn(0, (cards.size - 1).coerceAtLeast(0))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Large preview — fixed height, centred, standard 3:4 aspect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(3f / 4f)
            ) {
                LargeCardPreview(url = cards.getOrNull(safeIdx)?.mediaUrl, idx = safeIdx)
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
                Text("Карточки", color = DeckTextSec, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("${cards.size}", color = DeckTextSec.copy(0.5f), fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            // Fixed height — cards won't overflow
            CardDeckSelector(
                totalCount = cards.size,
                selectedIdx = safeIdx,
                onSelect = { selectedIdx = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = 8.dp),
            ) { idx, isSelected ->
                MemeCardFace(url = cards[idx].mediaUrl, isSelected = isSelected)
            }
        } else {
            Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                Text(stringResource(Res.string.packs_details_empty_cards), color = DeckTextSec.copy(0.6f))
            }
        }
    }
}

// ── Situation Pack Screen ─────────────────────────────────────────────────────
@Composable
private fun SituationPackScreen(pack: SituationPack, cards: List<SituationCard>) {
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
                .height(280.dp)
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
                Text("Карточки", color = DeckTextSec, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("${cards.size}", color = DeckTextSec.copy(0.5f), fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            CardDeckSelector(
                totalCount = cards.size,
                selectedIdx = safeIdx,
                onSelect = { selectedIdx = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = 8.dp),
            ) { idx, isSelected ->
                SituationCardFace(
                    text = cards[idx].promptText,
                    accent = SituationAccents[idx % SituationAccents.size],
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

// ── Large Previews ────────────────────────────────────────────────────────────
@Composable
private fun LargeCardPreview(url: String?, idx: Int) {
    val scale by animateFloatAsState(1f, spring(0.65f, 350f), label = "ps")
    Card(
        modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, DeckBorder),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        elevation = CardDefaults.cardElevation(12.dp),
    ) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            if (!url.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = DeckAccent,
                                strokeWidth = 3.dp
                            )
                        }
                    },
                    error = {
                        CardBack(modifier = Modifier.fillMaxSize())
                    }
                )
            }
        }
    }
}

@Composable
private fun LargeSituationPreview(text: String, idx: Int) {
    val accent = SituationAccents[idx % SituationAccents.size]
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B1E)),
        elevation = CardDefaults.cardElevation(12.dp),
    ) {
        Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
            Box(Modifier.size(120.dp).background(Brush.radialGradient(listOf(accent.copy(0.12f), Color.Transparent)), CircleShape))
            Text(text.ifBlank { "—" }, color = DeckTextPri, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 26.sp, textAlign = TextAlign.Center)
        }
    }
}

// ── Card Faces (shown inside PeekingCard) ─────────────────────────────────────
@Composable
internal fun MemeCardFace(url: String, isSelected: Boolean) {
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        if (url.isNotBlank()) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = DeckAccent,
                            strokeWidth = 2.dp
                        )
                    }
                },
                error = {
                    CardBack(modifier = Modifier.fillMaxSize())
                }
            )
        }
    }
}

@Composable
internal fun SituationCardFace(text: String, accent: Color, isSelected: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F0B1E)),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(60.dp).background(Brush.radialGradient(listOf(accent.copy(0.18f), Color.Transparent)), CircleShape))
        Text(text, color = DeckTextPri, fontSize = 10.sp, lineHeight = 14.sp, textAlign = TextAlign.Center,
            maxLines = 8, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(8.dp))
    }
}

// ── Info Row ──────────────────────────────────────────────────────────────────
@Composable
private fun PackInfoRow(
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
