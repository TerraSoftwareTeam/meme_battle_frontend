package com.dev.memebattle.feature.packs.impl.presentation.view.create

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
import com.dev.memebattle.feature.packs.impl.presentation.component.create.PacksCreateComponent
import com.dev.memebattle.feature.packs.impl.presentation.store.create.PacksCreateStore
import com.dev.memebattle.feature.packs.impl.presentation.view.create.widgets.*
import com.dev.memebattle.feature.packs.impl.presentation.view.shared.UploadProgressCard
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.getString
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacksCreateView(
    component: PacksCreateComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    var currentPromptInput by remember { mutableStateOf("") }

    val filePicker = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Multiple(),
        title = "Select Images"
    ) { files ->
        if (files != null) {
            component.onIntent(PacksCreateStore.Intent.UpdateSelectedFiles(state.selectedFiles + files))
        }
    }

    LaunchedEffect(component) {
        component.effects.collectLatest { effect ->
            when (effect) {
                is PacksCreateStore.Effect.NavigateBack -> component.onIntent(PacksCreateStore.Intent.Close)
                is PacksCreateStore.Effect.Created -> {
                    component.showNotification(message = getString(Res.string.packs_create_success))
                    component.onCreated(effect.packId, effect.kind)
                }
                is PacksCreateStore.Effect.ShowError -> {
                    component.showNotification(message = effect.message, isError = true)
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
                        text = stringResource(Res.string.packs_create_title),
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { component.onIntent(PacksCreateStore.Intent.Close) }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PackTypeSelector(
                        selectedType = state.type,
                        onTypeSelected = { component.onIntent(PacksCreateStore.Intent.UpdateType(it)) }
                    )
                }

                item {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { component.onIntent(PacksCreateStore.Intent.UpdateName(it)) },
                        label = { Text(stringResource(Res.string.packs_create_name_label), color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = outlinedTextFieldColors()
                    )
                }

                item {
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { component.onIntent(PacksCreateStore.Intent.UpdateDescription(it)) },
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
                            onCheckedChange = { component.onIntent(PacksCreateStore.Intent.UpdateIsPublic(it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentColor, checkedTrackColor = AccentColor.copy(alpha = 0.5f))
                        )
                    }
                }

                item {
                    SafetyLevelSelector(
                        selectedLevel = state.safetyLevel,
                        onLevelSelected = { component.onIntent(PacksCreateStore.Intent.UpdateSafetyLevel(it)) }
                    )
                }

                item {
                    LanguageSelector(
                        selectedLanguage = state.languageCode,
                        onLanguageSelected = { component.onIntent(PacksCreateStore.Intent.UpdateLanguage(it)) }
                    )
                }

                if (state.type == PacksCreateStore.PackType.Memes) {
                    createMemesSection(
                        selectedFiles = state.selectedFiles,
                        onLaunchPicker = { filePicker.launch() },
                        onRemoveFile = { file ->
                            component.onIntent(PacksCreateStore.Intent.UpdateSelectedFiles(state.selectedFiles - file))
                        }
                    )
                } else {
                    createSituationsSection(
                        prompts = state.prompts,
                        currentPromptInput = currentPromptInput,
                        onPromptInputChange = { currentPromptInput = it },
                        onAddPrompt = {
                            component.onIntent(PacksCreateStore.Intent.AddPrompt(it))
                            currentPromptInput = ""
                        },
                        onRemovePrompt = {
                            component.onIntent(PacksCreateStore.Intent.RemovePrompt(it))
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                if (state.uploadProgress != null) {
                    UploadProgressCard(progressState = state.uploadProgress)
                } else {
                    Button(
                        onClick = { component.onIntent(PacksCreateStore.Intent.Create) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = state.isCreateEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentColor,
                            disabledContainerColor = SurfaceColor
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = stringResource(Res.string.packs_create_submit),
                                color = if (state.isCreateEnabled) Color.White else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
