package com.dev.memebattle.feature.home.impl.presentation.view.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Styled text field shared across all auth forms.
 */
@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = {
            Text(placeholder, color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp)
        },
        singleLine = true,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Color(0xFF7C5DFA),
            unfocusedBorderColor = Color(0xFF3B2F5E),
            focusedLabelColor    = Color(0xFF7C5DFA),
            unfocusedLabelColor  = Color(0xFF887A9E),
            focusedTextColor     = Color.White,
            unfocusedTextColor   = Color.White,
            cursorColor          = Color(0xFF7C5DFA),
        ),
        shape = RoundedCornerShape(14.dp),
    )
}

/**
 * Full-width primary button with built-in loading spinner.
 */
@Composable
internal fun AuthPrimaryButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor         = Color(0xFF6C47FF),
            disabledContainerColor = Color(0xFF3B2F5E),
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(22.dp),
                color       = Color.White,
                strokeWidth = 2.5.dp,
            )
        } else {
            Text(
                text       = text,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
            )
        }
    }
}
