package com.dev.memebattle.feature.packs.impl.presentation.view.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.feature.packs.impl.presentation.component.edit.PacksEditComponent
import com.dev.memebattle.feature.packs.impl.presentation.store.edit.PacksEditStore
import com.dev.memebattle.feature.packs.impl.presentation.view.create.widgets.*
import com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets.MemeCardFace
import com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets.SituationCardFace
import com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets.LargeCardPreview
import com.dev.memebattle.feature.packs.impl.presentation.view.details.widgets.LargeSituationPreview
import com.dev.memebattle.feature.packs.impl.presentation.view.shared.CardDeckSelector
import com.dev.memebattle.feature.packs.impl.presentation.view.details.SituationAccents
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacksEditView(
    component: PacksEditComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    var currentPromptInput by remember { mutableStateOf("") }
    var deckSelectedIdx by remember { mutableStateOf(0) }

    val filePicker = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Multiple(),
        title = "Select Images"
    ) { files ->
        if (!files.isNullOrEmpty()) {
            val updated = state.selectedFiles + files
            component.onIntent(PacksEditStore.Intent.UpdateSelectedFiles(updated))
            deckSelectedIdx = state.memeCards.size + updated.size - 1
        }
    }

    LaunchedEffect(component) {
        component.effects.collectLatest { effect ->
            when (effect) {
                is PacksEditStore.Effect.NavigateBack -> component.onIntent(PacksEditStore.Intent.Close)
                is PacksEditStore.Effect.Saved -> {
                    component.navigateToDetails(effect.packId, effect.kind)
                }
                is PacksEditStore.Effect.Deleted -> {
                    component.onIntent(PacksEditStore.Intent.Close)
                }
                is PacksEditStore.Effect.ShowNotification -> {
                    component.showNotification(message = effect.message, isError = effect.isError)
                }
            }
        }
    }


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = "Редактирование пэка",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { component.onIntent(PacksEditStore.Intent.Close) }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )

            if (state.isLoading && state.name.isBlank()) {
                Box(Modifier.fillMaxSize().weight(1f), Alignment.Center) {
                    CircularProgressIndicator(color = AccentColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Deck Selector Section
                    val totalCards = if (state.kind == "meme") {
                        state.memeCards.size + state.selectedFiles.size
                    } else {
                        state.situationCards.size + state.promptsToAdd.size
                    }

                    if (totalCards > 0) {
                        item {
                            val safeIdx = deckSelectedIdx.coerceIn(0, (totalCards - 1).coerceAtLeast(0))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(3f / 4f)
                                ) {
                                    if (state.kind == "meme") {
                                        if (safeIdx < state.memeCards.size) {
                                            LargeCardPreview(model = state.memeCards[safeIdx].mediaUrl, idx = safeIdx)
                                        } else {
                                            val file = state.selectedFiles[safeIdx - state.memeCards.size]
                                            var bytes by remember(file) { mutableStateOf<ByteArray?>(null) }
                                            LaunchedEffect(file) { bytes = file.readBytes() }
                                            LargeCardPreview(model = bytes, idx = safeIdx)
                                        }
                                    } else {
                                        if (safeIdx < state.situationCards.size) {
                                            LargeSituationPreview(text = state.situationCards[safeIdx].promptText, idx = safeIdx)
                                        } else {
                                            val prompt = state.promptsToAdd[safeIdx - state.situationCards.size]
                                            LargeSituationPreview(text = prompt, idx = safeIdx)
                                        }
                                    }
                                }
                            }

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Карточки", color = TextSecondary, fontSize = 14.sp)
                                    Button(
                                        onClick = {
                                            if (state.kind == "meme") {
                                                if (safeIdx < state.memeCards.size) {
                                                    component.onIntent(PacksEditStore.Intent.DeleteMemeCard(state.memeCards[safeIdx].id))
                                                } else {
                                                    val file = state.selectedFiles[safeIdx - state.memeCards.size]
                                                    component.onIntent(PacksEditStore.Intent.UpdateSelectedFiles(state.selectedFiles - file))
                                                }
                                            } else {
                                                if (safeIdx < state.situationCards.size) {
                                                    component.onIntent(PacksEditStore.Intent.DeleteSituationCard(state.situationCards[safeIdx].id))
                                                } else {
                                                    component.onIntent(PacksEditStore.Intent.RemovePrompt(safeIdx - state.situationCards.size))
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ErrorColor.copy(alpha = 0.2f), contentColor = ErrorColor),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Удалить выбранную", fontSize = 12.sp)
                                    }
                                }

                                CardDeckSelector(
                                    totalCount = totalCards,
                                    selectedIdx = safeIdx,
                                    onSelect = { deckSelectedIdx = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .padding(horizontal = 8.dp),
                                ) { idx, isSelected ->
                                    if (state.kind == "meme") {
                                        if (idx < state.memeCards.size) {
                                            MemeCardFace(model = state.memeCards[idx].mediaUrl, isSelected = isSelected)
                                        } else {
                                            val file = state.selectedFiles[idx - state.memeCards.size]
                                            var bytes by remember(file) { mutableStateOf<ByteArray?>(null) }
                                            LaunchedEffect(file) { bytes = file.readBytes() }
                                            MemeCardFace(model = bytes, isSelected = isSelected)
                                        }
                                    } else {
                                        if (idx < state.situationCards.size) {
                                            SituationCardFace(
                                                text = state.situationCards[idx].promptText,
                                                accent = SituationAccents[idx % SituationAccents.size],
                                                isSelected = isSelected,
                                            )
                                        } else {
                                            val prompt = state.promptsToAdd[idx - state.situationCards.size]
                                            SituationCardFace(
                                                text = prompt,
                                                accent = SituationAccents[idx % SituationAccents.size],
                                                isSelected = isSelected,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }

                    // Form Fields Section
                    item {
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { component.onIntent(PacksEditStore.Intent.UpdateName(it)) },
                            label = { Text(stringResource(Res.string.packs_create_name_label), color = TextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = outlinedTextFieldColors()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = state.description,
                            onValueChange = { component.onIntent(PacksEditStore.Intent.UpdateDescription(it)) },
                            label = { Text(stringResource(Res.string.packs_create_description_label), color = TextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = outlinedTextFieldColors()
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(Res.string.packs_create_public_pack), color = TextPrimary)
                            Switch(
                                checked = state.isPublic,
                                onCheckedChange = { component.onIntent(PacksEditStore.Intent.UpdateIsPublic(it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = AccentColor, checkedTrackColor = AccentColor.copy(alpha = 0.5f))
                            )
                        }
                    }

                    item {
                        SafetyLevelSelector(
                            selectedLevel = state.safetyLevel,
                            onLevelSelected = { component.onIntent(PacksEditStore.Intent.UpdateSafetyLevel(it)) }
                        )
                    }

                    item {
                        LanguageSelector(
                            selectedLanguage = state.languageCode,
                            onLanguageSelected = { component.onIntent(PacksEditStore.Intent.UpdateLanguage(it)) }
                        )
                    }

                    // Add new items section
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Добавить новые", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (state.kind == "meme") {
                        createMemesSection(
                            selectedFiles = emptyList(),
                            onLaunchPicker = { filePicker.launch() },
                            onRemoveFile = { }
                        )
                    } else {
                        createSituationsSection(
                            prompts = emptyList(),
                            currentPromptInput = currentPromptInput,
                            onPromptInputChange = { currentPromptInput = it },
                            onAddPrompt = {
                                component.onIntent(PacksEditStore.Intent.AddPrompt(it))
                                deckSelectedIdx = state.situationCards.size + state.promptsToAdd.size
                                currentPromptInput = ""
                            },
                            onRemovePrompt = { }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }

                    item {
                        Button(
                            onClick = { component.onIntent(PacksEditStore.Intent.DeletePack) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ErrorColor.copy(alpha = 0.1f), 
                                contentColor = ErrorColor
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Удалить пэк", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = { component.onIntent(PacksEditStore.Intent.Save) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = state.isSaveEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        disabledContainerColor = SurfaceColor
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Сохранить",
                            color = if (state.isSaveEnabled) Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
