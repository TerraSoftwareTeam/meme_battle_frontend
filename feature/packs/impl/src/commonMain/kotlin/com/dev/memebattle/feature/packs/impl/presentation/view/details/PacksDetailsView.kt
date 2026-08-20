package com.dev.memebattle.feature.packs.impl.presentation.view.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import com.dev.memebattle.core.ui.share.rememberLinkSharer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dev.memebattle.feature.packs.impl.presentation.component.details.PacksDetailsComponent
import com.dev.memebattle.feature.packs.impl.presentation.store.details.PacksDetailsStore
import com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets.MemePackScreen
import com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets.SituationPackScreen
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.packs_details_title

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
                actions = {
                    val shareLink = rememberLinkSharer()
                    val packId = state.packId
                    val kindStr = if (state.kind == PacksDetailsStore.PackKind.Situation) "situation" else "meme"
                    IconButton(
                        onClick = {
                            val link = "https://play.meme.skyfly.hackclub.app/pack/$packId?kind=$kindStr"
                            shareLink(link, "Пачка карт MemeBattle")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Поделиться",
                            tint = DeckTextPri
                        )
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
