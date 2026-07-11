package com.dev.memebattle.feature.packs.impl.presentation.view.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.memebattle.feature.packs.impl.presentation.component.create.PacksCreateComponent
import com.dev.memebattle.feature.packs.impl.presentation.store.create.PacksCreateStore
import com.dev.memebattle.feature.packs.impl.presentation.view.create.widgets.PackTypeSelector
import com.dev.memebattle.feature.packs.impl.presentation.view.create.widgets.SafetyLevelSelector
import com.dev.memebattle.feature.packs.impl.presentation.view.create.widgets.FileImageCard
import com.dev.memebattle.feature.packs.impl.presentation.view.create.widgets.BackgroundTop
import com.dev.memebattle.feature.packs.impl.presentation.view.create.widgets.BackgroundBottom
import com.dev.memebattle.feature.packs.impl.presentation.view.create.widgets.TextPrimary
import com.dev.memebattle.feature.packs.impl.presentation.view.create.widgets.TextSecondary
import com.dev.memebattle.feature.packs.impl.presentation.view.create.widgets.AccentColor
import com.dev.memebattle.feature.packs.impl.presentation.view.create.widgets.ErrorColor
import com.dev.memebattle.feature.packs.impl.presentation.view.create.widgets.SurfaceColor
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.getString
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                    component.onCreated(effect.packId)
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

                if (state.type == PacksCreateStore.PackType.Memes) {
                    item {
                        Button(
                            onClick = { filePicker.launch() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                stringResource(Res.string.packs_create_select_images),
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (state.selectedFiles.isNotEmpty()) {
                        item {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.selectedFiles.forEach { file ->
                                    FileImageCard(
                                        file = file,
                                        onRemove = {
                                            component.onIntent(PacksCreateStore.Intent.UpdateSelectedFiles(state.selectedFiles - file))
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = currentPromptInput,
                                onValueChange = { currentPromptInput = it },
                                label = { Text(stringResource(Res.string.packs_create_situation_prompt), color = TextSecondary) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (currentPromptInput.isNotBlank()) {
                                        component.onIntent(PacksCreateStore.Intent.AddPrompt(currentPromptInput))
                                        currentPromptInput = ""
                                    }
                                }),
                                colors = outlinedTextFieldColors()
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (currentPromptInput.isNotBlank()) {
                                        component.onIntent(PacksCreateStore.Intent.AddPrompt(currentPromptInput))
                                        currentPromptInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(AccentColor, RoundedCornerShape(12.dp))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                            }
                        }
                    }

                    itemsIndexed(state.prompts) { index, prompt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(prompt, color = TextPrimary, modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { component.onIntent(PacksCreateStore.Intent.RemovePrompt(index)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = ErrorColor)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
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

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedBorderColor = AccentColor,
    unfocusedBorderColor = SurfaceColor,
    cursorColor = AccentColor
)
