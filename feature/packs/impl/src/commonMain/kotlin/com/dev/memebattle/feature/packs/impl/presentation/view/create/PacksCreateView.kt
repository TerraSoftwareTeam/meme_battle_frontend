package com.dev.memebattle.feature.packs.impl.presentation.view.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import coil3.compose.AsyncImage
import com.dev.memebattle.core.domain.packs.model.SafetyLevel
import com.dev.memebattle.feature.packs.impl.presentation.component.create.PacksCreateComponent
import com.dev.memebattle.feature.packs.impl.presentation.store.create.PacksCreateStore
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.getString
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.*

private val BackgroundTop = Color(0xFF1A1035)
private val BackgroundBottom = Color(0xFF08040F)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFB0A2C7)
private val AccentColor = Color(0xFF8B5CF6)
private val ErrorColor = Color(0xFFEF4444)
private val SurfaceColor = Color(0xFF2A1B4E)

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
                                stringResource(Res.string.packs_create_select_images, state.selectedFiles.size),
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
                    .padding(24.dp)
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
private fun PackTypeSelector(
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

@Composable
private fun SafetyLevelSelector(
    selectedLevel: SafetyLevel,
    onLevelSelected: (SafetyLevel) -> Unit
) {
    Column {
        Text(stringResource(Res.string.packs_create_safety_level), color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(SurfaceColor, RoundedCornerShape(8.dp))
        ) {
            val levels = SafetyLevel.entries
            levels.forEach { level ->
                val isSelected = selectedLevel == level
                val textRes = when (level) {
                    SafetyLevel.FAMILY_FRIENDLY -> Res.string.packs_create_safety_0
                    SafetyLevel.SPICY -> Res.string.packs_create_safety_16
                    SafetyLevel.EXPLICIT -> Res.string.packs_create_safety_18
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AccentColor else Color.Transparent)
                        .clickable { onLevelSelected(level) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(textRes),
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun FileImageCard(
    file: PlatformFile,
    onRemove: () -> Unit
) {
    var bytes by remember(file) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(file) {
        bytes = file.readBytes()
    }

    Box(
        modifier = Modifier
            .width(100.dp)
            .aspectRatio(3f / 4f)
            .background(Color.Black, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        if (bytes != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F0820)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = bytes,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceColor),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = AccentColor,
                    strokeWidth = 2.dp
                )
            }
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
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
