package com.dev.memebattle.feature.packs.impl.presentation.view.create.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.core.PlatformFile
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.*

@OptIn(ExperimentalLayoutApi::class)
internal fun LazyListScope.createMemesSection(
    selectedFiles: List<PlatformFile>,
    onLaunchPicker: () -> Unit,
    onRemoveFile: (PlatformFile) -> Unit
) {
    item {
        Button(
            onClick = onLaunchPicker,
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
    if (selectedFiles.isNotEmpty()) {
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedFiles.forEach { file ->
                    FileImageCard(
                        file = file,
                        onRemove = { onRemoveFile(file) }
                    )
                }
            }
        }
    }
}

internal fun LazyListScope.createSituationsSection(
    prompts: List<String>,
    currentPromptInput: String,
    onPromptInputChange: (String) -> Unit,
    onAddPrompt: (String) -> Unit,
    onRemovePrompt: (Int) -> Unit
) {
    item {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = currentPromptInput,
                onValueChange = onPromptInputChange,
                label = { Text(stringResource(Res.string.packs_create_situation_prompt), color = TextSecondary) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (currentPromptInput.isNotBlank()) {
                        onAddPrompt(currentPromptInput)
                    }
                }),
                colors = outlinedTextFieldColors()
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (currentPromptInput.isNotBlank()) {
                        onAddPrompt(currentPromptInput)
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

    itemsIndexed(prompts) { index, prompt ->
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
                onClick = { onRemovePrompt(index) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = ErrorColor)
            }
        }
    }
}

@Composable
internal fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedBorderColor = AccentColor,
    unfocusedBorderColor = SurfaceColor,
    cursorColor = AccentColor
)
